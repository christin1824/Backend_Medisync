ALTER TABLE IF EXISTS public.user_health
  ADD COLUMN IF NOT EXISTS username VARCHAR(255);

UPDATE public.user_health
SET username = user_name
WHERE (username IS NULL OR username = '')
  AND (user_name IS NOT NULL AND user_name <> '');

UPDATE public.user_health
SET user_name = username
WHERE (user_name IS NULL OR user_name = '')
  AND (username IS NOT NULL AND username <> '');