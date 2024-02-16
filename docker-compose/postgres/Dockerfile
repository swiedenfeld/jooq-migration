FROM bitnami/postgres:16.1
RUN apt update && apt install -y pgxnclient build-essential postgresql-server-dev-16  postgresql-16-pgvector \
    python3-pip postgresql-plpython3-16 faker
RUN pgxn install postgresql_anonymizer
RUN pgxn install vector
RUN pgxn install postgresql_faker
RUN pgxn install pg_uuidv7
RUN apt remove -y build-essential \
  && apt purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
  && apt clean \
  && rm -rf /var/lib/apt/lists/*

