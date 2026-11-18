export type DistanceUnit = 'MILES' | 'KILOMETERS';

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
    experienceLevel: ExperienceLevel;
    availableTrainingDaysPerWeek: number;
    currentWeeklyRunningDistance: number | null;
    runningDistanceUnit: DistanceUnit | null;
    goal: FitnessGoal;
}

export interface CreateFitnessGoalRequest {
    type: GoalType;
    targetDate: string;
    targetTimeMinutes?: number | null;
}

export interface CreateAthleteProfileRequest {
    displayName: string;
    experienceLevel: ExperienceLevel;
    availableTrainingDaysPerWeek: number;
    currentWeeklyRunningDistance?: number | null;
    runningDistanceUnit?: DistanceUnit | null;
    goal: CreateFitnessGoalRequest;
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
    weeks: CreateTrainingWeekRequest[];
}

export interface GenerateTrainingPlanRequest {
    name: string;
    startDate: string;
    endDate: string;
}