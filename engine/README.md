# engine
The engine module is a fat JAR that packages all essential libraries and resources required to run the application. It includes foundational frameworks such as Spring, Hibernate, and Vaadin, alongside production-ready, precompiled frontend code. The engine module serves as the core runtime environment, managing backend and frontend interactions and ensuring seamless execution within the PacOS system.
###### Call `mvn clean package` to build fatJar
### Key Components
#### - Spring and Hibernate: 
Provides the core functionality for dependency injection, transaction management, and ORM (Object-Relational Mapping), enabling efficient backend operations and database interactions.
#### - Vaadin Framework: 
Delivers a rich UI experience with precompiled frontend resources tailored for production, allowing fast and responsive user interfaces.
#### - Production-Optimized frontend: 
All frontend code is precompiled for production, ensuring optimal performance and reduced load times.
### Execution
The engine module is launched by the starter module, which handles initialization and configuration. The engine module, once started, runs as the primary application environment, supporting the following functions:

Hosting backend services and handling database operations through Spring and Hibernate.
Serving the frontend interface powered by Vaadin.
Managing system dependencies and executing the core application logic as directed by the PacOS system.
### Usage
To run the application:

Start the `starter` module, which will automatically initialize and launch the engine, 
or via IDE run `org.pacos.core.Application.main`
Ensure that the configuration and dependencies are set up as per the pacos-config guidelines.
### Deployment
The engine module is optimized for production environments, with all libraries and resources consolidated into a single JAR for streamlined deployment and simplified dependency management.
