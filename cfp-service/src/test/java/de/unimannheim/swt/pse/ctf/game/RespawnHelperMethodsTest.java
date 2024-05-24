package de.unimannheim.swt.pse.ctf.game;

import de.unimannheim.swt.pse.ctf.game.engine.RespawnHelperMethods;
import de.unimannheim.swt.pse.ctf.game.engine.Square;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains tests the implementation of the RespawnHelperMethods class.
 * Author: sebgeige
 */
public class RespawnHelperMethodsTest {

    /**
     * Class contains tests for the RespawnHelperMethods class,
     * the purpose of which is to determine a square in the grid on which a piece can respawn
     * in the multiflag version of the game. Each method is tested numberOfTestRounds times with
     * randomly generated parameters.
     * -
     * Tests in the class:
     * --- testIsInsideGrid()
     *      checking edge cases
     * --- testFillAdjacencyList()
     *      comparing amount of corner, edge and inner squares with length of lists in the adjacency list
     * --- testBFS()
     *      comparing the distance of the squares in the grid from the base
     *      by the counted distance in the eight directions the queen in chess can move
     * --- testGetSquareToSpawnSimple()
     *      empty grid with one base: chosen square to spawn should be in one of the adjacent 3 / 5 / 8 squares
     * --- testGetSquareToSpawnAdvanced()
     *      Generate a grid with few randomly chosen possible squares to spawn.
     *      Compare the distance of the chosen square with the free square closest to the base besides the
     *      chosen square. This distance must not be smaller than the distance of the chosen square
     */

    private String[][] grid;
    private final int numberOfTestRounds = 50;

    public String[][] fillGridRandomSize(String teamId, boolean empty){

        int amountRows, amountColumns;

        // 2 <= amountRows, amountColumns <= 10; select random values
        do{
            amountRows = (int) (Math.random() * 11);
            amountColumns = (int) (Math.random() * 11);
        }while(amountRows < 3 || amountColumns < 3);

        return this.fillGridFixedSize(amountRows, amountColumns, teamId, empty);
    }

    public String[][] fillGridFixedSize(int rows, int columns, String teamId, boolean empty){
        String[][] grid = new String[rows][columns];

        for(int row = 0; row < rows; row++){
            for(int column = 0; column < columns; column++){
                if(empty){
                    grid[row][column] = "";
                }else{
                    grid[row][column] = "___";
                }
            }
        }

        //place base at random position in the grid
        grid[(int) (Math.random() * rows)][(int) (Math.random() * columns)] = "b:" + teamId;

        return grid;
    }

    public String[][] fillGridManually(){

        return new String[][]{
                {"", "", "p:1_2", "", ""},
                {"", "p:1_1", "b:1", "p:1_3", ""},
                {"b", "", "", "", "b"},
                {"", "p:2_1", "b:2", "p:2_3", ""},
                {"", "", "p:2_2", "", ""}
        };
    }

    @Test
    public void testIsInsideGrid(){
        System.out.println("\ntesting the isInsideGrid\n");
        RespawnHelperMethods respawnHelperMethods = new RespawnHelperMethods();

        for(int testRun = 0; testRun < 25; testRun++){
            this.grid = this.fillGridRandomSize("0", false);
            respawnHelperMethods.setGrid(this.grid);

            assertTrue(respawnHelperMethods.isInsideGrid(0, 0));
            assertFalse(respawnHelperMethods.isInsideGrid(grid.length, grid[0].length));
            assertFalse(respawnHelperMethods.isInsideGrid(-1, -1));
        }

    }

