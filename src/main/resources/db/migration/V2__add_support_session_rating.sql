ALTER TABLE support_sessions 
ADD COLUMN rating TINYINT NULL,
ADD COLUMN feedback TEXT NULL;

-- Ensure rating is between 1 and 5 if not null
ALTER TABLE support_sessions
ADD CONSTRAINT chk_support_session_rating CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5));
