import {ApiError, apiRequest} from './client';

import type {LoginRequest, RegisterAccountRequest, UserAccount} from './types';

export async function getCurrentAccount(signal: AbortSignal | null = null): Promise<UserAccount | null> {
    try {
        return await apiRequest<UserAccount>('/auth/me', {signal, cache: 'no-store'});
    } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
            return null;
        }

        throw error;
    }
}

export function registerAccount(request: RegisterAccountRequest): Promise<UserAccount> {
    return apiRequest<UserAccount>('/auth/register', {method: 'POST', body: request});
}

export function login(request: LoginRequest): Promise<void> {
    return apiRequest<void>('/auth/login', {
        method: 'POST',
        body: new URLSearchParams({email: request.email, password: request.password}),
    });
}

export function logout(): Promise<void> {
    return apiRequest<void>('/auth/logout', {method: 'POST'});
}