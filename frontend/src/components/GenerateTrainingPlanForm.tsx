import {useRef, useState} from 'react';

import {generateTrainingPlan} from '../api/trainingPlans';
import {ApiError} from '../api/client';

import type {GenerateTrainingPlanRequest, TrainingPlan} from '../api/types';

type GenerationState =
    | { status: 'idle' }
    | { status: 'generating' }
    | { status: 'error'; message: string }
    | { status: 'success'; message: string };

function localToday(): string {
    const now = new Date();

    return [
        now.getFullYear(),
        String(now.getMonth() + 1).padStart(2, '0'),
        String(now.getDate()).padStart(2, '0'),
    ].join('-');
}

export default function GenerateTrainingPlanForm(props: {athleteId: string; onGenerated: () => void; }) {
    const [generation, setGeneration] = useState<GenerationState>({status: 'idle',});
    const pending = useRef(false);
    const generating = generation.status === 'generating';

    function showError(message: string) {
        setGeneration({ status: 'error', message });
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

        if (!planName || !startDate || !endDate) {
            showError('Enter a plan name and both dates.');
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

        const request: GenerateTrainingPlanRequest = {
            name: planName,
            startDate,
            endDate,
        };

        pending.current = true;
        setGeneration({ status: 'generating' });

        let plan: TrainingPlan;

        try {
            plan = await generateTrainingPlan(props.athleteId, request);
        } catch (error: unknown) {
            showError(error instanceof ApiError ? error.message : 'Unable to generate a plan. Please try again.');
            return;
        } finally {
            pending.current = false;
        }

        form.reset();

        setGeneration({status: 'success', message: `Generated plan: ${plan.name}`});

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

            <fieldset disabled={generating}>
                <legend>Plan details</legend>

                <p>
                    <label>
                        Plan name
                        <input name="name" type="text" required />
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
            </fieldset>

            <button type="submit" disabled={generating}>
                {generating ? 'Generating...' : 'Generate plan'}
            </button>

            {generation.status === 'error' && (<p role="alert">{generation.message}</p>)}

            {generation.status === 'success' && (<p role="status">{generation.message}</p>)}
        </form>
    );
}