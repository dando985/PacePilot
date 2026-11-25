// Centralized API client for making requests to the backend API.
// This module provides a single function, `apiRequest`, which handles making HTTP requests and processing responses, including error handling.

const API_BASE_URL = '/api';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);

type ApiRequestOptions = Omit<RequestInit, 'body' | 'credentials'> & {body?: unknown};

type CsrfToken = {headerName: string; token: string};


export class ApiError extends Error {
    readonly status: number;

    constructor(status: number, message: string) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

export async function apiRequest<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
    // Destructure the options to separate the body and headers (renamed to providedHeaders) from other request options.
    const {body, headers: providedHeaders, ...requestOptions} = options;
    const method = (requestOptions.method ?? 'GET').toUpperCase();
    const headers = new Headers(providedHeaders);

    if (!headers.has('Accept')) {
        headers.set('Accept', 'application/json, application/problem+json');
    }

    let requestBody: string | undefined;

    if (body instanceof URLSearchParams) {
        requestBody = body.toString();

        if (!headers.has('Content-Type')) {
            headers.set('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8');
        }
    } else if (body !== undefined) {
        requestBody = JSON.stringify(body);

        if (!headers.has('Content-Type')) {
            headers.set('Content-Type', 'application/json');
        }
    }

    if (!SAFE_METHODS.has(method)) {
        const csrf = await getCsrfToken(requestOptions.signal);
        headers.set(csrf.headerName, csrf.token);
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...requestOptions,
        method,
        credentials: 'same-origin',
        headers,
        body: requestBody,
    });

    if (!response.ok) {
        throw new ApiError(response.status, await readErrorMessage(response));
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}

async function getCsrfToken(signal: AbortSignal | null = null): Promise<CsrfToken> {
    const response = await fetch(`${API_BASE_URL}/auth/csrf`, {
        credentials: 'same-origin',     // lets browser send the session cookie through existing /api proxy
        cache: 'no-store',
        signal,
        headers: {Accept: 'application/json'}
    });

    if (!response.ok) {
        throw new ApiError(response.status, await readErrorMessage(response));
    }

    const data: unknown = await response.json();

    if (!isRecord(data)
        || typeof data.headerName !== 'string'
        || data.headerName.trim().length === 0
        || typeof data.token !== 'string'
        || data.token.trim().length === 0) {
        throw new Error('The server returned an invalid CSRF token.');
    }

    return {headerName: data.headerName, token: data.token};
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