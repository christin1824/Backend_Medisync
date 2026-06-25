CREATE TABLE IF NOT EXISTS public.user_health (
  id BIGSERIAL PRIMARY KEY,
  firebase_uid VARCHAR(255) NOT NULL UNIQUE,
  user_name VARCHAR(255),
  full_name VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(50),
  device_token VARCHAR(500),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

ALTER TABLE IF EXISTS public.user_health
  ADD COLUMN IF NOT EXISTS device_token VARCHAR(500);

ALTER TABLE IF EXISTS public.user_health
  ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);

UPDATE public.user_health uh
SET full_name = COALESCE(
  NULLIF(full_name, ''),
  NULLIF(user_name, '')
)
WHERE full_name IS NULL OR full_name = '';

CREATE TABLE IF NOT EXISTS public.elder_profile (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL UNIQUE,
  full_name VARCHAR(255),
  phone VARCHAR(50),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.steps_logs (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  total_steps INTEGER NOT NULL,
  recorded_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.heart_rate_logs (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  heart_rate_bpm INTEGER NOT NULL,
  recorded_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.oxygen_logs (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  oxygen_saturation INTEGER NOT NULL,
  oxygen_status VARCHAR(50),
  recorded_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.danger_events (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  event_type VARCHAR(50),
  status VARCHAR(50),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  title VARCHAR(255),
  message TEXT,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.realtime_locations (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.location_history (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  recorded_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.emergency_contacts (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  name VARCHAR(255),
  phone VARCHAR(50),
  relation VARCHAR(100),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.medical_history (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  description TEXT,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.audit_log (
  id BIGSERIAL PRIMARY KEY,
  user_health_id BIGINT NOT NULL,
  action VARCHAR(100),
  object_type VARCHAR(100),
  object_id BIGINT,
  details JSONB,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_elder_profile_user'
        AND table_name = 'elder_profile'
  ) THEN
    ALTER TABLE public.elder_profile
      ADD CONSTRAINT fk_elder_profile_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_steps_logs_user'
        AND table_name = 'steps_logs'
  ) THEN
    ALTER TABLE public.steps_logs
      ADD CONSTRAINT fk_steps_logs_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_heart_rate_logs_user'
        AND table_name = 'heart_rate_logs'
  ) THEN
    ALTER TABLE public.heart_rate_logs
      ADD CONSTRAINT fk_heart_rate_logs_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_oxygen_logs_user'
        AND table_name = 'oxygen_logs'
  ) THEN
    ALTER TABLE public.oxygen_logs
      ADD CONSTRAINT fk_oxygen_logs_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_danger_events_user'
        AND table_name = 'danger_events'
  ) THEN
    ALTER TABLE public.danger_events
      ADD CONSTRAINT fk_danger_events_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_realtime_locations_user'
        AND table_name = 'realtime_locations'
  ) THEN
    ALTER TABLE public.realtime_locations
      ADD CONSTRAINT fk_realtime_locations_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_location_history_user'
        AND table_name = 'location_history'
  ) THEN
    ALTER TABLE public.location_history
      ADD CONSTRAINT fk_location_history_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_emergency_contacts_user'
        AND table_name = 'emergency_contacts'
  ) THEN
    ALTER TABLE public.emergency_contacts
      ADD CONSTRAINT fk_emergency_contacts_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_medical_history_user'
        AND table_name = 'medical_history'
  ) THEN
    ALTER TABLE public.medical_history
      ADD CONSTRAINT fk_medical_history_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_audit_log_user'
        AND table_name = 'audit_log'
  ) THEN
    ALTER TABLE public.audit_log
      ADD CONSTRAINT fk_audit_log_user
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id) ON DELETE CASCADE;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_elder_profile_user ON public.elder_profile(user_health_id);
CREATE INDEX IF NOT EXISTS idx_steps_logs_user ON public.steps_logs(user_health_id);
CREATE INDEX IF NOT EXISTS idx_heart_rate_logs_user ON public.heart_rate_logs(user_health_id);
CREATE INDEX IF NOT EXISTS idx_oxygen_logs_user ON public.oxygen_logs(user_health_id);
CREATE INDEX IF NOT EXISTS idx_danger_events_user ON public.danger_events(user_health_id);
CREATE INDEX IF NOT EXISTS idx_realtime_locations_user ON public.realtime_locations(user_health_id);
CREATE INDEX IF NOT EXISTS idx_location_history_user ON public.location_history(user_health_id);
CREATE INDEX IF NOT EXISTS idx_emergency_contacts_user ON public.emergency_contacts(user_health_id);
CREATE INDEX IF NOT EXISTS idx_medical_history_user ON public.medical_history(user_health_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON public.audit_log(user_health_id);
