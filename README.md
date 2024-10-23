![Vortex](docs/img/vortex-logo.svg)

# Vortex

## Overview

TBD

![Vortex-Overview](docs/img/vortex-overview.png)

## Core Functionalities

TBD

## Key Features

## Benefits


## Architecture

Vortex follows a modular architecture, comprising several components that work together to deliver its functionality:
![Vortex-Overview](docs/img/vortex-architecture.png)


### Key Components

- **Vortex API**:  TBD.

- **Vortex Portal**: TBD.

## Get Started

To begin, clone the repository and navigate to the project directory:

```console
git clone https://github.com/mycloudnexus/vortex.git
cd vortex
```

### Run via docker

The simplest way to get Vortex up and running is with Docker. Follow these steps:

1. Navigate to the docker directory:

```
cd docker
```

2. Start the services using Docker Compose:
```
docker-compose up
```

If all services start correctly, the following servers will be accessible:

- Portal: http://localhost:3000
- API Server: http://localhost:8000


### Run via Source Code

For a more customizable setup, you can compile and run the project from the source code. Ensure the following prerequisites are installed:

#### Prerequisites

Before compile the source code, ensure you have the following prerequisites:

- [Maven] (https://maven.apache.org)
- [JDK 17 or later](https://openjdk.org/)
- NodeJS 20 or later (https://nodejs.org/en)
- NPM

You can verify the installed versions with the following commands:

```
1. mvn -v
2. java -version
3. node -v
4. npm -v
```

#### Step 1 - Run the Portal

1. From the base of the repository, navigate to the portal directory:
```
cd vortex-app/vortex-app-portal
```
2. Install the dependencies and start the development server:
```
npm install
npm run dev
```

3. Open the portal in your browser at [http://localhost:5173](http://localhost:5173).  


#### Step 2 - Run the API Server

The API Server relies on a PostgreSQL database. You can set up a PostgreSQL server using Docker:

1. **Start PostgreSQL** using Docker Compose:

   ```bash
   cd docker
   docker-compose up db
   ```

   Alternatively, you can set up PostgreSQL manually based on your environment's requirements.

2. **Compile the source code**:

   ```bash
   mvn package -DskipTests
   ```

3. **Set up environment variables**:

   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/vortex
   export DB_USERNAME=postgresql
   export DB_PASSWORD=password
   ```

4. **Run the API Server**:

   ```bash
   java -jar vortex-app/vortex-app-controller/target/*.jar
   ```

5. Access the Swagger UI at [http://localhost:8000](http://localhost:8000).  
   You should now be able to log into the portal at [http://localhost:5173](http://localhost:5173)


### Code Structure
```
vortex/
│
├── vortex-app/
│   └── vortex-app-api/
│   │   └── src
│   │   │   └── main
│   │   │   └── test
│   │   └── pom.xml
│   └── vortex-app-portal/
│   └── pom.xml
│
├── vortex-java-sdk/
│   └── vortex-java-sdk-core/
│   │   └── src
│   │   │   └── main
│   │   │   └── test
│   │   └── pom.xml
│   └── vortex-java-sdk-test/
│   └── pom.xml
│
├── docs/
│   └── developer_guide.md
│   └── configuration.md│
│
├── .github/
│   ├── workflows/
│   │   └── ci.yml
│   ├── ISSUE_TEMPLATE/
│   │   └── bug_report.md
│   │   └── feature_request.md
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── CODE_OF_CONDUCT.md
│
├── .mvn/
│   └──jvm.config
│
├── docker/
│   ├── docker-compose.yaml
│
├── README.md
├── CONTRIBUTING.md
├── LICENSE.md
├── CHANGELOG.md
├── pom.xml
└── .gitignore

```

### Configuration

The behavior of the Vortex can be customized using command-line arguments or environment variables. Refer to the [Configuration](./docs/configuration.md) documentation for a list of available options and their descriptions.


## Contributing

We welcome contributions from the community! If you'd like to contribute to the Vortex project, please follow our [Contribution Guidelines](./CONTRIBUTING.md).

## Coding Standards

This project follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) for coding conventions. Please ensure that your code adheres to these standards before submitting a pull request.

## License

This project is licensed under the [Apache 2.0](./LICENSE).
