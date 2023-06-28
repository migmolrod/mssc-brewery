# Executables (local)
DOCKER_COMP = docker compose

# Docker containers
BREWERY_CONT = $(DOCKER_COMP) exec #SERVICE_NAME

# Misc
.DEFAULT_GOAL = help
.PHONY        : help build up start down logs sh composer vendor sf cc

## —— Microservices Spring Cloud Makefile —————————————————————————————————————
help: ## Outputs this help screen
	@grep -E '(^[a-zA-Z0-9\./_-]+:.*?##.*$$)|(^##)' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}{printf "\033[32m%-30s\033[0m %s\n", $$1, $$2}' | sed -e 's/\[32m##/[33m/'

## —— Docker ——————————————————————————————————————————————————————————————————
build: ## Build compiler image
	@./deploy.sh build

run: ## Runs docker compose
	@./deploy.sh run

#composer: ## Run composer, pass the parameter "c=" to run a given command, example: make composer c='req symfony/orm-pack'
#	@$(eval c ?=)
#	@$(COMPOSER) $(c)
