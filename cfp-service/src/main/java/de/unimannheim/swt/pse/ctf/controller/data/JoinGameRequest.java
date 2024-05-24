package de.unimannheim.swt.pse.ctf.controller.data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class represents the request to let a team join a game session.
 */
public class JoinGameRequest {
    @Schema(
            description = "proposed unique team identifier (i.e., name)"
    )
    private String teamId;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}

