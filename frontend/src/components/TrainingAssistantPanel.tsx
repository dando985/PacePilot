import {useEffect, useId, useRef, useState} from 'react';

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
            <h3>PacePilot assistant</h3>

            <p>
                Ask about training, recovery, or nutrition. Choose a plan to include
                your workouts and progress.
            </p>

            <p className="assistant-plan-picker">
                <label>
                    Training plan (optional)
                    <select value={selectedPlanId ?? ''}
                        onChange={(event) => setPlanId(event.currentTarget.value)}>
                        <option value="">General training question</option>

                        {availablePlans.map((plan) => (
                            <option key={plan.id} value={plan.id}>
                                {plan.name}{plan.archived ? ' (archived)' : ''}{' '}
                                ({plan.startDate} – {plan.endDate})
                            </option>
                        ))}
                    </select>
                </label>
            </p>

            <AssistantQuestionForm key={`${props.athleteId}:${selectedPlanId ?? 'profile'}`} athleteId={props.athleteId}
                trainingPlanId={selectedPlanId}/>
        </section>
    );
}

function AssistantQuestionForm(props: {athleteId: string; trainingPlanId: string | null}) {
    const questionHintId = useId();
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
            <form className="assistant-form" aria-label="Ask PacePilot" aria-busy={asking}
                  onSubmit={(event) => {
                    event.preventDefault();
                    void handleSubmit(event.currentTarget);
                }}
            >
                <p>
                    <label>
                        Your question
                        <textarea name="question" rows={3} maxLength={MAX_QUESTION_LENGTH} aria-describedby={questionHintId}
                            placeholder={props.trainingPlanId === null
                                ? 'How should an easy run feel?' : 'How many workouts have I completed in this plan?'}
                            disabled={asking} required
                        />
                    </label>

                    <small id={questionHintId}>
                        Up to {MAX_QUESTION_LENGTH} characters.
                    </small>
                </p>

                <button type="submit" disabled={asking}>
                    {asking ? 'Generating...' : 'Ask PacePilot'}
                </button>

                {assistantState.status === 'asking' && (<p role="status">Preparing your answer...</p>)}

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
                        <h5>Sources</h5>

                        {assistantState.response.sources.length === 0 ? (
                            <p>
                                No document sources were returned. Answers can also
                                use your saved profile and selected plan.
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

                                    {source.sectionTitle?.trim() && (<p>Section: {source.sectionTitle}</p>)}

                                    {source.source?.trim() && (<p>Source: {source.source}</p>)}

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