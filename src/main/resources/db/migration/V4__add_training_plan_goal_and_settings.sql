ALTER TABLE training_plans
    ADD COLUMN goal_type VARCHAR(30),
    ADD COLUMN goal_target_date DATE,
    ADD COLUMN goal_target_time_minutes INTEGER,
    ADD COLUMN available_training_days_per_week INTEGER,
    ADD COLUMN distance_unit VARCHAR(20);

-- Copy each athlete's existing goal and settings onto their existing plans.
-- Preserve any higher training frequency already present in a plan.
UPDATE training_plans AS plan
SET goal_type = athlete.goal_type,
    goal_target_date = athlete.target_date,
    goal_target_time_minutes = athlete.target_time_minutes,
    available_training_days_per_week = GREATEST(
            athlete.available_training_days_per_week,
            COALESCE(
                    (
                        SELECT MAX(week_days.active_days)
                        FROM (
                                 SELECT CAST(COUNT(DISTINCT workout.scheduled_date) AS INTEGER) AS active_days
                                 FROM planned_workouts AS workout
                                 WHERE workout.plan_id = plan.id
                                   AND workout.workout_type <> 'REST'
                                 GROUP BY workout.week_number
                             ) AS week_days
                    ),
                    0
            )
                                       ),
    distance_unit = COALESCE(
            athlete.running_distance_unit,
            'KILOMETERS'
                    )
    FROM athlete_profiles AS athlete
WHERE plan.athlete_profile_id = athlete.id;

ALTER TABLE training_plans
    ALTER COLUMN goal_type SET NOT NULL,
ALTER COLUMN goal_target_date SET NOT NULL,
    ALTER COLUMN available_training_days_per_week SET NOT NULL,
    ALTER COLUMN distance_unit SET NOT NULL,

    ADD CONSTRAINT training_plans_goal_type_valid
        CHECK (goal_type IN (
            'GENERAL_FITNESS',
            'FIVE_K',
            'TEN_K',
            'HALF_MARATHON',
            'MARATHON'
        )),

    ADD CONSTRAINT training_plans_goal_target_time_positive
        CHECK (
            goal_target_time_minutes IS NULL
            OR goal_target_time_minutes > 0
        ),

    ADD CONSTRAINT training_plans_training_days_valid
        CHECK (available_training_days_per_week BETWEEN 1 AND 7),

    ADD CONSTRAINT training_plans_distance_unit_valid
        CHECK (distance_unit IN ('MILES', 'KILOMETERS'));