package de.unimannheim.swt.pse.ctf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unimannheim.swt.pse.ctf.controller.data.*;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.state.GameState;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple system 'live' test for {@link GameSessionController}.
 *
 * @see <a href="https://spring.io/guides/gs/testing-web">Guide</a>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GameSessionControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateGameSession() throws Exception {
        // create a new game session
        GameSessionRequest gameSessionRequest = new GameSessionRequest();
        gameSessionRequest.setTemplate(createGameTemplate());

        GameSessionResponse gameSession = restTemplate.postForObject("http://localhost:" + port + "/api/gamesession",
                gameSessionRequest, GameSessionResponse.class);
        assertNotNull(gameSession.getId());

        String gameSessionId = gameSession.getId();

        // check game session
        gameSession = restTemplate.getForObject("http://localhost:" + port + "/api/gamesession/{gameSessionId}",
                GameSessionResponse.class, gameSessionId);
        assertEquals(gameSessionId, gameSession.getId());

        // check game state
        GameState gameState = restTemplate.getForObject("http://localhost:" + port + "/api/gamesession/{gameSessionId}/state",
                GameState.class, gameSessionId);

        // let two teams join
        JoinGameRequest team1 = new JoinGameRequest();
        team1.setTeamId("team1");
        JoinGameResponse joinGameResponse1 = restTemplate.postForObject("http://localhost:" + port + "/api/gamesession/{gameSessionId}/join",
                team1, JoinGameResponse.class, gameSessionId);
        JoinGameRequest team2 = new JoinGameRequest();
        team2.setTeamId("team2");
        JoinGameResponse joinGameResponse2 = restTemplate.postForObject("http://localhost:" + port + "/api/gamesession/{gameSessionId}/join",
                team2, JoinGameResponse.class, gameSessionId);

        // check game state again
        gameState = restTemplate.getForObject("http://localhost:" + port + "/api/gamesession/{gameSessionId}/state",
                GameState.class, gameSessionId);

        // make some fake move
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setTeamId(joinGameResponse1.getTeamId());
        moveRequest.setTeamSecret(joinGameResponse1.getTeamSecret());
        moveRequest.setPieceId("somepieceid");
        moveRequest.setNewPosition(new int[]{1,2});

        restTemplate.postForObject("http://localhost:" + port + "/api/gamesession/{gameSessionId}/move",
                moveRequest, void.class, gameSessionId);

        // give up
        GiveupRequest giveupRequest = new GiveupRequest();
        giveupRequest.setTeamId(joinGameResponse1.getTeamId());
        giveupRequest.setTeamSecret(joinGameResponse1.getTeamSecret());

        restTemplate.postForObject("http://localhost:" + port + "/api/gamesession/{gameSessionId}/giveup",
                giveupRequest, void.class, gameSessionId);

        // delete game
        restTemplate.delete("http://localhost:" + port + "/api/gamesession/{sessionId}", gameSessionId);
    }

    MapTemplate createGameTemplate() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        MapTemplate mapTemplate = objectMapper.readValue(
                getClass().getResourceAsStream("/maptemplates/10x10_2teams_example.json"), MapTemplate.class);

        return mapTemplate;
    }
}