    @Test
    public void testFillAdjacencyList(){
        System.out.println("\ntesting the fillAdjacencyList\n");

        RespawnHelperMethods respawnHelperMethods = new RespawnHelperMethods();
        this.grid = this.fillGridManually();
        respawnHelperMethods.setGrid(this.grid);
        respawnHelperMethods.setTeamId("1");

        respawnHelperMethods.fillAdjacencyList();

        /*
        the base for the specified team is located correctly
        for team 1 the position is 1, 2; for team 2 it is 3, 2
         */
        assertArrayEquals(new int[]{1, 2}, respawnHelperMethods.getBaseOfTeam().getPositionInGridArray());

        /*
        edge case: for a 1 x 1 grid,
        the square should have 0 adjacent squares,
        and thus an empty adjacency list
         */
        this.grid = this.fillGridFixedSize(1, 1, "0", false);

        respawnHelperMethods.setGrid(this.grid);
        respawnHelperMethods.fillAdjacencyList();

        for(List<Square> l: respawnHelperMethods.getAdjacencyList()){
            assertEquals(0, l.size());
        }

        /*
        for a rectangle m x n grid (m, n >= 2)
            the amount of corner squares is 4,
            the amount of squares on the edge is (rows - 2) * 2 + (columns - 2) * 2
            the amount of inner squares is (rows - 2) * (columns - 2)
        corner squares have 3 adjacent squares, edge squares 5, and inner squares 8

        Idea for the following test: amount of lists of length 3, 5 and 8 should correspond
        to the amount of corner, edge and inner squares

        along the way, check whether the indices in the adjacency list saved in the squares are correct

        test for a number of randomly chosen grid sizes
         */

        for(int testround = 0; testround < this.numberOfTestRounds; testround++){

            this.grid = this.fillGridRandomSize("0", false);
            int amountRows = this.grid.length;
            int amountColumns = this.grid[0].length;

            int amountCornerSquares = 4;
            int amountEdgeSquares = (amountRows - 2) * 2 + (amountColumns - 2) * 2;
            int amountInnerSquares = (amountRows - 2) * (amountColumns - 2);

            int listLengthTree = 0;
            int listLengthFive = 0;
            int listLengthEight = 0;

            respawnHelperMethods.setGrid(this.grid);
            respawnHelperMethods.fillAdjacencyList();

            //the indices in the adjacency list saved in the squares are correct
            int count = 0;
            for(int row = 0; row < this.grid.length; row++){
                for(int column = 0; column < this.grid[0].length; column++){
                    assertEquals(count++, respawnHelperMethods.getSquareByGridName().get(row + "_" + column).getIndexInAdjacencyList());
                }
            }

            for(List<Square> l: respawnHelperMethods.getAdjacencyList()){
                switch (l.size()){
                    case 3:
                        listLengthTree++;
                        break;
                    case 5:
                        listLengthFive++;
                        break;
                    case 8:
                        listLengthEight++;
                        break;
                }
            }

            assertEquals(amountCornerSquares, listLengthTree);
            assertEquals(amountEdgeSquares, listLengthFive);
            assertEquals(amountInnerSquares, listLengthEight);
            assertEquals((listLengthTree + listLengthFive + listLengthEight), (this.grid.length * this.grid[0].length));
        }

    }

    @Test
    public void testBFS(){

        /*
        IMPORTANT
        This is, because the randomly generated grids in this test class have no empty squares
        (String.isEmpty() is false for every square). Therefore, candidatesForSpawning will always
        be empty, and the return value of respawnHelperMethods.getSquareToSpawn() will always be null.
        Choosing of the square will be tested in the next method, this one is supposed to test BFS
        (as it is the basis for choosing a square to respawn)
         */

        /*
        test for a number of randomly generated grids, with a randomly placed base
        e.g. 50 grids
         */
        System.out.println("\ntesting BFS\n");

        RespawnHelperMethods respawnHelperMethods = new RespawnHelperMethods();

        String teamId = "1";

        for(int testround = 0; testround < this.numberOfTestRounds; testround++){

            this.grid = this.fillGridRandomSize(teamId, false);

            respawnHelperMethods.setGrid(this.grid);
            respawnHelperMethods.setTeamId(teamId);

            respawnHelperMethods.fillAdjacencyList();
            respawnHelperMethods.getSquareToSpawn();

            /*
            Now all the squares in the Hashmap should be initialized with the correct distance to the base.
            Correct localisation of the base was already tested in the testFillAdjacencyList().
            -
            Also, respawnHelperMethods.getSquareToSpawn() does not terminate early and all squares are visited,
            as no square in the grid is empty (corresponding parameter of this.fillGridRandomSize)
            Build an int array with the distances to the base
             */

            int[][] distancesToBase = new int[this.grid.length][this.grid[0].length];

            for(int row = 0; row < this.grid.length; row++){
                for(int column = 0; column < this.grid[0].length; column++){
                    Square s = respawnHelperMethods.getSquareByGridName().get(row + "_" + column);
                    distancesToBase[row][column] = s.getDistanceToBase();
                    System.out.print(s.getDistanceToBase() + " ");
                }
                System.out.println();
            }

            System.out.println("----------------------------------------");

            /*
            if BFS worked correctly, starting from the base the distance should increase by 1
            in each of the eight directions with each step.
            -
            Idea for this test adopted from ValidMoveHelperMethods.possibleSquares() method
             */

            int[][] distances = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

            int[] positionOfBase = respawnHelperMethods.getBaseOfTeam().getPositionInGridArray();

            for(int i = 0; i < distances.length; i++) {

                int distanceToBaseLastIteration = 0;

                for (int dist = 1; dist <= 10; dist++) {
                    try {
                        int row = positionOfBase[0] + dist * distances[i][0];
                        int column = positionOfBase[1] + dist * distances[i][1];

                        /*in the next line, out of Bounds could happen.
                        Then the rim of the board is reached, break in the current direction
                        */
                        assertEquals(distanceToBaseLastIteration + 1, distancesToBase[row][column]);

                        distanceToBaseLastIteration++;

                    } catch (ArrayIndexOutOfBoundsException aibE) {
                        break;
                    }
                }
            }
        }
    }

