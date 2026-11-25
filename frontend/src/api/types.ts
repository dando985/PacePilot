export type DistanceUnit = 'MILES' | 'KILOMETERS';

export type WeightUnit = 'KILOGRAMS' | 'POUNDS';

export type Gender =
    | 'MALE'
    | 'FEMALE'
    | 'NON_BINARY'
    | 'OTHER'
    | 'PREFER_NOT_TO_SAY';

export type ExperienceLevel =
    | 'BEGINNER'
    | 'INTERMEDIATE'
    | 'ADVANCED';

export type GoalType =
    | 'GENERAL_FITNESS'
    | 'FIVE_K'
    | 'TEN_K'
    | 'HALF_MARATHON'
    | 'MARATHON';

export interface FitnessGoal {
    type: GoalType;
    targetDate: string;
    targetTimeMinutes: number | null;
}

export interface AthleteProfile {
    id: string;
    displayName: string;
    birthDate: string;
    weight: number;
    weightUnit: WeightUnit;
    gender: Gender;
    experienceLevel: ExperienceLevel;
}

export interface CreateFitnessGoalRequest {
    type: GoalType;
    targetDate: string;
    targetTimeMinutes?: number | null;
}

export interface CreateAthleteProfileRequest {
    displayName: string;
    birthDate: string;
    weight: number;
    weightUnit: WeightUnit;
    gender: Gender;
    experienceLevel: ExperienceLevel;
}

export interface UpdateAthleteProfileRequest {
    displayName: string;
    birthDate: string;
    weight: number;
    weightUnit: WeightUnit;
    gender: Gender;
    experienceLevel: ExperienceLevel;
}

export type WorkoutType =
    | 'REST'
    | 'WALK'
    | 'EASY_RUN'
    | 'RECOVERY_RUN'
    | 'LONG_RUN'
    | 'TEMPO_RUN'
    | 'INTERVALS'
    | 'STRENGTH'
    | 'CROSS_TRAINING'
    | 'RACE';

export interface PlannedWorkout {
    id: string;
    scheduledDate: string;
    type: WorkoutType;
    title: string;
    targetDistance: number | null;
    distanceUnit: DistanceUnit | null;
    targetDurationMinutes: number | null;
    instructions: string;
}

export interface TrainingWeek {
    weekNumber: number;
    startDate: string;
    workouts: PlannedWorkout[];
}

export interface TrainingPlan {
    id: string;
    athleteProfileId: string;
    name: string;
    startDate: string;
    endDate: string;
    goal: FitnessGoal;
    availableTrainingDaysPerWeek: number;
    distanceUnit: DistanceUnit;
    archived: boolean;
    weeks: TrainingWeek[];
}

export interface CreatePlannedWorkoutRequest {
    scheduledDate: string;
    type: WorkoutType;
    title: string;
    targetDistance?: number | null;
    distanceUnit?: DistanceUnit | null;
    targetDurationMinutes?: number | null;
    instructions: string;
}

export interface CreateTrainingWeekRequest {
    weekNumber: number;
    startDate: string;
    workouts: CreatePlannedWorkoutRequest[];
}

export interface CreateTrainingPlanRequest {
    name: string;
    startDate: string;
    endDate: string;
    goal: CreateFitnessGoalRequest;
    availableTrainingDaysPerWeek: number;
    distanceUnit: DistanceUnit;
    weeks: CreateTrainingWeekRequest[];
}

export interface GenerateTrainingPlanRequest {
    name: string;
    startDate: string;
    endDate: string;
    goal: CreateFitnessGoalRequest;
    availableTrainingDaysPerWeek: number;
    distanceUnit: DistanceUnit;
}

export interface WorkoutLog {
    id: string;
    athleteProfileId: string;
    plannedWorkoutId: string;
    completedDate: string;
    actualDistance: number | null;
    distanceUnit: DistanceUnit | null;
    actualDurationMinutes: number | null;
    notes: string | null;
}

export interface CreateWorkoutLogRequest {
    completedDate: string;
    actualDistance?: number | null;
    distanceUnit?: DistanceUnit | null;
    actualDurationMinutes?: number | null;
    notes?: string | null;
}

export interface UpdateWorkoutLogRequest {
    completedDate: string;
    actualDistance: number | null;
    distanceUnit: DistanceUnit | null;
    actualDurationMinutes: number | null;
    notes: string | null;
}

export interface AskTrainingAssistantRequest {
    athleteProfileId: string;
    question: string;
    trainingPlanId?: string | null;
}

export interface TrainingAnswerSource {
    citationNumber: number;
    chunkId: string;
    documentTitle: string | null;
    sectionTitle: string | null;
    source: string | null;
    content: string;
    similarity: number;
}

export interface TrainingAnswer {
    question: string;
    answer: string;
    sources: TrainingAnswerSource[];
}

export interface RenameTrainingPlanRequest {
    name: string;
}

export interface UpdateTrainingPlanArchiveRequest {
    archived: boolean;
}

export interface UserAccount {
    id: string;
    email: string;
    createdAt: string;
}

export interface RegisterAccountRequest {
    email: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}