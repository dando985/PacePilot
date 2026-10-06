CREATE TABLE training_plans
(
    id                  UUID PRIMARY KEY,
    athlete_profile_id  UUID NOT NULL,
    name                TEXT NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,

    CONSTRAINT training_plans_athlete_fk
        FOREIGN KEY (athlete_profile_id)
            REFERENCES athlete_profiles (id)
            ON DELETE RESTRICT,

    CONSTRAINT training_plans_name_not_blank
        CHECK (char_length(btrim(name)) > 0),

    CONSTRAINT training_plans_dates_valid
        CHECK (end_date >= start_date)
);

CREATE INDEX training_plans_athlete_idx
    ON training_plans (athlete_profile_id);


CREATE TABLE training_weeks
(
    plan_id      UUID NOT NULL,
    week_number  INTEGER NOT NULL,
    start_date   DATE NOT NULL,

    PRIMARY KEY (plan_id, week_number),

    CONSTRAINT training_weeks_plan_fk
        FOREIGN KEY (plan_id)
            REFERENCES training_plans (id)
            ON DELETE CASCADE,

    CONSTRAINT training_weeks_number_positive
        CHECK (week_number >= 1)
);


CREATE TABLE planned_workouts
(
    id                       UUID PRIMARY KEY,
    plan_id                  UUID NOT NULL,
    week_number              INTEGER NOT NULL,
    position_in_week         INTEGER NOT NULL,
    scheduled_date           DATE NOT NULL,
    workout_type             VARCHAR(30) NOT NULL,
    title                    TEXT NOT NULL,
    target_distance          DOUBLE PRECISION,
    distance_unit            VARCHAR(20),
    target_duration_minutes  INTEGER,
    instructions             TEXT NOT NULL,

    CONSTRAINT planned_workouts_week_fk
        FOREIGN KEY (plan_id, week_number)
            REFERENCES training_weeks (plan_id, week_number)
            ON DELETE CASCADE,

    CONSTRAINT planned_workouts_position_unique
        UNIQUE (plan_id, week_number, position_in_week),

    CONSTRAINT planned_workouts_position_nonnegative
        CHECK (position_in_week >= 0),

    CONSTRAINT planned_workouts_type_valid
        CHECK (
            workout_type IN (
                             'REST',
                             'WALK',
                             'EASY_RUN',
                             'RECOVERY_RUN',
                             'LONG_RUN',
                             'TEMPO_RUN',
                             'INTERVALS',
                             'STRENGTH',
                             'CROSS_TRAINING',
                             'RACE'
                )
            ),

    CONSTRAINT planned_workouts_title_not_blank
        CHECK (char_length(btrim(title)) > 0),

    CONSTRAINT planned_workouts_distance_valid
        CHECK (
            target_distance IS NULL
                OR (
                target_distance > 0
                    AND target_distance < 'Infinity'::DOUBLE PRECISION
                )
            ),

    CONSTRAINT planned_workouts_distance_complete
        CHECK (
            (target_distance IS NULL AND distance_unit IS NULL)
                OR
            (target_distance IS NOT NULL AND distance_unit IS NOT NULL)
            ),

    CONSTRAINT planned_workouts_distance_unit_valid
        CHECK (
            distance_unit IS NULL
                OR distance_unit IN ('MILES', 'KILOMETERS')
            ),

    CONSTRAINT planned_workouts_duration_positive
        CHECK (
            target_duration_minutes IS NULL
                OR target_duration_minutes > 0
            ),

    CONSTRAINT planned_workouts_instructions_not_blank
        CHECK (char_length(btrim(instructions)) > 0)
);