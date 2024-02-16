#!/bin/bash

set -e
set -u

function create_user_and_database() {
  local database=$1
  echo "  Creating user and database '$database'"
  psql -v ON_ERROR_STOP=1 --username postgres <<EOSQL
	    CREATE USER ${database}_user PASSWORD '${database}_user';
	    CREATE USER ${database}_admin WITH PASSWORD '${database}_admin';
	    ALTER USER ${database}_admin WITH CREATEROLE;
	    CREATE DATABASE $database WITH OWNER ${database}_admin encoding='UTF8' locale='en_US.utf8';
EOSQL

  psql -v ON_ERROR_STOP=1 --username postgres -d "$database" <<EOSQL
      CREATE SCHEMA ${database};
      ALTER USER ${database}_user SET search_path TO ${database},extensions;
      ALTER USER ${database}_admin SET search_path TO ${database},extensions;
      GRANT USAGE ON SCHEMA ${database} TO ${database}_user;
      GRANT ALL ON SCHEMA ${database} TO ${database}_admin;
      ALTER USER ${database}_user SET search_path TO ${database},extensions;

      GRANT select,insert,update,delete ON ALL TABLES IN SCHEMA ${database} TO ${database}_user;
      GRANT select,usage ON ALL SEQUENCES IN SCHEMA ${database} to ${database}_user;
      GRANT execute ON ALL FUNCTIONS IN SCHEMA ${database} to ${database}_user;

      ALTER DEFAULT PRIVILEGES FOR USER ${database}_admin IN SCHEMA ${database} GRANT select,insert,update,delete ON TABLES TO ${database}_user;
      ALTER DEFAULT PRIVILEGES FOR USER ${database}_admin IN SCHEMA ${database} GRANT select,usage ON SEQUENCES TO ${database}_user;
      ALTER DEFAULT PRIVILEGES FOR user ${database}_admin IN SCHEMA ${database} GRANT execute ON FUNCTIONS TO ${database}_user;

EOSQL
    psql -v ON_ERROR_STOP=1 --username postgres -d "$database" < /docker-entrypoint-initdb.d/0020-extensions.sql
    psql -v ON_ERROR_STOP=1 --username postgres -d "$database" < /docker-entrypoint-initdb.d/0030-pgulid.sql
}

if [ -n "$POSTGRESQL_MULTIPLE_DATABASES" ]; then
  echo "Multiple database creation requested: $POSTGRESQL_MULTIPLE_DATABASES"
  for db in $(echo "$POSTGRESQL_MULTIPLE_DATABASES" | tr ',' ' '); do
    create_user_and_database "$db"
  done
  echo "Multiple databases created"
fi
