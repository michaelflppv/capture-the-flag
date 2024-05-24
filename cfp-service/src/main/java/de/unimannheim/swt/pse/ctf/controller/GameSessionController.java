package de.unimannheim.swt.pse.ctf.controller;

import de.unimannheim.swt.pse.ctf.CtfApplication;
import de.unimannheim.swt.pse.ctf.game.exceptions.ForbiddenMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.controller.data.*;
import de.unimannheim.swt.pse.ctf.game.engine.Game;
import de.unimannheim.swt.pse.ctf.game.state.GameState;

import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Team;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * This controller class defines several RESTful endpoints for managing game sessions / states:
 *
 * <ul>
 *  <li>POST `/api/gamesession` for creating a new game session,</li>
 *  <li>GET `/api/gamesession/{sessionId}` for retrieving a game session and its status,</li>
 *  <li>POST `/api/gamesession/{sessionId}/join` for a new team to join the game session,</li>
 *  <li>GET `/api/gamesession/{sessionId}/state` for retrieving the current game state for a specific game session,</li>
 *  <li>POST `/api/gamesession/{sessionId}/move` for making a move request for a specific game session, and</li>
 *  <li>POST `/api/gamesession/{sessionId}/giveup` for making a request to give up the game for a specific game session, and</li>
 *  <li>DELETE `/api/gamesession/{sessionId}` for deleting a specific game session.</li>
 * </ul>
 *
 * Important: Modifications to this controller are not allowed.
 */
@RestController
@RequestMapping("/api")
public class GameSessionController {

    private static final Logger LOG = LoggerFactory.getLogger(GameSessionController.class);

    private Map<String, GameSession> gameSessions;

