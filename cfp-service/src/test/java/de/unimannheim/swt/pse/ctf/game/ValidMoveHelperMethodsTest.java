package de.unimannheim.swt.pse.ctf.game;

import de.unimannheim.swt.pse.ctf.game.engine.ValidMoveHelperMethods;
import de.unimannheim.swt.pse.ctf.game.map.Directions;
import de.unimannheim.swt.pse.ctf.game.map.Movement;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import de.unimannheim.swt.pse.ctf.game.map.Shape;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * This class contains tests the implementation of the ValidMoveHelperMethods class.
 * Author: sebgeige
 */
public class ValidMoveHelperMethodsTest {

    /**
     * Class contains tests for the ValidMoveHelperMethods class,
     * the purpose of which is to determine whether the move a piece wants to make is legal
     * (i.e. the intended square can be occupied by the piece in accordance with the rules of the game).
     * Each method is tested numberOfTestRounds times with randomly generated parameters.
     * -
     * Tests in the class:
     * --- testOrientedPiece()
     *      generating grids of random size with a randomly located entry which should be recognised
     *      as the base. Checking whether the directions of a piece are changed based on the position
     *      of the base.
     * --- testValueOfTargetSquareInPossibleSquares()
     *      Chose one of 6 possible entries of the new square in the grid:
     *      block, own base, own piece, empty, opponent base,
     *      opponent piece of randomly chosen lesser, equal or greater attack power.
     *      Make sure the expected value equals the value returned by the method (-1, 0, 1 or 2).
     * --- testPossibleSquares()
     *      Generate a grid of random size (at least 3x3). Place one base each in the upper and lower half,
     *      fill 12,5% of squares with own and 12,5% of squares with opponent pieces with randomly generated
     *      attack power, and 5% of squares with blocks.
     *      Chose one piece randomly and calculate it's possible squares array.
     *      The method does not rely on JUnit Tests, as these would have been quite complex and likely would have
     *      relied on reproducing the procedure in the method itself, making method and test very similar.
     *      As of now (02.04.2024) the tests therefore rely on visual comparison of grid and the
     *      corresponding possible squares array.
     */

    private final int numberOfTestRounds = 50;

    public String[][] fillGridRandomSize(boolean empty) {

        int amountRows, amountColumns;

        // 2 <= amountRows, amountColumns <= 10; select random values
        do {
            amountRows = (int) (Math.random() * 11);
            amountColumns = (int) (Math.random() * 11);
        } while (amountRows < 3 || amountColumns < 3);

        return this.fillGridFixedSize(amountRows, amountColumns, empty);
    }

    public String[][] fillGridFixedSize(int rows, int columns, boolean empty) {
        String[][] grid = new String[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (empty) {
                    grid[row][column] = "";
                } else {
                    grid[row][column] = "______";
                }
            }
        }

