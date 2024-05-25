# Capture The Flag Game


## Project Overview

This project is part of the CS306 Software Engineering Practical course of the University of Mannheim, Germany.

The project goal is to create a digital version of the popular game 'Capture the Flag' that offers an interactive and engaging experience for players. The game is easy to understand and play. The software implements all the rules and mechanics of 'Capture the Flag' as described in the requirements document. Additionally, it provides flexibility by allowing players to create custom maps in terms of size, teams, pieces, and number of flags in each base. It allows for the selection of AI-powered players of varying difficulties to fill team slots. To enable multiple players to participate in a single game session from various locations, a client-server architecture using the RESTful API is employed. The project is deemed successful if the final software meets all requirements, including cross-compatibility with third-party servers and clients that implement the same RESTful API.

Both the client and server are implemented in Java (SDK 17), the UI is realized using JavaFX. Furthermore, a **requirements specification** can always be found in [REQUIREMENTS_SPECIFICATION.md](cfp-service/REQUIREMENTS_SPECIFICATION.md).


# User Manual
## Open your Game
![image](https://github.com/michaelflppv/capture-the-flag/assets/113472068/8dff6957-3764-4064-abca-2d52ce87c0a6)
## The Lobby & Create your Game
![image](https://github.com/michaelflppv/capture-the-flag/assets/113472068/60ce6170-3860-42a5-9d14-846e1200a590)
## Join a Game & Create your own Map Template
![image](https://github.com/michaelflppv/capture-the-flag/assets/113472068/75670b2f-e9c7-4f61-ba29-a73574d77580)

# Developer Manual
This part of the document provides comprehensive information for developers to understand, set up, and contribute to the project. It also includes package descriptions and key extension points to help third-party developers integrate and extend the project.

## Running the Project
**Running the Server:**
Navigate to the de.unimannheim.swt.pse.ctf.game package, go into to CtfApplication class and run the main method.
**Running the Client:**
Navigate to the cfp4_client module and go into the de.sep.cfp4.gui package. There you need to open the Launcher class and execute the main method.

## Package Descriptions
We will now give a brief overview of the different packages inside of the project and highlight some of the most important classes and their functionality.

- **Package: [ctf.game.controller](cfp-service/src/main/java/de/unimannheim/swt/pse/ctf/controller):**
 This package includes controllers for handling HTTP requests related to game sessions.
GameSession: Represents a session of the game, managing the state and progress.
GameSessionController: Handles HTTP requests related to game sessions.

- **Package: [ctf.game.engine](cfp-service/src/main/java/de/unimannheim/swt/pse/ctf/game/engine):**
 This package contains the core game logic, including the game engine and helper methods.
GameEngine: Implementation of all aspects of the game logic.
RespawnHelperMethods: Helper methods for handling player respawn logic.
ValidMoveHelperMethods: Helper methods for validating player moves.

- **Package: [ctf.game.PiecePlacement](cfp-service/src/main/java/de/unimannheim/swt/pse/ctf/game/PiecePlacement):**
 This package includes constants and classes related to piece placement and game configuration.
PiecePlacement: Manages the placement of game pieces on the board.

- **Package: [cfp4.gui](cfp4_client/src/main/java/de/sep/cfp4):**
 This package comprises all components related to the client-side GUI, including controllers, models, views, and interfaces.
Launcher: Starts the GUI.
Database: Interface for database operations within the client application.
BoardModel: Represents the model for the game board in the client application.
MapEditorModel: Model for the map editor within the client application.

- **Package: [cfp4.AI](cfp4_client/src/main/java/de/sep/cfp4/technicalServices/ai):**
 This package contains all the components related to the artificial intelligence (AI) functionalities used in the game. This includes the implementation of the Monte Carlo Tree Search (MCTS) algorithm and other related models and utilities that enable AI-driven decision-making within the game.
MCTSClient: Interacts with the game. It contains methods for determining the value of a game state, checking if the game is terminated, and making the optimal move in the game. It also contains methods for interacting with the game's board model and the game's teams. NormalBot: Implements a more advanced AI that evaluates and ranks possible moves based on strategic considerations, making informed decisions to maximize the bot's advantage.
EasyBot: Implements a simple AI that makes random valid moves, prioritizing immediate opportunities to capture flags or opponent pieces. 

- **Package: [cfp4.gameClient](cfp4_client/src/main/java/de/sep/cfp4/technicalServices/network):**
 This package provides the client-side functionality for interacting with the game server, enabling the frontend to communicate with the backend for various game operations.
GameClient: Implements the client for the frontend to communicate with the backend, handling operations such as creating and joining game sessions, making moves, retrieving game state, and managing game sessions through HTTP requests.

## Extension Points
The project is designed with extensibility in mind. Here are some key extension points:

- **Game Logic**: The core game logic is implemented in the [de.unimannheim.swt.pse.ctf.game](cfp-service/src/main/java/de/unimannheim/swt/pse/ctf/game) package. You can extend or modify the game rules by creating new classes or extending existing ones in this package.

- **Controllers**: To add new functionalities accessible via HTTP, you can add or extend controllers in the de.unimannheim.swt.pse.ctf.controller package.
Data Transfer Objects (DTOs): The de.unimannheim.swt.pse.ctf.controller.data package contains DTOs for communication between the client and server. You can add new DTOs to support additional data exchanges.

- **Client GUI**: The client-side GUI can be extended by adding new controllers, views, or models in the de.sep.cfp4.gui package and its sub-packages.

- **Monte Carlo Tree Search (MCTS)**: The de.sep.cfp4.mcts package contains the MCTS algorithm implementation. You can extend this package to improve or customize the MCTS algorithm used in the game.

# Progress Report
**Network**

The RESTful API has been successfully implemented on both the client and server sides using methods that send HTTP requests with the required JSON strings generated using the third-party library Gson. Each game instance starts a new communication thread that stores the initially specified server URL and game session token and handles requests belonging to a single game session. Corresponding JUnit tests for the communication class have already been completed.

**Frontend**

A Graphical User Interface has been implemented. The home screen, joining existing games, creation of a new game scene, map editor and rendering of the game board are fully functional. We also developed a Olympic theme that uses different player graphics and shows different options for the bot difficulty.

**AI**

The core components of the hardest AI model based on the Monte Carlo Tree Search (MCTS) algorithm have been successfully implemented. The MCTS algorithm has been integrated with the client. The algorithm performs a search on the game tree to identify the optimal move to play. This search is performed in parallel using multiple threads to enhance performance. The convolutional neural network (CNN) model used in the MCTS algorithm has been implemented. This model is capable of accurately predicting the policy and value of a given state. The client, which interacts with the game and the MCTS algorithm, has been integrated. It provides methods for obtaining the current state of the game, making a move, and retrieving the valid moves for the current state. The easy bot which mostly makes random moves but captures pieces and the flag if possible and the normal bot which makes only strategic moves based on whether a piece or a flag can be captured, whether the own base is endangered and even one move in advance have both been completed. A graphical implementation was created so that when creating a game, a user can easily choose all types of bots to play against. We also created a spectator mode for players that let a bot play for them so you can still view the visual representation of the bots moves.

**Game Engine**

The GameEngine is fully implemented, capable of handling games with up to 4 players, also including the multi-flag game mode. It efficiently manages game state updates and game logic through well-organized methods, with time management being straightforward and game state updates more complex. To enhance clarity and maintainability, functionalities such as move validation and piece respawning in multi-flag games were extracted into separate support classes. Thread safety is ensured by synchronizing the game state within the GameEngine class.

**Move Validation**

The task of validating a move is handled by an instance of an extra class within the respective methods of the GameEngine. The process is based on generating a two-dimensional int-array, which is of the same size as the GameState.grid. We then save numbers from –1 to 2 in this array, resembling that moving to that square is either impossible or if possible, whether it is an empty square, contains an opponent piece or the opponent base.

**Tests**

All game functions have now been tested and are working properly. The tests for the server/GameEngine are fully implemented. The test classes for the two support classes are complete, relying on JUnit tests with randomly generated data. The GameEngine test class, despite its complexity, has also been successfully developed. Additionally, we have implemented tests for the GameClient, validated Map Templates, and rigorously tested AI Bots. The CtfApplication and Game Session Controller have undergone thorough testing. We used both random data and edge cases to ensure comprehensive coverage. The extensive suite of tests confirms the stability and reliability of the entire system.

# Documentation
## Domain Model
![image](https://github.com/michaelflppv/capture-the-flag/assets/113472068/09db2b4b-7631-4cef-b355-0a1e47b9ba5f)
## Architecture Diagram
![image](https://github.com/michaelflppv/capture-the-flag/assets/113472068/e0fc206d-44c9-4666-b25e-3d55c95230fb)


## Contributors
- Mikhail Filippov | mfilippo
- David Cebulla | dcebulla
- Jannis Wiederhöft | jwiederh
- Sebastian Geiger | sebgeige
- Jascha Herrmann | jasherrm
- Janos Gröhl | jgroehl

# Releases
The release for the final submission can be found [here](https://github.com/michaelflppv/capture-the-flag/releases/tag/v2.0.0).
