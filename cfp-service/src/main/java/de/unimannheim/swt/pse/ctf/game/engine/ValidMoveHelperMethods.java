package de.unimannheim.swt.pse.ctf.game.engine;

import de.unimannheim.swt.pse.ctf.game.map.Directions;
import de.unimannheim.swt.pse.ctf.game.map.Movement;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;

import java.util.Arrays;
import java.util.HashMap;

public class ValidMoveHelperMethods {

    /**
     * this class contains the methods
     * ----
     * Piece orientedPiece (Piece, GameState)
     * giving back a piece with possibly updated directions, so that up and left mean approaching [0][0] in the
     * grid regardless of the position of the base
     * ----
     * int valueOfTargetSquareInPossibleSquares(String, Piece, HashMap<String, Piece>)
     * evaluating whether a piece can go to a certain square of the grid, and if so,
     * what it finds there:
     *      not possible to occupy: -1
     *      possible to move, free square: 0
     *      possible to move, beat opponent piece: 1
     *      possible to move, opponent base: 2
     * ---
     * int[][] possibleSquares(Piece, GameState, HashMap<String, Piece>)
     * returns and array with the dimensions of the gameState.grid, filled with the values from
     * valueOfTargetSquareInPossibleSquares for a given piece
     * ---
     * boolean hasLegalMove (int[][])
     * returning whether a piece can make a legal move, based on the int[][] possibleSquares array
     */


    /**
     * solves the problem, that depending on the position
     * of the base on the board, up and left mean moving towards or away from [0][0] in gameState.grid.
     *-
     * The returned piece will have the value of its directions set so that up and left mean getting
     * closer to [0][0], regardless of the position of the base. This means, that if the base is in the upper
     * half of the board, the directions need to be inverted.
     *-
     * @param piece
     * @return Piece orientedPiece
     */

    public Piece orientedPiece(Piece piece, GameState gameState){

        if(piece.getDescription().getMovement().getShape() != null){
        /*
        piece has a Shape, and therefore no Directions.
        No updates necessary, as shape is symmetrical in every direction
         */
            return piece;
        }

    /*
    the directions need to be inverted, if the base is in the upper half of the grid. That way, up and left
    will intuitively mean getting closer to [0][0] (which is in the top left of the grid)
     */

        boolean lowerHalf = true;
        String[][] grid = gameState.getGrid();

        A: for(int row = 0; row < ((grid.length) / 2); row++){
            for (int column = 0; column < grid[0].length; column++){
                if(grid[row][column].equals("b:" + piece.getTeamId())){
                    //if the base is found here, it is in the upper half of the grid (due to the set limit in row)
                    lowerHalf = false;
                    break A;
                }
            }
        }

        if(lowerHalf){
            //all good, no updated necessary
            return piece;
        }

        //directions need to be inverted, as base is in the upper half

        Piece helpPiece = new Piece();

        helpPiece.setTeamId(piece.getTeamId());
        helpPiece.setId(piece.getId());


        helpPiece.setPosition(new int[]{piece.getPosition()[0], piece.getPosition()[1]});
        PieceDescription helpDescription = new PieceDescription();
        helpDescription.setAttackPower(piece.getDescription().getAttackPower());
        helpDescription.setCount(piece.getDescription().getCount());
        helpDescription.setType(piece.getDescription().getType());


        Movement helpMovement = new Movement();
        Directions helpDirections = new Directions();
        //oriented piece must have Directions, as Shape is null
        Directions orientedDirections = piece.getDescription().getMovement().getDirections();

        //changing the directions

        //up / down
        helpDirections.setUp(orientedDirections.getDown());
        helpDirections.setDown(orientedDirections.getUp());
        //left / right
        helpDirections.setLeft(orientedDirections.getRight());
        helpDirections.setRight(orientedDirections.getLeft());

        //upRight / upLeft (these need to be the opposite, i.e. upRight <-> downLeft
        helpDirections.setUpRight(orientedDirections.getDownLeft());
        helpDirections.setUpLeft(orientedDirections.getDownRight());
        //downRight / downLeft
        helpDirections.setDownRight(orientedDirections.getUpLeft());
        helpDirections.setDownLeft(orientedDirections.getUpRight());

        helpMovement.setDirections(helpDirections);
        helpMovement.setShape(null);

        helpDescription.setMovement(helpMovement);
        helpPiece.setDescription(helpDescription);

        return helpPiece;
    }

