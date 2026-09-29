CREATE TABLE athlete_profiles
(
    id                                UUID PRIMARY KEY,
    display_name                      VARCHAR(100) NOT NULL,
    experience_level                  VARCHAR(20)  NOT NULL,
    available_training_days_per_week  INTEGER      NOT NULL,
    current_weekly_running_distance   DOUBLE PRECISION,
    running_distance_unit             VARCHAR(20),
    goal_type                         VARCHAR(30)  NOT NULL,
    target_date                       DATE         NOT NULL,
    target_time_minutes               INTEGER,

    CONSTRAINT athlete_profiles_display_name_not_blank
        CHECK (char_length(btrim(display_name)) > 0),

    CONSTRAINT athlete_profiles_experience_level_valid
        CHECK (experience_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),

    CONSTRAINT athlete_profiles_training_days_valid
        CHECK (available_training_days_per_week BETWEEN 1 AND 7),

    CONSTRAINT athlete_profiles_running_distance_nonnegative
        CHECK (current_weekly_running_distance IS NULL
                OR current_weekly_running_distance >= 0),

    CONSTRAINT athlete_profiles_running_distance_complete
        CHECK ((current_weekly_running_distance IS NULL AND running_distance_unit IS NULL)
                OR
            (current_weekly_running_distance IS NOT NULL AND running_distance_unit IS NOT NULL)),

    CONSTRAINT athlete_profiles_distance_unit_valid
        CHECK (running_distance_unit IS NULL OR running_distance_unit IN ('MILES', 'KILOMETERS')),

    CONSTRAINT athlete_profiles_goal_type_valid
        CHECK (goal_type IN ('GENERAL_FITNESS','FIVE_K','TEN_K','HALF_MARATHON','MARATHON')),

    CONSTRAINT athlete_profiles_target_time_positive
        CHECK (target_time_minutes IS NULL OR target_time_minutes > 0)
);