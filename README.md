# MaiBuddy-API

### Getting started

#### Prerequisites

1. Install Docker:

```Bash
brew install docker
```

2. Install Java 17

#### Running the local database

Run MongoDb in Docker:

```Bash
docker run -d -p 27017:27017 --name mongodb-maibuddy mongo:latest
```

#### Running the backend

Run the backend locally with command or simply press Run from your IDE

 ```Bash
 ./gradlew :backend:run
 ```