    @Test
    public void testGetSquareToSpawnSimple(){
        System.out.println("\ntesting getSquareToSpawnSimple\n");

        RespawnHelperMethods respawnHelperMethods = new RespawnHelperMethods();

        String teamId = "1";

        for(int testround = 0; testround < this.numberOfTestRounds; testround++) {

            this.grid = this.fillGridRandomSize(teamId, true);

            respawnHelperMethods.setGrid(this.grid);

            respawnHelperMethods.setTeamId(teamId);

            respawnHelperMethods.fillAdjacencyList();

            //respawnHelperMethods.getBaseOfTeam() was already tested
            int[] positionBaseOfTeam = respawnHelperMethods.getBaseOfTeam().getPositionInGridArray();

            /*the chosen position to respawn must be ...
            - within the grid
            - 1 row/colum from the base, as the grid is empty except for the randomly positioned base
             */
            int[] chosenPositionToSpawn = respawnHelperMethods.getSquareToSpawn();

            //chosen square is inside grid
            assertTrue(respawnHelperMethods.isInsideGrid(chosenPositionToSpawn[0], chosenPositionToSpawn[1]));

            //none the chosen square is not further than 1 away from the base in each direction
            assertTrue(Math.abs(positionBaseOfTeam[0] - chosenPositionToSpawn[0]) <= 1);
            assertTrue(Math.abs(positionBaseOfTeam[1] - chosenPositionToSpawn[1]) <= 1);

            //if the distance in one direction is 0, it has to be one in the other direction
            if(Math.abs(positionBaseOfTeam[0] - chosenPositionToSpawn[0]) == 0){
                assertEquals(1, Math.abs(positionBaseOfTeam[1] - chosenPositionToSpawn[1]));
            }
            if(Math.abs(positionBaseOfTeam[1] - chosenPositionToSpawn[1]) == 0){
                assertEquals(1, Math.abs(positionBaseOfTeam[0] - chosenPositionToSpawn[0]));
            }
        }
    }

