import {apiRequest} from './client';

import type {AskTrainingAssistantRequest, TrainingAnswer} from './types';

export function askTrainingAssistant(request: AskTrainingAssistantRequest, signal: AbortSignal | null = null): Promise<TrainingAnswer> {
    return apiRequest<TrainingAnswer>('/assistant/ask', {method: 'POST', body: request, signal,});
}