import {useId, useState} from 'react';

import {
    Bar,
    BarChart,
    CartesianGrid,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';

import {formatPace} from '../utils/weeklyTrainingMetrics';

import type {
    WeeklyMetric,
    WeeklyTrainingMetrics,
} from '../utils/weeklyTrainingMetrics';

const metricOptions: {value: WeeklyMetric; label: string}[] = [
    {value: 'distance', label: 'Recorded distance'},
    {value: 'durationMinutes', label: 'Training time'},
    {value: 'paceSeconds', label: 'Average pace'},
    {value: 'stepCount', label: 'Steps'},
    {value: 'caloriesBurnedKcal', label: 'Calories burned'},
    {value: 'averageHeartRateBpm', label: 'Average workout heart rate'},
];

const dateFormatter = new Intl.DateTimeFormat(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    timeZone: 'UTC',
});

function formatDate(date: string): string {
    return dateFormatter.format(new Date(`${date}T00:00:00Z`));
}

function formatValue(value: number | null, metric: WeeklyMetric): string {
    if (value === null || !Number.isFinite(value)) {
        return 'Not recorded';
    }

    if (metric === 'paceSeconds') {
        return formatPace(value);
    }

    return value.toLocaleString(undefined, {
        maximumFractionDigits:
            metric === 'distance' ? 2 : metric === 'averageHeartRateBpm' ? 1 : 0
    });
}

function formatAxisValue(value: number, metric: WeeklyMetric): string {
    if (metric === 'paceSeconds') {
        return formatPace(value);
    }

    return value.toLocaleString(undefined, {notation: 'compact', maximumFractionDigits: 1,});
}

export default function WeeklyMetricChart(props: {data: WeeklyTrainingMetrics}) {
    const id = useId();

    const [metric, setMetric] = useState<WeeklyMetric>('distance');

    const headingId = `${id}-heading`;
    const descriptionId = `${id}-description`;

    const distanceUnit = props.data.distanceUnit === 'MILES' ? 'mi' : 'km';

    const units: Record<WeeklyMetric, string> = {
        distance: distanceUnit,
        durationMinutes: 'min',
        paceSeconds: `min/${distanceUnit}`,
        stepCount: 'steps',
        caloriesBurnedKcal: 'kcal',
        averageHeartRateBpm: 'bpm',
    };

    const descriptions: Record<WeeklyMetric, string> = {
        distance: `Recorded distance in ${distanceUnit}.`,
        durationMinutes: 'Total recorded workout time in minutes.',
        paceSeconds: `Combined pace in min/${distanceUnit} from logs with both distance and duration. Lower values mean a faster pace.`,
        stepCount: 'Steps recorded in workout logs.',
        caloriesBurnedKcal: 'Calories recorded in workout logs in kcal.',
        averageHeartRateBpm: 'Mean of recorded workout heart-rate averages in bpm.',
    };

    const metricLabel = metricOptions.find((option) => option.value === metric)?.label ?? 'Recorded distance';

    const chartData = props.data.days.map((day) => {
        const value = day[metric];

        return {
            ...day,
            value: typeof value === 'number' && Number.isFinite(value) ? value : null,
        };
    });

    const hasData = chartData.some((day) => day.value !== null);

    const maximum = Math.max(1, ...chartData.map((day) => day.value ?? 0));

    return (
        <section className="weekly-metric-chart" aria-labelledby={headingId} aria-describedby={descriptionId}>
            <header className="weekly-chart-header">
                <h3 id={headingId}>Daily activity</h3>

                <label className="weekly-chart-selector">
                    Metric
                    <select value={metric} onChange={(event) => {
                            const option = metricOptions.find(
                                (item) =>
                                    item.value === event.currentTarget.value
                            );

                            if (option !== undefined) {
                                setMetric(option.value);
                            }
                        }}
                    >
                        {metricOptions.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </label>
            </header>

            <p id={descriptionId} className="weekly-chart-description">
                {descriptions[metric]}
            </p>

            {!hasData && (
                <p className="weekly-chart-empty" role="status">
                    No measurements recorded for this metric this week.
                </p>
            )}

            <ResponsiveContainer width="100%" height={300}>
                <BarChart data={chartData} accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 0,}}>
                    <CartesianGrid vertical={false} stroke="var(--color-border)" strokeDasharray="3 6"/>

                    <XAxis dataKey="date" interval={0} axisLine={false} tickLine={false} tickMargin={10}
                        tick={{
                            fill: 'var(--color-muted)',
                            fontSize: 12,
                        }}
                        tickFormatter={(date: string) =>
                            props.data.days.find(
                                (day) => day.date === date
                            )?.label ?? date
                        }
                    />

                    <YAxis width={64} domain={[0, maximum]} axisLine={false} tickLine={false}
                        allowDecimals={
                            metric === 'distance'
                            || metric === 'averageHeartRateBpm'
                        }
                        tick={{
                            fill: 'var(--color-muted)',
                            fontSize: 12,
                        }}
                        tickFormatter={(value: number) =>
                            formatAxisValue(value, metric)
                        }
                    />

                    <Tooltip filterNull={false} isAnimationActive={false} cursor={{
                        fill: 'var(--color-raised)',
                        fillOpacity: 0.5,
                    }}
                        content={({active, label}) => {
                            if (!active || typeof label !== 'string') {
                                return null;
                            }

                            const day = chartData.find(
                                (item) => item.date === label
                            );

                            if (day === undefined) {
                                return null;
                            }

                            const count = day.recordedCounts[metric];

                            return (
                                <div className="weekly-chart-tooltip">
                                    <p>
                                        <time dateTime={day.date}>
                                            {formatDate(day.date)}
                                        </time>
                                    </p>

                                    <p className="weekly-chart-tooltip-value">
                                        {formatValue(day.value, metric)}
                                        {day.value !== null ? ` ${units[metric]}` : ''}
                                    </p>

                                    {count > 0 && (
                                        <p>
                                            From {count} workout{' '}
                                            {count === 1 ? 'log' : 'logs'}
                                        </p>
                                    )}
                                </div>
                            );
                        }}
                    />

                    <Bar dataKey="value" name={metricLabel} fill="var(--color-primary)" radius={[6, 6, 0, 0]}
                         maxBarSize={48} isAnimationActive={false}
                    />
                </BarChart>
            </ResponsiveContainer>

            <details className="weekly-chart-data">
                <summary>View daily values</summary>

                <div className="weekly-chart-table">
                    <table>
                        <caption>{metricLabel} by day</caption>
                        <thead>
                        <tr>
                            <th scope="col">Day</th>
                            <th scope="col">
                                Value ({units[metric]})
                            </th>
                            <th scope="col">Logs used</th>
                        </tr>
                        </thead>

                        <tbody>
                        {chartData.map((day) => (
                            <tr key={day.date}>
                                <th scope="row">
                                    <time dateTime={day.date}>
                                        {formatDate(day.date)}
                                    </time>
                                </th>

                                <td>
                                    {formatValue(day.value, metric)}
                                </td>

                                <td>
                                    {day.recordedCounts[metric]}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </details>
        </section>
    );
}