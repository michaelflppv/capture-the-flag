package de.unimannheim.swt.pse.ctf.game;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import de.unimannheim.swt.pse.ctf.game.engine.GameEngine;
import de.unimannheim.swt.pse.ctf.game.engine.ValidMoveHelperMethods;
import de.unimannheim.swt.pse.ctf.game.exceptions.InvalidMove;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.Movement;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import de.unimannheim.swt.pse.ctf.game.map.Directions;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class contains tests the implementation of the GameEngine class.
 * Author: jwiederh,sebgeige, jasherrm
 */
public class GameEngineTests {

    /**
     * The GameEngineTests class contains a series of unit tests for the GameEngine class, which manages the game
     * logic for a grid-based game with multiple teams and pieces. Each test method in this class is designed to verify
     * the functionality of the game engine under various scenarios.
     *
     * Tests in the class:
     *
     * ---  testIsGameOver_FlagCaptured()
     *      This test checks the scenario where the game ends due to a flag capture. It sets up a game, simulates a
     *      flag capture, and verifies if the game ends correctly.
     *
     * ---  testIsGameOver_TimeIsOver()
     *      This test checks the scenario where the game ends due to time running out. It sets up a game, simulates
     *      the passage of time, and verifies if the game ends when the time is over.
     *
     * ---  testIsGameOver_DrawScenario()
     *      This test checks the scenario where the game ends in a draw. It sets up a game in a way that no team can
     *      make a valid move, simulates the passage of time, and verifies if the game ends in a draw.
     *
     * ---  testgiveUp_AllowedRequest()
     *      This test checks the scenario where a team gives up. It sets up a game, simulates a team giving up,
     *      and verifies if the game ends correctly with the other team as the winner.
     *
     * ---  makeMove_InvalidMove()
     *      This test checks the scenario where an invalid move is attempted. It sets up a game, simulates an invalid
     *      move, and expects an `InvalidMove` exception.
     *
     * ---  makeMove_ValidMove()
     *      This test checks the scenario where a valid move is made. It sets up a game, simulates a valid move,
     *      and verifies if the move is executed correctly.
     *
     * ---  makeMove_CaptureFlag()
     *      This test checks the scenario where a flag is captured. It sets up a game, simulates a flag capture,
     *      and verifies if the flag is captured correctly.
     *
     * ---  makeMove_CapturePiece()
     *      This test checks the scenario where a piece is captured. It sets up a game, simulates a piece capture,
     *      and verifies if the piece is captured correctly.
     *
     * ---  testThreePlayerGame_FlagCapture()
     *      This test checks the scenario of a three-team game where a flag is captured. It sets up a game with three
     *      teams, simulates a flag capture, and verifies if the game ends correctly with the right team as the winner.
     *
     * ---  MultiFlag_MakeMove_CapturePiece()
     *      This test checks the scenario where multiple flags are captured. It sets up a game, simulates a piece
     *      capturing multiple flags, and verifies if the game ends correctly with the right team as the winner.
     *
     * ---  GameTimeOver_MostPiecesWins()
     *      This test checks the scenario where the game time runs out and the team with the most pieces wins.
     *      It sets up a game, simulates the passage of time, and verifies if the game ends correctly with the team
     *      having the most pieces as the winner.
     *
     * ---  GameTimeOver_Draw()
     *      This test checks the scenario where the game time runs out and the game ends in a draw. It sets up a game,
     *      simulates the passage of time, and verifies if the game ends in a draw.
     *
     * ---  MoveTimeOver_SkipMove()
     *      This test checks the scenario where the move time runs out and the move is skipped. It sets up a game,
     *      simulates the passage of move time, and verifies if the current team's move is skipped.
     */


    private GameEngine gameEngine;


