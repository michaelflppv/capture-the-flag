package de.unimannheim.swt.pse.ctf.controller.data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class represents a move request,
 * which contains the piece ID and the new position for the piece.
 */
public class MoveRequest {

    @Schema(
            description = "unique identifier of the team"
    )
    private String teamId;
    @Schema(
            description = "the team secret which is checked if team is allowed to make a move"
    )
    private String teamSecret;
    @Schema(
            description = "the piece by its unique identifier to move"
    )
    private String pieceId;
    @Schema(
            description = "the new position of the piece"
    )
    private int[] newPosition;

    public MoveRequest() {
        this.pieceId = "";
        this.newPosition = new int[2];
    }

    public String getPieceId() {
        return pieceId;
    }

    public void setPieceId(String pieceId) {
        this.pieceId = pieceId;
    }

    public int[] getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(int[] newPosition) {
        this.newPosition = newPosition;
    }

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

