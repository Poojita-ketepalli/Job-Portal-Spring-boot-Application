PostgreSQL Driver - client-side software library or plugin that allows applications to communicate with a PostgreSQL database server.
docker run -d --name db -e POSTGRES_PASSWORD=poojita postgres
docker run -d --name pgadmin -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=poojita dpage/pgadmin4


docker network create my-network
docker run -d --name db --network my-network -e POSTGRES_PASSWORD=poojita postgres
docker run -d --name pgadmin --network my-network -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=poojita dpage/pgadmin4
docker exec -it pgadmin ping db

Instead of running commands like abve, I created docker-compose.yaml file.
run docker compose up

Login to pgadmin - http://localhost:5050/browser/
Add Required detail to create a server.
create jobapp database

Initially -> monolithic application 8080 port
Transitioning into microservice architecture -> 
company -> 8081 port
Jobs -> 8082 port
Reviews -> 8083 port

![img.png](img.png)

APIs for reviews in monolithic application:
http://localhost:8080/companies/2/reviews
http://localhost:8080/companies/1/reviews
http://localhost:8080/companies/1/reviews/1
http://localhost:8080/companies/1/reviews/1
http://localhost:8080/companies/1/reviews/1

Restructuring to microservice:
http://localhost:8083/reviews?companyId=1
http://localhost:8083/reviews?companyId=1
http://localhost:8083/reviews/1
http://localhost:8083/reviews/1
http://localhost:8083/reviews/1

why we need to do this: each microservice should be independent of each other. So getting companyId inside the other services is not good.
We also created each database seperately in pgadmin

We will use RestTemplate for interService communication

servive registry (eureka) = 8761 - http://localhost:8761/

open feign and resttemplate for microservices synchronous communication

zipkin used for distributed tracing
docker run -d -p 9411:9411 openzipkin/zipkin
http://localhost:9411

micrometer

spring cloud config server - for centralized configurations management

spring cloud gateway for API gateway configuration - 8085 port

Resilience4J for fault tolerence - http://localhost:8082/actuator/circuitbreakers

MessageQueues - RabbitMQ (docker container)
We can send review and company details from job(post) method itself. Or we can send rating information to company and show the average rating in company
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

http://localhost:15672/#/

running application in command line
jar -tf .\target\companyms-0.0.1-SNAPSHOT.jar (check if everything is packaged in jar file)
java -jar .\target\companyms-0.0.1-SNAPSHOT.jar (execute the jar file to start the application)


building images for each microservice

cd service-registry
./mvnw spring-boot:build-image "-Dspring-boot.build-image.imageName=poojitak23/service-registry"
docker push poojitak23/service-registry

cd ..
cd companyms
./mvnw spring-boot:build-image "-Dspring-boot.build-image.imageName=poojitak23/companyms"
docker push poojitak23/companyms

cd ..
cd reviewms
./mvnw spring-boot:build-image "-Dspring-boot.build-image.imageName=poojitak23/reviewms"
docker push poojitak23/reviewms

cd ..
cd jobms
./mvnw spring-boot:build-image "-Dspring-boot.build-image.imageName=poojitak23/jobms"
docker push poojitak23/jobms

cd ..
cd config-server
./mvnw spring-boot:build-image "-Dspring-boot.build-image.imageName=poojitak23/config-server"
docker push poojitak23/config-server

cd ..
cd api-gateway
./mvnw spring-boot:build-image "-Dspring-boot.build-image.imageName=poojitak23/gatewayms"
docker push poojitak23/gatewayms


kubernetes - use minikube
minikube start
minikube dashboard - we can monitor everything in this UI

kubectl apply -f first-pod.yaml
kubectl get pods

kubectl apply -f first-service.yaml
kubectl get service

kubectl apply -f .\services\postgres\
kubectl exec -it postgres-0 -- psql -U poojita
\l -> check list of databases


