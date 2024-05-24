package de.unimannheim.swt.pse.ctf.game.PiecePlacement;

import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

import java.util.Arrays;

/**
 * This class is being used to represents the possible piece placements on the map
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 02.05.2024
 */
public class PiecePlacement {

    /* the initial grid (game board) as a two-dimensional list (array)
     * P.S. see class GameState.java
     */
    private final String[][] grid;

    // the constants for the PiecePlacement class
    private final Const constants;

    // the teams playing the game (2 or 4)
    private final Team[] teams;

    // the maximum amount of pieces that can be placed on the teams half of the map
    private final int maxPieces;

    // initialize an instance of the PlacementHelperMethods class
    private final PlacementHelperMethods helperMethods = new PlacementHelperMethods();

    /**
     * Constructor for the PiecePlacement class
     *
     * @param template {@link MapTemplate} the game specific configuration
     * @param teams {@link Team[]} the teams playing the game
     */
    public PiecePlacement(MapTemplate template, String[][] grid, Team[] teams) {
        this.constants = new Const(template);
        // the initial grid (game board) as a two-dimensional list (array)
        this.grid =  grid;

        /* Fill the new grid with empty strings
         * It is expected that the grid would be just initialized and wouldn't contain any data
         * The grid is filled with empty strings to prevent null pointer exception
         */
        clearGrid(this.grid);

        // the teams playing the game
        this.teams = teams;

        // Exception handling for the case of too few/many teams
        if (this.teams.length < 2 || this.teams.length > 4) {
            throw new IllegalArgumentException("The number of teams must be between 2 and 4");
        }

        /* get the maximum amount of pieces that can be placed on the teams half of the map
         * Size of the grid is nxn, there should be at least 2-4 bases and  2-4 borders on the map for each team
         * Prioritization of the arrangement of the square objects: base > pieces > block
         * P.S. see in Const.java
         */
        if (this.teams.length == 3) {
            this.maxPieces = (this.grid.length * this.grid[0].length) / this.teams.length - 1 - this.constants.getBaseNumber() - this.constants.getMinBordersNumber();
        } else {
            this.maxPieces = (this.grid.length * this.grid[0].length) / this.teams.length - this.constants.getBaseNumber() - this.constants.getMinBordersNumber();
        }
    }

    /**
     * This method is being used to define the placement type for placing the pieces on the map
     *
     * @param pieces        {@link Piece[]} the list (array) of pieces to be placed on the map
     * @param placementType {@link PlacementType} one of the possible piece placements on the map (symmetrical, spaced_out, defensive)
     */
    public void placePieces(Piece[] pieces, PlacementType placementType) {
        if (pieces.length > this.maxPieces) {
            //throw new IllegalArgumentException("Too many pieces to place on the map.");
            System.out.println("Too many pieces to place on the map.");
        }

        // Warning if the grid size is too small for the placement type
        if (this.grid.length < 6) {
            //throw new IllegalArgumentException("The grid size is too small for the placement type, pieces will be placed randomly.");
            System.out.println("The grid size is too small for the placement type, pieces will be placed randomly.");
        }

        /* place pieces on the map based on the size of the grid & placement type
         * For the grid size of 2x2-5x5, the placement type doesn't matter, the pieces will be placed randomly
         * The grid size of 2x2-3x3 are special cases, where no block will be placed in order to fulfill the
         * functionality
         * For the grid size of 6x6-10x10, the placement type will be considered
         */
        if (this.grid.length < 4) {
            place2x2(pieces, this.teams);
        } else if (this.grid.length < 6) {
            placeRandomly(pieces, this.teams);
        } else {
            switch (placementType) {
                case symmetrical:
                    // place piece symmetrically
                    placeSymmetrically(pieces, this.teams);
                    break;
                case spaced_out:
                    // place pieces spaced out
                    placeSpacedOut(pieces, this.teams);
                    break;
                case defensive:
                    // place pieces defensively
                    placeDefensively(pieces, this.teams);
                    break;
                default:
                    // no fulfillment of the functionality
                    // might be possible to use 'placeRandomly(pieces, this.teams)'
                    break;
            }
        }

        // place the blocks on the map
        this.helperMethods.placeBlock(this.grid, constants);

        // update external pieces' positions
        updatePiecePosition();
        // update external bases' positions
        updateBasePosition();
    }

