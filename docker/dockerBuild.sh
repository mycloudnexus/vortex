#!/bin/sh
# execute this script from the root of the project

# Build the docker image for the api-api
docker buildx build -t cloudnexusopsdockerhub/vortex-app-api:latest -f ./docker/app-api/Dockerfile .


# Build the docker image for the portal-app
docker buildx build -t cloudnexusopsdockerhub/vortex-app-portal:latest -f ./docker/app-portal/Dockerfile .