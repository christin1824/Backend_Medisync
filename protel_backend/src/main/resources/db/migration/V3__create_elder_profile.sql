CREATE TABLE IF NOT EXISTS public.elder_profile (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NULL,
  full_name VARCHAR(255),
  phone VARCHAR(50),
  profile_image_url TEXT,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  CONSTRAINT uq_elder_profile_user UNIQUE (user_health_id)
);

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1
      FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_elder_user'
        AND table_name = 'elder_profile'
  ) THEN
    ALTER TABLE public.elder_profile
      ADD CONSTRAINT fk_elder_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id);
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_elder_user ON public.elder_profile(user_health_id);