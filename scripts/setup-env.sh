docker-compose -f docker/db-local.yml --project-name=nerd-alert up -d
docker-compose -f docker/db-test.yml --project-name=nerd-alert-test up -d
./gradlew flywayMigrate generateSampleJooqSchemaSource
