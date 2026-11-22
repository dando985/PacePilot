import type {DistanceUnit, TrainingPlan, WorkoutLog} from '../api/types';

const KILOMETERS_PER_MILE = 1.609344;

export interface TrainingPlanProgress {
    plannedActiveWorkouts: number;
    completedActiveWorkouts: number;
    completionPercentage: number | null;
    recordedDistance: number | null;
    distanceUnit: DistanceUnit;
    distanceLogCount: number;
    recordedDurationMinutes: number | null;
    durationLogCount: number;
}

export function calculateTrainingPlanProgress(plan: TrainingPlan, logs: WorkoutLog[]): TrainingPlanProgress {
    // Create a single array of all planned workouts across all weeks in the training plan
    const workouts = plan.weeks.flatMap((week) => week.workouts);

    // Create a set of workout IDs from the planned workouts
    const workoutIds = new Set(workouts.map((workout) => workout.id));

    // Create a set of planned active workout IDs (excluding rest days) from the input training plan
    const activeWorkoutIds = new Set(
        workouts
        .filter((workout) => workout.type !== 'REST')
        .map((workout) => workout.id)
    );

    // Create a map of workout logs by their planned workout ID
    const logsByWorkoutId = new Map<string, WorkoutLog>();
    for (const log of logs) {
        if (log.athleteProfileId === plan.athleteProfileId && workoutIds.has(log.plannedWorkoutId)) {
            logsByWorkoutId.set(log.plannedWorkoutId, log);
        }
    }

    let completedActiveWorkouts = 0;
    let totalDistance = 0;
    let totalDurationMinutes = 0;
    let distanceLogCount = 0;
    let durationLogCount = 0;

    for (const log of logsByWorkoutId.values()) {
        if (activeWorkoutIds.has(log.plannedWorkoutId)) {
            completedActiveWorkouts++;
        }

        if (log.actualDistance !== null && log.distanceUnit !== null) {
            totalDistance += convertDistance(log.actualDistance, log.distanceUnit, plan.distanceUnit);
            distanceLogCount++;
        }

        if (log.actualDurationMinutes !== null) {
            totalDurationMinutes += log.actualDurationMinutes;
            durationLogCount++;
        }
    }

    const plannedActiveWorkouts = activeWorkoutIds.size;

    return {
        plannedActiveWorkouts,
        completedActiveWorkouts,
        completionPercentage: plannedActiveWorkouts === 0 ? null : (completedActiveWorkouts / plannedActiveWorkouts) * 100,
        recordedDistance: distanceLogCount === 0 ? null : totalDistance,
        distanceUnit: plan.distanceUnit,
        distanceLogCount,
        recordedDurationMinutes: durationLogCount === 0 ? null : totalDurationMinutes,
        durationLogCount,
    };
}

function convertDistance(distance: number, from: DistanceUnit, to: DistanceUnit): number {
    if (from === to) {
        return distance;
    }

    return from === 'MILES' ? distance * KILOMETERS_PER_MILE : distance / KILOMETERS_PER_MILE;
}