# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Leverage the Docker layer cache for dependencies.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# postgresql-client des depots Ubuntu "jammy" est fige en version 14, plus ancienne que le
# serveur applicatif (postgres:16-alpine, docker-compose.yml) : pg_dump refuse de sauvegarder un
# serveur plus recent que lui-meme ("server version mismatch"), ce qui fait echouer TOUTES les
# sauvegardes (SauvegardeService) sans exception visible cote applicatif, juste un statut ECHOUEE
# en base. Depot officiel PGDG pour installer postgresql-client-16, aligne sur la version serveur.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates gnupg lsb-release \
    && install -d /usr/share/postgresql-common/pgdg \
    && curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc \
    && echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client-16 \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r leconsulat && useradd -r -g leconsulat leconsulat \
    && mkdir -p /app/data /app/backups /app/uploads \
    && chown -R leconsulat:leconsulat /app

COPY --from=build /build/target/le-consulat-backend.jar app.jar

USER leconsulat
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
