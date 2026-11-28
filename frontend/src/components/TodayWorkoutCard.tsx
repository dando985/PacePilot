import {useEffect, useId, useState} from 'react';

import {getTodayDate} from '../utils/weeklyTrainingMetrics';

import type {TrainingPlan, WorkoutLog, WorkoutType} from '../api/types';

const workoutTypeLabels: Record<WorkoutType, string> = {
    REST: 'Rest day',
    WALK: 'Walk',
    EASY_RUN: 'Easy run',
    RECOVERY_RUN: 'Recovery run',
    LONG_RUN: 'Long run',
    TEMPO_RUN: 'Tempo run',
    INTERVALS: 'Intervals',
    STRENGTH: 'Strength',
    CROSS_TRAINING: 'Cross training',
    RACE: 'Race',
};

const dateFormatter = new Intl.DateTimeFormat(undefined, {
    weekday: 'long',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    timeZone: 'UTC',
});

function formatDate(date: string): string {
    return dateFormatter.format(new Date(`${date}T00:00:00Z`));
}

export default function TodayWorkoutCard(props: { athleteId: string; plans: TrainingPlan[]; logs: WorkoutLog[]; }) {
    const headingId = useId();
    const [today, setToday] = useState(getTodayDate);

    useEffect(() => {
        const updateToday = () => setToday(getTodayDate());
        const timer = window.setInterval(updateToday, 60_000);

        window.addEventListener('focus', updateToday);
        document.addEventListener('visibilitychange', updateToday);

        return () => {
            window.clearInterval(timer);
            window.removeEventListener('focus', updateToday);
            document.removeEventListener('visibilitychange', updateToday);
        };
    }, []);

    const todaysWorkouts = props.plans
        .filter((plan) =>
            plan.athleteProfileId === props.athleteId && !plan.archived
        )
        .flatMap((plan) =>
            plan.weeks.flatMap((week) =>
                week.workouts
                    .filter((workout) => workout.scheduledDate === today)
                    .map((workout) => ({plan, workout}))
            )
        )
        .sort((first, second) =>
            first.plan.name.localeCompare(second.plan.name)
            || first.workout.title.localeCompare(second.workout.title)
            || first.workout.id.localeCompare(second.workout.id)
        );

    const logsByWorkoutId = new Map<string, WorkoutLog>();

    for (const log of props.logs) {
        if (log.athleteProfileId === props.athleteId) {
            logsByWorkoutId.set(log.plannedWorkoutId, log);
        }
    }

    return (
        <section className="today-workout-card" aria-labelledby={headingId}>
            <header className="today-workout-header">
                <h2 id={headingId}>
                    {todaysWorkouts.length > 1 ? "Today's workouts" : "Today's workout"}
                </h2>

                <time dateTime={today}>{formatDate(today)}</time>
            </header>

            {todaysWorkouts.length === 0 ? (
                <p className="today-workout-empty">
                    No workouts scheduled for today.
                </p>
            ) : (
                <ul className="today-workout-list">
                    {todaysWorkouts.map(({plan, workout}) => {
                        const log = logsByWorkoutId.get(workout.id);
                        const hasDistance = workout.targetDistance !== null && workout.distanceUnit !== null;

                        return (
                            <li key={`${plan.id}:${workout.id}`}>
                                <div className="today-workout-item-header">
                                    <div>
                                        <p className="today-workout-plan">
                                            {plan.name}
                                        </p>
                                        <h3>{workout.title}</h3>
                                    </div>

                                    <span className="today-workout-status" data-logged={log !== undefined}>
                                        {log !== undefined ? 'Logged' : 'Not logged'}
                                    </span>
                                </div>

                                <p className="today-workout-type">
                                    {workoutTypeLabels[workout.type]}
                                </p>

                                {(hasDistance || workout.targetDurationMinutes !== null) && (
                                    <dl className="today-workout-targets">
                                        {workout.targetDistance !== null && workout.distanceUnit !== null && (
                                                <div>
                                                    <dt>Planned distance</dt>
                                                    <dd>
                                                        {workout.targetDistance.toLocaleString(undefined, {
                                                            maximumFractionDigits: 2,
                                                        })}{' '}
                                                        {workout.distanceUnit === 'MILES' ? 'mi' : 'km'}
                                                    </dd>
                                                </div>
                                            )}

                                        {workout.targetDurationMinutes !== null && (
                                            <div>
                                                <dt>Planned duration</dt>
                                                <dd>
                                                    {workout.targetDurationMinutes.toLocaleString()} min
                                                </dd>
                                            </div>
                                        )}
                                    </dl>
                                )}

                                <p className="today-workout-instructions">
                                    {workout.instructions}
                                </p>

                                {log !== undefined && (
                                    <p className="today-workout-log-note">
                                        Logged on{' '}
                                        <time dateTime={log.completedDate}>
                                            {formatDate(log.completedDate)}
                                        </time>.
                                    </p>
                                )}
                            </li>
                        );
                    })}
                </ul>
            )}
        </section>
    );
}