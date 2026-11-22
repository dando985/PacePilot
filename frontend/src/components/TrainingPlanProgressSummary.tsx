import {calculateTrainingPlanProgress} from '../utils/trainingPlanProgress';

import type {TrainingPlan, WorkoutLog} from '../api/types';

export default function TrainingPlanProgressSummary(props: {plan: TrainingPlan; logs: WorkoutLog[]}) {
    const progress = calculateTrainingPlanProgress(props.plan, props.logs);
    const distanceUnit = progress.distanceUnit === 'MILES' ? 'mi' : 'km';

    const distanceText = progress.recordedDistance === null ? 'Not recorded'
        : `${progress.recordedDistance.toLocaleString(undefined, {maximumFractionDigits: 2,})} ${distanceUnit}`;

    const durationText = progress.recordedDurationMinutes === null
        ? 'Not recorded' : `${progress.recordedDurationMinutes.toLocaleString()} min`;

    return (
        <section className="plan-progress" aria-label="Training plan progress">
            <h4>Progress</h4>

            {progress.completionPercentage === null ? (<p>No active workouts in this plan.</p>) : (
                <>
                    <p className="plan-progress-completion">
                        {progress.completedActiveWorkouts} of{' '}
                        {progress.plannedActiveWorkouts} active workouts completed{' '}
                        ({progress.completionPercentage.toLocaleString(undefined, {
                        maximumFractionDigits: 1,
                    })}%)
                    </p>

                    <progress aria-label="Active workout completion"
                              value={progress.completedActiveWorkouts}
                              max={progress.plannedActiveWorkouts}
                    />
                </>
            )}

            <dl className="plan-progress-totals">
                <div>
                    <dt>Recorded distance</dt>
                    <dd>
                        <span className="plan-progress-value">{distanceText}</span>

                        {progress.distanceLogCount > 0 && (
                            <small>
                                From {progress.distanceLogCount}{' '}
                                {progress.distanceLogCount === 1 ? 'log' : 'logs'}
                            </small>
                        )}
                    </dd>
                </div>

                <div>
                    <dt>Recorded duration</dt>
                    <dd>
                        <span className="plan-progress-value">{durationText}</span>

                        {progress.durationLogCount > 0 && (
                            <small>
                                From {progress.durationLogCount}{' '}
                                {progress.durationLogCount === 1 ? 'log' : 'logs'}
                            </small>
                        )}
                    </dd>
                </div>
            </dl>
        </section>
    );
}