        return grid;
    }

    @Test
    public void testOrientedPiece() {

        System.out.println("\ntesting the orientedPiece\n");

        for (int testround = 0; testround < this.numberOfTestRounds; testround++) {

            //setup
            Piece pieceWithDirections = new Piece();
            GameState gameState = new GameState();
            String[][] grid = this.fillGridRandomSize(false);
            gameState.setGrid(grid);


            int[] positionOfBase = new int[]{-1, -1};
            int candidate;

            do {
                candidate = (int) (Math.random() * grid.length);
            /*
            lazy evaluation,
            if the number of rows is uneven, the base cannot be located in the middle row
             */
                if (grid.length % 2 == 1 && candidate != grid.length / 2) {
                    positionOfBase[0] = candidate;
                } else {
                    positionOfBase[0] = candidate;
                }
            } while (positionOfBase[0] == -1);

            positionOfBase[1] = (int) (Math.random() * grid[0].length);

            boolean lowerHalf = positionOfBase[0] >= grid.length / 2;

            gameState.getGrid()[positionOfBase[0]][positionOfBase[1]] = "b:1--";

            for (String[] strings : grid) {
                for (String string : strings) {
                    System.out.print(string + " ");
                }
                System.out.println();
            }

            pieceWithDirections = new Piece();
            pieceWithDirections.setTeamId("1--");
            pieceWithDirections.setId("1");
            //asymmetrical movement so it is possible to check, whether the values change
            Directions directions = new Directions();
            List<Integer> directionsList = new LinkedList<>();
            for (int i = 0; i < 8; i++) {
                directionsList.add(i);
            }
            //random order of the directions
            Collections.shuffle(directionsList);

            directions.setLeft(directionsList.get(0));
            directions.setRight(directionsList.get(1));
            directions.setUp(directionsList.get(2));
            directions.setDown(directionsList.get(3));
            directions.setUpLeft(directionsList.get(4));
            directions.setUpRight(directionsList.get(5));
            directions.setDownLeft(directionsList.get(6));
            directions.setDownRight(directionsList.get(7));

            pieceWithDirections.setDescription(new PieceDescription());
            pieceWithDirections.getDescription().setMovement(new Movement());

            //setting directions and movement
            pieceWithDirections.getDescription().getMovement().setDirections(directions);
            pieceWithDirections.getDescription().getMovement().setShape(null);


            ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();
            Piece updatedPiece = validMoveHelperMethods.orientedPiece(pieceWithDirections, gameState);
            Directions updatedDirections = updatedPiece.getDescription().getMovement().getDirections();

            System.out.println("\nDirections of original piece (left) and updated piece (right).");
            if (lowerHalf) {
                System.out.println("Directions should not be swapped.");
            } else {
                System.out.println("Directions should be swapped.");
            }
            String[] nameOfDirection = new String[]{"left:\t\t", "right:\t\t", "up:\t\t\t", "down:\t\t",
                    "upLeft:\t\t", "upRight:\t", "downLeft:\t", "downRight:\t"};

            for (int i = 0; i < 8; i++) {
                System.out.println(nameOfDirection[i] + directionsList.get(i) + " -> " + updatedDirections.getDirectionsAsArray()[i]);
            }

            System.out.println("\n----------------------------------------\n");

            //Testing

            //shape should not have changed
            assertEquals(pieceWithDirections.getDescription().getMovement().getShape(),
                    updatedPiece.getDescription().getMovement().getShape());
            //if the base of the piece was located in the lower half, the directions should not have changed
            if (lowerHalf) {
                assertEquals(pieceWithDirections.getDescription().getMovement().getDirections(), updatedDirections);
            } else {
            /*
            otherwise (base of the piece was in the upper half),
            the values of the directions which are inverse to one another (e.g. downRight vs. upLeft)
            should have been swapped
             */
                int[] updatedDirectionsArray = updatedDirections.getDirectionsAsArray();

                assertEquals(directionsList.get(0), updatedDirectionsArray[1]);
                assertEquals(directionsList.get(1), updatedDirectionsArray[0]);
                assertEquals(directionsList.get(2), updatedDirectionsArray[3]);
                assertEquals(directionsList.get(3), updatedDirectionsArray[2]);
                assertEquals(directionsList.get(4), updatedDirectionsArray[7]);
                assertEquals(directionsList.get(5), updatedDirectionsArray[6]);
                assertEquals(directionsList.get(6), updatedDirectionsArray[5]);
                assertEquals(directionsList.get(7), updatedDirectionsArray[4]);

            }
        }
    }


    @Test
    public void testValueOfTargetSquareInPossibleSquares() {

        System.out.println("\ntesting the valueOfTargetSquareInPossibleSquares\n");

        //setup

        /*
        the simulated own piece is piece 1 of team 1, the opponent team is team 2
        possible entries: block, own base, (other) own piece, empty, opponent base, opponent piece
         */
        String[] possibleStringsForEntry = new String[]{"b", "b:1", "p:1_2", "", "b:2", "p:2_1"};
        Piece ownPiece = new Piece();
        ownPiece.setTeamId("1");
        ownPiece.setId("1");
        ownPiece.setDescription(new PieceDescription());

        Piece secondOwnPiece = new Piece();
        secondOwnPiece.setTeamId("1");
        secondOwnPiece.setId("2");

        Piece opponentPiece = new Piece();
        opponentPiece.setTeamId("2");
        opponentPiece.setId("1");
        opponentPiece.setDescription(new PieceDescription());

        HashMap<String, Piece> pieceByGridName = new HashMap<>();
        pieceByGridName.put("p:1_2", secondOwnPiece);
        pieceByGridName.put("p:2_1", opponentPiece);

        ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();

        int indexOfEntry;

        for (int testround = 0; testround < this.numberOfTestRounds; testround++) {

            /*
            only opponent piece should be tested multiple times,
            as it is the only case in which parameters change with each call
             */
            indexOfEntry = testround < possibleStringsForEntry.length ? testround : possibleStringsForEntry.length - 1;

            String entryNewSquare = possibleStringsForEntry[indexOfEntry];

            if(indexOfEntry == possibleStringsForEntry.length - 1){
                /*opponent piece on new square
                generate equal attack power with slightly increased probability
                 */
                int decision = (int) (Math.random() * 5);
                if(decision < 4){
                    //80%, here equal attack power has probability 1/100
                    ownPiece.getDescription().setAttackPower((int) (Math.random() * 100));
                    opponentPiece.getDescription().setAttackPower((int) (Math.random() * 100));
                }else{
                    //20%
                    int equalAttackPower = (int) (Math.random() * 100);
                    ownPiece.getDescription().setAttackPower(equalAttackPower);
                    opponentPiece.getDescription().setAttackPower(equalAttackPower);
                }

            }

            //testing

            int valueOfSquare = validMoveHelperMethods.valueOfTargetSquareInPossibleSquares(entryNewSquare, ownPiece, pieceByGridName);

            switch (indexOfEntry){
                case 0:
                    //block
                    System.out.println("Entry of new square: '" + entryNewSquare + "' = block");
                    assertEquals(-1, valueOfSquare);
                    break;
                case 1:
                    //own base
                    System.out.println("Entry of new square: '" + entryNewSquare + "' = own base");
                    assertEquals(-1, valueOfSquare);
                    break;
                case 2:
                    //own piece
                    System.out.println("Entry of new square: '" + entryNewSquare + "' = own piece");
                    assertEquals(-1, valueOfSquare);
                    break;
                case 3:
                    //empty
                    System.out.println("Entry of new square: '" + entryNewSquare + "' = empty square");
                    assertEquals(0, valueOfSquare);
                    break;
                case 4:
                    //opponent base
                    System.out.println("Entry of new square: '" + entryNewSquare + "' = opponent base");
                    assertEquals(2, valueOfSquare);
                    break;
                case 5:
                    //opponent piece
                    System.out.println("Entry of new square: '" + entryNewSquare + "' = opponent piece");
                    System.out.println(" --- own piece attack power:      " + ownPiece.getDescription().getAttackPower());
                    System.out.println(" --- opponent piece attack power: " + opponentPiece.getDescription().getAttackPower());

                    if(ownPiece.getDescription().getAttackPower() < opponentPiece.getDescription().getAttackPower()){
                        assertEquals(-1, valueOfSquare);
                    }else{
                        assertEquals(1, valueOfSquare);
                    }
            }

            System.out.println("Value of new square: " + valueOfSquare);
            System.out.println("\n----------------------------------------\n");
        }

    }

    @Test
    public void testPossibleSquares(){

        System.out.println("\ntesting the possibleSquares\n");

        /*
        procedure in this test is under the assumption of the (preciously asserted)
        functionality of the orientedPiece() and valueOfTargetSquareInPossibleSquares() methods.
        However, this test will most likely fail if either of the two methods produces incorrect results

        generate a grid of random size, but at least 3 x 3.
        Place each one base in the upper and lower half,
        12,5% of squares with own and opponent pieces each, with randomly generated attack power
        5% of squares with blocks
         */

        Set<String> filledPositions = new HashSet<>();
        HashMap<String, Piece> pieceByGridName = new HashMap<>();

        int row, column, amountOfSquares, middleRow, teamId, pieceId;

        String[][] grid;

        Directions directions;

        for(int testround = 0; testround < this.numberOfTestRounds; testround++){

            filledPositions = new HashSet<>();
            pieceByGridName = new HashMap<>();

            grid = this.fillGridRandomSize(true);
            amountOfSquares = grid.length * grid[0].length;
            middleRow = grid.length / 2;

            //base team 1
            row = (int) (Math.random() * middleRow);
            column = (int) (Math.random() * grid[0].length);
            grid[row][column] = "b:1";
            filledPositions.add(row + "_" + column);

            //base team 2
            int odd = grid.length % 2;
            do{
                row = (int) (Math.random() * middleRow + middleRow + odd);
                column = (int) (Math.random() * grid[0].length);
            }while(!filledPositions.add(row + "_" + column));
            grid[row][column] = "b:2";

            //generate own and opponent pieces
            Piece piece;

            for(int i = 0; i < (amountOfSquares / 8); i++){

                piece = new Piece();
                piece.setTeamId("1");
                piece.setId(String.valueOf(i + 1));
                piece.setDescription(new PieceDescription());
                piece.getDescription().setAttackPower((int) (Math.random() * 11));

                do{
                    row = (int) (Math.random() * grid.length);
                    column = (int) (Math.random() * grid[0].length);
                }while(!filledPositions.add(row + "_" + column));
                grid[row][column] = "p:1_" + (i + 1);
                pieceByGridName.put("p:1_" + (i + 1), piece);

                piece.setPosition(new int[]{row, column});

                piece = new Piece();
                piece.setTeamId("2");
                piece.setId(String.valueOf(i + 1));
                piece.setDescription(new PieceDescription());
                piece.getDescription().setAttackPower((int) (Math.random() * 11));

                do{
                    row = (int) (Math.random() * grid.length);
                    column = (int) (Math.random() * grid[0].length);
                }while(!filledPositions.add(row + "_" + column));
                grid[row][column] = "p:2_" + (i + 1);
                pieceByGridName.put("p:2_" + (i + 1), piece);

                piece.setPosition(new int[]{row, column});
            }

            for(int i = 0; i < amountOfSquares / 20; i++){
                do{
                    row = (int) (Math.random() * grid.length);
                    column = (int) (Math.random() * grid[0].length);
                }while(!filledPositions.add(row + "_" + column));
                grid[row][column] = "b";
            }

            //choose a team and a piece
            teamId = (int) (Math.random() * 2 + 1);
            pieceId = (int) (Math.random() * (amountOfSquares / 8) + 1);

            Piece chosenPiece = pieceByGridName.get("p:" + teamId + "_" + pieceId);
            chosenPiece.getDescription().setMovement(new Movement());

            //randomly giving the piece directions or shape
            if(Math.random() < 0.5){

                System.out.println("piece has directions");

                directions = new Directions();

                //directions as a star with increasing length of beams from 12 o'clock onwards
                directions.setLeft(2);
                directions.setRight(2);
                directions.setUp(2);
                directions.setDown(2);
                directions.setUpLeft(2);
                directions.setUpRight(2);
                directions.setDownLeft(2);
                directions.setDownRight(2);

                chosenPiece.getDescription().getMovement().setDirections(directions);
                chosenPiece.getDescription().getMovement().setShape(null);

            }else{

                System.out.println("piece has shape");

                Shape shape = new Shape();
                chosenPiece.getDescription().getMovement().setShape(shape);
                chosenPiece.getDescription().getMovement().setDirections(null);
            }

            System.out.println("chosen piece: p:" + teamId + "_" + pieceId + "\n");
            System.out.println("Pieces in the pieceByGridName Hashmap: ");
            for(String key: pieceByGridName.keySet()){
                System.out.println(key + ", attack power: " + pieceByGridName.get(key).getDescription().getAttackPower());
            }
            System.out.println("\n");

            ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();
            GameState gameState = new GameState();
            gameState.setGrid(grid);
            int[][] valuesOfSquares = validMoveHelperMethods.possibleSquares(chosenPiece, gameState, pieceByGridName);

            for(int i  = 0; i < grid.length; i++){
                for(String entry: grid[i]){
                    System.out.print(entry);
                    for(int entryLenght = entry.length(); entryLenght < 6; entryLenght++){
                        System.out.print("-");
                    }
                    System.out.print(" ");
                }

                System.out.print("\t\t");
                for(int value: valuesOfSquares[i]){
                    System.out.print(value + " ");
                    if(value != -1){
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }


            System.out.println("\n----------------------------------------\n");

        }
    }
}

