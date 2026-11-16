// Centralized API client for making requests to the backend API.
// This module provides a single function, `apiRequest`, which handles making HTTP requests and processing responses, including error handling.

const API_BASE_URL = '/api';

type ApiRequestOptions = Omit<RequestInit, 'body'> & { body?: unknown; };

export class ApiError extends Error {
    readonly status: number;

    constructor(status: number, message: string) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

export async function apiRequest<T>(path: string, options: ApiRequestOptions = {},): Promise<T> {
    const { body, headers: providedHeaders, ...requestOptions } = options;
    const headers = new Headers(providedHeaders);

    if (!headers.has('Accept')) {
        headers.set('Accept', 'application/json, application/problem+json');
    }

    if (body !== undefined) {
        headers.set('Content-Type', 'application/json');
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...requestOptions,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
    });

    if (!response.ok) {
        const message = await readErrorMessage(response);
        throw new ApiError(response.status, message);
    }

    return response.json() as Promise<T>;
}

async function readErrorMessage(response: Response): Promise<string> {
    const fallback = `Request failed with status ${response.status}.`;

    try {
        // Attempt to parse the response body as JSON.
        const data: unknown = await response.json();

        // Ensure the response data is an object before attempting to access its properties.
        if (!isRecord(data)) {
            return fallback;
        }

        // Check for common error fields in the response data and return the first non-empty string found.
        for (const field of ['detail', 'message', 'title', 'error']) {
            const value = data[field];

            if (typeof value === 'string' && value.trim().length > 0) {
                return value;
            }
        }

        // If no specific error message is found, return the fallback message.
        return fallback;
    } catch (error) {
        if (error instanceof Error && error.name === 'AbortError') {
            throw error;
        }

        return fallback;
    }
}

// Type guard to check if a value is a record (object) with string keys and unknown values.
function isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null && !Array.isArray(value);
}