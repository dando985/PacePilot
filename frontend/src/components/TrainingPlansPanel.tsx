import {useEffect, useRef, useState} from 'react';

import {getTrainingPlans, setTrainingPlanArchived} from '../api/trainingPlans';
import {getWorkoutLogs} from '../api/workoutLogs';
import CreateWorkoutLogForm from './CreateWorkoutLogForm';
import EditWorkoutLogForm from './EditWorkoutLogForm';
import GenerateTrainingPlanForm from './GenerateTrainingPlanForm';
import RenameTrainingPlanForm from './RenameTrainingPlanForm';
import TrainingAssistantPanel from './TrainingAssistantPanel';
import TrainingPlanProgressSummary from './TrainingPlanProgressSummary';
import {ApiError} from '../api/client';

import type {DistanceUnit, GoalType, TrainingPlan, WorkoutLog} from '../api/types';

type PlanState =
    | {status: 'loading'; plans: TrainingPlan[]}
    | {status: 'success'; plans: TrainingPlan[]; logs: WorkoutLog[]}
    | {status: 'error'; message: string; plans: TrainingPlan[]};

type ArchiveState =
    | {status: 'idle'}
    | {status: 'saving'; planId: string}
    | {status: 'error'; message: string};

const goalLabels: Record<GoalType, string> = {
    GENERAL_FITNESS: 'General fitness',
    FIVE_K: '5K',
    TEN_K: '10K',
    HALF_MARATHON: 'Half marathon',
    MARATHON: 'Marathon',
};

const distanceLabels: Record<DistanceUnit, string> = {
    MILES: 'Miles',
    KILOMETERS: 'Kilometers',
};

