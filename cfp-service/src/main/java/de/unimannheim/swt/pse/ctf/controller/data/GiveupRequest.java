package de.unimannheim.swt.pse.ctf.controller.data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class represents the request to give up a game.
 */
public class GiveupRequest {
    @Schema(
            description = "unique identifier of the team"
    )
    private String teamId;
    @Schema(
            description = "the team secret which is checked if team is allowed to give up"
    )
    private String teamSecret;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamSecret() {
        return teamSecret;
    }

    public void setTeamSecret(String teamSecret) {
        this.teamSecret = teamSecret;
    }
}

