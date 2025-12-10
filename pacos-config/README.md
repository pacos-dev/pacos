# pacos-config
The pacos-config module serves as a shared configuration and setup solution for the starter and engine applications. It provides the essential logic and components needed for system initialization, configuration management, and inter-module communication.

## Key Features
#### - Comprehensive Configuration Management:
Provides a centralized source for application configurations, allowing consistent access across both starter and engine modules.
#### - API Client for Remote Repository Access: 
Enables seamless retrieval of remote assets and updates by providing a client interface for connecting to a remote repository.
#### - Static Database Access: 
Facilitates initialization and startup processes by establishing necessary database tables and providing static access for core operations.
#### - RMI Register Configuration: 
Configures the RMI (Remote Method Invocation) registry, allowing for smooth communication between the starter and engine applications.
Functionality


## Initial Launch
When the starter application launches for the first time, pacos-config is responsible for:

- Loading and applying the initial application configuration.
- Setting up basic database tables required for dependency and module management.
- Downloading the latest system artifacts based on the current configuration and version.
- Saving configuration details for all active modules and plugins.

## Subsequent Launches
For all following launches, pacos-config performs the following:

- Loads and applies the stored application configuration.
- Provides module and plugin paths from the saved configuration, ensuring these are added to the engine classpath at startup.
- Enables access to the remote repository client API, facilitating system and plugin updates.
## Usage
Integrate pacos-config by setting up its configuration files and ensuring access to the API client and database components. This module streamlines system startup, initialization, and configuration handling, making it easier to manage dependencies, updates, and module interactions across applications.