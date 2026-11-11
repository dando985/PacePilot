CREATE TABLE workout_logs
(
    id                       UUID PRIMARY KEY,
    athlete_profile_id       UUID NOT NULL,
    planned_workout_id       UUID NOT NULL,
    completed_date           DATE NOT NULL,
    actual_distance          DOUBLE PRECISION,
    distance_unit            VARCHAR(20),
    actual_duration_minutes  INTEGER,
    notes                    TEXT,

    CONSTRAINT workout_logs_athlete_fk
        FOREIGN KEY (athlete_profile_id)
            REFERENCES athlete_profiles (id)
            ON DELETE RESTRICT,

    CONSTRAINT workout_logs_planned_workout_fk
        FOREIGN KEY (planned_workout_id)
            REFERENCES planned_workouts (id)
            ON DELETE RESTRICT,

    CONSTRAINT workout_logs_planned_workout_unique
        UNIQUE (planned_workout_id),

    CONSTRAINT workout_logs_distance_valid
        CHECK (
            actual_distance IS NULL
                OR (
                actual_distance > 0
                    AND actual_distance < 'Infinity'::DOUBLE PRECISION
                )
            ),

    CONSTRAINT workout_logs_distance_complete
        CHECK (
            (actual_distance IS NULL AND distance_unit IS NULL)
                OR
            (actual_distance IS NOT NULL AND distance_unit IS NOT NULL)
            ),

    CONSTRAINT workout_logs_distance_unit_valid
        CHECK (
            distance_unit IS NULL
                OR distance_unit IN ('MILES', 'KILOMETERS')
            ),

    CONSTRAINT workout_logs_duration_positive
        CHECK (
            actual_duration_minutes IS NULL
                OR actual_duration_minutes > 0
            ),

    CONSTRAINT workout_logs_notes_not_blank
        CHECK (
            notes IS NULL
                OR char_length(btrim(notes)) > 0
            )
);

CREATE INDEX workout_logs_athlete_date_idx
    ON workout_logs (athlete_profile_id, completed_date DESC, id ASC);