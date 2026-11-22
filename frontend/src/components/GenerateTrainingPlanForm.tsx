import {useRef, useState} from 'react';

import {generateTrainingPlan} from '../api/trainingPlans';
import {ApiError} from '../api/client';

import type {DistanceUnit, GenerateTrainingPlanRequest, GoalType, TrainingPlan,} from '../api/types';

interface Option<T extends string> {
    value: T;
    label: string;
}

const goalOptions: Option<GoalType>[] = [
    {value: 'GENERAL_FITNESS', label: 'General fitness'},
    {value: 'FIVE_K', label: '5K'},
    {value: 'TEN_K', label: '10K'},
    {value: 'HALF_MARATHON', label: 'Half marathon'},
    {value: 'MARATHON', label: 'Marathon'},
];

const distanceOptions: Option<DistanceUnit>[] = [
    {value: 'MILES', label: 'Miles'},
    {value: 'KILOMETERS', label: 'Kilometers'},
];

const MAX_PLAN_DAYS = 26 * 7;
const MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

type GenerationState =
    | {status: 'idle'}
    | {status: 'generating'}
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

export default function GenerateTrainingPlanForm(props: {athleteId: string; onGenerated: () => void}) {
    const [generation, setGeneration] = useState<GenerationState>({status: 'idle'});
    const pending = useRef(false);
    const generating = generation.status === 'generating';

    function showError(message: string) {
        setGeneration({status: 'error', message});
    }

    async function handleSubmit(form: HTMLFormElement) {
        if (pending.current || !form.reportValidity()) {
            return;
        }

        const data = new FormData(form);
        const read = (name: string) => String(data.get(name) ?? '').trim();

        const planName = read('name');
        const startDate = read('startDate');
        const endDate = read('endDate');
        const targetDate = read('targetDate');
        const trainingDaysText = read('availableTrainingDaysPerWeek');
        const trainingDays = Number(trainingDaysText);
        const targetTimeText = read('targetTimeMinutes');
        const targetTimeMinutes = targetTimeText === '' ? null : Number(targetTimeText);

        // Find the first matching goal type and distance unit from the options arrays.
        const goalType = goalOptions.find((option) => option.value === read('goalType'))?.value;

        const distanceUnit = distanceOptions.find(
            (option) => option.value === read('distanceUnit'))?.value;

        if (!planName || !startDate || !endDate || !targetDate || !trainingDaysText || !goalType || !distanceUnit) {
            showError('Complete all required fields.');
            return;
        }

        if (startDate < localToday()) {
            showError('The start date must be today or a future date.');
            return;
        }

        if (endDate < startDate) {
            showError('The end date must be on or after the start date.');
            return;
        }

        const planDays =
            (Date.parse(`${endDate}T00:00:00Z`) - Date.parse(`${startDate}T00:00:00Z`)) / MILLISECONDS_PER_DAY + 1;

        if (!Number.isInteger(planDays) || planDays < 1 || planDays > MAX_PLAN_DAYS) {
            showError('Choose a plan period of 1 to 84 days, including both dates.');
            return;
        }

        if (targetDate < startDate) {
            showError('The goal target date must be on or after the plan start date.');
            return;
        }

        if (!Number.isInteger(trainingDays) || trainingDays < 1 || trainingDays > 7) {
            showError('Training days must be a whole number from 1 to 7.');
            return;
        }

        if (targetTimeMinutes !== null &&
            (!Number.isInteger(targetTimeMinutes) || targetTimeMinutes < 1 || targetTimeMinutes > 2_147_483_647)) {
            showError('Target finish time must be a positive whole number within the supported range.');
            return;
        }

        const request: GenerateTrainingPlanRequest = {
            name: planName,
            startDate,
            endDate,
            goal: {
                type: goalType,
                targetDate,
                targetTimeMinutes,
            },
            availableTrainingDaysPerWeek: trainingDays,
            distanceUnit,
        };

        pending.current = true;
        setGeneration({status: 'generating'});

        let plan: TrainingPlan;

        try {
            plan = await generateTrainingPlan(props.athleteId, request);
        } catch (error: unknown) {
            showError(error instanceof ApiError ? error.message : 'Unable to generate a plan. Please try again.');
            return;
        } finally {
            pending.current = false;
        }

        // Reset the form after successful submission
        form.reset();

        setGeneration({
            status: 'success',
            message: `Generated plan: ${plan.name}`,
        });

        // Call the onGenerated callback to notify the parent component that a new plan has been generated
        props.onGenerated();
    }

    return (
        <form className="plan-generation-form" aria-busy={generating}
              onSubmit={(event) => {
                event.preventDefault();
                void handleSubmit(event.currentTarget);
            }}
        >
            <h3>Generate a training plan</h3>

            <p>You can generate up to 12 weeks (84 days) at a time.</p>

            <fieldset disabled={generating}>
                <legend>Plan details</legend>

                <p>
                    <label>
                        Plan name
                        <input name="name" type="text" required/>
                    </label>
                </p>

                <p>
                    <label>
                        Start date
                        <input name="startDate" type="date" min={localToday()} required/>
                    </label>
                </p>

                <p>
                    <label>
                        End date
                        <input name="endDate" type="date" min={localToday()} required/>
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
                        Preferred distance unit
                        <select name="distanceUnit" defaultValue="" required>
                            <option value="" disabled>Select distance unit</option>
                            {distanceOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>
            </fieldset>

            <fieldset disabled={generating}>
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
                        Goal target date
                        <input name="targetDate" type="date" min={localToday()} required/>
                    </label>
                    <small>This can be later than the end of the training plan.</small>
                </p>

                <p>
                    <label>
                        Target finish time in minutes (optional)
                        <input name="targetTimeMinutes" type="number" min={1} max={2_147_483_647} step={1}/>
                    </label>
                </p>
            </fieldset>

            <button type="submit" disabled={generating}>
                {generating ? 'Generating...' : 'Generate plan'}
            </button>

            {generation.status === 'error' && (<p role="alert">{generation.message}</p>)}

            {generation.status === 'success' && (<p role="status">{generation.message}</p>)}
        </form>
    );
}