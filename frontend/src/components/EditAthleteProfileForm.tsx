import {useRef, useState} from 'react';

import {updateAthleteProfile} from '../api/athletes';
import {ApiError} from '../api/client';

import type {AthleteProfile, ExperienceLevel, Gender, UpdateAthleteProfileRequest, WeightUnit} from '../api/types';

interface Option<T extends string> {
    value: T;
    label: string;
}

const experienceOptions: Option<ExperienceLevel>[] = [
    {value: 'BEGINNER', label: 'Beginner'},
    {value: 'INTERMEDIATE', label: 'Intermediate'},
    {value: 'ADVANCED', label: 'Advanced'},
];

const genderOptions: Option<Gender>[] = [
    {value: 'MALE', label: 'Male'},
    {value: 'FEMALE', label: 'Female'},
    {value: 'NON_BINARY', label: 'Non-binary'},
    {value: 'OTHER', label: 'Other'},
    {value: 'PREFER_NOT_TO_SAY', label: 'Prefer not to say'},
];

const weightUnitOptions: Option<WeightUnit>[] = [
    {value: 'KILOGRAMS', label: 'Kilograms'},
    {value: 'POUNDS', label: 'Pounds'},
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

export default function EditAthleteProfileForm(
    props: {
        profile: AthleteProfile;
        onUpdated: (profile: AthleteProfile) => void;
        onCancel: () => void;
    }
) {
    const [submission, setSubmission] = useState<SubmissionState>({status: 'idle'});
    const pending = useRef(false);
    const saving = submission.status === 'saving';

    function showError(message: string) {
        setSubmission({status: 'error', message});
    }

    async function handleSubmit(form: HTMLFormElement) {
        // Prevent multiple submissions or submission if the form is invalid
        if (pending.current || !form.reportValidity()) {
            return;
        }

        const data = new FormData(form);
        const read = (name: string) => String(data.get(name) ?? '').trim();

        const displayName = read('displayName');
        const birthDate = read('birthDate');
        const weightText = read('weight');
        const weight = Number(weightText);

        const experienceLevel = experienceOptions.find(
            (option) => option.value === read('experienceLevel'))?.value;

        const gender = genderOptions.find(
            (option) => option.value === read('gender'))?.value;

        const weightUnit = weightUnitOptions.find(
            (option) => option.value === read('weightUnit'))?.value;

        if (!displayName || !birthDate || !weightText || !experienceLevel || !gender || !weightUnit) {
            showError('Complete all required fields.');
            return;
        }

        if (displayName.length > 100) {
            showError('Display name cannot exceed 100 characters.');
            return;
        }

        if (birthDate > localToday()) {
            showError('Birth date cannot be in the future.');
            return;
        }

        if (!Number.isFinite(weight) || weight <= 0) {
            showError('Weight must be a finite number greater than zero.');
            return;
        }

        const request: UpdateAthleteProfileRequest = {
            displayName,
            birthDate,
            weight,
            weightUnit,
            gender,
            experienceLevel,
        };

        pending.current = true;
        setSubmission({status: 'saving'});

        let profile: AthleteProfile;

        try {
            profile = await updateAthleteProfile(props.profile.id, request);
        } catch (error: unknown) {
            showError(
                error instanceof ApiError
                    ? error.message
                    : 'Unable to confirm the update. Refresh athlete profiles to check whether your changes were saved.'
            );
            return;
        } finally {
            pending.current = false;
        }

        setSubmission({status: 'success', message: `Updated profile for ${profile.displayName}.`});

        props.onUpdated(profile);
    }

    return (
        <form
            className="profile-form profile-edit-form" aria-busy={saving}
            onSubmit={(event) => {
                event.preventDefault();
                void handleSubmit(event.currentTarget);
            }}
        >
            <h2>Edit athlete profile</h2>

            <fieldset disabled={saving}>
                <legend>Profile details</legend>

                <p>
                    <label>
                        Display name
                        <input name="displayName" autoComplete="nickname" defaultValue={props.profile.displayName}
                            maxLength={100} required
                        />
                    </label>
                </p>

                <p>
                    <label>
                        Birth date
                        <input name="birthDate" type="date" autoComplete="bday" defaultValue={props.profile.birthDate}
                            max={localToday()} required
                        />
                    </label>
                </p>

                <p>
                    <label>
                        Gender
                        <select name="gender" defaultValue={props.profile.gender} required>
                            <option value="" disabled>Select gender</option>
                            {genderOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>

                <p>
                    <label>
                        Experience level
                        <select name="experienceLevel" defaultValue={props.profile.experienceLevel} required>
                            <option value="" disabled>Select experience level</option>
                            {experienceOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>
            </fieldset>

            <fieldset disabled={saving}>
                <legend>Body measurements</legend>

                <p>
                    <label>
                        Weight
                        <input name="weight" type="number" defaultValue={props.profile.weight} min={0}
                               step="any" required
                        />
                    </label>
                </p>

                <p>
                    <label>
                        Weight unit
                        <select name="weightUnit" defaultValue={props.profile.weightUnit} required>
                            <option value="" disabled>Select weight unit</option>
                            {weightUnitOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>
            </fieldset>

            <div className="profile-form-actions">
                <button type="submit" disabled={saving}>
                    {saving ? 'Saving...' : 'Save changes'}
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