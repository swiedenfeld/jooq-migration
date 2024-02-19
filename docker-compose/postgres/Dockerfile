FROM postgres:16.2
USER root
RUN apt update && apt install -y gnupg2 curl lsb-release
RUN curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc| gpg --dearmor -o /etc/apt/trusted.gpg.d/postgresql.gpg
RUN echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list
RUN apt update && apt install -y pgxnclient build-essential postgresql-server-dev-16  postgresql-16-pgvector \
    python3-pip postgresql-plpython3-16 faker
RUN pgxn install postgresql_anonymizer
RUN pgxn install vector
RUN pgxn install postgresql_faker
RUN pgxn install pg_uuidv7
RUN apt remove -y build-essential \
  && apt purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
  && apt clean \
  && rm -rf /var/lib/apt/lists/* \
USER 1001
CMD ["postgres", "-c", "shared_preload_libraries=pg_stat_statements,anon" ]