    public GameSessionController() {
        this.gameSessions = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * To manage a game session, you can create a new game session by sending a `POST` request to the `/api/gamesession`
     * endpoint with a `GameSessionRequest` payload that specifies the number of players and the grid size.
     * <p>
     * This will create a new game session with a unique session ID and an initial game state.
     *
     * @param request {@link GameSessionRequest}
     * @return unique session ID created
     */
    @Operation(summary = "Create a new game session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game session created"),
            @ApiResponse(responseCode = "500", description = "Unknown error occurred")
    })
    @PostMapping("/gamesession")
    public GameSessionResponse createGameSession(@RequestBody GameSessionRequest request) {
        LOG.info("createGameSession request");

        // game session ID
        String sessionId = UUID.randomUUID().toString();

        // initialize new game engine with initial state
        Game game = CtfApplication.createGameEngine();
        game.create(request.getTemplate());

        // store game state
        this.gameSessions.put(sessionId, new GameSession(game));

        // create response
        GameSessionResponse sessionResponse = createGameSessionResponse(
                sessionId, game);

        return sessionResponse;
    }

    /**
     * You can retrieve the current session for a specific game session by sending a `GET` request to
     * the `/api/gamesession/{sessionId}` endpoint with the session ID.
     *
     * @param sessionId unique session id
     * @return GameSessionResponse
     */
    @Operation(summary = "Get the current game session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game session response returned"),
            @ApiResponse(responseCode = "404", description = "Game session not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error occurred")
    })
    @GetMapping("/gamesession/{sessionId}")
    public GameSessionResponse getGameSession(@Parameter(description = "existing game session id") @PathVariable String sessionId) {
        LOG.info("getGameSession request");

        Game game = this.getGame(sessionId);

        // create response
        GameSessionResponse sessionResponse = createGameSessionResponse(
                sessionId, game);

        return sessionResponse;
    }

    /**
     * You can retrieve the current game state for a specific game session by sending a `GET` request to
     * the `/api/gamesession/{sessionId}/state` endpoint with the session ID.
     *
     * @param sessionId unique session id
     * @return GameState
     */
    @Operation(summary = "Get the current game state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game state returned"),
            @ApiResponse(responseCode = "404", description = "Game session not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error occurred")
    })
    @GetMapping("/gamesession/{sessionId}/state")
    public GameState getGameState(@Parameter(description = "existing game session id") @PathVariable String sessionId) {
        LOG.info("getGameState request");

        Game game = this.getGame(sessionId);

        return game.getCurrentGameState();
    }

    /**
     * New teams can join a game session by sending a `POST` request to the `/api/gamesession/{sessionId}/join`
     * endpoint with a `JoinGameRequest` payload that specifies the team to join (i.e., team id).
     *
     * @param sessionId   unique session id
     * @param joinRequest {@link JoinGameRequest}
     */
    @Operation(summary = "New team joins a game session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team joined"),
            @ApiResponse(responseCode = "404", description = "Game session not found"),
            @ApiResponse(responseCode = "429", description = "No more team slots available"),
            @ApiResponse(responseCode = "500", description = "Unknown error occurred")
    })
    @PostMapping("/gamesession/{sessionId}/join")
    public JoinGameResponse joinGame(@Parameter(description = "existing game session id") @PathVariable String sessionId, @RequestBody JoinGameRequest joinRequest) {
        LOG.info("joinGame request");

        GameSession gameSession = this.gameSessions.get(sessionId);
        if (gameSession == null) {
            throw new GameSessionNotFound();
        }

        Team team = gameSession.getGame().joinGame(joinRequest.getTeamId());

        // create response
        JoinGameResponse response = new JoinGameResponse();
        response.setGameSessionId(sessionId);
        response.setTeamId(team.getId());
        response.setTeamColor(team.getColor());

        // create a team secret to make move requests a little secure
        String teamSecret = gameSession.createTeamSecret(team.getId());
        response.setTeamSecret(teamSecret);

        return response;
    }

    /**
     * You can make a move request for a specific game session by sending a `POST` request to the
     * `/api/gamesession/{sessionId}/move` endpoint with a `MoveRequest` payload that specifies the piece ID and the new position.
     * This will update the game state based on the move request and notify other players of the updated game state.
     *
     * @param sessionId   unique session id
     * @param moveRequest {@link MoveRequest}
     */
    @Operation(summary = "Make a move in a game session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid move"),
            @ApiResponse(responseCode = "404", description = "Game session not found"),
            @ApiResponse(responseCode = "403", description = "Move is forbidden for given team (anti-cheat)"),
            @ApiResponse(responseCode = "409", description = "Invalid move"),
            @ApiResponse(responseCode = "410", description = "Game is over"),
            @ApiResponse(responseCode = "500", description = "Unknown error occurred")
    })
    @PostMapping("/gamesession/{sessionId}/move")
    public void makeMove(@Parameter(description = "existing game session id") @PathVariable String sessionId, @RequestBody MoveRequest moveRequest) {
        LOG.info("makeMove request");

        GameSession gameSession = this.gameSessions.get(sessionId);
        if (gameSession == null) {
            throw new GameSessionNotFound();
        }

        // allowed to make this move?
        if(!gameSession.isAllowed(moveRequest.getTeamId(), moveRequest.getTeamSecret())) {
            throw new ForbiddenMove();
        }

        Game game = gameSession.getGame();

        Move move = new Move();
        move.setPieceId(moveRequest.getPieceId());
        move.setNewPosition(moveRequest.getNewPosition());
        move.setTeamId(moveRequest.getTeamId());

        game.makeMove(move);

    }

    /**
     * A team can give up the game for a specific game session by sending a `POST` request to
     * the `/api/gamesession/{sessionId}/giveup` endpoint with the session ID.
     *
     * @param sessionId unique session id
     * @param giveupRequest {@link GiveupRequest}
     */
    @Operation(summary = "Give up a game in a game session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed"),
            @ApiResponse(responseCode = "404", description = "Game session not found"),
            @ApiResponse(responseCode = "403", description = "Give up is forbidden for given team (anti-cheat)"),
            @ApiResponse(responseCode = "410", description = "Game is over"),
            @ApiResponse(responseCode = "500", description = "Unknown error occurred")
    })
    @PostMapping("/gamesession/{sessionId}/giveup")
    public void giveUp(@Parameter(description = "existing game session id") @PathVariable String sessionId, @RequestBody GiveupRequest giveupRequest) {
        LOG.info("giveUp request");

        GameSession gameSession = this.gameSessions.get(sessionId);
        if (gameSession == null) {
            throw new GameSessionNotFound();
        }

        // allowed to make this move?
        if(!gameSession.isAllowed(giveupRequest.getTeamId(), giveupRequest.getTeamSecret())) {
            throw new ForbiddenMove();
        }

        Game game = gameSession.getGame();

        game.giveUp(giveupRequest.getTeamId());
    }

    /**
     * Finally, you can delete a specific game session by sending a `DELETE` request to the `/api/gamesession/{sessionId}`
     * endpoint with the session ID.
     *
     * @param sessionId unique session id
     */
    @Operation(summary = "Delete a specific game session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game session removed"),
            @ApiResponse(responseCode = "404", description = "Game session not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error occurred")
    })
    @DeleteMapping("/gamesession/{sessionId}")
    public void deleteGameSession(@Parameter(description = "existing game session id") @PathVariable String sessionId) {
        LOG.info("deleteGameSession request");

        if (!this.gameSessions.containsKey(sessionId)) {
            throw new GameSessionNotFound();
        }

        this.gameSessions.remove(sessionId);
    }

    /**
     * Helper method to get current {@link Game}.
     *
     * @param sessionId
     * @return
     */
    private Game getGame(String sessionId) {
        GameSession gameSession = this.gameSessions.get(sessionId);
        if (gameSession == null) {
            throw new GameSessionNotFound();
        }

        return gameSession.getGame();
    }

    /**
     * Helper method to create GameSessionResponse.
     *
     * @param sessionId Game session ID
     * @param game {@link Game}
     * @return GameSessionResponse
     */
    private GameSessionResponse createGameSessionResponse(String sessionId, Game game) {
        GameSessionResponse sessionResponse = new GameSessionResponse();
        sessionResponse.setId(sessionId);
        sessionResponse.setGameStarted(game.getStartedDate());
        sessionResponse.setGameEnded(game.getEndDate());

        sessionResponse.setRemainingGameTimeInSeconds(game.getRemainingGameTimeInSeconds());
        sessionResponse.setRemainingMoveTimeInSeconds(game.getRemainingMoveTimeInSeconds());

        sessionResponse.setGameOver(game.isGameOver());
        sessionResponse.setWinner(game.getWinner());

        return sessionResponse;
    }
}