    /**
     * Initializes and sets up the game environment for a specified number of teams.
     * This method prepares the game by creating a new GameEngine instance, configuring
     * a map template, defining pieces for the teams, and setting up an initial game state.
     * The method performs the following actions:
     * 1. Creates a new GameEngine instance.
     * 2. Sets up the map template with specified attributes such as grid size, number of teams,
     *    number of flags, number of blocks, placement type, total time limit, and move time limit.
     * 3. Defines two types of pieces with specific movement capabilities, attack power, and quantities.
     *    - Piece1 can move left and right.
     *    - Piece2 can move up and down.
     * 4. Sets up an example 10x10 grid where all spaces are initially empty, represented by an empty string ("").
     *
     * @param numberOfTeams The number of teams to be set up in the game. Each team will be assigned
     *                      a unique ID and the pieces as per the PieceDescriptions.
     */

    public void setUpPlayers(int numberOfTeams, int numberOfFlags) {
        //Create new GameEngine Instance
        gameEngine = new GameEngine();

        //Set the different values in the MapTemplate
        MapTemplate template = new MapTemplate();
        template.setGridSize(new int[]{10, 10});
        template.setTeams(numberOfTeams);
        template.setFlags(numberOfFlags);
        template.setBlocks(4);
        template.setPlacement(PlacementType.symmetrical);
        template.setTotalTimeLimitInSeconds(600);
        template.setMoveTimeLimitInSeconds(30);


        //Create Descriptions for two pieces. Piece one can move from left to right, Piece can move up and down.
        PieceDescription[] piecedescs = new PieceDescription[2];

        // Initialize first PieceDescription
        PieceDescription piece1 = new PieceDescription();
        piece1.setType("Piece1");
        piece1.setAttackPower(5);
        piece1.setCount(1);
        Movement movement1 = new Movement();
        Directions directions1 = new Directions();
        directions1.setLeft(1);
        directions1.setRight(1);
        directions1.setUp(1);
        directions1.setDown(1);
        movement1.setDirections(directions1);
        piece1.setMovement(movement1);
        piecedescs[0] = piece1;

        // Initialize second PieceDescription
        PieceDescription piece2 = new PieceDescription();
        piece2.setType("Piece2");
        piece2.setAttackPower(10);
        piece2.setCount(1);
        Movement movement2 = new Movement();
        Directions directions2 = new Directions();
        directions2.setLeft(2);
        directions2.setRight(2);
        directions2.setUp(2);
        directions2.setDown(2);
        movement2.setDirections(directions2);
        piece2.setMovement(movement2);
        piecedescs[1] = piece2;

        template.setPieces(piecedescs);
        gameEngine.create(template);

        //Create 10x10 example grid, where all spaces are empty.
        String[][] examplegrid = new String[10][10];

        // Fill the grid with empty strings to represent an empty square.
        for (int i = 0; i < examplegrid.length; i++) {
            //matches an empty string
            // Empty square
            Arrays.fill(examplegrid[i], "");
        }
        //Set the grid in the GameState
        this.gameEngine.getCurrentGameState().setGrid(examplegrid);

        //Let Teams join the Game
        String[] TeamIDs= new String[]{"a","b","c","d"};
        for(int i = 0;i<numberOfTeams;i++){
            this.gameEngine.joinGame(TeamIDs[i]);
        }
    }

    /**
     * Updates the entry in the grid at the specified position with the given ID.
     * @param row represents the row index in which the id should be placed
     * @param column represents the column index in which the id should be placed
     * @param gridobject The String representation of a piece, base, block or empty space. Further specified in the GameState class
     */

    public void updateGridEntry(int row, int column, String gridobject) {
       String [][]grid = this.gameEngine.getCurrentGameState().getGrid();
       grid[row][column] = gridobject;
       this.gameEngine.getCurrentGameState().setGrid(grid);
    }

