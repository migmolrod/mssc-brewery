build:
	mvn package -DskipTests -pl cloud/mssc-service-registry
	mvn docker:build -DskipTests -pl cloud/mssc-service-registry

	mvn package -DskipTests -pl cloud/mssc-config-server
	mvn docker:build -DskipTests -pl cloud/mssc-config-server

	mvn package -DskipTests -pl cloud/mssc-api-gateway
	mvn docker:build -DskipTests -pl cloud/mssc-api-gateway

	mvn package -DskipTests -pl services/mssc-beer-inventory-service
	mvn docker:build -DskipTests -pl services/mssc-beer-inventory-service

	mvn package -DskipTests -pl services/mssc-beer-service
	mvn docker:build -DskipTests -pl services/mssc-beer-service

#	mvn package -DskipTests -pl services/mssc-beer-order-service
#	mvn docker:build -DskipTests -pl services/mssc-beer-order-service

	docker image prune -f

up:
	docker compose up -d

down:
	docker compose down --volumes
