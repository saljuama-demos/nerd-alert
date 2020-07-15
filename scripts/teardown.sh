docker-compose -f docker/db-local.yml down
docker-compose -f docker/db-test.yml down
docker volume prune -f
