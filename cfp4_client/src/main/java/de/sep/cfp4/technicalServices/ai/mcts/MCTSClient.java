package de.sep.cfp4.technicalServices.ai.mcts;

import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import de.sep.cfp4.application.model.BoardModel;
import de.unimannheim.swt.pse.ctf.game.state.Team;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


/**
 * The MCTSClient class is responsible for managing the Monte Carlo Tree Search algorithm.
 * It uses the BoardModel class to interact with the game and the MonteCarloTreeSearch class
 * to perform the MCTS algorithm.
 * Current Move Time: 5,4-7,8 seconds
 * (Be careful: the pytorch native platform will be added to your cache)
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 10.05.2024
 */

public class MCTSClient {
    // The BoardModel instance that represents the current state of the game.
    private final BoardModel boardModel;
    // The Arguments instance that holds the hyperparameters for the Monte Carlo Tree Search (MCTS) algorithm.
    private final Arguments args;
    // The MonteCarloTreeSearch instance that represents the MCTS algorithm model.
    private MonteCarloTreeSearch mcts;

    // The NDManager instance used for managing the MCTS algorithm.
    private final NDManager manager;

    // A String that represents the identifier of the team.
    private final String teamID;
    // An integer that represents the number of rows in the game grid.
    private final int rowCount;
    // An integer that represents the number of columns in the game grid.
    private final int columnCount;
    // An integer that represents the total number of possible actions in the game.
    private final int actionSize;

    /**
     * Constructor for the MCTSClient class.
     *
     * @param boardModel {@link BoardModel} the game on which the MCTS algorithm is applied
     */
    public MCTSClient(BoardModel boardModel) {
        this.boardModel = boardModel;
        this.args = new Arguments();
        this.manager = NDManager.newBaseManager();

        this.teamID = boardModel.getTeamID();

        this.rowCount = boardModel.getGrid().length;
        this.columnCount = boardModel.getGrid()[0].length;
        this.actionSize = this.rowCount * this.columnCount * 8;

        // Initialize the MCTS algorithm
        initializeMCTS();

        // Start the MCTS AI
        this.start();
    }

