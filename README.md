# Docker Practical Exercise - Critical Software Development

This repository was used for a Critical Software Development practice I did while studying at University of Málaga.

The project consists on a Java web application using the Spark framework to offer a simple service that allows creating and displaying temperatures on a graph. Data is stored on a Redis database.

Endpoints:

*   `GET /listar`: shows a list of values and timestamps for each value
*   `GET /nuevo/{value}`: adds a new value
*   `GET /grafica`: shows a graph of the last 10 values added
*   `GET /listajson`: returns the last 10 values added to the data lake in JSON format

## Setup instructions

Using Docker Swarm:

```
docker pull danielperezporrasuma/temp-api:latest
docker swarm init
docker stack deploy -c docker-compose.yml temp-api
```

[http://localhost/listar](http://localhost/listar) opens the application.