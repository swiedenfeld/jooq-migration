
create schema extensions;

-- make sure everybody can use everything in the extensions schema
grant usage on schema extensions to public;
grant execute on all functions in schema extensions to public;

-- include future extensions
alter default privileges in schema extensions
    grant execute on functions to public;

alter default privileges in schema extensions
    grant usage on types to public;

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS pg_uuidv7 WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA extensions; -- used for EXCLUSION on uuid

-- faker extension
CREATE SCHEMA faker;
CREATE EXTENSION IF NOT EXISTS faker WITH SCHEMA faker CASCADE;


