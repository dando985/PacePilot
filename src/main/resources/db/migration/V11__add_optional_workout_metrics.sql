ALTER TABLE workout_logs
    ADD COLUMN step_count INTEGER,
    ADD COLUMN calories_burned_kcal INTEGER,
    ADD COLUMN average_heart_rate_bpm INTEGER,

    ADD CONSTRAINT workout_logs_step_count_non_negative
        CHECK (
            step_count IS NULL
            OR step_count >= 0
        ),

    ADD CONSTRAINT workout_logs_calories_non_negative
        CHECK (
            calories_burned_kcal IS NULL
            OR calories_burned_kcal >= 0
        ),

    ADD CONSTRAINT workout_logs_average_heart_rate_positive
        CHECK (
            average_heart_rate_bpm IS NULL
            OR average_heart_rate_bpm > 0
        );