    /**
     * method to determine the value of the int[][] possible squares array
     * depending on the entry of the target array and the moving piece
     *-
     * not possible to occupy: -1
     * possible to move, free square: 0
     * possible to move, beat opponent piece: 1
     * possible to move, opponent base: 2
     *-
     * @param entryNewSquare
     * @param piece
     * @return int value for the int[][] possible squares array
     */
    public int valueOfTargetSquareInPossibleSquares(String entryNewSquare, Piece piece, HashMap<String, Piece> pieceByGridName){
        /*
        only possible to move if target square is empty, opponent base, or opponent piece with <= attack power
        unfortunately, in switch statements regex are not possible, so we need to check with if-else
        */

        //matches an empty string which is the representation of an empty space in the grid
        if (entryNewSquare.isEmpty()) {

            //empty

            return 0;

        } else if (entryNewSquare.matches("b:.+")) {

            //make sure it's an opponent base

            String teamIdOfBaseOnTarget = entryNewSquare.substring(2);
            if (! teamIdOfBaseOnTarget.equals(piece.getTeamId())) {
                return 2;

            }
        } else if (entryNewSquare.matches("p:.+_.+")) {

            //make sure it's an opponent piece

            String teamIdOfPieceOnTarget = pieceByGridName.get(entryNewSquare).getTeamId();
            if (! teamIdOfPieceOnTarget.equals(piece.getTeamId())) {
                Piece pieceOnTarget = pieceByGridName.get(entryNewSquare);

                //attack power needs to be smaller or equal

                if(pieceOnTarget.getDescription().getAttackPower() <= piece.getDescription().getAttackPower()){
                    return 1;
                }
            }
        }

        //if we get here, the target square cannot be occupied, so change nothing
        return -1;
    }

    /**
     * expects the move a player wants to make (instance of class Move), and returns an int[][] array,
     * whose dimensions correspond to the gamestate.grid. The squares the piece which wants to move
     * cannot occupy have the following values:
     *-
     * not possible to occupy: -1
     * (out of range, block, own piece, own base, behind something if piece cannot jump, stronger opponent piece)
     * possible to move, free square: 0
     * possible to move, beat opponent piece: 1
     * possible to move, opponent base: 2
     *
     * @param
     * @return int[][] possibleSquares
     */

    public int[][] possibleSquares(Piece piece, GameState gameState, HashMap<String, Piece> pieceByGridName) {

        String[][] grid = gameState.getGrid();

        int[][] possibleSquares = new int[grid.length][grid[0].length];

        /*
        initialize with -1
        note: this way, the square the piece has been standing on before the move will have value -1
         */
        for (int row = 0; row < possibleSquares.length; row++) {
            for (int column = 0; column < possibleSquares[0].length; column++) {
                possibleSquares[row][column] = -1;
            }
        }

        int[] oldPosition = {piece.getPosition()[0], piece.getPosition()[1]};

        if (piece.getDescription().getMovement().getShape() != null) {
        /*
        piece has Movement Shape (l-shape, two squares in one direction and one square perpendicular).
        Below all possible directions from the current position, in form {distanceRow, distanceColumn}
         */

            int[][] possibleDistancesLShape = {{-2, 1}, {-1, 2}, {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}};

            for (int[] possibleDistance : possibleDistancesLShape) {
                try {
                    int newRowPos = oldPosition[0] + possibleDistance[0];
                    int newColPos = oldPosition[1] + possibleDistance[1];
                /*
                in the following line ArrayIndexOutOfBoundsException could happen, yet no problem.
                Getting the entry of the target array
                 */
                    String entryNewSquare = grid[newRowPos][newColPos];

                    possibleSquares[newRowPos][newColPos] = valueOfTargetSquareInPossibleSquares(entryNewSquare, piece, pieceByGridName);

                }catch(ArrayIndexOutOfBoundsException aibE){
                    //continue, but is unnecessary as last statement in a loop
                }
            }
        }else{
        /*
        piece has to have directions, as Movement.Shape is null
        idea: extend from the old position in each direction,
        as long as there is no hindrance such as block, own piece and so forth.

        credits for the following idea to dcebulla,
        sebgeige would have solved it with 8 for loops and a lot of code duplication like some bloody first semester
         */

            Directions pieceDirections = piece.getDescription().getMovement().getDirections();

            int[] maxPossibleDistance = {pieceDirections.getUp(), pieceDirections.getDown(),
                    pieceDirections.getLeft(), pieceDirections.getRight(),
                    pieceDirections.getUpLeft(), pieceDirections.getUpRight(),
                    pieceDirections.getDownLeft(), pieceDirections.getDownRight()};

            int[][] distances = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

            A: for(int i = 0; i < maxPossibleDistance.length; i++){
                for(int dist = 1; dist <= maxPossibleDistance[i]; dist++){
                    try{
                        int row = oldPosition[0] + dist * distances[i][0];
                        int column = oldPosition[1] + dist * distances[i][1];

                    /*in the next line, out of Bounds could happen.
                    Then the rim of the board is reached, break in the current direction
                     */
                        String entryNewSquare = grid[row][column];

                        int valueOfTargetSquare = this.valueOfTargetSquareInPossibleSquares(entryNewSquare, piece, pieceByGridName);

                        possibleSquares[row][column] = valueOfTargetSquare;

                        if(valueOfTargetSquare != 0){
                            continue A;
                            //as with values -1, 1, 2 the piece cannot move further in that direction
                        }

                    }catch (ArrayIndexOutOfBoundsException aibE){
                        continue A;
                    }
                }
            }
        }

        return possibleSquares;
    }


    public static boolean hasLegalMove(int[][] possibleMoves) {
        for (int[] row : possibleMoves) {
            for (int move : row) {
                if (move != -1) { // -1 indicates an illegal move
                    return true; // At least one legal move exists
                }
            }
        }
        return false; // No legal moves available
    }
}
