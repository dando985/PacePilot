import {useId, useMemo, useState} from 'react';

import WeeklyMetricChart from './WeeklyMetricChart';

import {addDays, calculateWeeklyTrainingMetrics, formatPace, getWeekStart} from '../utils/weeklyTrainingMetrics';

import type {DistanceUnit, TrainingPlan, WorkoutLog} from '../api/types';

const dateFormatter = new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    timeZone: 'UTC',
});

function formatDate(date: string): string {
    return dateFormatter.format(new Date(`${date}T00:00:00Z`));
}

function formatNumber(value: number | null, fractionDigits = 0): string {
    return value === null || !Number.isFinite(value) ? '—' : value.toLocaleString(undefined, {maximumFractionDigits: fractionDigits,});
}

function formatDuration(minutes: number | null): string {
    if (minutes === null || !Number.isFinite(minutes)) {
        return '—';
    }

    const hours = Math.floor(minutes / 60);
    const remaining = minutes % 60;

    if (hours === 0) {
        return `${remaining.toLocaleString()} min`;
    }

    return remaining === 0 ? `${hours.toLocaleString()} hr` : `${hours.toLocaleString()} hr ${remaining.toLocaleString()} min`;
}

function logCountText(count: number): string {
    return count === 0 ? 'No measurements recorded.' : `From ${count.toLocaleString()} workout ${count === 1 ? 'log' : 'logs'}.`;
}

export default function WeeklyTrainingOverview(props: { athleteId: string; plans: TrainingPlan[]; logs: WorkoutLog[]; }) {
    const headingId = useId();
    const [weekStart, setWeekStart] = useState(() => getWeekStart());
    const [distanceUnit, setDistanceUnit] = useState<DistanceUnit>('MILES');

    const metrics = useMemo(
        () => calculateWeeklyTrainingMetrics(
            props.athleteId,
            props.plans,
            props.logs,
            weekStart,
            distanceUnit
        ),
        [
            props.athleteId,
            props.plans,
            props.logs,
            weekStart,
            distanceUnit,
        ]
    );

    const {summary} = metrics;
    const counts = summary.recordedCounts;
    const longestRun = summary.longestRun;

    const unit = distanceUnit === 'MILES' ? 'mi' : 'km';
    const currentWeekStart = getWeekStart();

    const cards = [
        {
            id: 'distance',
            label: 'Recorded distance',
            value: formatNumber(summary.distance, 2),
            unit: summary.distance === null ? '' : unit,
            note: logCountText(counts.distance),
        },
        {
            id: 'duration',
            label: 'Training time',
            value: formatDuration(summary.durationMinutes),
            unit: '',
            note: logCountText(counts.durationMinutes),
        },
        {
            id: 'workouts',
            label: 'Workout logs',
            value: formatNumber(summary.workoutCount),
            unit: '',
            note: 'Includes rest-day logs.',
        },
        {
            id: 'pace',
            label: 'Average pace',
            value: formatPace(summary.paceSeconds),
            unit: summary.paceSeconds === null ? '' : `min/${unit}`,
            note: logCountText(counts.paceSeconds),
        },
    ];

    function changeWeek(offset: number) {
        setWeekStart((current) => {
            const next = addDays(current, offset);
            return next > getWeekStart() ? current : next;
        });
    }

    return (
        <section className="weekly-overview" aria-labelledby={headingId}>
            <header className="weekly-overview-header">
                <div>
                    <h2 id={headingId}>Weekly training</h2>

                    <p className="weekly-overview-range" aria-live="polite">
                        <time dateTime={metrics.weekStart}>
                            {formatDate(metrics.weekStart)}
                        </time>

                        {' – '}

                        <time dateTime={metrics.weekEnd}>
                            {formatDate(metrics.weekEnd)}
                        </time>
                    </p>
                </div>

                <div className="weekly-overview-controls">
                    <div className="weekly-week-navigation" role="group" aria-label="Week navigation">
                        <button type="button" aria-label="Previous week" onClick={() => changeWeek(-7)}>
                            Previous
                        </button>

                        <button className="weekly-current-week" type="button" onClick={() => setWeekStart(getWeekStart())}>
                            This week
                        </button>

                        <button type="button" aria-label="Next week" onClick={() => changeWeek(7)} disabled={weekStart >= currentWeekStart}>
                            Next
                        </button>
                    </div>

                    <label className="weekly-overview-unit">
                        Distance unit
                        <select value={distanceUnit} onChange={(event) => {
                                const value = event.currentTarget.value;

                                if (value === 'MILES' || value === 'KILOMETERS') {
                                    setDistanceUnit(value);
                                }
                            }}
                        >
                            <option value="MILES">Miles</option>
                            <option value="KILOMETERS">
                                Kilometers
                            </option>
                        </select>
                    </label>
                </div>
            </header>

            <div className="weekly-stat-grid">
                {cards.map((card) => (
                    <article key={card.id} className="weekly-stat-card">
                        <h3>{card.label}</h3>

                        <p className="weekly-stat-value">
                            {card.value}

                            {card.unit !== '' && (<span className="weekly-stat-unit">{' '}{card.unit}</span>)}
                        </p>

                        <p className="weekly-stat-note">
                            {card.note}
                        </p>
                    </article>
                ))}
            </div>

            <WeeklyMetricChart data={metrics}/>

            <div className="weekly-detail-grid">
                <article className="weekly-detail-card">
                    <h3>Longest run</h3>

                    <p className="weekly-stat-value">
                        {formatNumber(longestRun?.distance ?? null, 2)}

                        {longestRun !== null && (
                            <span className="weekly-stat-unit">
                                {' '}{unit}
                            </span>
                        )}
                    </p>

                    {longestRun === null ? (
                        <p className="weekly-stat-note">
                            No running distance recorded for this week.
                        </p>
                    ) : (
                        <>
                            <p className="weekly-detail-title">
                                {longestRun.title}
                            </p>

                            <p className="weekly-stat-note">
                                <time dateTime={longestRun.completedDate}>
                                    {formatDate(longestRun.completedDate)}
                                </time>
                            </p>
                        </>
                    )}
                </article>

                <article className="weekly-detail-card">
                    <h3>Training consistency</h3>

                    <p className="weekly-stat-value">
                        {metrics.activeDays.toLocaleString()}

                        <span className="weekly-stat-unit">
                            {' '}
                            {metrics.activeDays === 1 ? 'day' : 'days'}
                        </span>
                    </p>

                    <p className="weekly-stat-note">
                        Days with logged training.
                    </p>

                    <ul className="weekly-consistency-days">
                        {metrics.days.map((day) => (
                            <li key={day.date} data-active={day.workoutCount > 0}>
                                <time dateTime={day.date} title={formatDate(day.date)}>
                                    {day.label}
                                </time>

                                <span>
                                    {day.workoutCount.toLocaleString()}
                                </span>
                            </li>
                        ))}
                    </ul>

                    <p className="weekly-stat-note">
                        Workouts logged per day.
                    </p>
                </article>
            </div>
        </section>
    );
}