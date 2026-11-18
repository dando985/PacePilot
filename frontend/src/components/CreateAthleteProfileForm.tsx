import { useRef, useState } from 'react';

import { createAthleteProfile } from '../api/athletes';
import { ApiError } from '../api/client';

import type {AthleteProfile, CreateAthleteProfileRequest, DistanceUnit, ExperienceLevel, GoalType,} from '../api/types';

interface Option<T extends string> {
    value: T;
    label: string;
}

type SubmissionState =
    | { status: 'idle' }
    | { status: 'saving' }
    | { status: 'error'; message: string }
    | { status: 'success'; message: string };

const experienceOptions: Option<ExperienceLevel>[] = [
    { value: 'BEGINNER', label: 'Beginner' },
    { value: 'INTERMEDIATE', label: 'Intermediate' },
    { value: 'ADVANCED', label: 'Advanced' },
];

const goalOptions: Option<GoalType>[] = [
    { value: 'GENERAL_FITNESS', label: 'General fitness' },
    { value: 'FIVE_K', label: '5K' },
    { value: 'TEN_K', label: '10K' },
    { value: 'HALF_MARATHON', label: 'Half marathon' },
    { value: 'MARATHON', label: 'Marathon' },
];

const distanceOptions: Option<DistanceUnit>[] = [
    { value: 'MILES', label: 'Miles' },
    { value: 'KILOMETERS', label: 'Kilometers' },
];

// Retrieves current dat in YYYY-MM-DD format
function localToday(): string {
    const now = new Date();

    return [
        now.getFullYear(),
        String(now.getMonth() + 1).padStart(2, '0'),
        String(now.getDate()).padStart(2, '0'),
    ].join('-');
}

// Form for creating a new athlete profile.
export default function CreateAthleteProfileForm(props: {onCreated: () => void}) {
    const [submission, setSubmission] = useState<SubmissionState>({status: 'idle'});
    const pending = useRef(false);
    const saving = submission.status === 'saving';

    function showError(message: string) {
        setSubmission({status: 'error', message});
    }

    // Prevents a separate form submission from occurring while a previous submission is still pending or exits if form is invalid
    async function handleSubmit(form: HTMLFormElement) {
        if (pending.current || !form.reportValidity()) {
            return;
        }

        // Read form data from form fields
        const data = new FormData(form);
        // Helper function to read and trim FormData values and return them as strings.
        const read = (name: string) => String(data.get(name) ?? '').trim();

        const displayName = read('displayName');
        const targetDate = read('targetDate');
        const trainingDays = Number(read('availableTrainingDaysPerWeek'));
        const experienceLevel = experienceOptions.find((option) => option.value === read('experienceLevel'),)?.value;
        const goalType = goalOptions.find((option) => option.value === read('goalType'),)?.value;
        const distanceUnit = distanceOptions.find((option) => option.value === read('runningDistanceUnit'),)?.value;
        const distanceText = read('currentWeeklyRunningDistance');
        const timeText = read('targetTimeMinutes');
        const weeklyDistance = distanceText === '' ? null : Number(distanceText);
        const targetTimeMinutes = timeText === '' ? null : Number(timeText);

        if (!displayName || !experienceLevel || !goalType || !targetDate) {
            showError('Complete all required fields.');
            return;
        }

        if (!Number.isInteger(trainingDays) || trainingDays < 1 || trainingDays > 7) {
            showError('Training days must be a whole number from 1 to 7.');
            return;
        }

        if (weeklyDistance !== null && (!Number.isFinite(weeklyDistance) || weeklyDistance < 0)) {
            showError('Weekly running distance must be a finite, nonnegative number.');
            return;
        }

        const hasDistance = weeklyDistance !== null;
        const hasDistanceUnit = distanceUnit !== undefined;

        if (hasDistance !== hasDistanceUnit) {
            showError('Provide both weekly running distance and its unit, or leave both blank.');
            return;
        }

        if (targetTimeMinutes !== null && (!Number.isInteger(targetTimeMinutes) || targetTimeMinutes < 1 || targetTimeMinutes > 2_147_483_647)) {
            showError('Target time must be a positive whole number within the supported range.');
            return;
        }

        if (targetDate < localToday()) {
            showError('Choose today or a future date for your goal.');
            return;
        }

        const request: CreateAthleteProfileRequest = {
            displayName,
            experienceLevel,
            availableTrainingDaysPerWeek: trainingDays,
            currentWeeklyRunningDistance: weeklyDistance,
            runningDistanceUnit: distanceUnit ?? null,
            goal: {
                type: goalType,
                targetDate,
                targetTimeMinutes,
            },
        };

        pending.current = true;
        setSubmission({status: 'saving'});

        let profile: AthleteProfile;

        try {
            profile = await createAthleteProfile(request);
        } catch (error: unknown) {
            showError(error instanceof ApiError ? error.message : 'Unable to create the profile. Please try again.',);
            return;
        } finally {
            pending.current = false;
        }

        form.reset();

        setSubmission({
            status: 'success',
            message: `Created profile for ${profile.displayName}.`,
        });

        // Notify the parent component that a new profile has been created, requesting a reload of the athlete profiles list.
        props.onCreated();
    }

    return (
        <form className="profile-form" aria-busy={saving} onSubmit={(event) => {
            event.preventDefault();
            void handleSubmit(event.currentTarget);
        }}>
            <h2>Create athlete profile</h2>

            <fieldset disabled={saving}>
                <legend>Profile details</legend>

                <p>
                    <label>
                        Display name
                        <input name="displayName" autoComplete="nickname" maxLength={100} required/>
                    </label>
                </p>

                <p>
                    <label>
                        Experience level
                        <select name="experienceLevel" defaultValue="" required>
                            <option value="" disabled>Select experience level</option>
                            {experienceOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>

                <p>
                    <label>
                        Available training days per week
                        <input name="availableTrainingDaysPerWeek" type="number" min={1} max={7} step={1} required/>
                    </label>
                </p>

                <p>
                    <label>
                        Weekly running distance (optional)
                        <input name="currentWeeklyRunningDistance" type="number" min={0} step="any"/>
                    </label>
                </p>

                <p>
                    <label>
                        Distance unit
                        <select name="runningDistanceUnit" defaultValue="">
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

            <fieldset disabled={saving}>
                <legend>Fitness goal</legend>

                <p>
                    <label>
                        Goal
                        <select name="goalType" defaultValue="" required>
                            <option value="" disabled>Select a goal</option>
                            {goalOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>

                <p>
                    <label>
                        Target date
                        <input name="targetDate" type="date" min={localToday()} required/>
                    </label>
                </p>

                <p>
                    <label>
                        Target time in minutes (optional)
                        <input name="targetTimeMinutes" type="number" min={1} max={2_147_483_647} step={1}/>
                    </label>
                </p>
            </fieldset>

            <button type="submit" disabled={saving}>{saving ? 'Creating...' : 'Create profile'}</button>

            {submission.status === 'error' && (<p role="alert">{submission.message}</p>)}

            {submission.status === 'success' && (<p role="status">{submission.message}</p>)}
        </form>
    );
}