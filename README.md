# Docker Practical Exercise - Critical Software Development

This repository was used for a practical exercise for subject "Desarrollo de Software Crítico" (Critical Software Development) during my Software Engineering studies at University of Málaga.

The project consists on a Java web application using the Spark framework to offer a simple service that allows creating and displaying temperatures on a graph. Data is stored on a Redis database.

Endpoints:

*   `GET /listar`: shows a list of values and timestamps for each value
*   `GET /nuevo/{value}`: adds a new value
*   `GET /grafica`: shows a graph of the last 10 values added
*   `GET /listajson`: returns the last 10 values added to the data lake in JSON format

# Setup instructions

## With Docker Swarm

```
docker pull danielperezporrasuma/temp-api:latest
docker swarm init
docker stack deploy -c docker-compose.yml temp-api
```

[http://localhost/listar](http://localhost/listar) opens the application.

## With Google Kubernetes Engine

Using Google Kubernetes Engine, create a Google Cloud project and run the following command in the Google Cloud Shell:

```
gcloud components install kubectl
```

Then use the following commands to set up the cluster and the services:

```
gcloud container clusters dscrit-temp-api --num-nodes=3 --zone=us-west1-a
git clone https://github.com/DanielPerezPorras/DockerTemperatureApiPractice
cd DockerTemperatureApiPractice/kubernetes
kubectl apply -f redis-deployment.yaml
kubectl apply -f redis-service.yaml
kubectl apply -f temp-api-deployment.yaml
kubectl apply -f temp-api-service.yaml
kubectl get service
```

After running `get service`, you will get a table like this:

```
NAME                      TYPE           CLUSTER-IP     EXTERNAL-IP     PORT(S)        AGE
dscrit-temp-api-service   LoadBalancer   10.80.2.87     34.105.104.46   80:31561/TCP   45s
kubernetes                ClusterIP      10.80.0.1      <none>          443/TCP        113m
redis                     ClusterIP      10.80.12.137   <none>          6379/TCP       48s
```

Enter http://{EXTERNAL-IP}/listar to start using the application.

Don't forget to delete the services created if you don't plan on using them anymore!

```
kubectl delete service dscrit-temp-api-service
kubectl delete service redis
```

Finally, delete the cluster:

```
gcloud container clusters delete dscrit-temp-api --zone=us-west1-a
```
