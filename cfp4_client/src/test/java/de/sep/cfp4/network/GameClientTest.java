package de.sep.cfp4.network;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import de.sep.cfp4.technicalServices.network.GameClient;
import de.unimannheim.swt.pse.ctf.CtfApplication;
import de.unimannheim.swt.pse.ctf.controller.data.*;
import de.unimannheim.swt.pse.ctf.game.exceptions.*;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * This class tests the implementation of the GameClient class.
 *
 * @author dcebulla, jasherrm
 * @version 0.0.2
 * @see GameClient
 */

class GameClientTest {

  /**
   * The GameClientTest class contains a series of unit tests for the GameClient class, which
   * manages the communication with the game server. Each test method in this class is designed to
   * verify the functionality of the GameClient under various scenarios.
   *
   * Tests in the class:
   *
   * ---  createGameSessionTest()
   *      This test checks the creation of a game session. It creates a MapTemplate and a game session,
   *      and verifies if a session ID is returned.
   *
   * ---  getGameSessionTestSuccess()
   *      This test checks the retrieval of an existing game session. It creates a game session and then
   *      tries to retrieve it, verifying if the game session is successfully retrieved.
   *
   * ---  getGameSessionTestFail()
   *      This test checks the retrieval of a non-existing game session. It tries to retrieve a
   *      non-existing game session, expecting a GameSessionNotFound exception.
   *
   * ---  getGameStateTestSuccess()
   *      This test checks the retrieval of the game state of an existing game session. It creates a
   *      game session and then retrieves the game state, verifying if the game state is successfully retrieved.
   *
   * ---  getGameStateTestFail()
   *      This test checks the retrieval of the game state of a non-existing game session. It tries to
   *      retrieve the game state of a non-existing game session, expecting a GameSessionNotFound exception.
   *
   * ---  deleteGameSessionTestSuccess()
   *      This test checks the deletion of an existing game session. It creates a game session, retrieves it,
   *      and then deletes it, verifying if the game session is successfully deleted.
   *
   * ---  deleteGameSessionTestFail()
   *      This test checks the deletion of a non-existing game session. It tries to delete a non-existing
   *      game session, expecting a GameSessionNotFound exception.
   *
   * ---  joinGameTestSuccess()
   *      This test checks joining an existing game session. It creates a game session and then tries to
   *      join it, verifying if the joining is successful.
   *
   * ---  joinGameTestFailSessionNotFound()
   *      This test checks joining a non-existing game session. It tries to join a non-existing game session,
   *      expecting a GameSessionNotFound exception.
   *
   * ---  giveUpTestSuccess()
   *      This test checks giving up in an existing game session. It creates a game session, joins a team,
   *      and then gives up, verifying if the giving up is successful.
   *
   * ---  giveUpTestFailSessionNotFound()
   *      This test checks giving up in a non-existing game session. It tries to give up in a non-existing
   *      game session, expecting a GameSessionNotFound exception.
   *
   * ---  joinGameTestFailSlotsFull()
   *      This test checks joining a game session when all team slots are already filled. It attempts to join
   *      a game session that is already full, expecting a NoMoreTeamSlots exception.
   *
   * ---  giveUpTestFailForbiddenMove()
   *      This test checks giving up in a game session when this move is illegal. It attempts to give up in a
   *      game session where giving up is a forbidden move, expecting a ForbiddenMove exception.
   *
   * ---  giveUpTestFailGameOver()
   *      This test checks giving up in a game session that has already ended. It attempts to give up in a
   *      game session that has already ended, expecting a GameOver exception.
   *
   * ---  makeMoveTestSuccess()
   *      This test checks making a move in an existing game session. It creates a game session, joins a team,
   *      and then makes a move, verifying if the move is successful.
   *
   * ---  makeMoveTestFailSessionNotFound()
   *      This test checks making a move in a non-existing game session. It attempts to make a move in a
   *      non-existing game session, expecting a GameSessionNotFound exception.
   *
   * ---  makeMoveTestFailForbiddenMove()
   *      This test checks making an illegal move in an existing game session. It attempts to make an illegal
   *      move in a game session, expecting a ForbiddenMove exception.
   *
   * ---  makeMoveTestFailGameOver()
   *      This test checks making a move in a game session that has already ended. It attempts to make a move
   *      in a game session that has already ended, expecting a GameOver exception.
   *
   * ---  makeMoveTestFailInvalidMove()
   *      This test checks making an invalid move in an existing game session. It attempts to make an invalid
   *      move in a game session, expecting an InvalidMove exception.
   */

