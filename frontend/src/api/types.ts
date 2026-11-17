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