export default function TrainingPlansPanel(props: {athleteId: string}) {
    const [planState, setPlanState] = useState<PlanState>({status: 'loading', plans: []});
    const [reloadCount, setReloadCount] = useState(0);
    const [renamingPlanId, setRenamingPlanId] = useState<string | null>(null);
    const [editingLogId, setEditingLogId] = useState<string | null>(null);
    const [planMessage, setPlanMessage] = useState<string | null>(null);
    const [showArchived, setShowArchived] = useState(false);
    const [archiveState, setArchiveState] = useState<ArchiveState>({status: 'idle'});
    const [showGenerationForm, setShowGenerationForm] = useState(false);
    const [generationFormVersion, setGenerationFormVersion] = useState(0);

    const generationFormId = `plan-generation-${props.athleteId}`;

    const archivePending = useRef(false);
    const archiving = archiveState.status === 'saving';

    useEffect(() => {
        const controller = new AbortController();

        async function loadPlansAndLogs() {
            try {
                const [plans, logs] = await Promise.all([
                    getTrainingPlans(props.athleteId, controller.signal),
                    getWorkoutLogs(props.athleteId, controller.signal),
                ]);

                if (!controller.signal.aborted) {
                    setPlanState({status: 'success', plans, logs});

                    setRenamingPlanId((currentId) =>
                        plans.some((plan) => plan.id === currentId) ? currentId : null
                    );

                    setEditingLogId((currentId) =>
                        logs.some((log) => log.id === currentId) ? currentId : null
                    );
                }
            } catch (error: unknown) {
                if (controller.signal.aborted) {
                    return;
                }

                setPlanState((current) => ({
                    status: 'error',
                    plans: current.plans,
                    message:
                        error instanceof ApiError
                            ? error.message : 'Unable to load training plans and workout logs. Please try again.',
                }));
            }
        }

        void loadPlansAndLogs();

        return () => {controller.abort();};
    }, [props.athleteId, reloadCount]);

    function reloadPlans() {
        setRenamingPlanId(null);
        setEditingLogId(null);
        setPlanMessage(null);
        setArchiveState((current) =>
            current.status === 'saving' ? current : {status: 'idle'}
        );
        setPlanState((current) => ({status: 'loading', plans: current.plans}));
        setReloadCount((count) => count + 1);
    }

    function handlePlanGenerated() {
        setShowGenerationForm(false);
        setGenerationFormVersion((version) => version + 1);
        setPlanMessage('New plan generated');
        setReloadCount((count) => count + 1);
    }

    function startRenamingPlan(planId: string) {
        if (archivePending.current) {
            return;
        }
        setPlanMessage(null);
        setRenamingPlanId(planId);
    }

    function startEditingLog(logId: string) {
        setPlanMessage(null);
        setEditingLogId(logId);
    }

    function handlePlanRenamed(renamedPlan: TrainingPlan) {
        setPlanState((current) => {
            if (current.status !== 'success') {
                return current;
            }

            return {
                ...current,
                plans: current.plans.map((plan) => plan.id === renamedPlan.id ? renamedPlan : plan
                ),
            };
        });

        setRenamingPlanId((currentId) => currentId === renamedPlan.id ? null : currentId);

        setPlanMessage(`Renamed plan to ${renamedPlan.name}.`);
        setReloadCount((count) => count + 1);
    }

    async function handleArchiveChange(plan: TrainingPlan) {
        if (archivePending.current || renamingPlanId === plan.id) {
            return;
        }

        archivePending.current = true;
        setPlanMessage(null);
        setArchiveState({status: 'saving', planId: plan.id});

        let updatedPlan: TrainingPlan;

        try {
            updatedPlan = await setTrainingPlanArchived(plan.id, {archived: !plan.archived,});
        } catch (error: unknown) {
            setArchiveState({
                status: 'error',
                message:
                    error instanceof ApiError
                        ? error.message
                        : 'Unable to confirm the archive change. Refresh training plans to check its status.',
            });

            return;
        } finally {
            archivePending.current = false;
        }

        setPlanState((current) => {
            if (current.status !== 'success') {
                return current;
            }

            return {
                ...current,
                plans: current.plans.map((existing) =>
                    existing.id === updatedPlan.id ? updatedPlan : existing
                ),
            };
        });

        setArchiveState({status: 'idle'});

        setPlanMessage(
            `${updatedPlan.archived ? 'Archived' : 'Restored'} ${updatedPlan.name}.`
        );

        setReloadCount((count) => count + 1);
    }

    function handleLogSaved(log: WorkoutLog) {
        setPlanState((current) => {
            if (current.status !== 'success') {
                return current;
            }

            return {
                ...current,
                logs: [
                    ...current.logs.filter(
                        (existing) => existing.plannedWorkoutId !== log.plannedWorkoutId
                    ),
                    log,
                ],
            };
        });

        setEditingLogId((currentId) => currentId === log.id ? null : currentId);

        setPlanMessage('Workout log saved.');
        setReloadCount((count) => count + 1);
    }

    const visiblePlans = planState.status === 'success'
        ? planState.plans.filter((plan) => showArchived || !plan.archived) : [];

    const logsByWorkoutId = new Map<string, WorkoutLog>();

    if (planState.status === 'success') {
        for (const log of planState.logs) {
            logsByWorkoutId.set(log.plannedWorkoutId, log);
        }
    }

    return (
        <div className="training-workspace">
            <section className="training-plans">
            <header className="training-plans-header">
                <h2>Training plans</h2>

                <div className="plan-form-actions">
                    <button type="button" onClick={reloadPlans} disabled={planState.status === 'loading' || archiving}>
                        {planState.status === 'error' ? 'Retry' : 'Refresh'}
                    </button>

                    <button type="button" aria-expanded={showGenerationForm} aria-controls={generationFormId}
                        onClick={() => setShowGenerationForm((current) => !current)}
                            disabled={archiving}
                    >
                        <span aria-hidden="true">
                            {showGenerationForm ? '−' : '+'}
                        </span>
                        {showGenerationForm ? 'Hide form' : 'New plan'}
                    </button>
                </div>
            </header>


            <section id={generationFormId} aria-label="New training plan" hidden={!showGenerationForm}>
                <GenerateTrainingPlanForm key={generationFormVersion} athleteId={props.athleteId}
                                          onGenerated={handlePlanGenerated}
                />
            </section>

            {planMessage !== null && (
                <p role="status" className="plan-update-message">
                    {planMessage}
                </p>
            )}

            {archiveState.status === 'saving' && (<p role="status">Updating plan archive status...</p>)}

            {archiveState.status === 'error' && (<p role="alert">{archiveState.message}</p>)}

            {planState.status === 'success' && planState.plans.length > 0 && (
                <p className="plan-archive-filter">
                    <label>
                        <input
                            type="checkbox"
                            checked={showArchived}
                            onChange={(event) => setShowArchived(event.currentTarget.checked)}
                            disabled={archiving}
                        />
                        Show archived plans
                    </label>
                </p>
            )}

            {planState.status === 'loading' && (<p role="status">Loading training plans and workout logs...</p>)}

            {planState.status === 'error' && (<p role="alert">{planState.message}</p>)}

            {planState.status === 'success' && (
                visiblePlans.length === 0 ? (
                    <p>
                        {planState.plans.length === 0
                            ? 'No training plans yet. Choose New plan to create your first one.'
                            : 'No active training plans. Select "Show archived plans" to view or restore archived plans.'}
                    </p>
                ) : (
                    <div className="plan-list">
                        {visiblePlans.map((plan) => (
                            <article key={plan.id} className="plan-card" data-archived={plan.archived}>
                                <header className="plan-card-header">
                                    <h3>{plan.name}</h3>

                                    <button type="button" onClick={() => startRenamingPlan(plan.id)}
                                        disabled={archiving}
                                    >
                                        Rename plan
                                    </button>
                                </header>

                                {renamingPlanId === plan.id && (
                                    <RenameTrainingPlanForm key={plan.id} plan={plan} onRenamed={handlePlanRenamed}
                                        onCancel={() => setRenamingPlanId(null)}
                                    />
                                )}

                                <div className="plan-archive-actions">
                                    <p>Status: {plan.archived ? 'Archived' : 'Active'}</p>

                                    <button type="button" onClick={() => {void handleArchiveChange(plan);}}
                                        disabled={archiving || renamingPlanId === plan.id}
                                    >
                                        {archiveState.status === 'saving' &&
                                        archiveState.planId === plan.id
                                            ? (plan.archived ? 'Restoring...' : 'Archiving...')
                                            : (plan.archived ? 'Restore plan' : 'Archive plan')}
                                    </button>
                                </div>

                                <p>{plan.startDate} – {plan.endDate}</p>

                                <p>Goal: {goalLabels[plan.goal.type]}</p>

                                <p>Goal target date: {plan.goal.targetDate}</p>

                                {plan.goal.targetTimeMinutes !== null && (
                                    <p>
                                        Target finish time: {plan.goal.targetTimeMinutes} min
                                    </p>
                                )}

                                <p>
                                    Training days per week: {plan.availableTrainingDaysPerWeek}
                                </p>

                                <p>
                                    Preferred distance unit: {distanceLabels[plan.distanceUnit]}
                                </p>

                                <TrainingPlanProgressSummary plan={plan} logs={planState.logs}/>

                                <details>
                                    <summary>View workouts</summary>

                                    {plan.weeks.map((week) => (
                                        <section key={week.weekNumber} className="plan-week">
                                            <h4>Week {week.weekNumber}</h4>
                                            <p>Starts {week.startDate}</p>

                                            <ul className="workout-list">
                                                {week.workouts.map((workout) => {
                                                    const log = logsByWorkoutId.get(workout.id);

                                                    return (
                                                        <li key={workout.id}>
                                                            <h5>{workout.title}</h5>
                                                            <p>Scheduled: {workout.scheduledDate}</p>

                                                            <p className="workout-status">
                                                                {log ? 'Completed' : 'Not logged'}
                                                            </p>

                                                            {workout.targetDistance !== null &&
                                                                workout.distanceUnit !== null && (
                                                                    <p>
                                                                        Planned distance: {workout.targetDistance}{' '}
                                                                        {workout.distanceUnit === 'MILES' ? 'mi' : 'km'}
                                                                    </p>
                                                                )}

                                                            {workout.targetDurationMinutes !== null && (
                                                                <p>
                                                                    Planned duration: {workout.targetDurationMinutes} min
                                                                </p>
                                                            )}

                                                            <p className="workout-instructions">
                                                                {workout.instructions}
                                                            </p>

                                                            {log ? (
                                                                <div className="workout-log">
                                                                    {editingLogId === log.id ? (
                                                                        <EditWorkoutLogForm key={log.id} log={log}
                                                                            onUpdated={handleLogSaved}
                                                                            onCancel={() => setEditingLogId(null)}
                                                                        />
                                                                    ) : (
                                                                        <>
                                                                            <header className="workout-log-header">
                                                                                <h6>Completion details</h6>

                                                                                <button type="button"
                                                                                    onClick={() => startEditingLog(log.id)}
                                                                                >
                                                                                    Edit log
                                                                                </button>
                                                                            </header>

                                                                            <p>Completed: {log.completedDate}</p>

                                                                            {log.actualDistance !== null &&
                                                                                log.distanceUnit !== null && (
                                                                                    <p>
                                                                                        Actual distance: {log.actualDistance}{' '}
                                                                                        {log.distanceUnit === 'MILES'
                                                                                            ? 'mi'
                                                                                            : 'km'}
                                                                                    </p>
                                                                                )}

                                                                            {log.actualDurationMinutes !== null && (
                                                                                <p>
                                                                                    Actual duration: {log.actualDurationMinutes} min
                                                                                </p>
                                                                            )}

                                                                            {log.notes !== null && (
                                                                                <p className="workout-log-notes">
                                                                                    Notes: {log.notes}
                                                                                </p>
                                                                            )}
                                                                        </>
                                                                    )}
                                                                </div>
                                                            ) : (
                                                                <details className="workout-log-details">
                                                                    <summary>Log workout</summary>

                                                                    <CreateWorkoutLogForm planId={plan.id}
                                                                        workoutId={workout.id}
                                                                        onCreated={handleLogSaved}
                                                                    />
                                                                </details>
                                                            )}
                                                        </li>
                                                    );
                                                })}
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

            <aside className="assistant-sidebar" aria-label="Training assistant">
                <TrainingAssistantPanel athleteId={props.athleteId} plans={planState.plans}/>
            </aside>
        </div>
    );
}