    /**
     * This method is being used to handle the special placement of the pieces on the 2x2 map
     *
     * @param pieces {@link Piece[]} the list (array) of pieces to be placed on the map
     * @param teams  {@link Team[]} the teams playing the game
     */
    public void place2x2(Piece[] pieces, Team[] teams) {
        if (teams.length == 2) {
            // place the teams' bases in the center of each half of the grid
            this.grid[this.grid.length - 1][1] = "b:" + teams[0].getId();
            this.grid[0][1] = "b:" + teams[1].getId();

            // place 2 pieces for each team
            this.grid[this.grid.length - 1][0] = assignPiece(pieces[0].getId(), teams[0]);
            this.grid[0][0] = assignPiece(pieces[0].getId(), teams[1]);
            if (this.grid.length > 2) {
                this.grid[this.grid.length - 1][grid[0].length - 1] = assignPiece(pieces[1].getId(), teams[0]);
                this.grid[0][grid[0].length - 1] = assignPiece(pieces[1].getId(), teams[1]);
            }
        } else if (teams.length == 3) {
            // place the teams' bases in the center of each quarter of the grid
            this.grid[this.grid.length - 1][0] = "b:" + teams[0].getId();
            this.grid[0][0] = "b:" + teams[1].getId();
            this.grid[this.grid.length - 2][grid[0].length - 1] = "b:" + teams[2].getId();

            // place 1 piece for each team
            if (this.grid.length > 2) {
                this.grid[this.grid.length - 1][1] = assignPiece(pieces[0].getId(), teams[0]);
                this.grid[0][1] = assignPiece(pieces[0].getId(), teams[1]);
                this.grid[this.grid.length - 2][grid[0].length - 2] = assignPiece(pieces[0].getId(), teams[2]);
            } else {
                // Warning about no functionality for the grid size of 2x2 and 3 teams
                //throw new IllegalArgumentException("There is no functionality for the grid size of 2x2 and 3 teams.");
                System.out.println("There is no functionality for the grid size of 2x2 and 3 teams.");
            }
        } else if (teams.length == 4) {
            // place the teams' bases in the center of each quarter of the grid
            this.grid[this.grid.length - 1][0] = "b:" + teams[0].getId();
            this.grid[0][0] = "b:" + teams[1].getId();
            this.grid[0][grid[0].length - 1] = "b:" + teams[2].getId();
            this.grid[this.grid.length - 1][grid[0].length - 1] = "b:" + teams[3].getId();

            // place 1 piece for each team
            if (this.grid.length > 2) {
                this.grid[this.grid.length - 2][0] = assignPiece(pieces[0].getId(), teams[0]);
                this.grid[0][1] = assignPiece(pieces[0].getId(), teams[1]);
                this.grid[this.grid.length - 2][grid[0].length - 1] = assignPiece(pieces[0].getId(), teams[2]);
                this.grid[this.grid.length - 1][grid[0].length - 2] = assignPiece(pieces[0].getId(), teams[3]);
            } else {
                // Warning about no functionality for the grid size of 2x2 and 4 teams
                //throw new IllegalArgumentException("There is no functionality for the grid size of 2x2 and 4 teams.");
                System.out.println("There is no functionality for the grid size of 2x2 and 4 teams.");
            }
        }
    }

