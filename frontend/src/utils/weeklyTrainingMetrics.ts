import type {DistanceUnit, PlannedWorkout, TrainingPlan, WorkoutLog} from '../api/types';

const KILOMETERS_PER_MILE = 1.609344;

const weekdayFormatter = new Intl.DateTimeFormat(undefined, {weekday: 'short', timeZone: 'UTC'});

export type WeeklyMetric =
    | 'distance'
    | 'durationMinutes'
    | 'paceSeconds'
    | 'stepCount'
    | 'caloriesBurnedKcal'
    | 'averageHeartRateBpm';

export interface LongestRun {
    workoutId: string;
    title: string;
    completedDate: string;
    distance: number;
}

export interface TrainingMetrics {
    workoutCount: number;
    distance: number | null;
    durationMinutes: number | null;
    paceSeconds: number | null;
    stepCount: number | null;
    caloriesBurnedKcal: number | null;
    averageHeartRateBpm: number | null;
    longestRun: LongestRun | null;
    recordedCounts: Record<WeeklyMetric, number>;
}

export interface DailyTrainingMetrics extends TrainingMetrics {
    date: string;
    label: string;
}

export interface WeeklyTrainingMetrics {
    weekStart: string;
    weekEnd: string;
    distanceUnit: DistanceUnit;
    days: DailyTrainingMetrics[];
    summary: TrainingMetrics;
    activeDays: number;
}

interface LoggedWorkout {
    log: WorkoutLog;
    workout: PlannedWorkout;
}

export function getTodayDate(): string {
    const now = new Date();

    return [
        now.getFullYear(),
        String(now.getMonth() + 1).padStart(2, '0'),
        String(now.getDate()).padStart(2, '0'),
    ].join('-');
}

export function addDays(date: string, count: number): string {
    const value = parseDate(date);
    value.setUTCDate(value.getUTCDate() + count);
    return value.toISOString().slice(0, 10);
}

export function getWeekStart(date: string = getTodayDate()): string {
    const dayOfWeek = parseDate(date).getUTCDay();
    return addDays(date, -((dayOfWeek + 6) % 7));
}

export function formatPace(seconds: number | null): string {
    if (seconds === null || !Number.isFinite(seconds) || seconds < 0) {
        return '—';
    }

    const rounded = Math.round(seconds);
    const minutes = Math.floor(rounded / 60);
    const remainder = String(rounded % 60).padStart(2, '0');

    return `${minutes}:${remainder}`;
}

export function calculateWeeklyTrainingMetrics(athleteId: string,
                                               plans: TrainingPlan[],
                                               logs: WorkoutLog[],
                                               dateInWeek: string,
                                               distanceUnit: DistanceUnit
): WeeklyTrainingMetrics {
    const weekStart = getWeekStart(dateInWeek);
    const weekEnd = addDays(weekStart, 6);
    const workoutsById = new Map<string, PlannedWorkout>();

    for (const plan of plans) {
        if (plan.athleteProfileId !== athleteId) {
            continue;
        }

        for (const week of plan.weeks) {
            for (const workout of week.workouts) {
                workoutsById.set(workout.id, workout);
            }
        }
    }

    const entriesByWorkoutId = new Map<string, LoggedWorkout>();

    for (const log of logs) {
        const workout = workoutsById.get(log.plannedWorkoutId);

        if (log.athleteProfileId === athleteId && workout !== undefined) {
            entriesByWorkoutId.set(workout.id, {log, workout});
        }
    }

    const entries = [...entriesByWorkoutId.values()]
        .filter(({log}) => log.completedDate >= weekStart && log.completedDate <= weekEnd)
        .sort((a, b) => a.log.completedDate.localeCompare(b.log.completedDate) || a.log.id.localeCompare(b.log.id)
        );

    const days = Array.from({length: 7}, (_, index): DailyTrainingMetrics => {
            const date = addDays(weekStart, index);

            const dayEntries = entries.filter(
                ({log}) => log.completedDate === date
            );

            return {
                date,
                label: weekdayFormatter.format(parseDate(date)),
                ...summarize(dayEntries, distanceUnit),
            };
        }
    );

    return {
        weekStart,
        weekEnd,
        distanceUnit,
        days,
        summary: summarize(entries, distanceUnit),
        activeDays: days.filter((day) => day.workoutCount > 0).length,
    };
}

