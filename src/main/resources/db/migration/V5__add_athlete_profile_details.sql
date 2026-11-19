-- Existing profiles need real values before these columns can be NOT NULL.
-- A later migration will enforce the required fields after backfilling them.
ALTER TABLE athlete_profiles
    ADD COLUMN birth_date DATE,
    ADD COLUMN weight DOUBLE PRECISION,
    ADD COLUMN weight_unit VARCHAR(20),
    ADD COLUMN gender VARCHAR(20);

ALTER TABLE athlete_profiles
    ADD CONSTRAINT athlete_profiles_birth_date_valid
        CHECK (birth_date IS NULL OR birth_date <= CURRENT_DATE),

    ADD CONSTRAINT athlete_profiles_weight_valid
        CHECK (weight IS NULL OR (weight > 0 AND weight < 'Infinity'::DOUBLE PRECISION)),

    ADD CONSTRAINT athlete_profiles_weight_unit_valid
        CHECK (weight_unit IS NULL OR weight_unit IN ('KILOGRAMS', 'POUNDS')
        ),

    ADD CONSTRAINT athlete_profiles_weight_complete
        CHECK (
            (weight IS NULL AND weight_unit IS NULL)
            OR
            (weight IS NOT NULL AND weight_unit IS NOT NULL)
        ),

    ADD CONSTRAINT athlete_profiles_gender_valid
        CHECK (gender IS NULL OR gender IN (
                'MALE',
                'FEMALE',
                'NON_BINARY',
                'OTHER',
                'PREFER_NOT_TO_SAY'
            )
        );