    /**
     * This method is being used to handle the special placement of the pieces on the < 6x6 map
     *
     * @param pieces {@link Piece[]} the list (array) of pieces to be placed on the map
     * @param teams  {@link Team[]} the teams playing the game
     */
    public void placeRandomly(Piece[] pieces, Team[] teams) {
        // Filling the grid for the case of 2-4 teams
        if (teams.length == 2) {
            // halfGrid as a half of the map for 2 teams
            String[][] halfGrid = halfGridSizeTwo();
            clearGrid(halfGrid);

            // placement of the pieces in the halfGrid
            helperMethods.arrangeRandomly(halfGrid, pieces, teams, this.maxPieces);

            // implement the full grid, which has the upper side as the halfGrid mirrored
            mirrorGrid2(halfGrid, this.grid, teams);
        } else if (teams.length == 3 || teams.length == 4) {
            // halfGrid as a quarter of the map for 3-4 teams
            String[][] halfGrid = halfGridSizeFour();
            clearGrid(halfGrid);

            // placement of the pieces in the halfGrid
            helperMethods.arrangeRandomly(halfGrid, pieces, teams, this.maxPieces);

            // mirror the halfGrid to the full grid for each teams' amount
            if (teams.length == 3) {
                // implement the full grid, which has the upper side as the halfGrid mirrored
                mirrorGrid3(halfGrid, this.grid, teams);
            } else {
                // implement the full grid, which has the upper side as the halfGrid mirrored
                mirrorGrid4(halfGrid, this.grid, teams);
            }
        }
    }

    /**
     * This method is being used to handle the symmetrical placement of the pieces on the map
     *
     * @param pieces {@link Piece[]} the list (array) of pieces to be placed on the map
     * @param teams  {@link Team[]} the teams playing the game
     */
    public void placeSymmetrically(Piece[] pieces, Team[] teams) {
        /* Array for positions of the pieces in the halfGrid
         * The size of the array is limited by the number of pieces, which were passed to the method or the
         * maximum amount of pieces that can be placed on the teams half of the map
         */
        Position[] positions = ((pieces.length < this.maxPieces) ? new Position[pieces.length] : new Position[this.maxPieces]);

        // Filling the grid for the case of 2-4 teams
        if (teams.length == 2) {
            // halfGrid as a half of the map for 2 teams
            String[][] halfGrid = halfGridSizeTwo();
            clearGrid(halfGrid);

            // placement of the pieces in the halfGrid
            helperMethods.arrangeInArcs(halfGrid, pieces, positions);

            // fill the halfGrid with the team's base, pieces and blocks
            fillHalfGrid(halfGrid, teams[0], positions);

            // implement the full grid, which has the upper side as the halfGrid mirrored
            mirrorGrid2(halfGrid, this.grid, teams);
        } else if (teams.length == 3 || teams.length == 4) {
            // halfGrid as a quarter of the map for 3 and 4 teams
            String[][] halfGrid = halfGridSizeFour();
            clearGrid(halfGrid);

            //placement of the pieces in the halfGrid
            helperMethods.arrangeInArcs(halfGrid, pieces, positions);

            // fill the halfGrid with the team's base, pieces and blocks
            fillHalfGrid(halfGrid, teams[0], positions);

            // implement the full grid, which has the upper side as the halfGrid mirrored
            if (teams.length == 3) {
                mirrorGrid3(halfGrid, this.grid, teams);
            } else {
                mirrorGrid4(halfGrid, this.grid, teams);
            }
        }
    }

