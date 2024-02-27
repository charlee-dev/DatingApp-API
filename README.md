# DatingApp Backend

## Introduction

Welcome to the backend repository for DatingApp, a state-of-the-art dating platform designed to connect people in more meaningful ways. This backend is built with Kotlin using the Ktor framework and GraphQL, leveraging the Expedia Group's GraphiQL library for an interactive API exploration. Our choice of MongoDB for storage, with the support of the Mongo coroutines client, ensures efficient and non-blocking data access. This project demonstrates secure communication practices, authentication mechanisms, and the use of GraphQL mutations, queries, and subscriptions for real-time chat functionality. Additionally, it showcases the use of the Kotlin case pattern for effective data handling and pattern matching.

## Features

- **Ktor Framework**: Utilizes the lightweight, asynchronous framework provided by Ktor for building scalable and efficient web applications.

- **GraphQL API**: Implements a comprehensive GraphQL API using the Expedia Group's GraphiQL library, facilitating complex queries, mutations, and real-time data subscriptions with ease.

- **Authentication**: Showcases secure authentication practices, ensuring that user data and communications are protected.

- **MongoDB Storage**: Uses MongoDB for data storage, coupled with the Mongo coroutines client, to provide a non-blocking, reactive data layer.

- **Real-Time Chat**: Leverages GraphQL subscriptions to implement real-time chat functionalities, allowing users to communicate instantly and securely within the platform.

- **Kotlin Case Pattern**: Demonstrates the use of Kotlin's powerful case pattern for efficient data processing and pattern matching, enhancing the code's readability and maintainability.

## Technology Stack

- **Ktor**: A Kotlin-based framework for building asynchronous servers and clients in connected systems.
- **GraphQL**: A query language for your API, and a server-side runtime for executing queries by using a type system you define for your data.
- **Expedia Group GraphiQL**: An integrated GraphiQL interface to explore GraphQL queries and mutations interactively.
- **MongoDB**: A NoSQL database used for storing application data, with support for complex queries, secondary indexes, and real-time aggregation.
- **Mongo Coroutines Client**: A MongoDB client designed for Kotlin that supports coroutines, providing a non-blocking, reactive programming model.

## Architecture

The backend architecture of DatingApp is designed for scalability, maintainability, and high performance:

- **API Layer (GraphQL)**: Acts as the interface between the frontend and backend, processing requests and responses via GraphQL endpoints.
- **Business Logic Layer**: Contains the core functionality of the application, including authentication, user profile management, and chat operations.
- **Data Access Layer (DAL)**: Handles data persistence and retrieval from MongoDB, utilizing the Mongo coroutines client for asynchronous operations.
- **Security**: Implements robust security measures for authentication and authorization, ensuring safe and secure access to the application's features.
