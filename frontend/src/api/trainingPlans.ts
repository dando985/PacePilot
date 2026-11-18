import {apiRequest} from './client';

import type {CreateTrainingPlanRequest, GenerateTrainingPlanRequest, TrainingPlan} from './types';

export function getTrainingPlan(planId: string, signal: AbortSignal | null = null,): Promise<TrainingPlan> {
    return apiRequest<TrainingPlan>(`/training-plans/${encodeURIComponent(planId)}`, {signal});
}

export function getTrainingPlans(athleteId: string, signal: AbortSignal | null = null,): Promise<TrainingPlan[]> {
    return apiRequest<TrainingPlan[]>(`/athletes/${encodeURIComponent(athleteId)}/training-plans`, {signal});
}

export function createTrainingPlan(athleteId: string, request: CreateTrainingPlanRequest,): Promise<TrainingPlan> {
    return apiRequest<TrainingPlan>(`/athletes/${encodeURIComponent(athleteId)}/training-plans`, {method: 'POST', body: request});
}

export function generateTrainingPlan(athleteId: string, request: GenerateTrainingPlanRequest,): Promise<TrainingPlan> {
    return apiRequest<TrainingPlan>(`/athletes/${encodeURIComponent(athleteId)}/training-plans/generate`, {method: 'POST', body: request});
}