  private final String SERVER_URL = "http://localhost:8888";
  private final GameClient client = new GameClient(URI.create(this.SERVER_URL));
  private String sessionId;

  GameClientTest() throws URISyntaxException {
  }


  /**
   * Start the server before running the tests.
   *
   * @throws URISyntaxException if the server URL is invalid
   */
  @BeforeAll
  static void setUp() throws URISyntaxException {
    CtfApplication.main(new String[0]);
  }

  @AfterAll
  static void shutDownServer() {
    CtfApplication.stopApplication();
  }

  /**
   * Test the creation of a game session.
   *
   * @throws IOException          if no connection to Server can be established
   * @throws InterruptedException if the communication with the server is interrupted
   */
  @Test
  void createGameSessionTest() throws IOException, InterruptedException {
    try (InputStreamReader reader = new InputStreamReader(
            getClass().getResourceAsStream("10x10_2teams_example.json"))) {
      MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);
      GameSessionResponse response = this.client.createGameSession(template);
      this.sessionId = response.getId();
      assertNotNull(this.sessionId);
    }
  }

  /**
   * Test the retrieval of a game session.
   *
   * @throws IOException          if no connection to Server can be established
   * @throws InterruptedException if the communication with the server is interrupted
   */
  @Test
  void getGameSessionTestSuccess() throws IOException, InterruptedException {
    this.createGameSessionTest();
    assertNotNull(this.client.getGameSession(this.sessionId));
  }

  /**
   * Test the retrieval of a game session that does not exist.
   */
  @Test
  void getGameSessionTestFail() {
    assertThrows(GameSessionNotFound.class, () -> this.client.getGameSession("nonexistent"));
  }

  /**
   * Test the retrieval of the game state.
   *
   * @throws IOException          if no connection to Server can be established
   * @throws InterruptedException if the communication with the server is interrupted
   */
  @Test
  void getGameStateTestSuccess() throws IOException, InterruptedException {
    this.createGameSessionTest();
    System.out.println(this.sessionId);
    assertNotNull(this.client.getGameState(this.sessionId));
  }


  /**
   * Test the retrieval of the game state of a game session that does not exist.
   */
  @Test
  void getGameStateTestFail() {
    assertThrows(GameSessionNotFound.class, () -> this.client.getGameState("nonexistent"));
  }


  /**
   * Test the deletion of a game session.
   *
   * @throws IOException          if no connection to Server can be established
   * @throws InterruptedException if the communication with the server is interrupted
   */
  @Test
  void deleteGameSessionTestSuccess() throws IOException, InterruptedException {
    this.createGameSessionTest();
    assertNotNull(this.client.getGameSession(this.sessionId));
    this.client.deleteGameSession(this.sessionId);
    assertThrows(GameSessionNotFound.class, () -> this.client.getGameSession(this.sessionId));
  }

  /**
   * Test the deletion of a game session that does not exist.
   */
  @Test
  void deleteGameSessionTestFail() {
    assertThrows(GameSessionNotFound.class, () -> this.client.deleteGameSession("nonexistent"));
  }

  @Test
  void joinGameTestSuccess() throws IOException, InterruptedException {
    this.createGameSessionTest();
    JoinGameRequest someTeamJoinRequest = new JoinGameRequest();
    someTeamJoinRequest.setTeamId("someTeam");
    JoinGameResponse joinGameResponse = this.client.joinGame(this.sessionId, someTeamJoinRequest);
    assertNotNull(joinGameResponse);
    assertEquals("someTeam", joinGameResponse.getTeamId());
  }

  @Test
  void joinGameTestFailSessionNotFound() {
    JoinGameRequest someTeamJoinRequest = new JoinGameRequest();
    someTeamJoinRequest.setTeamId("someTeam");
    assertThrows(GameSessionNotFound.class,
            () -> this.client.joinGame("nonexistent", someTeamJoinRequest));
  }

  @Test
  void giveUpTestSuccess() throws IOException, InterruptedException {
    this.createGameSessionTest();
    JoinGameRequest someTeamJoinRequest = new JoinGameRequest();
    someTeamJoinRequest.setTeamId("someTeam");
    JoinGameResponse joinGameResponse = this.client.joinGame(this.sessionId, someTeamJoinRequest);

    GiveupRequest giveupRequest = new GiveupRequest();
    giveupRequest.setTeamId(joinGameResponse.getTeamId());
    giveupRequest.setTeamSecret(joinGameResponse.getTeamSecret());

    this.client.giveUp(this.sessionId, giveupRequest);
  }

  @Test
  void giveUpTestFailSessionNotFound() {
    GiveupRequest giveupRequest = new GiveupRequest();
    giveupRequest.setTeamId("someTeam");
    giveupRequest.setTeamSecret("someSecret");
    assertThrows(GameSessionNotFound.class, () -> this.client.giveUp("nonexistent", giveupRequest));
  }


  /**
   * Test the joining of a game session if the team slots are already full. The current DummyServer
   * implementation does not support this test since it does not keep track of the number of teams
   * that have joined a game session.
   */

  @Test
  void joinGameTestFailSlotsFull() throws IOException, InterruptedException {
    this.createGameSessionTest();
    JoinGameRequest joinRequest = new JoinGameRequest();
    joinRequest.setTeamId("someTeam");

    // Fill the game session by joining multiple times
    for (int i = 0; i < 2; i++) {
      this.client.joinGame(this.sessionId, joinRequest);
    }

    // Now the game session should be full and the next attempt to join should throw a NoMoreTeamSlots exception
    assertThrows(NoMoreTeamSlots.class, () -> this.client.joinGame(this.sessionId, joinRequest));
  }

  /**
   * Test the give up of a game session if this move is illegal due to a wrong team secret.
   */

  @Test
  void giveUpTestFailForbiddenMove() throws IOException, InterruptedException {
    this.createGameSessionTest();
    // Team 1 joins the game
    JoinGameRequest joinRequestTeam1 = new JoinGameRequest();
    joinRequestTeam1.setTeamId("someTeam");
    JoinGameResponse joinResponseTeam1 = this.client.joinGame(this.sessionId, joinRequestTeam1);

    // Team 2 joins the game
    JoinGameRequest joinRequestTeam2 = new JoinGameRequest();
    joinRequestTeam2.setTeamId("someOtherTeam");
    JoinGameResponse joinResponseTeam2 = this.client.joinGame(this.sessionId, joinRequestTeam2);

    // Try to give up as team 2
    GiveupRequest giveupRequest = new GiveupRequest();
    giveupRequest.setTeamId(joinResponseTeam2.getTeamId());
    //giveupRequest.setTeamSecret(joinResponseTeam2.getTeamSecret());

    // Assuming that giving up is a forbidden move
    assertThrows(ForbiddenMove.class, () -> this.client.giveUp(this.sessionId, giveupRequest));
  }

  /**
   * Test the give up of a game session if the game session has already ended.
   */
  @Test
  @Disabled
  void giveUpTestFailGameOver() throws IOException, InterruptedException {
    this.createGameSessionTest();
    // Team 1 joins the game
    JoinGameRequest joinRequestTeam1 = new JoinGameRequest();
    joinRequestTeam1.setTeamId("someTeam");
    JoinGameResponse joinResponseTeam1 = this.client.joinGame(this.sessionId, joinRequestTeam1);

    // Team 2 joins the game
    JoinGameRequest joinRequestTeam2 = new JoinGameRequest();
    joinRequestTeam2.setTeamId("someOtherTeam");
    JoinGameResponse joinResponseTeam2 = this.client.joinGame(this.sessionId, joinRequestTeam2);

    // Give up as team 1
    GiveupRequest giveupRequestTeam1 = new GiveupRequest();
    giveupRequestTeam1.setTeamId(joinResponseTeam1.getTeamId());
    giveupRequestTeam1.setTeamSecret(joinResponseTeam1.getTeamSecret());
    this.client.giveUp(this.sessionId, giveupRequestTeam1);

    // Give up request for team 2
    GiveupRequest giveupRequestTeam2 = new GiveupRequest();
    giveupRequestTeam2.setTeamId(joinResponseTeam2.getTeamId());
    giveupRequestTeam2.setTeamSecret(joinResponseTeam2.getTeamSecret());

    // Assuming that the game session has already ended, the give up attempt for team2 should throw a GameOver exception
    assertThrows(GameOver.class, () -> this.client.giveUp(this.sessionId, giveupRequestTeam2));
  }

  /**
   * Test the making of a move. The current DummyServer implementation does not support this test.
   */
  @Test
  void makeMoveTestSuccess() throws IOException, InterruptedException {
    try (InputStreamReader reader = new InputStreamReader(
            getClass().getResourceAsStream("simpleTestMapTemplate.json"))) {
      MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);
      GameSessionResponse response = this.client.createGameSession(template);
      this.sessionId = response.getId();

      // Team 1 joins the game
      JoinGameRequest joinRequestTeam1 = new JoinGameRequest();
      joinRequestTeam1.setTeamId("someTeam");
      JoinGameResponse joinResponseTeam1 = this.client.joinGame(this.sessionId, joinRequestTeam1);

      // Team 2 joins the game
      JoinGameRequest joinRequestTeam2 = new JoinGameRequest();
      joinRequestTeam2.setTeamId("someOtherTeam");
      JoinGameResponse joinResponseTeam2 = this.client.joinGame(this.sessionId, joinRequestTeam2);

      // Get the current game state
      GameState gameState = this.client.getGameState(this.sessionId);

      String[][] grid = gameState.getGrid();
      for (int i = 0; i < grid.length; i++) {
        for (int j = 0; j < grid[i].length; j++) {
          System.out.print(grid[i][j].equals("") ? " x " : grid[i][j]);
        }
        System.out.println();
      }

      // Get the first piece of the joined team
      Piece piece = Arrays.stream(gameState.getTeams())
              .filter(team -> team.getId().equals(joinResponseTeam1.getTeamId()))
              .flatMap(team -> Arrays.stream(team.getPieces()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("No pieces found for the joined team"));

      // Create a move request to move the piece to a new position
      MoveRequest moveRequest = new MoveRequest();
      moveRequest.setTeamId(joinResponseTeam1.getTeamId());
      moveRequest.setTeamSecret(joinResponseTeam1.getTeamSecret());
      moveRequest.setPieceId(piece.getId());
      moveRequest.setNewPosition(new int[]{piece.getPosition()[0], piece.getPosition()[1] + 1});

      // Make the move
      assertDoesNotThrow(() -> this.client.makeMove(this.sessionId, moveRequest));
    }
  }

  /**
   * Test the making of a move if the game session does not exist. The current DummyServer
   * implementation does not support this test.
   */

  @Test
  void makeMoveTestFailSessionNotFound() {
    MoveRequest moveRequest = new MoveRequest();
    moveRequest.setTeamId("someTeam");
    moveRequest.setPieceId("validPieceId");
    moveRequest.setNewPosition(new int[]{1, 1});
    assertThrows(GameSessionNotFound.class, () -> this.client.makeMove("nonexistent", moveRequest));
  }

  /**
   * Test the making of a move if the move is illegal.
   */

  @Test
  void makeMoveTestFailForbiddenMove() throws IOException, InterruptedException {
    try (InputStreamReader reader = new InputStreamReader(
            getClass().getResourceAsStream("simpleTestMapTemplate.json"))) {
      MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);
      GameSessionResponse response = this.client.createGameSession(template);
      this.sessionId = response.getId();

      // Team 1 joins the game
      JoinGameRequest joinRequestTeam1 = new JoinGameRequest();
      joinRequestTeam1.setTeamId("someTeam");
      JoinGameResponse joinResponseTeam1 = this.client.joinGame(this.sessionId, joinRequestTeam1);

      // Team 2 joins the game
      JoinGameRequest joinRequestTeam2 = new JoinGameRequest();
      joinRequestTeam2.setTeamId("someOtherTeam");
      JoinGameResponse joinResponseTeam2 = this.client.joinGame(this.sessionId, joinRequestTeam2);

      // Get the current game state
      GameState gameState = this.client.getGameState(this.sessionId);
      Piece piece = Arrays.stream(gameState.getTeams())
              .filter(team -> team.getId().equals(joinResponseTeam1.getTeamId()))
              .flatMap(team -> Arrays.stream(team.getPieces()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("No pieces found for the joined team"));

      MoveRequest moveRequest = new MoveRequest();
      moveRequest.setTeamId(joinResponseTeam1.getTeamId());
      moveRequest.setTeamSecret("defectTeamSecretofCheater");
      moveRequest.setPieceId(piece.getId());
      moveRequest.setNewPosition(new int[]{piece.getPosition()[0],
              piece.getPosition()[1] + 1}); // Assuming this position is illegal

      assertThrows(ForbiddenMove.class, () -> this.client.makeMove(this.sessionId, moveRequest));
    }
  }

  /**
   * Test the making of a move if the game session has already ended.
   */

  @Test
  void makeMoveTestFailGameOver() throws IOException, InterruptedException {
    try (InputStreamReader reader = new InputStreamReader(
            getClass().getResourceAsStream("simpleTestMapTemplate.json"))) {
      MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);
      GameSessionResponse response = this.client.createGameSession(template);
      this.sessionId = response.getId();

      // Team 1 joins the game
      JoinGameRequest joinRequestTeam1 = new JoinGameRequest();
      joinRequestTeam1.setTeamId("someTeam");
      JoinGameResponse joinResponseTeam1 = this.client.joinGame(this.sessionId, joinRequestTeam1);

      // Team 2 joins the game
      JoinGameRequest joinRequestTeam2 = new JoinGameRequest();
      joinRequestTeam2.setTeamId("someOtherTeam");
      JoinGameResponse joinResponseTeam2 = this.client.joinGame(this.sessionId, joinRequestTeam2);

      Piece piece = Arrays.stream(this.client.getGameState(this.sessionId).getTeams())
              .filter(team -> team.getId().equals(joinResponseTeam2.getTeamId()))
              .flatMap(team -> Arrays.stream(team.getPieces()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("No pieces found for the joined team"));

      // Give up as team 1
      GiveupRequest giveupRequestTeam1 = new GiveupRequest();
      giveupRequestTeam1.setTeamId(joinResponseTeam1.getTeamId());
      giveupRequestTeam1.setTeamSecret(joinResponseTeam1.getTeamSecret());
      this.client.giveUp(this.sessionId, giveupRequestTeam1);

      // Assuming that the game session has already ended
      MoveRequest moveRequest = new MoveRequest();
      moveRequest.setTeamId(joinResponseTeam2.getTeamId());
      moveRequest.setTeamSecret(joinResponseTeam2.getTeamSecret());
      moveRequest.setPieceId(piece.getId());
      moveRequest.setNewPosition(new int[]{piece.getPosition()[0], piece.getPosition()[1] + 1});

      assertThrows(GameOver.class, () -> this.client.makeMove(this.sessionId, moveRequest));
    }
  }

  /**
   * Test the making of a move if the move request is invalid.
   */

  @Test
  void makeMoveTestFailInvalidMove() throws IOException, InterruptedException {
    try (InputStreamReader reader = new InputStreamReader(
            getClass().getResourceAsStream("simpleTestMapTemplate.json"))) {
      MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);
      GameSessionResponse response = this.client.createGameSession(template);
      this.sessionId = response.getId();

      // Team 1 joins the game
      JoinGameRequest joinRequestTeam1 = new JoinGameRequest();
      joinRequestTeam1.setTeamId("someTeam");
      JoinGameResponse joinResponseTeam1 = this.client.joinGame(this.sessionId, joinRequestTeam1);

      // Team 2 joins the game
      JoinGameRequest joinRequestTeam2 = new JoinGameRequest();
      joinRequestTeam2.setTeamId("someOtherTeam");
      JoinGameResponse joinResponseTeam2 = this.client.joinGame(this.sessionId, joinRequestTeam2);

      // Get the current game state
      GameState gameState = this.client.getGameState(this.sessionId);
      Piece piece = Arrays.stream(gameState.getTeams())
              .filter(team -> team.getId().equals(joinResponseTeam1.getTeamId()))
              .flatMap(team -> Arrays.stream(team.getPieces()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("No pieces found for the joined team"));

      MoveRequest moveRequest = new MoveRequest();
      moveRequest.setTeamId(joinResponseTeam1.getTeamId());
      moveRequest.setTeamSecret(joinResponseTeam1.getTeamSecret());
      moveRequest.setPieceId(piece.getId());
      moveRequest.setNewPosition(new int[]{piece.getPosition()[0] - 1, piece.getPosition()[1]});

      // Assuming this position is illegal
      assertThrows(InvalidMove.class, () -> this.client.makeMove(this.sessionId, moveRequest));
    }
  }
}