    /**
     * This method is being used to handle the spaced out placement of the pieces on the map
     *
     * @param pieces {@link Piece[]} the list (array) of pieces to be placed on the map
     * @param teams  {@link Team[]} the teams playing the game
     */
    public void placeSpacedOut(Piece[] pieces, Team[] teams) {
        /* Array for positions of the pieces in the halfGrid
         * The size of the array is limited by the number of pieces, which were passed to the method or the
         * maximum amount of pieces that can be placed on the teams half of the map
         */
        Position[] positions = ((pieces.length < this.maxPieces) ? new Position[pieces.length] : new Position[this.maxPieces]);

        // Filling the grid for the case of 2-4 teams
        if (teams.length == 2) {
            // halfGrid as a half of the map for 2 teams
            String[][] halfGrid = halfGridSizeTwo();
            clearGrid(halfGrid);

            // placement of the pieces in the halfGrid
            helperMethods.arrangeAlternating(halfGrid, pieces, positions);

            // fill the halfGrid with the team's base, pieces and blocks
            fillHalfGrid(halfGrid, teams[0], positions);

            // implement the full grid, which has the upper side as the halfGrid mirrored
            mirrorGrid2(halfGrid, this.grid, teams);
        } else if (teams.length == 3 || teams.length == 4) {
            // halfGrid as a quarter of the map for 4 teams
            String[][] halfGrid = halfGridSizeFour();
            clearGrid(halfGrid);

            // placement of the pieces in the halfGrid
            helperMethods.arrangeAlternating(halfGrid, pieces, positions);

            // fill the halfGrid with the team's base, pieces and blocks
            fillHalfGrid(halfGrid, teams[0], positions);

            // implement the full grid, which has the upper side as the halfGrid mirrored
            if (teams.length == 3) {
                mirrorGrid3(halfGrid, this.grid, teams);
            } else {
                mirrorGrid4(halfGrid, this.grid, teams);
            }
        }
    }

    /**
     * This method is being used to handle the defensive placement of the pieces on the map
     *
     * @param pieces {@link Piece[]} the list (array) of pieces to be placed on the map
     * @param teams  {@link Team[]} the teams playing the game
     */
    public void placeDefensively(Piece[] pieces, Team[] teams) {
        /* Array for positions of the pieces in the halfGrid
         * The size of the array is limited by the number of pieces, which were passed to the method or the
         * maximum amount of pieces that can be placed on the teams half of the map
         */
        Position[] positions = ((pieces.length < this.maxPieces) ? new Position[pieces.length] : new Position[this.maxPieces]);

        // Filling the grid for the case of 2-4 teams
        if (teams.length == 2) {
            // halfGrid as a half of the map for 2 teams
            String[][] halfGrid = halfGridSizeTwo();
            clearGrid(halfGrid);

            // placement of the pieces in the halfGrid
            helperMethods.arrangeInDiamonds(halfGrid, pieces, positions);

            // fill the halfGrid with the team's base, pieces and blocks
            fillHalfGrid(halfGrid, teams[0], positions);

            // implement the full grid, which has the upper side as the halfGrid mirrored
            mirrorGrid2(halfGrid, this.grid, teams);
        } else if (teams.length == 3 || teams.length == 4) {
            // halfGrid as a quarter of the map for 4 teams
            String[][] halfGrid = halfGridSizeFour();
            clearGrid(halfGrid);

            // placement of the pieces in the halfGrid
            helperMethods.arrangeInDiamonds(halfGrid, pieces, positions);

            // fill the halfGrid with the team's base, pieces and blocks
            fillHalfGrid(halfGrid, teams[0], positions);

            // implement the full grid, which has the upper side as the halfGrid mirrored
            if (teams.length == 3) {
                mirrorGrid3(halfGrid, this.grid, teams);
            } else {
                mirrorGrid4(halfGrid, this.grid, teams);
            }
        }
    }