    /**
     * This method is used to start the Monte Carlo Tree Search (MCTS) AI.
     */
    public void start() {
        System.out.println("Starting MCTS AI");
        while (this.boardModel.isGameInProgress()) {
            int teamNumber = this.boardModel.getTeamNumberByID(this.teamID);
            if (this.boardModel.getGameState().getCurrentTeam() == teamNumber) {
                makeBestMove();
                // Wait for the next turn
                try {
                    synchronized (this) {
                        wait(1000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted", e);
                }
            } else {
                try {
                    synchronized (this) {
                        wait(1000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted", e);
                }
            }
        }
    }

    /**
     * This method is used to initialize the Monte Carlo Tree Search (MCTS) algorithm model.
     */
    public void initializeMCTS() {
        // Initialize the MCTS CNN model
        ArchitectureModel model = new ArchitectureModel(this, 9, 3);
        model.initialize(this.manager, DataType.FLOAT32, new Shape(1, 3, this.rowCount, this.columnCount));

        // Initialize the MCTS algorithm
        this.mcts = new MonteCarloTreeSearch(model, this, this.args);
    }

    /**
     * Method to get the initial state of the game.
     *
     * @return the initial state of the game
     */
    public String[][] getInitialState() {
        return this.boardModel.getGrid();
    }

    /**
     * Method to get the next state of the game, after a move has been made
     * from a previous state.
     *
     * @return the current state of the game
     */
    public String[][] getNextState(String[][] state, int action) {
        String[][] nextState = copyState(state);
        int[] newPosition = castActionToPosition(action);

        for (int i = 0; i < nextState.length; i++) {
            for (int j = 0; j < nextState.length; j++) {
                if (nextState[i][j].startsWith("p:") && nextState[i][j].contains(this.teamID)) {
                    int[][] reachableSquares = this.boardModel.getReachableSquares(i, j);
                    for (int k = 0; k < this.rowCount; k++) {
                        for (int l = 0; l < this.columnCount; l++) {
                            if (reachableSquares[k][l] != -1) {
                                nextState[newPosition[0]][newPosition[1]] = nextState[i][j];
                                nextState[i][j] = "";
                            }
                        }
                    }
                }
            }
        }

        return nextState;
    }

    /**
     * Method to get the valid moves for the current state of the game.
     *
     * @return the valid moves for the current state of the game
     */
    public int[] getValidMoves(String[][] state) {
        // Initialize the validMoves array
        int[] validMoves = new int[this.actionSize];

        // fill validMoves array with zeros
        Arrays.fill(validMoves, 0);

        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[0].length; j++) {
                String teamID = this.boardModel.getTeamID();
                if (state[i][j].startsWith("p:") && state[i][j].contains(teamID)) {
                    int[][] reachableSquares = this.boardModel.getReachableSquares(i, j);
                    for (int k = 0; k < state.length; k++) {
                        for (int l = 0; l < state[0].length; l++) {
                            if (reachableSquares[k][l] != -1) {
                                int row = state.length - 1 - k;
                                int column = state[0].length - 1 - l;
                                validMoves[row * state[0].length + column] = 1;
                            }
                        }
                    }
                }
            }
        }

        return validMoves;
    }


    /**
     * Method to get the value of the game.
     * Winner: 1, True
     * Draw: 0, True
     * else: 0, False
     *
     * @return the value of the game
     */
    public float getValue(int action) {
        int[] actionPosition = castActionToPosition(action);
        int result = 0;

        // Check if opponent's base contains one flag
        boolean lastFlag = false;
        for (Team team : this.boardModel.getGameState().getTeams()) {
            if (team  != null) {
                int[] basePosition = team.getBase();
                if (actionPosition[0] == basePosition[0] && actionPosition[1] == basePosition[1]) {
                    if (team.getFlags() == 1) {
                        lastFlag = true;
                    }
                }
            }
        }

        for (int i = 0; i < getOpponentsBasePositions().length; i++) {
            if (actionPosition[0] == getOpponentsBasePositions()[i][0] && actionPosition[1] == getOpponentsBasePositions()[i][1]) {
                if (lastFlag) {
                    result += 1;
                }
            }
        }

        return result;
    }

    /**
     * Method to check if the game is terminated.
     * Winner: 1, True
     * Draw: 0, True
     * else: 0, False
     *
     * @return true if the game is terminated, false otherwise
     */
    public boolean getTerminated(String[][] state, int action) {
        int[] actionPosition = castActionToPosition(action);

        // Check if opponent's base contains one flag
        boolean result = false;
        for (Team team : this.boardModel.getGameState().getTeams()) {
            if (team  != null) {
                int[] basePosition = team.getBase();
                if (actionPosition[0] == basePosition[0] && actionPosition[1] == basePosition[1]) {
                    if (team.getFlags() == 1) {
                        result = true;
                    }
                }
            }
        }

        // Check if the game is won
        for (int i = 0; i < getOpponentsBasePositions().length; i++) {
            if (actionPosition[0] == getOpponentsBasePositions()[i][0] && actionPosition[1] == getOpponentsBasePositions()[i][1]) {
                return result;
            }
        }

        // Check if the game is a draw
        return Arrays.stream(getValidMoves(state)).sum() == 0;
    }

    /**
     * Method to get the opponent's base positions.
     *
     * @return the opponent's base positions
     */
    public int[][] getOpponentsBasePositions() {
        Team[] teams = this.boardModel.getGameState().getTeams();
        List<int[]> opponentsBasePositions = new ArrayList<>();

        for (Team team : teams) {
            if (team != null && !team.getId().equals(this.teamID)) {
                int[] basePosition = team.getBase();
                opponentsBasePositions.add(basePosition);
            }
        }

        return opponentsBasePositions.toArray(new int[0][]);
    }

    /**
     * Method to get the team's base position.
     *
     * @return the team's base position
     */
    public int[] getTeamBasePosition() {
        Team[] teams = this.boardModel.getGameState().getTeams();
        int[] teamBasePosition = new int[2];

        for (Team team : teams) {
            if (team != null && team.getId().equals(this.teamID)) {
                teamBasePosition = team.getBase();
                break;
            }
        }

        return teamBasePosition;
    }

    /**
     * Method to get the opponent's team identifier.
     *
     * @return the current player
     */
    public String getOpponent(String id) {
        String opponentTeamID = null;

        // Assuming there are only two teams
        for (Team team : this.boardModel.getGameState().getTeams()) {
            if (team  != null && !team.getId().equals(id)) {
                opponentTeamID = team.getId();
                break;
            }
        }

        return opponentTeamID;
    }

    /**
     * Method to get the opponent's value.
     *
     * @param value the value of the opponent
     * @return the value of the opponent
     */
    public float getOpponentValue(float value) {
        return -value;
    }

    /**
     * Method to change the perspective of the game.
     *
     * @return the state of the game
     */
    public String[][] changePerspective(String[][] rotatedState, String teamID) {
        String[][] perspectiveChangedState = copyState(rotatedState);

        if (teamID.equals(this.teamID)) {
            return perspectiveChangedState;
        }

        String opponentTeamID = null;

        for (Team team : this.boardModel.getGameState().getTeams()) {
            if (team != null && !team.getId().equals(this.teamID)) {
                opponentTeamID = team.getId();
                break;
            }
        }

        if (opponentTeamID == null) {
            throw new RuntimeException("Opponent's team ID not found");
        }

        for (int i = 0; i < perspectiveChangedState.length; i++) {
            for (int j = 0; j < perspectiveChangedState[i].length; j++) {
                String cell = perspectiveChangedState[i][j];
                if (cell.contains(this.teamID)) {
                    perspectiveChangedState[i][j] = cell.replace(this.teamID, opponentTeamID);
                } else if (cell.contains(opponentTeamID)) {
                    perspectiveChangedState[i][j] = cell.replace(opponentTeamID, this.teamID);
                }
            }
        }

        return perspectiveChangedState;
    }

    /**
     * Method to encode the state of the game.
     *
     * @param state the state of the game
     * @return the encoded state of the game
     */
    public float[][][] getEncodedState(String[][] state) {
        int height = state.length;
        int width = state[0].length;
        float[][][] encodedState = new float[3][height][width];

        // fill the encodedState with 0.0f
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    encodedState[i][j][k] = 0.0f;
                }
            }
        }

