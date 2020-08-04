docker-compose -f docker/db-local.yml --project-name=nerd-alert down
docker-compose -f docker/db-test.yml --project-name=nerd-alert-test down
docker volume prune -f
./gradlew cleanJooqSchema
