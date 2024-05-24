package de.sep.cfp4.technicalServices.network;

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

/**
 * Interface for the game API.
 * @version 0.0.1
 * @author dcebulla
 */
public interface GameAPI {

  GameSessionResponse createGameSession(MapTemplate template) throws IOException, InterruptedException;
  GameSessionResponse getGameSession(String sessionId) throws IOException, InterruptedException, GameSessionNotFound;
  GameState getGameState(String sessionId) throws IOException, InterruptedException, GameSessionNotFound;
  void deleteGameSession(String sessionId) throws IOException, InterruptedException, GameSessionNotFound;
  void makeMove(String sessionId, MoveRequest moveRequest) throws IOException, InterruptedException, ForbiddenMove, GameSessionNotFound, InvalidMove, GameOver;
  JoinGameResponse joinGame(String sessionId, JoinGameRequest joinGameRequest) throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots;
  void giveUp(String sessionId, GiveupRequest giveupRequest) throws IOException, InterruptedException, ForbiddenMove, GameSessionNotFound, GameOver;

}
