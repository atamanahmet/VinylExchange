ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMPTZ;