    /**
     * This method is being used to update the position of each piece (as for a class Piece)
     * P.S. see class Piece.java
     */
    private void updatePiecePosition() {
        for (int i = 0; i < this.grid.length; i++) {
            for (int j = 0; j < this.grid[0].length; j++) {
                if (this.grid[i][j].contains("p:")) {
                    String[] piece = this.grid[i][j].split("_");
                    for (Team team : this.teams) {
                        if (team.getId().equals(piece[0].substring(2))) {
                            for (Piece p : team.getPieces()) {
                                if (p.getId().equals(piece[1])) {
                                    p.setPosition(new int[]{i, j});
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is being used to update the position of each base (as for a class Team)
     * P.S. see class Team.java
     */
    private void updateBasePosition() {
        for (int i = 0; i < this.grid.length; i++) {
            for (int j = 0; j < this.grid[0].length; j++) {
                if (this.grid[i][j].contains("b:")) {
                    String[] base = this.grid[i][j].split(":");
                    for (Team team : this.teams) {
                        if (team.getId().equals(base[1])) {
                            team.setBase(new int[]{i, j});
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is being used to assign a piece to a team in the format p:teamId_pieceId
     *
     * @param pieceId {@link String} the id of the piece
     * @param team    {@link Team} the team to which the piece should be assigned
     * @return String the formatted string with the team id and the piece id
     */
    private String assignPiece(String pieceId, Team team) {
        return "p:" + team.getId() + "_" + pieceId;
    }

    /**
     * This method is being used to fill the grid with empty strings
     *
     * @param grid {@link String[][]} the grid to be cleared
     */
    private void clearGrid(String[][] grid) {
        for (String[] strings : grid) {
            Arrays.fill(strings, "");
        }
    }

    /**
     * This method is being used to create a half of the grid for 2 teams
     *
     * @return String[][] the half of the grid
     */
    public String[][] halfGridSizeTwo() {
        if (this.grid.length % 2 != 0) {
            return new String[(this.grid.length - 1) / 2][this.grid[0].length - 1];
        }
        else {
            return new String[this.grid.length / 2][this.grid[0].length];
        }
    }

    /**
     * This method is being used to create a quarter of the grid for 3-4 teams
     *
     * @return String[][] the half of the grid
     */
    public String[][] halfGridSizeFour() {
        if (this.grid.length % 2 != 0) {
            return new String[(this.grid.length - 1) / 2][(this.grid[0].length - 1) / 2];
        }
        else {
            return new String[this.grid.length / 2][this.grid[0].length / 2];
        }
    }

    /**
     * Fills halfGrid with the team's base, pieces and blocks
     *
     * @param halfGrid  {@link String[][]} the half of the grid to be filled
     * @param team      {@link Team} the team to which the pieces belong
     * @param positions {@link Position[]} the positions of the pieces
     */
    private void fillHalfGrid(String[][] halfGrid, Team team, Position[] positions) {
        // place the team's base in the center of the halfGrid
        halfGrid[halfGrid.length / 2][halfGrid[0].length / 2] = "b:" + teams[0].getId();

        // place pieces for the team around the base
        for (Position position : positions) {
            if (position != null && halfGrid[position.getRowIndex()][position.getColumnIndex()].isEmpty()) {
                halfGrid[position.getRowIndex()][position.getColumnIndex()] = assignPiece(position.getPieceId(), team);
            }
        }
    }

    /**
     * Mirrors halfGrid to grid for 2 Teams
     *
     * @param halfGrid {@link String[][]} the half of the grid to be mirrored
     * @param grid     {@link String[][]} the full grid
     * @param teams    {@link Team[]} the teams playing the game
     */
    public void mirrorGrid2(String[][] halfGrid, String[][] grid, Team[] teams) {
        for (int i = 0; i < halfGrid.length; i++) {
            for (int j = 0; j < halfGrid[0].length; j++) {
                grid[grid.length - 1 - i][j] = halfGrid[i][j];

                if (halfGrid[i][j].startsWith("b:")) {
                    grid[i][j] = halfGrid[i][j].replace(teams[0].getId(), teams[1].getId());
                } else if (halfGrid[i][j].contains("p:")) {
                    grid[i][j] = halfGrid[i][j].replace("p:" + teams[0].getId() + "_", "p:" + teams[1].getId() + "_");
                } else {
                    grid[i][j] = halfGrid[i][j];
                }
            }
        }
    }

    /**
     * Mirrors halfGrid to grid for 3 Teams
     *
     * @param halfGrid {@link String[][]} the half of the grid to be mirrored
     * @param grid     {@link String[][]} the full grid
     * @param teams    {@link Team[]} the teams playing the game
     */
    public void mirrorGrid3(String[][] halfGrid, String[][] grid, Team[] teams) {
        // temporary array for the 3rd team
        String[][] thirdTeamArray = new String[halfGrid.length][halfGrid[0].length];

        // rotate the thirdTeamArray at 90 degrees left
        for (int i = 0; i < halfGrid.length; i++) {
            for (int j = 0; j < halfGrid[0].length; j++) {
                thirdTeamArray[halfGrid.length - 1 - j][i] = halfGrid[i][j];
            }
        }

        // mirror the halfGrid to the full grid
        for (int i = 0; i < halfGrid.length; i++) {
            for (int j = 0; j < halfGrid[0].length; j++) {
                grid[grid.length - 1 - i][j] = halfGrid[i][j];

                // change the ids for the 2nd team
                if (halfGrid[i][j].startsWith("b:")) {
                    grid[i][j] = halfGrid[i][j].replace(teams[0].getId(), teams[1].getId());
                } else if (halfGrid[i][j].contains("p:")) {
                    grid[i][j] = halfGrid[i][j].replace("p:" + teams[0].getId() + "_", "p:" + teams[1].getId() + "_");
                } else {
                    grid[i][j] = halfGrid[i][j];
                }

                // change the ids for the 3rd team
                if (thirdTeamArray[i][j].startsWith("b:")) {
                    grid[thirdTeamArray.length / 2 + 1 + i][grid.length - 1 - j] = thirdTeamArray[i][j].replace(teams[0].getId(), teams[2].getId());
                } else if (thirdTeamArray[i][j].contains("p:")) {
                    grid[thirdTeamArray.length / 2 + 1 + i][grid.length - 1 - j] = thirdTeamArray[i][j].replace("p:" + teams[0].getId() + "_", "p:" + teams[2].getId() + "_");
                } else {
                    grid[thirdTeamArray.length / 2 + 1 + i][grid.length - 1 - j] = thirdTeamArray[i][j];
                }
            }
        }
    }

    /**
     * Mirrors halfGrid to grid for 4 Teams
     *
     * @param halfGrid {@link String[][]} the half of the grid to be mirrored
     * @param grid     {@link String[][]} the full grid
     * @param teams    {@link Team[]} the teams playing the game
     */
    public void mirrorGrid4(String[][] halfGrid, String[][] grid, Team[] teams) {
        for (int i = 0; i < halfGrid.length; i++) {
            for (int j = 0; j < halfGrid[0].length; j++) {
                grid[grid.length - 1 - i][j] = halfGrid[i][j];
                if (halfGrid[i][j].startsWith("b:")) {
                    grid[i][j] = halfGrid[i][j].replace(teams[0].getId(), teams[1].getId());
                    grid[i][grid.length - 1 - j] = halfGrid[i][j].replace(teams[0].getId(), teams[2].getId());
                    grid[grid.length - 1 - i][grid.length - 1 - j] = halfGrid[i][j].replace(teams[0].getId(), teams[3].getId());
                } else if (halfGrid[i][j].contains("p:")) {
                    grid[i][j] = halfGrid[i][j].replace("p:" + teams[0].getId() + "_", "p:" + teams[1].getId() + "_");
                    grid[i][grid.length - 1 - j] = halfGrid[i][j].replace("p:" + teams[0].getId() + "_", "p:" + teams[2].getId() + "_");
                    grid[grid.length - 1 - i][grid.length - 1 - j] = halfGrid[i][j].replace("p:" + teams[0].getId() + "_", "p:" + teams[3].getId() + "_");
                } else {
                    grid[i][j] = halfGrid[i][j];
                    grid[i][grid.length - 1 - j] = halfGrid[i][j];
                    grid[grid.length - 1 - i][grid.length - 1 - j] = halfGrid[i][j];
                }
            }
        }
    }
}