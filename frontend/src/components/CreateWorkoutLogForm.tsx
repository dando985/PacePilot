import {useRef, useState} from 'react';

import {createWorkoutLog} from '../api/workoutLogs';
import {ApiError} from '../api/client';

import type {CreateWorkoutLogRequest, DistanceUnit, WorkoutLog} from '../api/types';

interface Option<T extends string> {
    value: T;
    label: string;
}

const distanceOptions: Option<DistanceUnit>[] = [
    {value: 'MILES', label: 'Miles'},
    {value: 'KILOMETERS', label: 'Kilometers'},
];

type SubmissionState =
    | {status: 'idle'}
    | {status: 'saving'}
    | {status: 'error'; message: string}
    | {status: 'success'; message: string};

function localToday(): string {
    const now = new Date();

    return [
        now.getFullYear(),
        String(now.getMonth() + 1).padStart(2, '0'),
        String(now.getDate()).padStart(2, '0'),
    ].join('-');
}

export default function CreateWorkoutLogForm(props: {planId: string; workoutId: string; onCreated: (log: WorkoutLog) => void;}) {
    const [submission, setSubmission] = useState<SubmissionState>({status: 'idle'});
    const pending = useRef(false);
    const saving = submission.status === 'saving';
    const saved = submission.status === 'success';

    function showError(message: string) {
        setSubmission({status: 'error', message: message});
    }

    async function handleSubmit(form: HTMLFormElement) {
        if (pending.current || saved || !form.reportValidity()) {
            return;
        }

        const data = new FormData(form);
        const read = (name: string) => String(data.get(name) ?? '').trim();

        const completedDate = read('completedDate');
        const distanceText = read('actualDistance');
        const unitText = read('distanceUnit');
        const durationText = read('actualDurationMinutes');
        const notes = read('notes');

        const actualDistance = distanceText === '' ? null : Number(distanceText);
        const actualDurationMinutes = durationText === '' ? null : Number(durationText);

        const distanceUnit = distanceOptions.find((option) => option.value === unitText)?.value ?? null;

        if (!completedDate) {
            showError('Enter a completion date.');
            return;
        }

        if (completedDate > localToday()) {
            showError('Completion date cannot be in the future.');
            return;
        }

        if (actualDistance !== null && (!Number.isFinite(actualDistance) || actualDistance <= 0)) {
            showError('Actual distance must be a finite number greater than zero.');
            return;
        }

        if (unitText !== '' && distanceUnit === null) {
            showError('Select a valid distance unit.');
            return;
        }

        if ((actualDistance !== null) !== (distanceUnit !== null)) {
            showError('Provide both actual distance and its unit, or leave both blank.');
            return;
        }

        if (actualDurationMinutes !== null &&
            (!Number.isInteger(actualDurationMinutes) || actualDurationMinutes < 1 || actualDurationMinutes > 2147483647)) {
            showError('Actual duration must be a positive whole number within the supported range.');
            return;
        }

        const request: CreateWorkoutLogRequest = {
            completedDate,
            actualDistance,
            distanceUnit,
            actualDurationMinutes,
            notes: notes === '' ? null : notes,
        };

        pending.current = true;
        setSubmission({status: 'saving'});

        let log: WorkoutLog;

        try {
            log = await createWorkoutLog(props.planId, props.workoutId, request);
        } catch (error: unknown) {
            showError(error instanceof ApiError
                    ? error.message
                    : 'Unable to confirm the save. Refresh the training plans to check whether the workout was logged.'
            );
            return;
        } finally {
            pending.current = false;
        }

        setSubmission({
            status: 'success',
            message: 'Workout logged successfully.',
        });

        props.onCreated(log);
    }

    return (
        <form className="workout-log-form" aria-busy={saving} onSubmit={(event) => {
            event.preventDefault();
            void handleSubmit(event.currentTarget);
        }}
        >
            <h6>Log workout</h6>

            <fieldset disabled={saving || saved}>
                <legend>Completion details</legend>

                <p>
                    <label>
                        Completion date
                        <input name="completedDate" type="date" defaultValue={localToday()} max={localToday()} required/>
                    </label>
                </p>

                <p>
                    <label>
                        Actual duration in minutes (optional)
                        <input name="actualDurationMinutes" type="number" min={1} max={2147483647} step={1}/>
                    </label>
                </p>
            </fieldset>

            <fieldset disabled={saving || saved}>
                <legend>Distance</legend>

                <p>
                    <label>
                        Actual distance (optional)
                        <input name="actualDistance" type="number" min={0} step="any"/>
                    </label>
                </p>

                <p>
                    <label>
                        Distance unit
                        <select name="distanceUnit" defaultValue="">
                            <option value="">Not provided</option>
                            {distanceOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>
            </fieldset>

            <p>
                <label>
                    Notes (optional)
                    <textarea name="notes" rows={3} disabled={saving || saved}/>
                </label>
            </p>

            <button type="submit" disabled={saving || saved}>
                {saving ? 'Saving...' : saved ? 'Workout logged' : 'Save workout log'}
            </button>

            {submission.status === 'error' && (<p role="alert">{submission.message}</p>)}

            {submission.status === 'success' && (<p role="status">{submission.message}</p>)}
        </form>
    );
}