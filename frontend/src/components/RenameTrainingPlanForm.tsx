import {useRef, useState} from 'react';

import {renameTrainingPlan} from '../api/trainingPlans';
import {ApiError} from '../api/client';

import type {RenameTrainingPlanRequest, TrainingPlan} from '../api/types';

type SubmissionState =
    | {status: 'idle'}
    | {status: 'saving'}
    | {status: 'error'; message: string}
    | {status: 'success'; message: string};

export default function RenameTrainingPlanForm(
    props: {
        plan: TrainingPlan;
        onRenamed: (plan: TrainingPlan) => void;
        onCancel: () => void;
    }
) {
    const [submission, setSubmission] = useState<SubmissionState>({status: 'idle'});
    const pending = useRef(false);
    const saving = submission.status === 'saving';

    async function handleSubmit(form: HTMLFormElement) {
        if (pending.current || !form.reportValidity()) {
            return;
        }

        const data = new FormData(form);
        const name = String(data.get('name') ?? '').trim();

        if (!name) {
            setSubmission({status: 'error', message: 'Enter a training plan name.'});
            return;
        }

        const request: RenameTrainingPlanRequest = {name};

        pending.current = true;
        setSubmission({status: 'saving'});

        let plan: TrainingPlan;

        try {
            plan = await renameTrainingPlan(props.plan.id, request);
        } catch (error: unknown) {
            setSubmission({
                status: 'error',
                message:
                    error instanceof ApiError
                        ? error.message
                        : 'Unable to confirm the rename. Refresh training plans to check whether the name was saved.',
            });
            return;
        } finally {
            pending.current = false;
        }

        setSubmission({
            status: 'success',
            message: 'Training plan renamed successfully.',
        });

        props.onRenamed(plan);
    }

    return (
        <form className="plan-rename-form" aria-label="Rename training plan" aria-busy={saving}
              onSubmit={(event) => {event.preventDefault();void handleSubmit(event.currentTarget);}}
        >
            <h4>Rename training plan</h4>

            <p>
                <label>
                    Plan name
                    <input name="name" defaultValue={props.plan.name} disabled={saving} required/>
                </label>
            </p>

            <div className="plan-form-actions">
                <button type="submit" disabled={saving}>
                    {saving ? 'Saving...' : 'Save name'}
                </button>

                <button type="button" onClick={props.onCancel} disabled={saving}>
                    Cancel
                </button>
            </div>

            {submission.status === 'error' && (<p role="alert">{submission.message}</p>)}

            {submission.status === 'success' && (<p role="status">{submission.message}</p>)}
        </form>
    );
}