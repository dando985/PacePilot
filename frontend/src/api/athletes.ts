import {apiRequest} from './client';

import type {AthleteProfile, CreateAthleteProfileRequest, UpdateAthleteProfileRequest,} from './types';

export function getAthleteProfiles(signal: AbortSignal | null = null): Promise<AthleteProfile[]> {
    return apiRequest<AthleteProfile[]>('/athletes', {signal});
}

export function getAthleteProfile(athleteId: string, signal: AbortSignal | null = null): Promise<AthleteProfile> {
    return apiRequest<AthleteProfile>(`/athletes/${encodeURIComponent(athleteId)}`, {signal});
}

export function createAthleteProfile(request: CreateAthleteProfileRequest): Promise<AthleteProfile> {
    return apiRequest<AthleteProfile>('/athletes', {method: 'POST', body: request,});
}

export function updateAthleteProfile(athleteId: string, request: UpdateAthleteProfileRequest): Promise<AthleteProfile> {
    return apiRequest<AthleteProfile>(`/athletes/${encodeURIComponent(athleteId)}`, {method: 'PUT', body: request});
}