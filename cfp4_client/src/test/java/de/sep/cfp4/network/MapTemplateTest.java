package de.sep.cfp4.network;

import com.google.gson.Gson;
import de.sep.cfp4.technicalServices.network.GameClient;
import de.unimannheim.swt.pse.ctf.CtfApplication;
import de.unimannheim.swt.pse.ctf.controller.data.GameSessionResponse;
import de.unimannheim.swt.pse.ctf.game.map.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains tests for creating and validating MapTemplates.
 * Author: jwiederh, jasherrm
 */
public class MapTemplateTest {

    /**
     * The MapTemplateTest class contains a series of unit tests for the MapTemplate class, which manages the
     * configuration of a grid-based game map with multiple teams and pieces. Each test method in this class is
     * designed to verify the functionality of the map template under various scenarios.
     *
     * Tests in the class:
     *
     * ---  testAnyValidMapTemplate()
     *      This test checks the creation of 100 different MapTemplates and verifies their validity. It creates
     *      random MapTemplates within specified constraints and checks if they are valid.
     *
     * ---  testMultiFlagTemplate()
     *      This test checks the multi-flag template. It reads a multi-flag JSON file from the resources folder,
     *      deserializes it into a MapTemplate object, and verifies if the properties of the MapTemplate are as expected.
     *
     * ---  test4PlayerTemplate()
     *      This test checks the 4-player template. It reads a 4-player JSON file from the resources folder,
     *      deserializes it into a MapTemplate object, and verifies if the properties of the MapTemplate are as expected.
     *
     * ---  test10x10_2teams_exampleTemplate()
     *      This test checks the 10x10 2-teams example template. It reads a 10x10 2-teams example JSON file from the
     *      resources folder, deserializes it into a MapTemplate object, and verifies if the properties of the
     *      MapTemplate are as expected.
     */


    // URL of the server
    private final String SERVER_URL = "http://localhost:8888";
    // Client to communicate with the server
    private final GameClient client = new GameClient(URI.create(this.SERVER_URL));

    // Constants for grid size boundaries
    private static final int MIN_GRID_SIZE = 4;
    private static final int MAX_GRID_SIZE = 10;
    // Constants for the number of teams
    private static final int MIN_TEAMS = 2;
    private static final int MAX_TEAMS = 4;
    // Constants for the number of flags
    private static final int MIN_FLAGS = 1;
    // Constants for the number of pieces
    private static final int MIN_PIECES = 1;

    // Random number generator
    private static final Random random = new Random();

    /**
     * Set up the test environment by starting the application.
     *
     * @throws URISyntaxException if there is an error in the URI syntax
     */
    @BeforeAll
    static void setUp() throws URISyntaxException {
        CtfApplication.main(new String[0]);
    }
    @AfterAll
    static void shutDownServer() {
        CtfApplication.stopApplication();
    }

    /**
     * Constructor for the MapTemplateTest class.
     *
     * @throws URISyntaxException if there is an error in the URI syntax
     */
    MapTemplateTest() throws URISyntaxException {
    }

