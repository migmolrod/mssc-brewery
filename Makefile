build:
	mvn package -DskipTests -pl cloud/mssc-service-registry
	mvn docker:build -pl cloud/mssc-service-registry

	mvn package -DskipTests -pl cloud/mssc-config-server
	mvn docker:build -pl cloud/mssc-config-server

	mvn package -DskipTests -pl cloud/mssc-api-gateway
	mvn docker:build -pl cloud/mssc-api-gateway

	mvn package -DskipTests -pl services/mssc-beer-inventory-service
	mvn docker:build -pl services/mssc-beer-inventory-service

	mvn package -DskipTests -pl services/mssc-beer-service
	mvn docker:build -pl services/mssc-beer-service

	mvn package -DskipTests -pl services/mssc-beer-order-service
	mvn docker:build -pl services/mssc-beer-order-service

	docker image prune -f

start-local:
	docker compose -f docker-compose.yaml up -d

start-debug:
	docker compose -f docker-compose.elk.yaml up -d

stop-local:
	docker compose -f docker-compose.yaml down --volumes

stop-debug:
	docker compose -f docker-compose.elk.yaml down --volumes
