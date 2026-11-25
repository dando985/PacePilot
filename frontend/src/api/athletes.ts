import {ApiError, apiRequest} from './client';

import type {AthleteProfile, CreateAthleteProfileRequest, UpdateAthleteProfileRequest} from './types';

export async function getCurrentAthleteProfile(signal: AbortSignal | null = null): Promise<AthleteProfile | null> {
    try {
        return await apiRequest<AthleteProfile>('/athletes/me', {signal, cache: 'no-store'});
    } catch (error) {
        if (error instanceof ApiError && error.status === 404) {
            return null;
        }

        throw error;
    }
}

export function createAthleteProfile(request: CreateAthleteProfileRequest): Promise<AthleteProfile> {
    return apiRequest<AthleteProfile>('/athletes', {method: 'POST', body: request});
}

export function updateAthleteProfile(athleteId: string, request: UpdateAthleteProfileRequest): Promise<AthleteProfile> {
    return apiRequest<AthleteProfile>(`/athletes/${encodeURIComponent(athleteId)}`, {method: 'PUT', body: request});
}