function summarize(entries: LoggedWorkout[], distanceUnit: DistanceUnit): TrainingMetrics {
    const metrics: TrainingMetrics = {
        workoutCount: 0,
        distance: null,
        durationMinutes: null,
        paceSeconds: null,
        stepCount: null,
        caloriesBurnedKcal: null,
        averageHeartRateBpm: null,
        longestRun: null,
        recordedCounts: {
            distance: 0,
            durationMinutes: 0,
            paceSeconds: 0,
            stepCount: 0,
            caloriesBurnedKcal: 0,
            averageHeartRateBpm: 0,
        },
    };

    let paceDistance = 0;
    let paceMinutes = 0;
    let heartRateTotal = 0;

    const counts = metrics.recordedCounts;

    for (const entry of entries) {
        const {log, workout} = entry;

        metrics.workoutCount++;

        const distance = getRecordedDistance(log, distanceUnit);

        if (distance !== null) {
            metrics.distance = (metrics.distance ?? 0) + distance;
            counts.distance++;

            if (metrics.longestRun === null || distance > metrics.longestRun.distance) {
                metrics.longestRun = {
                    workoutId: workout.id,
                    title: workout.title,
                    completedDate: log.completedDate,
                    distance,
                };
            }

            // Pace uses only logs with both distance and duration recorded.
            if (isPositive(log.actualDurationMinutes)) {
                paceDistance += distance;
                paceMinutes += log.actualDurationMinutes;
                counts.paceSeconds++;
            }
        }

        if (isPositive(log.actualDurationMinutes)) {
            metrics.durationMinutes = (metrics.durationMinutes ?? 0) + log.actualDurationMinutes;

            counts.durationMinutes++;
        }

        if (isNonNegative(log.stepCount)) {
            metrics.stepCount = (metrics.stepCount ?? 0) + log.stepCount;
            counts.stepCount++;
        }

        if (isNonNegative(log.caloriesBurnedKcal)) {
            metrics.caloriesBurnedKcal = (metrics.caloriesBurnedKcal ?? 0) + log.caloriesBurnedKcal;
            counts.caloriesBurnedKcal++;
        }

        if (isPositive(log.averageHeartRateBpm)) {
            heartRateTotal += log.averageHeartRateBpm;
            counts.averageHeartRateBpm++;
        }
    }

    if (paceDistance > 0) {
        const pace = paceMinutes * 60 / paceDistance;
        metrics.paceSeconds = Number.isFinite(pace) ? pace : null;
    }

    if (counts.averageHeartRateBpm > 0) {
        metrics.averageHeartRateBpm = heartRateTotal / counts.averageHeartRateBpm;
    }

    return metrics;
}

function getRecordedDistance(log: WorkoutLog, distanceUnit: DistanceUnit): number | null {
    if (!isPositive(log.actualDistance) || (log.distanceUnit !== 'MILES' && log.distanceUnit !== 'KILOMETERS')) {
        return null;
    }

    const distance = convertDistance(log.actualDistance, log.distanceUnit, distanceUnit);

    return Number.isFinite(distance) && distance > 0 ? distance : null;
}

function convertDistance(distance: number, from: DistanceUnit, to: DistanceUnit): number {
    if (from === to) {
        return distance;
    }

    return from === 'MILES' ? distance * KILOMETERS_PER_MILE : distance / KILOMETERS_PER_MILE;
}

function isPositive(value: number | null | undefined): value is number {
    return typeof value === 'number' && Number.isFinite(value) && value > 0;
}

function isNonNegative(value: number | null | undefined): value is number {
    return typeof value === 'number' && Number.isFinite(value) && value >= 0;
}

function parseDate(date: string): Date {
    const value = new Date(`${date}T00:00:00Z`);

    if (!/^\d{4}-\d{2}-\d{2}$/.test(date) || Number.isNaN(value.getTime()) || value.toISOString().slice(0, 10) !== date) {
        throw new RangeError('A valid date in YYYY-MM-DD format is required.');
    }

    return value;
}