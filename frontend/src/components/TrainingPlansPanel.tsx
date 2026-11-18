import {useEffect, useState} from 'react';

import {getTrainingPlans} from '../api/trainingPlans';
import GenerateTrainingPlanForm from './GenerateTrainingPlanForm';
import {ApiError} from '../api/client';
import type {TrainingPlan} from '../api/types';

type PlanState =
    | {status: 'loading'}
    | {status: 'success'; plans: TrainingPlan[]}
    | {status: 'error'; message: string};

export default function TrainingPlansPanel(props: {athleteId: string}) {
    const [planState, setPlanState] = useState<PlanState>({status: 'loading'});
    const [reloadCount, setReloadCount] = useState(0);

    useEffect(() => {
        const controller = new AbortController();

        async function loadPlans() {
            try {
                const plans = await getTrainingPlans(props.athleteId, controller.signal,);

                if (!controller.signal.aborted) {
                    setPlanState({status: 'success', plans,});
                }
            } catch (error: unknown) {
                if (controller.signal.aborted) {
                    return;
                }

                setPlanState({status: 'error', message: error instanceof ApiError ? error.message : 'Unable to load training plans. Please try again.',});
            }
        }

        // Call the function to load training plans.
        void loadPlans();

        // Cleanup function to abort the fetch request if the component unmounts or reloadCount changes.
        return () => {controller.abort();};

        // Load training plans when the component mounts or when props.athleteId or reloadCount changes.
    }, [props.athleteId, reloadCount]);

    function reloadPlans() {
        setPlanState({ status: 'loading' });
        setReloadCount((count) => count + 1);
    }

    return (
        <section className="training-plans">
            <header className="training-plans-header">
                <h2>Training plans</h2>

                <button type="button" onClick={reloadPlans} disabled={planState.status === 'loading'}>
                    {planState.status === 'error' ? 'Retry' : 'Refresh'}
                </button>
            </header>

            <GenerateTrainingPlanForm athleteId={props.athleteId} onGenerated={reloadPlans}/>

            {planState.status === 'loading' && (<p role="status">Loading training plans...</p>)}

            {planState.status === 'error' && (<p role="alert">{planState.message}</p>)}

            {planState.status === 'success' && (
                planState.plans.length === 0 ? (
                    <p>No training plans yet for this athlete.</p>
                ) : (
                    <div className="plan-list">
                        {planState.plans.map((plan) => (
                            <article key={plan.id} className="plan-card">
                                <h3>{plan.name}</h3>
                                <p>{plan.startDate} – {plan.endDate}</p>

                                <details>
                                    <summary>View workouts</summary>

                                    {plan.weeks.map((week) => (
                                        <section key={week.weekNumber} className="plan-week">
                                            <h4>Week {week.weekNumber}</h4>
                                            <p>Starts {week.startDate}</p>

                                            <ul className="workout-list">
                                                {week.workouts.map((workout) => (
                                                    <li key={workout.id}>
                                                        <h5>{workout.title}</h5>
                                                        <p>{workout.scheduledDate}</p>

                                                        {workout.targetDistance !== null &&
                                                            workout.distanceUnit !== null && (
                                                                <p>
                                                                    Distance: {workout.targetDistance}{' '}
                                                                    {workout.distanceUnit === 'MILES'
                                                                        ? 'mi'
                                                                        : 'km'}
                                                                </p>
                                                            )}

                                                        {workout.targetDurationMinutes !== null && (
                                                            <p>
                                                                Duration: {workout.targetDurationMinutes} min
                                                            </p>
                                                        )}

                                                        <p className="workout-instructions">
                                                            {workout.instructions}
                                                        </p>
                                                    </li>
                                                ))}
                                            </ul>
                                        </section>
                                    ))}
                                </details>
                            </article>
                        ))}
                    </div>
                )
            )}
        </section>
    );
}