    /**
     * Prints the representation of a game board (grid) to the console. Each cell of the grid is
     * printed along with a space for readability, and each row of the grid is printed on a new line.
     *
     * @param grid A two-dimensional array of strings representing the game board. Each element in
     *             the array represents a cell on the board, which could contain a string reference
     *             to a piece, a block, a team's base, or an empty square. The format for each cell is
     *             as follows: an empty string ("")  for an empty square, "b" for a block, "b:tid" for
     *             a team's base with team ID, and "p:tid_pid" for a piece with its team ID and piece ID.
     */
    private void printGrid(String[][] grid) {
        for (String[] row : grid) {
            for (String cell : row) {
                if(cell.isEmpty()){
                    System.out.print("/" + " ");
                }
                else{
                    System.out.print(cell + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    //Prints out a representation of the Gri
    private void printPossibleSquares(int[][] possible_squares) {

        String[][] string_possible_squares = new String[possible_squares.length][];
        for (int i = 0; i < possible_squares.length; i++) {
            string_possible_squares[i] = new String[possible_squares[i].length];
            for (int j = 0; j < possible_squares[i].length; j++) {
                string_possible_squares[i][j] = String.valueOf(possible_squares[i][j]);
            }
        }
        printGrid(string_possible_squares);
    }

    private String getGridRepresentation(Piece p) {
        return "p:" + p.getTeamId() + "_" + p.getId();
    }

    @Test
    public void testIsGameOver_FlagCaptured() {
        int numberofplayers = 4;
        setUpPlayers(numberofplayers, 1);
        Team[] teams = this.gameEngine.getCurrentGameState().getTeams();
        for(int i = 0;i<numberofplayers-1;i++) {
            teams[i] = null;
        }
        this.gameEngine.isGameOver();
        assertArrayEquals(new String[]{"d"}, this.gameEngine.getWinner());
    }

    @Test
    public void testIsGameOver_TimeIsOver() {
        int numberofplayers = 4;
        setUpPlayers(numberofplayers,1);
        assertFalse(gameEngine.isGameOver());
    }

    //TODO: Not fully implemented
    @Test
    public void testIsGameOver_DrawScenario() {
        setUpPlayers(2,1); // Sets up a game with 2 teams
        // Manually set the game state to reflect a draw scenario

        Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
        Piece p1_T1 = t1.getPieces()[0];
        Piece p2_T1 = t1.getPieces()[1];
        Piece p1_T2 = t2.getPieces()[0];
        Piece p2_T2 = t2.getPieces()[1];

        t1.setBase(new int[]{3,3});
        this.updateGridEntry(4,4,"b:" + t1.getId());
        t2.setBase(new int[]{6,6});
        this.updateGridEntry(6,6,"b:" + t2.getId());

        //Place pieces in the corners
        p1_T1.setPosition(new int[]{0, 0});
        this.updateGridEntry(0,0,getGridRepresentation(p1_T1));
        p2_T1.setPosition(new int[]{0, 9});
        this.updateGridEntry(0,9,getGridRepresentation(p2_T1));
        p1_T2 .setPosition(new int[]{9, 0});
        this.updateGridEntry(9,0, getGridRepresentation(p1_T2));
        p2_T2.setPosition(new int[]{9, 9});
        this.updateGridEntry(9,9, getGridRepresentation(p2_T2));

        this.printGrid(this.gameEngine.getCurrentGameState().getGrid());
        assertFalse(gameEngine.isGameOver());
    }
    @Test
    public void testgiveUp_AllowedRequest() {
        //Setup Game with three teams
        setUpPlayers(3,1);

        // Manually set up the game
        Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
        Team t3 = this.gameEngine.getCurrentGameState().getTeams()[2];
        Piece p1_T1 = t1.getPieces()[0];
        Piece p1_T2 = t2.getPieces()[0];
        Piece p1_T3 = t3.getPieces()[0];
        t1.setBase(new int[]{3,3});
        this.updateGridEntry(3,3,"b:" + t1.getId());
        t2.setBase(new int[]{6,6});
        this.updateGridEntry(6,6,"b:" + t2.getId());
        t3.setBase(new int[]{7,7});
        this.updateGridEntry(7,7,"b:" + t3.getId());

        //Place pieces in the corners
        p1_T1.setPosition(new int[]{0, 0});
        this.updateGridEntry(0,0,getGridRepresentation(p1_T1));
        p1_T2.setPosition(new int[]{0, 9});
        this.updateGridEntry(0,9,getGridRepresentation(p1_T2));
        p1_T3.setPosition(new int[]{9, 0});
        this.updateGridEntry(9,0, getGridRepresentation(p1_T3));

        //It's the turn of Team 1 and they give up the Game
        this.gameEngine.giveUp(t1.getId());
        //It's the turn of Team 2 and they give up the Game
        this.gameEngine.giveUp(t2.getId());

        String[] winner = new String[]{t3.getId()};
        assertArrayEquals(winner, gameEngine.getWinner());
    }
    @Test
    public void makeMove_InvalidMove() {
        setUpPlayers(2,1); // sets up a game with 2 teams
        // Manually set up the Game

        //Set up only one piece for Team1
        Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Piece p1_T1 = t1.getPieces()[0];

        //Place pieces in the corner of the Grid
        p1_T1.setPosition(new int[]{0, 0});
        this.updateGridEntry(0,0,getGridRepresentation(p1_T1));

        //Print Grid to ensure the setup worked as intended
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        //Create a MoveRequest for the piece of Team1
        Move m = new Move();
        m.setPieceId(p1_T1.getId());
        m.setNewPosition(new int[]{3,3});

        //Print the valid moves of the Piece to ensure the Move Logic is working as intended
        ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();
        printPossibleSquares(validMoveHelperMethods.possibleSquares(p1_T1,this.gameEngine.getCurrentGameState(), this.gameEngine.pieceByGridName));

        //Checks if an invalidMove exception gets thrown. Test succeeds if invalid move is made.
        assertThrows(InvalidMove.class, () -> gameEngine.makeMove(m),"Invalid Move was not detected");

    }

    @Test
    public void makeMove_ValidMove() {
        setUpPlayers(2,1); // sets up a game with 2 teams
        // Manually set up the Game
        Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
        Piece p1_T1 = t1.getPieces()[0];
        Piece p1_T2 = t2.getPieces()[0];

        //Set the positions of the basses
        t1.setBase(new int[]{3,6});
        this.updateGridEntry(3,6,"b:" + t1.getId());
        t2.setBase(new int[]{6,6});
        this.updateGridEntry(6,6,"b:" + t2.getId());

        //Place first piece of Team1
        p1_T1.setPosition(new int[]{9, 8});
        this.updateGridEntry(9,8,getGridRepresentation(p1_T1));

        //Place first piece of Team2
        p1_T2.setPosition(new int[]{0, 0});
        this.updateGridEntry(0,0,getGridRepresentation(p1_T2));

        //Create a MoveRequest for the piece of Team1
        Move m = new Move();
        m.setTeamId(p1_T1.getTeamId());
        m.setPieceId(p1_T1.getId());
        m.setNewPosition(new int[]{9,7});

        //Print the possible Squares the piece can now move to
        ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();
        printPossibleSquares(validMoveHelperMethods.possibleSquares(p1_T1,this.gameEngine.getCurrentGameState(), this.gameEngine.pieceByGridName));

        //prints Grid before a move has been made
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        //Make the move
        this.gameEngine.makeMove(m);

        //Check if Piece Position and Grid are updated correctly
        System.out.println("Piece Position Piece 1: " + p1_T1.getPosition()[0] + " " + p1_T1.getPosition()[1] + "\n");
        printGrid(this.gameEngine.getCurrentGameState().getGrid());


        //We now make a move for the piece of Team2

        //This is the Gridrepresentation that should be in the GameState after the second move method executed. Used to ensure correctness in the JUnit Test
        String[][] resultGrid = new String[this.gameEngine.getCurrentGameState().getGrid().length][this.gameEngine.getCurrentGameState().getGrid()[0].length];
        for (int i = 0;i<this.gameEngine.getCurrentGameState().getGrid().length;i++) {
            for (int j = 0; j<this.gameEngine.getCurrentGameState().getGrid()[i].length;j++) {
                resultGrid[i][j]=this.gameEngine.getCurrentGameState().getGrid()[i][j];
            }
        }
        //Piece 1 of Team 1 should be in this position
        resultGrid[9][7] = getGridRepresentation(p1_T1);

        //Piece 1 of Team 1 should move to [0][1] and the former position[0][0] should be made an empty field again
        resultGrid[0][0] = "";
        resultGrid[0][1] = getGridRepresentation(p1_T2);

        //Print the possible Squares the piece of Team 2 can now move to
        printPossibleSquares(validMoveHelperMethods.possibleSquares(p1_T2,this.gameEngine.getCurrentGameState(), this.gameEngine.pieceByGridName));

        //Setting the position for the second move for the Piece of Team2
        Move m2 = new Move();
        m2.setTeamId(p1_T2.getTeamId());
        m2.setPieceId(p1_T2.getId());
        m2.setNewPosition(new int[]{0,1});

        //Make the Move
        this.gameEngine.makeMove(m2);

        //Check if the Position of Piece 2 and the Grid are updated correctly
        System.out.println("Piece Position Piece 2: " + p1_T2.getPosition()[0] + " " + p1_T2.getPosition()[1] + "\n");
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        //Check if the currentGrid is equal to the resultGrid we defined earlier
        assertArrayEquals(resultGrid, this.gameEngine.getCurrentGameState().getGrid());
    }

    @Test
    public void makeMove_CaptureFlag() {
        setUpPlayers(2,1); // sets up a game with 2 teams

        // Manually set up the Game
        Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
        Piece p1_T1 = t1.getPieces()[0];
        Piece p1_T2 = t2.getPieces()[0];

        //Set the positions of the basses
        t1.setBase(new int[]{3, 6});
        this.updateGridEntry(3, 6, "b:" + t1.getId());
        t2.setBase(new int[]{6, 6});
        this.updateGridEntry(6, 6, "b:" + t2.getId());

        //Place first piece of Team1
        p1_T1.setPosition(new int[]{5, 6});
        this.updateGridEntry(5, 6, getGridRepresentation(p1_T1));

        //Place first piece of Team2
        p1_T2.setPosition(new int[]{0, 0});
        this.updateGridEntry(0, 0, getGridRepresentation(p1_T2));

        //Create a MoveRequest so that the Piece of Team1 captures the Flag of Team2
        Move m = new Move();
        m.setTeamId(p1_T1.getTeamId());
        m.setPieceId(p1_T1.getId());
        m.setNewPosition(new int[]{6, 6});

        //Print the possible Squares the piece can now move to
        ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();
        printPossibleSquares(validMoveHelperMethods.possibleSquares(p1_T1,this.gameEngine.getCurrentGameState(), this.gameEngine.pieceByGridName));

        //Print the Grid before the move has been made to ensure correct setup
        printGrid(this.gameEngine.getCurrentGameState().getGrid());
        //Capture the Flag

        this.gameEngine.makeMove(m);
        //Print the Grid after the move has been made to ensure that the move executed correctly
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        this.gameEngine.isGameOver();
        //Ensure that the first Team one by capturing all of the enemys flags
        assertEquals("a", this.gameEngine.getWinner()[0]);
    }

    @Test
    public void makeMove_CapturePiece() {
        setUpPlayers(2,1); // sets up a game with 2 teams

        // Manually set up the Game

        //Initalizing the Team objects and piece objects
        Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
        Piece p1_T1 = t1.getPieces()[0];
        Piece p2_T1 = t1.getPieces()[1];
        Piece p1_T2 = t2.getPieces()[0];
        Piece p2_T2 = t2.getPieces()[1];

        //Set the positions of the bases
        t1.setBase(new int[]{3, 6});
        this.updateGridEntry(3, 6, "b:" + t1.getId());
        t2.setBase(new int[]{6, 6});
        this.updateGridEntry(6, 6, "b:" + t2.getId());

        //Place first Piece of Team1
        p1_T1.setPosition(new int[]{5, 5});
        this.updateGridEntry(5, 5, getGridRepresentation(p1_T1));
        //Place second Piece of Team1
        p2_T1.setPosition(new int[]{5, 4});
        this.updateGridEntry(5, 4, getGridRepresentation(p2_T1));

        //Place first piece of Team2
        p1_T2.setPosition(new int[]{6, 5});
        this.updateGridEntry(6, 5, getGridRepresentation(p1_T2));
        //Place second piece of Team2
        p2_T2.setPosition(new int[]{6, 4});
        this.updateGridEntry(6, 4, getGridRepresentation(p2_T2));

        //Create a MoveRequest so that the Piece of Team1 captures Piece1 of Team2
        Move m = new Move();
        m.setTeamId(p1_T1.getTeamId());
        m.setPieceId(p1_T1.getId());
        m.setNewPosition(new int[]{6, 5});

        printGrid(this.gameEngine.getCurrentGameState().getGrid());
        //Piece 1 of Team 1 captures Piece 1 of Team 2
        this.gameEngine.makeMove(m);
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        //Create a second move
        Move m2 = new Move();
        m2.setTeamId(p2_T2.getTeamId());
        m2.setPieceId(p2_T2.getId());
        m2.setNewPosition(new int[]{6, 5});

        //Piece 2 of Team 2 captures Piece 1 of Team 1
        this.gameEngine.makeMove(m2);
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        //Create a third move
        Move m3 = new Move();
        m3.setTeamId(p2_T1.getTeamId());
        m3.setPieceId(p2_T1.getId());
        m3.setNewPosition(new int[]{6, 4});

        //Piece 1 of Team1 walks to an empty field
        this.gameEngine.makeMove(m3);
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        //Create final move
        Move m4 = new Move();
        m4.setTeamId(p2_T2.getTeamId());
        m4.setPieceId(p2_T2.getId());
        m4.setNewPosition(new int[]{6, 4});

        //Team 2 captures the final piece of Team1

        this.gameEngine.makeMove(m4);
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        this.gameEngine.isGameOver();
        //Check if Team2 is the winner
        assertArrayEquals(new String[]{"b"}, this.gameEngine.getWinner());
    }
    @Test
    public void testThreePlayerGame_FlagCapture() {
        // Initialize the game with 3 players
        setUpPlayers(3,1);

        // Position of team bases
        Team team1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Team team2 = this.gameEngine.getCurrentGameState().getTeams()[1];
        Team team3 = this.gameEngine.getCurrentGameState().getTeams()[2];

        team1.setBase(new int[]{3,3});
        team2.setBase(new int[]{6,6});
        team3.setBase(new int[]{7,7});

        // Set the base positions for each team
        this.updateGridEntry(3, 3, "b:" + team1.getId()); // Team 1 base
        this.updateGridEntry(6, 6, "b:" + team2.getId()); // Team 2 base
        this.updateGridEntry(7, 7, "b:" + team3.getId()); // Team 3 base

        // Initialize pieces for each team
        Piece pieceTeam1 = team1.getPieces()[0];
        Piece pieceTeam2 = team2.getPieces()[0];
        Piece pieceTeam3 = team3.getPieces()[0];

        pieceTeam1.setPosition(new int[]{6,5});
        pieceTeam2.setPosition(new int[]{5,5});
        pieceTeam3.setPosition(new int[]{3,4});

        // Initial placement of pieces
        this.updateGridEntry(6, 5, getGridRepresentation(pieceTeam1)); // Team 1's piece starts at (6,5)
        this.updateGridEntry(5, 5, getGridRepresentation(pieceTeam2)); // Team 2's piece starts at (5,5)
        this.updateGridEntry(3, 4, getGridRepresentation(pieceTeam3)); // Team 3's piece starts at (3,4)

        //Print the grid to ensure everything looks correct
        this.printGrid(this.gameEngine.getCurrentGameState().getGrid());

        // Simulate movements for capturing bases
        // Team 1's piece moves to capture Team 2's flag
        Move move_Team1 = new Move();
        move_Team1.setTeamId(team1.getId());
        move_Team1 .setPieceId(pieceTeam1.getId());
        move_Team1 .setNewPosition(new int[]{6, 6}); // Moves to Team 2's base position
        this.gameEngine.makeMove(move_Team1);

        //Print the grid to ensure the Piece from Team 2 has been deleted
        this.printGrid(this.gameEngine.getCurrentGameState().getGrid());

        // Team 3's piece moves to capture Team 1's flag
        Move move_Team3 = new Move();
        move_Team3.setTeamId(team3.getId());
        move_Team3.setPieceId(pieceTeam3.getId());
        move_Team3.setNewPosition(new int[]{3, 3}); // Moves to Team 1's base position
        this.gameEngine.makeMove(move_Team3);

        // Assert flag capture scenarios
        assertTrue(this.gameEngine.isGameOver());
        assertArrayEquals(new String[]{team3.getId()}, this.gameEngine.getWinner());
    }



    @Test
    public void MultiFlag_MakeMove_CapturePiece() {
        setUpPlayers(2,2); // sets up a game with 2 teams

        // Manually set up the Game

        // Initializing the Team objects and piece objects
        Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
        Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
        Piece p1_T1 = t1.getPieces()[0];
        Piece p2_T1 = t1.getPieces()[1];
        Piece p1_T2 = t2.getPieces()[0];
        Piece p2_T2 = t2.getPieces()[1];

        // Set the positions of the bases
        t1.setBase(new int[]{3, 6});
        this.updateGridEntry(3, 6, "b:" + t1.getId());
        t2.setBase(new int[]{6, 6});
        this.updateGridEntry(6, 6, "b:" + t2.getId());

        // Place first Piece of Team1 in attack position
        p1_T1.setPosition(new int[]{5, 6}); // one step away from enemy base
        this.updateGridEntry(5, 6, getGridRepresentation(p1_T1));
        printGrid(this.gameEngine.getCurrentGameState().getGrid()); // Print grid after move

        // Place second Piece of Team1 in attack position
        p2_T1.setPosition(new int[]{6, 7}); // one steps away from enemy base
        this.updateGridEntry(6, 7, getGridRepresentation(p2_T1));
        printGrid(this.gameEngine.getCurrentGameState().getGrid()); // Print grid after move

        // Place first piece of Team2
        p1_T2.setPosition(new int[]{3, 3}); // Changed position
        this.updateGridEntry(3, 3, getGridRepresentation(p1_T2));
        printGrid(this.gameEngine.getCurrentGameState().getGrid()); // Print grid after move

        // Place second piece of Team2
        p2_T2.setPosition(new int[]{4, 4}); // Changed position
        this.updateGridEntry(4, 4, getGridRepresentation(p2_T2));
        printGrid(this.gameEngine.getCurrentGameState().getGrid()); // Print grid after move

        // Piece 1 of Team 1 captures first flag of Team 2
        Move m1 = new Move();
        m1.setTeamId(p1_T1.getTeamId());
        m1.setPieceId(p1_T1.getId());
        m1.setNewPosition(new int[]{6, 6}); // Moves to Team 2's base position
        this.gameEngine.makeMove(m1);
        System.out.println("Piece 1 of Team 1 captures first flag of Team 2");
        printGrid(this.gameEngine.getCurrentGameState().getGrid()); // Print grid after move

        // Check if Piece 1 of Team 1 is respawned back to its base
        int[] basePosition = t1.getBase();
        int[] piecePosition = p1_T1.getPosition();

        // Check positions around the base for the respawned piece
        boolean respawnedCorrectly = false;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip the base itself
                if (piecePosition[0] == basePosition[0] + i && piecePosition[1] == basePosition[1] + j) {
                    respawnedCorrectly = true;
                    break;
                }
            }
            if (respawnedCorrectly) break;
        }

        assertTrue(respawnedCorrectly, "Piece 1 of Team 1 did not respawn correctly");
        System.out.println("Piece 1 of Team 1 respawned correctly");
        printGrid(this.gameEngine.getCurrentGameState().getGrid()); // Print grid after move

        // Piece 2 of Team 2 makes a move
        Move m2 = new Move();
        m2.setTeamId(p1_T2.getTeamId()); // Set the team ID
        m2.setPieceId(p1_T2.getId()); // Set the piece ID
        m2.setNewPosition(new int[]{3, 4}); // Set the new position
        this.gameEngine.makeMove(m2);
        printGrid(this.gameEngine.getCurrentGameState().getGrid());

        // Piece 2 of Team 1 captures second flag of Team 2
        Move m3 = new Move();
        m3.setTeamId(p2_T1.getTeamId());
        m3.setPieceId(p2_T1.getId());
        m3.setNewPosition(new int[]{6, 6}); // Moves to Team 2's base position
        this.gameEngine.makeMove(m3);
        printGrid(this.gameEngine.getCurrentGameState().getGrid()); // Print grid after move

        // Check if Team 2 has no flags left and the game is over
        assertTrue(this.gameEngine.isGameOver());
        assertArrayEquals(new String[]{t1.getId()}, this.gameEngine.getWinner());
    }


    @Test
    public void GameTimeOver_MostPiecesWins() throws IOException {

        Gson gson = new Gson();

        gameEngine = new GameEngine();

        // Read the JSON file from the resources folder
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/de/sep/cfp4/gameService/10x10_2teams_example.json")))) {
            MapTemplate template = gson.fromJson(reader, MapTemplate.class);

            // Set the total time limit to 0 seconds
            template.setTotalTimeLimitInSeconds(0);

            gameEngine.create(template);

            //Let Teams join the Game
            String[] TeamIDs= new String[]{"a","b"};
            for(int i = 0;i< 2;i++){
                this.gameEngine.joinGame(TeamIDs[i]);
            }

            // Initializing the Team objects and piece objects
            Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
            Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
            Piece p1_T1 = t1.getPieces()[0];
            Piece p2_T1 = t1.getPieces()[1];
            Piece p1_T2 = t2.getPieces()[0];

            // Check if the game is over
            assertTrue(gameEngine.isGameOver());

            // Check if the player with the most pieces is the winner
            assertEquals("a", gameEngine.getWinner()[0]);
        }
    }

    @Test
    public void GameTimeOver_Draw() throws IOException {

        Gson gson = new Gson();

        gameEngine = new GameEngine();

        // Read the JSON file from the resources folder
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/de/sep/cfp4/gameService/10x10_2teams_example.json")))) {
            MapTemplate template = gson.fromJson(reader, MapTemplate.class);

            // Set the total time limit to 0 seconds
            template.setTotalTimeLimitInSeconds(0);

            gameEngine.create(template);

            //Let Teams join the Game
            String[] TeamIDs = new String[]{"a", "b"};
            for (int i = 0; i < 2; i++) {
                this.gameEngine.joinGame(TeamIDs[i]);
            }

            // Initializing the Team objects and piece objects
            Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
            Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
            Piece p1_T1 = t1.getPieces()[0];
            Piece p2_T1 = t1.getPieces()[1];
            Piece p1_T2 = t2.getPieces()[0];
            Piece p2_T2 = t2.getPieces()[1];

            // Check if the game is over
            assertTrue(gameEngine.isGameOver());

            // Expected winners
            String[] expectedWinners = new String[]{"a", "b"};

            // Check if the game ends in a draw
            assertArrayEquals(expectedWinners, gameEngine.getWinner());
        }
    }

    @Test
    public void MoveTimeOver_SkipMove()throws IOException {

        Gson gson = new Gson();

        gameEngine = new GameEngine();

        // Read the JSON file from the resources folder
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/de/sep/cfp4/gameService/10x10_2teams_example.json")))) {
            MapTemplate template = gson.fromJson(reader, MapTemplate.class);

            // Set the MoveTime limit to 3 second
            template.setMoveTimeLimitInSeconds(3);

            gameEngine.create(template);

            //Let Teams join the Game
            String[] TeamIDs = new String[]{"a", "b"};
            for (int i = 0; i < 2; i++) {
                this.gameEngine.joinGame(TeamIDs[i]);
            }

            // Initializing the Team objects and piece objects
            Team t1 = this.gameEngine.getCurrentGameState().getTeams()[0];
            Team t2 = this.gameEngine.getCurrentGameState().getTeams()[1];
            Piece p1_T1 = t1.getPieces()[0];
            Piece p2_T1 = t1.getPieces()[1];

            // Wait for the move time to expire
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Check if the current team is now team 2
            System.out.println("ID von t2: " + t2.getId());
            System.out.println("ID vom current Team: " + this.gameEngine.getCurrentGameState().getTeams()[this.gameEngine.getCurrentGameState().getCurrentTeam()].getId());
            assertEquals(t2.getId(), this.gameEngine.getCurrentGameState().getTeams()[this.gameEngine.getCurrentGameState().getCurrentTeam()].getId());
        }
    }
}