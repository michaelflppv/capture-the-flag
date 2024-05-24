package de.sep.cfp4.technicalServices.network;

import com.google.gson.Gson;
import de.unimannheim.swt.pse.ctf.controller.data.GameSessionRequest;
import de.unimannheim.swt.pse.ctf.controller.data.GameSessionResponse;
import de.unimannheim.swt.pse.ctf.controller.data.GiveupRequest;
import de.unimannheim.swt.pse.ctf.controller.data.JoinGameRequest;
import de.unimannheim.swt.pse.ctf.controller.data.JoinGameResponse;
import de.unimannheim.swt.pse.ctf.controller.data.MoveRequest;
import de.unimannheim.swt.pse.ctf.game.exceptions.ForbiddenMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameOver;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.exceptions.InvalidMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * This class is used to represent a client for the frontend to communicate with the backend.
 *
 * @author dcebulla
 * @version 0.0.3
 */
public class GameClient implements GameAPI {

  private final URI SERVER_URI;
  private final Gson gson;

  public GameClient(URI serverUrl) {
    this.SERVER_URI = serverUrl;
    this.gson = new Gson();
  }

  /**
   * Create a new game session.
   *
   * @param template the map to use for a new game session
   * @return the game session response, with the game id and the date the game started
   * @throws IOException          if an I/O error occurs while sending or receiving the request
   * @throws InterruptedException if the communication with the server is interrupted
   */
  public GameSessionResponse createGameSession(MapTemplate template)
      throws IOException, InterruptedException {
    GameSessionRequest request = new GameSessionRequest();
    request.setTemplate(template);

    String jsonRequest = this.gson.toJson(request);
    System.out.println(jsonRequest);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(this.SERVER_URI.resolve("/api/gamesession"))
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(jsonRequest))
        .build();
    return this.gson.fromJson(this.makeRequest(httpRequest).body(), GameSessionResponse.class);
  }


  /**
   * Get existing game session.
   *
   * @param sessionId the id of the game session
   * @return the game session response, with the game id and the date the game started
   * @throws IOException if no connection to the server can be established
   * @throws InterruptedException if method is interrupted during communication with the server
   * @throws GameSessionNotFound if the game session with the given id does not exist
   */
  public GameSessionResponse getGameSession(String sessionId) throws IOException, InterruptedException, GameSessionNotFound {
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(this.SERVER_URI.resolve("/api/gamesession/" + sessionId))
        .header("Content-Type", "application/json")
        .GET()
        .build();
    return this.gson.fromJson(this.makeRequest(httpRequest).body(), GameSessionResponse.class);
  }


  /**
   * Get the current game state.
   *
   * @param sessionId the id of the game session
   * @return the game state
   * @throws IOException if no connection to the server can be established
   * @throws InterruptedException if method is interrupted during communication with the server
   * @throws GameSessionNotFound if the game session with the given id does not exist
   */
  public GameState getGameState(String sessionId) throws IOException, InterruptedException, GameSessionNotFound {
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(this.SERVER_URI.resolve("/api/gamesession/" + sessionId + "/state"))
        .header("accept", "*/*")
        .GET()
        .build();
    return this.gson.fromJson(this.makeRequest(httpRequest).body(), GameState.class);
  }


  /**
   * Delete a game session.
   *
   * @param sessionId the id of the game session
   * @throws IOException if no connection to the server can be established
   * @throws InterruptedException if method is interrupted during communication with the server
   * @throws GameSessionNotFound if the game session with the given id does not exist
   */
  public void deleteGameSession(String sessionId) throws IOException, InterruptedException, GameSessionNotFound {
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(this.SERVER_URI.resolve("/api/gamesession/" + sessionId))
        .header("Content-Type", "application/json")
        .DELETE()
        .build();
    this.makeRequest(httpRequest);
  }


  /**
   * Make a move in the game.
   *
   * @param sessionId the id of the game session
   * @param moveRequest the move to make
   * @throws IOException if no connection to the server can be established
   * @throws InterruptedException if method is interrupted during communication with the server
   * @throws ForbiddenMove if the move is forbidden for the current team
   * @throws GameSessionNotFound if the game session with the given id does not exist
   * @throws InvalidMove if the move request is invalid
   * @throws GameOver if the game session has already ended
   */
  public void makeMove(String sessionId, MoveRequest moveRequest) throws IOException, InterruptedException,ForbiddenMove, GameSessionNotFound, InvalidMove, GameOver {
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(this.SERVER_URI.resolve("/api/gamesession/" + sessionId + "/move"))
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(this.gson.toJson(moveRequest)))
        .build();
    this.makeRequest(httpRequest);
  }

  /**
   * Join a game session.
   *
   * @param sessionId  the id of the game session
   * @param joinGameRequest join request from a team
   * @return join game response with the game session id, team secret, team id and team color
   * @throws IOException if no connection to the server can be established
   * @throws InterruptedException if method is interrupted during communication with the server
   * @throws GameSessionNotFound if the game session with the given id does not exist
   * @throws NoMoreTeamSlots if there are no more team slots available
   */
  public JoinGameResponse joinGame(String sessionId, JoinGameRequest joinGameRequest) throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots {
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(this.SERVER_URI.resolve("/api/gamesession/" + sessionId + "/join"))
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(this.gson.toJson(joinGameRequest)))
        .build();
    return this.gson.fromJson(this.makeRequest(httpRequest).body(), JoinGameResponse.class);
  }


  /**
   * Give up a specific game session.
   *
   * @param sessionId the id of the game session
   * @param giveupRequest give up request from a team with team id and team secret
   * @throws IOException if no connection to the server can be established
   * @throws InterruptedException if method is interrupted during communication with the server
   * @throws ForbiddenMove if the move is forbidden for the current team
   * @throws GameSessionNotFound if the game session with the given id does not exist
   * @throws GameOver if the game session has already ended
   */
  public void giveUp(String sessionId, GiveupRequest giveupRequest) throws IOException, InterruptedException, ForbiddenMove, GameSessionNotFound, GameOver {
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(this.SERVER_URI.resolve("/api/gamesession/" + sessionId + "/giveup"))
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(this.gson.toJson(giveupRequest)))
        .build();
    this.gson.fromJson(this.makeRequest(httpRequest).body(), JoinGameResponse.class);
  }


  /**
   * Auxiliary method to make a http request to the server and handle the response.
   *
   * @param request the http request to send to the server
   * @return the http response from the server
   * @throws IOException if an I/O error occurs while sending or receiving the request e.g. no connection to the server
   * @throws InterruptedException if method is interrupted during communication with the server
   */

  public HttpResponse<String> makeRequest(HttpRequest request) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

    return switch (response.statusCode()) {
      case 200 -> {
        //System.out.println("Request successful");
        yield response;
      }
      case 403 -> {
        System.out.println("Move is forbidden for given team (anti-cheat)");
        throw new ForbiddenMove();
      }
      case 404 -> {
        System.out.println("Game session not found");
        throw new GameSessionNotFound();
      }
      case 409 -> {
        System.out.println("Move is invalid");
        throw new InvalidMove();
      }
      case 410 -> {
        System.out.println("Game session has ended");
        throw new GameOver();
      }
      case 429 -> {
        System.out.println("No more team slots available");
        throw new NoMoreTeamSlots();
      }
      case 500 -> throw new RuntimeException("Unknown error occurred");
      default ->
          throw new RuntimeException("Unexpected HTTP status code: " + response.statusCode());
    };

  }

  /**
   * Method to test functionality of the client during development.
   */
  public static void main(String[] args) {

  }


}
