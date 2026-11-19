-- Migration V7: Remove legacy athlete training fields
-- This migration removes the legacy training fields from the athlete_profiles table.
ALTER TABLE athlete_profiles
DROP CONSTRAINT athlete_profiles_training_days_valid,
    DROP CONSTRAINT athlete_profiles_running_distance_nonnegative,
    DROP CONSTRAINT athlete_profiles_running_distance_complete,
    DROP CONSTRAINT athlete_profiles_distance_unit_valid,
    DROP CONSTRAINT athlete_profiles_goal_type_valid,
    DROP CONSTRAINT athlete_profiles_target_time_positive;

ALTER TABLE athlete_profiles
DROP COLUMN available_training_days_per_week,
    DROP COLUMN current_weekly_running_distance,
    DROP COLUMN running_distance_unit,
    DROP COLUMN goal_type,
    DROP COLUMN target_date,
    DROP COLUMN target_time_minutes;