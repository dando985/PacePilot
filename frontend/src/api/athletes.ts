import { apiRequest } from './client';

import type {AthleteProfile, CreateAthleteProfileRequest,} from './types';

export function getAthleteProfiles(signal: AbortSignal | null = null,): Promise<AthleteProfile[]> {
    return apiRequest<AthleteProfile[]>('/athletes', {signal});
}

export function getAthleteProfile(athleteId: string, signal: AbortSignal | null = null,): Promise<AthleteProfile> {
    return apiRequest<AthleteProfile>(`/athletes/${encodeURIComponent(athleteId)}`, {signal},);
}

export function createAthleteProfile(request: CreateAthleteProfileRequest,): Promise<AthleteProfile> {
    return apiRequest<AthleteProfile>('/athletes', {method: 'POST', body: request});
}