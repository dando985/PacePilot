ALTER TABLE athlete_profiles
    ADD COLUMN owner_account_id UUID NOT NULL;

ALTER TABLE athlete_profiles
    ADD CONSTRAINT athlete_profiles_owner_account_fk
        FOREIGN KEY (owner_account_id)
            REFERENCES user_accounts (id)
            ON DELETE CASCADE;

ALTER TABLE athlete_profiles
    ADD CONSTRAINT athlete_profiles_owner_account_unique
        UNIQUE (owner_account_id);

ALTER TABLE training_plans
DROP CONSTRAINT training_plans_athlete_fk,
    ADD CONSTRAINT training_plans_athlete_fk
        FOREIGN KEY (athlete_profile_id)
            REFERENCES athlete_profiles (id)
            ON DELETE CASCADE;

ALTER TABLE workout_logs
DROP CONSTRAINT workout_logs_athlete_fk,
    DROP CONSTRAINT workout_logs_planned_workout_fk,
    ADD CONSTRAINT workout_logs_athlete_fk
        FOREIGN KEY (athlete_profile_id)
            REFERENCES athlete_profiles (id)
            ON DELETE CASCADE,
    ADD CONSTRAINT workout_logs_planned_workout_fk
        FOREIGN KEY (planned_workout_id)
            REFERENCES planned_workouts (id)
            ON DELETE CASCADE;