    /**
     * Creates a valid MapTemplate with random parameters within specified constraints.
     *
     * @return a valid MapTemplate object
     */
    public static MapTemplate createValidMapTemplate() {
        MapTemplate template = new MapTemplate();

        // Set random grid size between 4x4 and 10x10
        int gridSize = random.nextInt(MAX_GRID_SIZE - MIN_GRID_SIZE + 1) + MIN_GRID_SIZE;
        template.setGridSize(new int[]{gridSize, gridSize});

        // Set random number of teams between 2 and 4
        int numberOfTeams = random.nextInt(MAX_TEAMS - 1) + MIN_TEAMS;
        template.setTeams(numberOfTeams);

        // Set at least one flag per team
        template.setFlags(MIN_FLAGS + random.nextInt(10));

        // Set random number of blocks
        int blocks = random.nextInt(gridSize);
        template.setBlocks(blocks);

        // Calculate number of pieces ensuring it fits within grid size constraints
        int numberOfPieces = random.nextInt(gridSize * gridSize - numberOfTeams - blocks);
        int numberOfPieceDescs = random.nextInt(8) + MIN_PIECES;
        ArrayList<PieceDescription> pieceDescriptionList = new ArrayList<>();

        for (int i = 0; i < numberOfPieceDescs; i++) {
            PieceDescription p = createRandomPieceDescription("Piece" + (i + 1));
            if (numberOfPieces - p.getCount() >= 0) {
                pieceDescriptionList.add(p);
                numberOfPieces -= p.getCount();
            }
        }

        PieceDescription[] pieces = new PieceDescription[pieceDescriptionList.size()];
        for (int i = 0; i < pieceDescriptionList.size(); i++) {
            pieces[i] = pieceDescriptionList.get(i);
        }
        template.setPieces(pieces);

        // Set random placement strategy
        PlacementType[] placements = PlacementType.values();
        PlacementType placement = placements[random.nextInt(placements.length)];
        template.setPlacement(placement);

        // Set random total game time limit in seconds
        int totalTimeLimitInSeconds = random.nextInt(3600);  // up to 1 hour
        template.setTotalTimeLimitInSeconds(totalTimeLimitInSeconds);

        // Set random move time limit in seconds
        int moveTimeLimitInSeconds = random.nextInt(3600);  // up to 1 hour
        template.setMoveTimeLimitInSeconds(moveTimeLimitInSeconds);

        // Build string representation of the MapTemplate
        StringBuilder sb = new StringBuilder();
        sb.append("MapTemplate {\n");
        sb.append("  Grid Size: ").append(gridSize).append("x").append(gridSize).append("\n");
        sb.append("  Teams: ").append(numberOfTeams).append("\n");
        sb.append("  Flags: ").append(template.getFlags()).append("\n");
        sb.append("  Blocks: ").append(blocks).append("\n");
        sb.append("  Placement: ").append(placement).append("\n");
        sb.append("  Total Time Limit: ").append(totalTimeLimitInSeconds).append(" seconds\n");
        sb.append("  Move Time Limit: ").append(moveTimeLimitInSeconds).append(" seconds\n");
        sb.append("  Pieces: [\n");
        for (PieceDescription piece : pieces) {
            String s = "PieceDescription{" +
                    "type='" + piece.getType() + '\'' +
                    ", attackPower=" + piece.getAttackPower() +
                    ", count=" + piece.getCount() +
                    ", left=" + piece.getMovement().getDirections().getLeft() +
                    ", right=" + piece.getMovement().getDirections().getRight() +
                    ", up=" + piece.getMovement().getDirections().getUp() +
                    ", down=" + piece.getMovement().getDirections().getDown() +
                    ", upLeft=" + piece.getMovement().getDirections().getUpLeft() +
                    ", upRight=" + piece.getMovement().getDirections().getUpRight() +
                    ", downLeft=" + piece.getMovement().getDirections().getDownLeft() +
                    ", downRight=" + piece.getMovement().getDirections().getDownRight() +
                    '}';
            sb.append(s).append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        System.out.print(sb);

        return template;
    }

    /**
     * Creates a random PieceDescription with the specified type and random parameters.
     *
     * @param type the type of the piece
     * @return a random PieceDescription object
     */
    private static PieceDescription createRandomPieceDescription(String type) {
        PieceDescription piece = new PieceDescription();
        piece.setType(type);
        piece.setAttackPower(random.nextInt(10) + 1);  // Random attack power between 1 and 10
        piece.setCount(random.nextInt(5) + 1);  // Random count between 1 and 5

        Movement movement = new Movement();
        Directions directions = new Directions();
        directions.setLeft(random.nextInt(3));
        directions.setRight(random.nextInt(3));
        directions.setUp(random.nextInt(3));
        directions.setDown(random.nextInt(3));
        directions.setUpLeft(random.nextInt(3));
        directions.setUpRight(random.nextInt(3));
        directions.setDownLeft(random.nextInt(3));
        directions.setDownRight(random.nextInt(3));
        movement.setDirections(directions);
        piece.setMovement(movement);

        return piece;
    }

    /**
     * Tests the creation of 100 different MapTemplates and verifies their validity.
     *
     * @throws IOException          if there is an I/O error during communication with the server
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testAnyValidMapTemplate() throws IOException, InterruptedException {
        // Test 100 different MapTemplates
        for (int i = 0; i < 100; i++) {
            GameSessionResponse response = this.client.createGameSession(createValidMapTemplate());
            assertNotNull(this.client.getGameSession(response.getId()));
        }
    }

    /**
     * Test the Multiflag Template.
     */
    @Test
    public void testMultiFlagTemplate() {

        // Read the multi-flag JSON file from the resources folder
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(
                    "/de/sep/cfp4/network/Multiflag.json")))) {
            MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);

            System.out.println("Number of teams: " + template.getTeams());
            assertEquals(2, template.getTeams());
            System.out.println("Number of flags: " + template.getFlags());
            assertEquals(2, template.getFlags());
            System.out.println("Grid size (width): " + template.getGridSize()[0]);
            assertEquals(6, template.getGridSize()[0]);
            System.out.println("Grid size (height): " + template.getGridSize()[1]);
            assertEquals(6, template.getGridSize()[1]);

        } catch (IOException e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }

    /**
     * Test the 4_player Template.
     */
    @Test
    public void test4PlayerTemplate() {

        // Read the 4-player JSON file from the resources folder
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(
                    "/de/sep/cfp4/network/4_Player.json")))) {
            MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);

            int teams = template.getTeams();
            int[] gridSize = template.getGridSize();
            System.out.println("Number of teams: " + teams);
            assertEquals(4, teams);
            System.out.println("Grid size (width): " + gridSize[0]);
            assertEquals(gridSize[0], gridSize[1]);

        } catch (IOException e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }

    /**
     * Test the 10x10_2teams_example Template.
     */
    @Test
    public void test10x10_2teams_exampleTemplate() {

        // Read the 10x10_2teams_example JSON file from the resources folder
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(
                    "/de/sep/cfp4/network/10x10_2teams_example.json")))) {
            MapTemplate template = new Gson().fromJson(reader, MapTemplate.class);

            int teams = template.getTeams();
            int[] gridSize = template.getGridSize();
            System.out.println("Number of teams: " + teams);
            assertTrue(teams >= 2 && teams <= 4);
            System.out.println("Grid size (width): " + gridSize[0]);
            assertEquals(gridSize[0], gridSize[1]);

        } catch (IOException e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }
}