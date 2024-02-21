FROM postgres:16.2
USER root
RUN apt-get update && apt-get install -y gnupg2 curl lsb-release
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc| gpg --dearmor -o /etc/apt/trusted.gpg.d/postgresql.gpg
RUN echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list
RUN apt-get update && apt-get install -y pgxnclient build-essential postgresql-server-dev-16  postgresql-16-pgvector \
    python3-pip postgresql-plpython3-16 faker \
&& pgxn install postgresql_anonymizer \
&& pgxn install vector \
&& pgxn install postgresql_faker \
&& pgxn install pg_uuidv7 \
&& apt-get remove -y build-essential \
  && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*
USER 999
CMD ["postgres", "-c", "shared_preload_libraries=pg_stat_statements" ]
USER 999
