import {apiRequest} from './client';

import type {CreateWorkoutLogRequest, WorkoutLog} from './types';

export function getWorkoutLog(logId: string, signal: AbortSignal | null = null): Promise<WorkoutLog> {
    return apiRequest<WorkoutLog>(`/workout-logs/${encodeURIComponent(logId)}`, {signal});
}

export function getWorkoutLogs(athleteId: string, signal: AbortSignal | null = null): Promise<WorkoutLog[]> {
    return apiRequest<WorkoutLog[]>(`/athletes/${encodeURIComponent(athleteId)}/workout-logs`, {signal});
}

export function createWorkoutLog(planId: string, workoutId: string, request: CreateWorkoutLogRequest): Promise<WorkoutLog> {
    return apiRequest<WorkoutLog>(`/training-plans/${encodeURIComponent(planId)}/workouts/
    ${encodeURIComponent(workoutId)}/logs`, {method: 'POST', body: request});
}