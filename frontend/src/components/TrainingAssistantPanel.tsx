import {useEffect, useRef, useState} from 'react';

import {askTrainingAssistant} from '../api/assistant';
import {ApiError} from '../api/client';

import type {AskTrainingAssistantRequest, TrainingAnswer, TrainingPlan} from '../api/types';

const MAX_QUESTION_LENGTH = 500;

type AssistantState =
    | {status: 'idle'}
    | {status: 'asking'}
    | {status: 'error'; message: string}
    | {status: 'success'; response: TrainingAnswer};

export default function TrainingAssistantPanel(props: {athleteId: string; plans: TrainingPlan[]}) {
    const [planId, setPlanId] = useState('');

    const availablePlans = props.plans.filter((plan) => plan.athleteProfileId === props.athleteId);

    const selectedPlan = availablePlans.find((plan) => plan.id === planId);
    const selectedPlanId = selectedPlan?.id ?? null;

    return (
        <section className="training-assistant">
            <h3>Training assistant</h3>

            <p>
                Ask questions using your profile and PacePilot's training knowledge.
                Select a plan to include its goal, schedule, workout logs, and progress.
                Detailed context focuses on nearby scheduled workouts and recent logs.
            </p>

            <p className="assistant-plan-picker">
                <label>
                    Plan context (optional)
                    <select value={selectedPlanId ?? ''} onChange={(event) =>
                        setPlanId(event.currentTarget.value)}>
                        <option value="">General question (no plan)</option>
                        {availablePlans.map((plan) => (
                            <option key={plan.id} value={plan.id}>
                                {plan.name}{plan.archived ? ' (archived)' : ''}{' '}
                                ({plan.startDate} – {plan.endDate})
                            </option>
                        ))}
                    </select>
                </label>
            </p>

            <AssistantQuestionForm key={`${props.athleteId}:${selectedPlanId ?? 'profile'}`} athleteId={props.athleteId} trainingPlanId={selectedPlanId}/>
        </section>
    );
}

function AssistantQuestionForm(props: {athleteId: string; trainingPlanId: string | null}) {
    const [assistantState, setAssistantState] = useState<AssistantState>({status: 'idle'});
    const pending = useRef<AbortController | null>(null);
    const asking = assistantState.status === 'asking';

    useEffect(() => {
        return () => {
            pending.current?.abort();
            pending.current = null;};
    }, []);

    function showError(message: string) {
        setAssistantState({status: 'error', message});
    }

    async function handleSubmit(form: HTMLFormElement) {
        if (pending.current || !form.reportValidity()) {
            return;
        }

        const data = new FormData(form);
        const question = String(data.get('question') ?? '').trim();

        if (!question) {
            showError('Enter a question.');
            return;
        }

        if (question.length > MAX_QUESTION_LENGTH) {
            showError(`Questions cannot exceed ${MAX_QUESTION_LENGTH} characters.`);
            return;
        }

        const request: AskTrainingAssistantRequest = {athleteProfileId: props.athleteId, question, trainingPlanId: props.trainingPlanId};

        const controller = new AbortController();
        pending.current = controller;
        setAssistantState({status: 'asking'});

        try {
            const response = await askTrainingAssistant(request, controller.signal);

            if (!controller.signal.aborted && pending.current === controller) {
                setAssistantState({status: 'success', response});
            }
        } catch (error: unknown) {
            if (controller.signal.aborted || pending.current !== controller) {
                return;
            }

            showError(error instanceof ApiError ? error.message : 'Unable to get an answer. Please try again.');
        } finally {
            if (pending.current === controller) {
                pending.current = null;
            }
        }
    }

    return (
        <>
            <form className="assistant-form" aria-busy={asking} onSubmit={(event) => {
                    event.preventDefault();
                    void handleSubmit(event.currentTarget);
                }}
            >
                <fieldset disabled={asking}>
                    <legend>Ask the assistant</legend>

                    <p>
                        <label>
                            Question (up to {MAX_QUESTION_LENGTH} characters)
                            <textarea name="question" rows={4} maxLength={MAX_QUESTION_LENGTH}
                                      placeholder={
                                          props.trainingPlanId === null
                                              ? 'How should an easy run feel?' : 'How many workouts have I completed in this plan?'
                                      }
                                      required
                            />
                        </label>
                    </p>
                </fieldset>

                <button type="submit" disabled={asking}>
                    {asking ? 'Generating...' : 'Ask assistant'}
                </button>

                {assistantState.status === 'asking' && (<p role="status">Generating an answer...</p>)}

                {assistantState.status === 'error' && (<p role="alert">{assistantState.message}</p>)}
            </form>

            {assistantState.status === 'success' && (
                <article className="assistant-answer" aria-live="polite">
                    <h4>Answer</h4>

                    <p className="assistant-question">
                        Question: {assistantState.response.question}
                    </p>

                    <p className="assistant-answer-text">
                        {assistantState.response.answer}
                    </p>

                    <section className="assistant-sources">
                        <h5>Retrieved sources</h5>

                        {assistantState.response.sources.length === 0 ? (
                            <p>
                                No training document passages were retrieved. Answers
                                about your profile or selected plan can use your saved
                                information.
                            </p>
                        ) : (
                            assistantState.response.sources.map((source) => (
                                <details
                                    key={`${source.citationNumber}:${source.chunkId}`}
                                    className="assistant-source"
                                >
                                    <summary>
                                        [{source.citationNumber}]{' '}
                                        {source.documentTitle?.trim() || 'Untitled document'}
                                    </summary>

                                    {source.sectionTitle?.trim() && (
                                        <p>Section: {source.sectionTitle}</p>
                                    )}

                                    {source.source?.trim() && (
                                        <p>Source: {source.source}</p>
                                    )}

                                    <p className="assistant-source-content">
                                        {source.content}
                                    </p>
                                </details>
                            ))
                        )}
                    </section>
                </article>
            )}
        </>
    );
}