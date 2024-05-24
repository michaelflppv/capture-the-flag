package de.unimannheim.swt.pse.ctf.game.PiecePlacement;

import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

import java.util.Random;

/**
 * This class is being used to store the helper methods for the placement of the pieces in the grid.
 *
 * @author mfilippo
 * @version 15.04.2024
 */
public class PlacementHelperMethods {

    /**
     * This method is used to arrange the pieces in the grid in a random way.
     *
     * @param halfGrid  {@link String[][]} the half of the grid to be filled
     * @param pieces    {@link Piece[]} the pieces to be placed in the grid
     * @param teams     {@link Team[]} the teams to which the pieces belong
     * @param maxPieces {@link int} the maximum number of pieces to be placed in the grid
     */
    public void arrangeRandomly(String[][] halfGrid, Piece[] pieces, Team[] teams, int maxPieces) {
        // place the team's base in the center of the halfGrid
        halfGrid[halfGrid.length / 2 - 1][halfGrid[0].length / 2] = "b:" + teams[0].getId();

        // random placement of the pieces in the halfGrid
        Random random = new Random();
        for (int i = 0; i < pieces.length && i < maxPieces; i++) {
            int randomRow = random.nextInt(halfGrid.length);
            int randomCol = random.nextInt(halfGrid[0].length);

            int totalAttempts = 0;
            while (!halfGrid[randomRow][randomCol].isEmpty()) {
                randomRow = random.nextInt(halfGrid.length);
                randomCol = random.nextInt(halfGrid[0].length);
                totalAttempts++;

                if (totalAttempts > halfGrid.length * halfGrid[0].length) {
                    break;
                }
            }

            halfGrid[randomRow][randomCol] = "p:" + teams[0].getId() + "_" + pieces[i].getId();
        }
    }

    /**
     * This method is used to arrange the pieces in the grid in the form of an arcs.
     *
     * @param halfGrid  {@link String[][]} the half of the grid to be filled
     * @param pieces    {@link Piece[]} the pieces to be placed in the grid
     * @param positions {@link Position[]} the positions of the pieces in the grid
     */
    public void arrangeInArcs(String[][] halfGrid, Piece[] pieces, Position[] positions) {
        int piecesCount = 0;
        int centerRow = halfGrid.length / 2;
        int centerCol = halfGrid[0].length / 2;

        // 1st arc around the base
        if (piecesCount < positions.length) {
            positions[piecesCount] = new Position(centerRow - 1, centerCol, pieces[piecesCount].getId());
            piecesCount++;
        }
        for (int i = 0; i < halfGrid.length - centerRow && i < halfGrid[0].length - centerCol - 1; i++) {
            if (piecesCount < positions.length) {
                positions[piecesCount] = new Position(centerRow + i, centerCol - 1 - i, pieces[piecesCount].getId());
                piecesCount++;
            }
            if (piecesCount < positions.length) {
                positions[piecesCount] = new Position(centerRow + i, centerCol + 1 + i, pieces[piecesCount].getId());
                piecesCount++;
            }
        }

        // 2nd arc around the base
        if (piecesCount < positions.length && centerRow - 2 >= 0) {
            positions[piecesCount] = new Position(centerRow - 2, centerCol, pieces[piecesCount].getId());
            piecesCount++;
        }
        for (int i = 0; i < halfGrid.length - centerRow + 1 && i < halfGrid[0].length - centerCol - 1; i++) {
            if (piecesCount < positions.length) {
                positions[piecesCount] = new Position(centerRow - 1 + i, centerCol - 1 - i, pieces[piecesCount].getId());
                piecesCount++;
            }
            if (piecesCount < positions.length) {
                positions[piecesCount] = new Position(centerRow - 1 + i, centerCol + 1 + i, pieces[piecesCount].getId());
                piecesCount++;
            }
        }

        // 3rd arc around the base
        for (int i = 0; i < halfGrid.length - centerRow + 2 && i < halfGrid[0].length - centerCol - 1; i++) {
            if (piecesCount < positions.length && centerRow - 2 + i >= 0) {
                positions[piecesCount] = new Position(centerRow - 2 + i, centerCol - 1 - i, pieces[piecesCount].getId());
                piecesCount++;
            }
            if (piecesCount < positions.length && centerRow - 2 + i >= 0) {
                positions[piecesCount] = new Position(centerRow - 2 + i, centerCol + 1 + i, pieces[piecesCount].getId());
                piecesCount++;
            }
        }

        // 4th arc around the base
        for (int i = 0; i < halfGrid.length - centerRow + 3 && i < halfGrid[0].length - centerCol - 2; i++) {
            if (piecesCount < positions.length && centerRow - 2 + i >= 0) {
                positions[piecesCount] = new Position(centerRow - 2 + i, centerCol - 2 - i, pieces[piecesCount].getId());
                piecesCount++;
            }
            if (piecesCount < positions.length && centerRow - 2 + i >= 0) {
                positions[piecesCount] = new Position(centerRow - 2 + i, centerCol + 2 + i, pieces[piecesCount].getId());
                piecesCount++;
            }
        }
    }

    /**
     * This method is used to arrange the pieces in the grid alternating.
     *
     * @param halfGrid  {@link String[][]} the half of the grid to be filled
     * @param pieces    {@link Piece[]} the pieces to be placed in the grid
     * @param positions {@link Position[]} the positions of the pieces in the grid
     */
    public void arrangeAlternating(String[][] halfGrid, Piece[] pieces, Position[] positions) {
        int piecesCount = 0;

        for (int i = halfGrid.length - 1; i >= 0; i--) {
            // 1st filling of alternating pieces
            if (i % 2 != 0) {
                for (int j = 0; j < halfGrid[0].length; j += 2) {
                    if (piecesCount < positions.length && halfGrid[i][j].isEmpty()) {
                        positions[piecesCount] = new Position(i, j, pieces[piecesCount].getId());
                        piecesCount++;
                    }
                }
            }
            // 2nd filling of alternating pieces
            else {
                for (int j = 1; j < halfGrid[0].length; j += 2) {
                    if (piecesCount < positions.length && halfGrid[i][j].isEmpty()) {
                        positions[piecesCount] = new Position(i, j, pieces[piecesCount].getId());
                        piecesCount++;
                    }
                }
            }
        }
    }

