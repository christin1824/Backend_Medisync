ALTER TABLE public.user_health
  ADD COLUMN IF NOT EXISTS phone VARCHAR(50);

ALTER TABLE public.health_logs
  ADD COLUMN IF NOT EXISTS user_health_id BIGINT;

ALTER TABLE public.notifications
  ADD COLUMN IF NOT EXISTS user_health_id BIGINT;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1
      FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_health_logs_user_health'
        AND table_name = 'health_logs'
  ) THEN
    ALTER TABLE public.health_logs
      ADD CONSTRAINT fk_health_logs_user_health
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id);
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1
      FROM information_schema.table_constraints
      WHERE constraint_name = 'fk_notifications_user_health'
        AND table_name = 'notifications'
  ) THEN
    ALTER TABLE public.notifications
      ADD CONSTRAINT fk_notifications_user_health
      FOREIGN KEY (user_health_id) REFERENCES public.user_health(id);
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_health_logs_user ON public.health_logs(user_health_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON public.notifications(user_health_id);