    @Test
    public void testGetSquareToSpawnAdvanced(){
        System.out.println("\ntesting getSquareToSpawnAdvanced\n");

        /*
        difference to simple:
        create a grid with very few empty squares and remember which one is the closest to the base,
        and check whether the algo can still find it.
        Also, if the candidatesForSpawning list is not empty after a round of BFS,
        all the squares in the list should have the same distance to the base
         */
        RespawnHelperMethods respawnHelperMethods = new RespawnHelperMethods();

        String teamId = "1";

        for(int testround = 0; testround < this.numberOfTestRounds; testround++) {

            this.grid = this.fillGridRandomSize(teamId, false);

            //chose 10% of squares as candidates, but at least 3
            int amountOfSquares = (this.grid.length * this.grid[0].length);
            int amountCandidates = amountOfSquares / 10;
            amountCandidates = Math.max(amountCandidates, 3);

            Set<Integer> freeSquares = new HashSet<>();
            Random random = new Random();
            while(freeSquares.size() < amountCandidates){
                freeSquares.add(random.nextInt(amountOfSquares));
            }

            System.out.println("Amount of chosen squares: " + freeSquares.size()  + " / " + amountOfSquares);

            //set the chosen squares as empty, to simulate the possible squares in the gameState.grid
            for(Integer integer: freeSquares){
                int row = integer / this.grid[0].length;
                int column = integer % this.grid[0].length;
                if(!this.grid[row][column].matches("b:.+")){
                    //not overwriting the base
                    this.grid[row][column] = "";
                }
            }

            respawnHelperMethods.setGrid(this.grid);
            respawnHelperMethods.setTeamId(teamId);
            respawnHelperMethods.fillAdjacencyList();

            //respawnHelperMethods.getBaseOfTeam() was already tested
            int[] positionBaseOfTeam = respawnHelperMethods.getBaseOfTeam().getPositionInGridArray();
            System.out.println("base of team:\t\t\t\t" + positionBaseOfTeam[0] + " - " + positionBaseOfTeam[1]);

            int[] chosenPositionToSpawn = respawnHelperMethods.getSquareToSpawn();
            System.out.println("chosen position to spawn:\t" + chosenPositionToSpawn[0] + " - " + chosenPositionToSpawn[1]);

            System.out.println("\non the left the candidates as +++, and to the right the chosen square as 000");
            System.out.println();

            for(int row = 0; row < this.grid.length; row++){
                for(int column = 0; column < this.grid[0].length; column++){
                    //print grid with candidates
                    if(this.grid[row][column].isEmpty()) {
                        //candidate square
                        System.out.print("+++\t");
                    }else{
                        //base of the team
                        System.out.print(this.grid[row][column] + "\t");
                    }
                }

                System.out.print("\t\t");

                for(int column = 0; column < this.grid[0].length; column++){
                    //print grid with marked chosen square to spawn
                    if(row == chosenPositionToSpawn[0] && column == chosenPositionToSpawn[1]){
                        //chosen square
                        System.out.print("000\t");
                    }else if(this.grid[row][column].isEmpty()) {
                        //candidate square
                        System.out.print("+++\t");
                    }else{
                        //base of the team
                        System.out.print(this.grid[row][column] + "\t");
                    }
                }
                System.out.println();
            }

            //chosen square is inside grid
            assertTrue(respawnHelperMethods.isInsideGrid(chosenPositionToSpawn[0], chosenPositionToSpawn[1]));

            //all the squares in the candidatesForSpawning list have the same distance to the base
            List<Square> candidatesForSpawning = respawnHelperMethods.getCandidatesForSpawning();
            int distanceOfFirst = candidatesForSpawning.get(0).getDistanceToBase();
            for(Square s: candidatesForSpawning){
                assertEquals(distanceOfFirst, s.getDistanceToBase());
            }

            //no possible square is closer to the base as the chosen square
            int rowOfBase = positionBaseOfTeam[0];
            int columnOfBase = positionBaseOfTeam[1];
            int distanceChosen = Math.max(Math.abs(rowOfBase - chosenPositionToSpawn[0]), Math.abs(columnOfBase - chosenPositionToSpawn[1]));

            int minDistanceFreeSquare = Integer.MAX_VALUE;
            int rowFreeSquare, columnFreeSquare, distanceFreeSquare;
            for(Integer integer: freeSquares){
                rowFreeSquare = integer / this.grid[0].length;
                columnFreeSquare = integer % this.grid[0].length;

                if(rowFreeSquare == positionBaseOfTeam[0] && columnFreeSquare == positionBaseOfTeam[1]){
                    //do not consider distance of base to base
                    continue;
                }
                if(rowFreeSquare == chosenPositionToSpawn[0] && columnFreeSquare == chosenPositionToSpawn[1]){
                    ////do not consider distance of base to chosen square to spawn
                    continue;
                }

                distanceFreeSquare = Math.max(Math.abs(rowOfBase - rowFreeSquare), Math.abs(columnOfBase - columnFreeSquare));
                if(distanceFreeSquare < minDistanceFreeSquare){
                    minDistanceFreeSquare = distanceFreeSquare;
                }
            }

            System.out.println("\ndistance [chosen square - base]:\t" + distanceChosen);
            System.out.println("distance [next free square - base]:\t" + minDistanceFreeSquare);

            assertTrue(distanceChosen <= minDistanceFreeSquare);

            System.out.println("\n---------------------------------------------------------\n");
        }
    }
}