    /**
     * This method is used to arrange the pieces in the grid in the form of diamonds.
     *
     * @param halfGrid  {@link String[][]} the half of the grid to be filled
     * @param pieces    {@link Piece[]} the pieces to be placed in the grid
     * @param positions {@link Position[]} the positions of the pieces in the grid
     */
    public void arrangeInDiamonds(String[][] halfGrid, Piece[] pieces, Position[] positions) {
        int piecesCount = 0;
        int centerRow = halfGrid.length / 2;
        int centerCol = halfGrid[0].length / 2;

        // 1st shape of a diamond around the base
        // vertical angles of the diamond
        if (piecesCount < positions.length) {
            positions[piecesCount] = new Position(0, centerCol, pieces[piecesCount].getId());
            piecesCount++;
        }
        if (piecesCount < positions.length) {
            positions[piecesCount] = new Position(halfGrid.length - 1, centerCol, pieces[piecesCount].getId());
            piecesCount++;
        }

        for (int i = 1; i <= centerRow && i <= (halfGrid.length - 1) / 2; i++) {
            if (piecesCount < positions.length && centerCol - i >= 0) {
                positions[piecesCount] = new Position(i, centerCol - i, pieces[piecesCount].getId());
                piecesCount++;
            }
            if (piecesCount < positions.length && centerCol + i < halfGrid[0].length) {
                positions[piecesCount] = new Position(i, centerCol + i, pieces[piecesCount].getId());
                piecesCount++;
            }
            if (piecesCount < positions.length && centerCol - i >= 0) {
                positions[piecesCount] = new Position(halfGrid.length - 1 - i, centerCol - i, pieces[piecesCount].getId());
                piecesCount++;
            }
            if (piecesCount < positions.length && centerCol + i < halfGrid[0].length) {
                positions[piecesCount] = new Position(halfGrid.length - 1 - i, centerCol + i, pieces[piecesCount].getId());
                piecesCount++;
            }
        }

        piecesCount = piecesCount - 2;

        if (piecesCount >= 0) {
            // 2nd shape of a diamond around the base
            for (int i = 0; i <= centerRow && i <= (halfGrid.length - 1) / 2; i++) {
                if (piecesCount < positions.length && centerCol - 1 - i >= 0) {
                    positions[piecesCount] = new Position(i, centerCol - 1 - i, pieces[piecesCount].getId());
                    piecesCount++;
                }
                if (piecesCount < positions.length && centerCol + 1 + i < halfGrid[0].length) {
                    positions[piecesCount] = new Position(i, centerCol + 1 + i, pieces[piecesCount].getId());
                    piecesCount++;
                }
                if (piecesCount < positions.length && centerCol - 1 - i >= 0) {
                    positions[piecesCount] = new Position(halfGrid.length - 1 - i, centerCol - 1 - i, pieces[piecesCount].getId());
                    piecesCount++;
                }
                if (piecesCount < positions.length && centerCol + 1 + i < halfGrid[0].length) {
                    positions[piecesCount] = new Position(halfGrid.length - 1 - i, centerCol + 1 + i, pieces[piecesCount].getId());
                    piecesCount++;
                }
            }
        }

        piecesCount = piecesCount - 2;

        if (piecesCount >= 0) {
            // 3rd shape of a diamond around the base
            for (int i = 0; i <= centerRow && i <= (halfGrid.length - 1) / 2; i++) {
                if (piecesCount < positions.length && centerCol - 2 - i >= 0) {
                    positions[piecesCount] = new Position(i, centerCol - 2 - i, pieces[piecesCount].getId());
                    piecesCount++;
                }
                if (piecesCount < positions.length && centerCol + 2 + i < halfGrid[0].length) {
                    positions[piecesCount] = new Position(i, centerCol + 2 + i, pieces[piecesCount].getId());
                    piecesCount++;
                }
                if (piecesCount < positions.length && centerCol - 2 - i >= 0) {
                    positions[piecesCount] = new Position(halfGrid.length - 1 - i, centerCol - 2 - i, pieces[piecesCount].getId());
                    piecesCount++;
                }
                if (piecesCount < positions.length && centerCol + 2 + i < halfGrid[0].length) {
                    positions[piecesCount] = new Position(halfGrid.length - 1 - i, centerCol + 2 + i, pieces[piecesCount].getId());
                    piecesCount++;
                }
            }
        }
    }

    /**
     * This method is used to find an empty position in the grid to place the block.
     *
     * @param grid {@link String[][]} the half of the grid to be filled
     */
    public void placeBlock(String[][] grid, Const constants) {
        Random random = new Random();

        int totalPositions = grid.length * grid[0].length;
        int tmpCounter = 0;

        for (int borderNum = 0; borderNum < constants.getMinBordersNumber(); borderNum++) {
            int randomBlockRow, randomBlockCol;

            do {
                randomBlockRow = random.nextInt(grid.length);
                randomBlockCol = random.nextInt(grid[0].length);

                tmpCounter++;
                if (tmpCounter > totalPositions) {
                    break;
                }
            } while (!grid[randomBlockRow][randomBlockCol].isEmpty());
            if (tmpCounter <= totalPositions) {
                grid[randomBlockRow][randomBlockCol] = "b";
            }
        }
    }
}
