package de.unimannheim.swt.pse.ctf.game.map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class represents a game specific configuration.
 */
public class MapTemplate {

    @Schema(
            description = "grid size. format [rows, columns]",
            example = "[10, 10]"
    )
    private int[] gridSize;
    @Schema(
            description = "number of teams (i.e., players)",
            example = "2"
    )
    private int teams;
    @Schema(
            description = "number of flags located in each team's base",
            example = "1"
    )
    private int flags;
    @Schema(
            description = "list of pieces configured in a game"
    )
    private PieceDescription[] pieces;
    @Schema(
            description = "number of blocks placed randomly (squares occupied by a block cannot be occupied by a piece)"
    )
    private int blocks;
    @Schema(
            description = "placement strategy for pieces",
            example = "symmetrical, spaced_out or defensive (see Enum 'PlacementType' for more details)"
    )
    private PlacementType placement;
    @Schema(
            description = "total game time limit in seconds - after time limit, a winner is determined  (-1 if none)"
    )
    private int totalTimeLimitInSeconds;
    @Schema(
            description = "time limit for moves in seconds - after time limit, a move request by the current team is disregarded (-1 if none)"
    )
    private int moveTimeLimitInSeconds;

    public PieceDescription[] getPieces() {
        return pieces;
    }

    public void setPieces(PieceDescription[] pieces) {
        this.pieces = pieces;
    }

    public PlacementType getPlacement() {
        return placement;
    }

    public void setPlacement(PlacementType placement) {
        this.placement = placement;
    }

    public int[] getGridSize() {
        return gridSize;
    }

    public void setGridSize(int[] gridSize) {
        this.gridSize = gridSize;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getBlocks() {
        return blocks;
    }

    public void setBlocks(int blocks) {
        this.blocks = blocks;
    }

    public int getTeams() {
        return teams;
    }

    public void setTeams(int teams) {
        this.teams = teams;
    }

    public int getTotalTimeLimitInSeconds() {
        return totalTimeLimitInSeconds;
    }

    public void setTotalTimeLimitInSeconds(int totalTimeLimitInSeconds) {
        this.totalTimeLimitInSeconds = totalTimeLimitInSeconds;
    }

    public int getMoveTimeLimitInSeconds() {
        return moveTimeLimitInSeconds;
    }

    public void setMoveTimeLimitInSeconds(int moveTimeLimitInSeconds) {
        this.moveTimeLimitInSeconds = moveTimeLimitInSeconds;
    }
}