        // Encode the state of the game
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                String cell = state[i][j];
                if (!cell.isEmpty() && !cell.contains(teamID)) {
                    encodedState[0][i][j] = 1.0f; // Enemy
                } else if (cell.isEmpty()) {
                    encodedState[1][i][j] = 1.0f; // Empty
                } else if (cell.contains(teamID)) {
                    encodedState[2][i][j] = 1.0f; // Piece or base
                }
            }
        }

        return encodedState;
    }

    /**
     * Method to make the best move in the game.
     */
    public void makeBestMove() {
        // Check if it's the current team's turn
        int teamNumber = this.boardModel.getTeamNumberByID(this.teamID);
        if (this.boardModel.getGameState().getCurrentTeam() == teamNumber) {
            String[][] neutralState = this.boardModel.getGrid();

            // If the opponent's base is already reachable, make the move
            for (int i = 0; i < neutralState.length; i++) {
                for (int j = 0; j < neutralState.length; j++) {
                    if (neutralState[i][j].startsWith("p:") && neutralState[i][j].contains(teamID)) {
                        int[][] reachableSquares = this.boardModel.getReachableSquares(i, j);
                        for (int k = 0; k < this.rowCount; k++) {
                            for (int l = 0; l < this.columnCount; l++) {
                                if (reachableSquares[k][l] != -1 && neutralState[k][l].contains("b:")) {
                                    try {
                                        boardModel.makeMove(i, j, k, l);
                                        break;
                                    } catch (Exception e) {
                                        System.out.println("Error making move");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Get the probabilities of the actions
            float[][] actionProbs = mcts.parallelSearch(neutralState);
            int bestAction = findBestAction(actionProbs);

            // Convert the action to a position
            int[] toCoordinates = castActionToPosition(bestAction);

            // Search through the grid to find the piece to move to the best position
            for (int i = 0; i < this.boardModel.getGrid().length; i++) {
                for (int j = 0; j < this.boardModel.getGrid().length; j++) {
                    if (this.boardModel.getGrid()[i][j].startsWith("p:") && this.boardModel.getGrid()[i][j].contains(teamID)) {
                        int[][] reachableSquares = this.boardModel.getReachableSquares(i, j);
                        if (reachableSquares[toCoordinates[0]][toCoordinates[1]] != -1) {
                            try {
                                boardModel.makeMove(i, j, toCoordinates[0], toCoordinates[1]);
                                break;
                            } catch (Exception e) {
                                System.out.println("Error making move");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to find the best action.
     *
     * @param results the results of the MCTS algorithm
     * @return the best action
     */
    public int findBestAction(float[][] results) {
        int bestAction = -1;
        float bestProbability = Float.NEGATIVE_INFINITY;

        for (float[] result : results) {
            for (int j = 0; j < result.length; j++) {
                if (result[j] > bestProbability) {
                    bestProbability = result[j];
                    bestAction = j;
                }
            }
        }

        return bestAction;
    }

    /**
     * Method to convert the action to a position in the game.
     *
     * @param action the action to convert
     * @return the position of the action
     */
    public int[] castActionToPosition(int action) {
        int row = this.rowCount - 1 - action / this.rowCount;
        int column = this.columnCount - 1 - action % this.columnCount;
        return new int[]{row, column};
    }

    /**
     * Method to copy the state of the game.
     *
     * @param state the state of the game
     * @return the copy of the state
     */
    public String[][] copyState(String[][] state) {
        String[][] copy = new String[state.length][state[0].length];
        for (int i = 0; i < state.length; i++) {
            System.arraycopy(state[i], 0, copy[i], 0, state[0].length);
        }
        return copy;
    }

    /**
     * Method to get the team identifier.
     *
     * @return the team identifier
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Method to get the team identifier.
     *
     * @return the team identifier
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Method to get the team identifier.
     *
     * @return the team identifier
     */
    public int getActionSize() {
        return this.actionSize;
    }

    /**
     * Method to get the team identifier.
     *
     * @return the team identifier
     */
    public String getTeamID() {
        return this.teamID;
    }

    /**
     * Method to get the board model.
     *
     * @return the board model
     */
    public BoardModel getBoardModel() {
        return this.boardModel;
    }

    /**
     * Method to get NDManager.
     *
     * @return the NDManager
     */
    public NDManager getManager() {
        return manager;
    }

    /**
     * Main method to start the MCTSClient (for testing purposes).
     *
     * @param args the command line arguments
     * @throws URISyntaxException   if the URI is invalid
     */
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        URI serverURL = new URI("http://localhost:8888"); // Replace with your server URL
        String gameSessionID = "279093b9-7c64-4a00-9942-d503549177bf"; // Replace with your game session ID
        String teamID = "mcts"; // Replace with your (wanted) team ID

        // Create a new instance of the MCTSClient
        BoardModel boardModel = new BoardModel(serverURL, gameSessionID, teamID);
        MCTSClient client = new MCTSClient(boardModel);
    }
}
