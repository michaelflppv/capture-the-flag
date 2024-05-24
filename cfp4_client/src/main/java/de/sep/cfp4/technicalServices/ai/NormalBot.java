package de.sep.cfp4.technicalServices.ai;

import de.sep.cfp4.application.model.BoardModel;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import de.unimannheim.swt.pse.ctf.game.map.Directions;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;


/**
 * Class for the normal AI Bot
 *
 * @author jgroehl
 * @version 0.0.4
 */
public class NormalBot implements AIBot{

    private final BoardModel board;
    private final String teamID;
    private Move nextMove;
    private Piece enemyPiece;
    private RankedMove rankedMove;

    public NormalBot(BoardModel boardModel)
        throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots {
        this.board = boardModel;
        this.teamID = board.getTeamID();
        this.startClient();
    }

    /**
     * Starts the AI-Client, triggers getNextMove() when it's the turn of the AI-Client and sends the MoveRequest.
     *
     */
    public void startClient(){
        System.out.println("Started Normal Bot");
        while (this.board.isGameInProgress()) {
            synchronized (this.board.getLock()) {
                if(this.board.getCurrentTeam() == this.board.getTeamNumberByID(teamID)){
                    try {
                        // Get next move and the corresponding piece coordinates
                        nextMove = getNextMove();
                        int pieceRow = 0;
                        int pieceColumn = 0;
                        for(int x = 0; x < board.getGrid().length; x++) {
                            for (int y = 0; y < board.getGrid()[x].length; y++) {
                                if(board.getGrid()[x][y].equals("p:" + teamID + "_" + nextMove.getPieceId())){
                                    pieceRow = x;
                                    pieceColumn = y;
                                }
                            }
                        }

                        // Make move request
                        board.makeMove(pieceRow, pieceColumn, nextMove.getNewPosition()[0],nextMove.getNewPosition()[1]);
                        this.board.getLock().wait(1000);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Returns the best move for the normal AI bot, considering all possible moves.
     * The Normal bot considers the current as well as the next move.
     *
     * @return best possible move of the AI bot
     */
    @Override
    public Move getNextMove() {
        TreeSet<RankedMove> rankedMoves = calculateMoves();
        RankedMove bestMove = rankedMoves.first();
        return bestMove.getMove();
    }

    /**
     * Returns a ranked (sorted) TreeSet of all possible Moves.
     *
     * @return set of ranked moves
     */
    @Override
    public TreeSet<RankedMove> calculateMoves() {
        // The TreeSet orders RankedMoves based on their Rank
        TreeSet<RankedMove> rankedMoves = new TreeSet<RankedMove>(new Comparator<RankedMove>() {
            @Override
            public int compare(RankedMove m1, RankedMove m2) {
                return (int)(m2.getRank() - m1.getRank()) == 0 ? Integer.parseInt(m2.getMove().getPieceId()) - Integer.parseInt(m1.getMove().getPieceId()): (int)(m2.getRank() - m1.getRank());
            }
        });

        int[][] reachableSquares;
        int[] newPosition;
        Move move;

        // Every piece has to be checked concerning its valid moves
        for(int x = 0; x < board.getGrid().length; x++){
            for(int y = 0; y < board.getGrid()[x].length; y++) {
                if(board.getGrid()[x][y].startsWith("p:" + teamID)) {
                    // Get all possible moves of the Piece p
                    reachableSquares = board.getReachableSquares(x, y);
                    for (int row = 0; row < reachableSquares.length; row++) {
                        for (int column = 0; column < reachableSquares[row].length; column++) {
                            if (reachableSquares[row][column] >= 0) {
                                move = new Move();
                                move.setPieceId(board.getGrid()[x][y].substring(board.getGrid()[x][y].lastIndexOf("_") + 1));
                                newPosition = new int[]{row, column};
                                move.setNewPosition(newPosition);
                                rankedMove = new RankedMove(move);
                                String[][] gridAfterMove = new String[board.getGrid().length][board.getGrid()[0].length];
                                for(int i=0; i< gridAfterMove.length; i++) {
                                    for (int j = 0; j < gridAfterMove[i].length; j++) {
                                        if(i==row && j==column){
                                            gridAfterMove[i][j] = "p:" + teamID + "_" + move.getPieceId();
                                        }else if(i==x && j==y){
                                            gridAfterMove[i][j] = "";
                                        }else{
                                            gridAfterMove[i][j] = this.board.getGrid()[i][j];
                                        }
                                    }
                                }
                                rankedMove.setRank(rankMove(row,column,reachableSquares) + rankFutureMove(row, column, gridAfterMove,this.board.getPieceByID(this.board.getGrid()[x][y])));
                                rankedMoves.add(rankedMove);
                            }
                        }
                    }
                }
            }
        }

        return rankedMoves;
    }

    /**
     * Ranks a move based on a pieces position
     * Only directly possible moves are considered and none in the future.
     *
     * @param row The piece wants to move to
     * @param column The piece wants to move to
     * @param reachableSquares All the reachable squares of the piece
     * @return a rank for a Move
     */
    private double rankMove(int row, int column, int[][] reachableSquares) {
        if(reachableSquares[row][column] > 0){
            if(board.getGrid()[row][column].startsWith("b:")){
                return 1000.0;
            }else{
                enemyPiece = board.getPieceByID(board.getGrid()[row][column]);
                return enemyPiece.getDescription().getAttackPower();
            }
        }
        return 0.0;
    }

    /**
     * Ranks a future move based on a pieces position
     * Only directly possible moves are considered and none in the future.
     *
     * @param row The piece wants to move to
     * @param column The piece wants to move to
     * @param grid The virtual grid after the move has been completed
     * @param piece The piece that wants to mak ethe move
     * @return a rank for a Move
     */
    private double rankFutureMove(int row, int column, String[][] grid, Piece piece) {
        int[] base = new int[2];
        double rank = 0.0;
        // Determine the position of the own base
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(grid[i][j].startsWith("b:" + piece.getTeamId())){
                    base = new int[]{i, j};
                }
            }
        }

        // Check if the piece itself can be beaten
        if(canBeBeaten(row, column, grid, piece)){
            rank = -1 * piece.getDescription().getAttackPower();
        }
        // Check if the own base can be attacked
        else if(canBeBeaten(base[0], base[1], grid, piece)){
            rank = -1000;
        }
        // Check future move
        else{
            int [][] reachableSquares = getReachableSquares(row, column, grid, piece);
            for (int i = 0; i < reachableSquares.length; i++) {
                for (int j = 0; j < reachableSquares[i].length; j++) {
                    if (reachableSquares[i][j] >= 0) {
                        if (reachableSquares[i][j] > 0) {
                            if (grid[i][j].startsWith("b:")) {
                                rank += 25;
                            } else {
                                enemyPiece = board.getPieceByID(grid[i][j]);
                                rank += enemyPiece.getDescription().getAttackPower() * 0.6;
                            }
                            }
                    }
                }
            }
        }
        return rank;
    }

    /**
     * Evaluates whether a piece/or base can be beaten by an opponent
     * Only directly possible moves are considered and none in the future.
     * @param row Of the piece/base to check
     * @param column Of the piece/base to check
     * @param grid The virtual grid after the move has been completed
     * @param piece The piece that wants to make the move
     * @return boolean if piece can be beaten
     */
    private boolean canBeBeaten(int row, int column, String[][] grid, Piece piece) {
        // Check all pieces that are not with the same teamID as the players
        for(int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if(grid[x][y].startsWith("p:") && !grid[x][y].contains(piece.getTeamId())) {
                    if (getReachableSquares(x,y,grid,board.getPieceByID(grid[x][y]))[row][column] > 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns a boolean array representing the squares a piece can move to.
     *
     * This is Copied from BoardModel and only adapted to handle "future" grids
     *
     * @param row    The row the piece is on.
     * @param column The column the piece in on.
     * @param grid A virtual grid that represents the GameState after a move in the future.
     * @param piece The piece representation of the piece that has moved
     * @return A boolean array representing the squares a piece can move to.
     */
    public int[][] getReachableSquares(int row, int column, String[][] grid, Piece piece) {
        int gridHeight = grid.length;
        int gridWidth = grid[0].length;

        // Default value of squares is automatically false
        int[][] reachableSquares = new int[gridHeight][gridWidth];

        // Initialize reachableSquares array with -1, meaning the square is not reachable.
        // Improved by IntelliSense
        for (int[] reachableSquare : reachableSquares) {
            Arrays.fill(reachableSquare, -1);
        }

        // If piece is null, return the current state of reachableSquares (all -1)
        if (piece == null) {
            return reachableSquares;
        }

        // Piece has a shape, so it can move in a specific way, allows for easily adding more shapes in the future.
        if (piece.getDescription().getMovement().getShape() != null) {
            return switch (piece.getDescription().getMovement().getShape().getType()) {
                case lshape -> {
                    int[][] positionOffset = new int[][]{{-2, -1}, {-2, 1}, {-1, 2}, {1, 2}, {2, 1}, {2, -1},
                            {1, -2}, {-1, -2}};
                    for (int[] offset : positionOffset) {
                        int newRow = row + offset[0];
                        int newColumn = column + offset[1];
                        if (!squareOutsideBoard(newRow, newColumn, grid)) {
                            reachableSquares[newRow][newColumn] = squareReachable(newRow, newColumn,
                                    piece.getDescription().getAttackPower(), grid, piece);
                        }
                    }
                    yield reachableSquares;
                }
                default -> {
                    yield reachableSquares;
                }
            };
        }

        Directions movement = piece.getDescription().getMovement().getDirections();
        int[] moves = new int[]{movement.getLeft(), movement.getRight(), movement.getUp(),
                movement.getDown(), movement.getUpLeft(), movement.getUpRight(), movement.getDownLeft(),
                movement.getDownRight()};

        // Directions for left, right, up, down, upLeft, upRight, downLeft, downRight
        int[][] moveDirections = new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}, {-1, -1}, {-1, 1},
                {1, -1}, {1, 1}};

        for (int i = 0; i < moves.length; i++) {
            for (int j = 1; j <= moves[i]; j++) {
                int newRow = row + j * moveDirections[i][0];
                int newColumn = column + j * moveDirections[i][1];

                // Stops the loop if a square is outside the board, since all the following squares will be outside the board as well.
                if (this.squareOutsideBoard(newRow, newColumn, grid)) {
                    break;
                }

                reachableSquares[newRow][newColumn] = squareReachable(newRow, newColumn,
                        piece.getDescription().getAttackPower(), grid, piece);

                // If the square is not empty, the piece can not move further in this direction.
                if (reachableSquares[newRow][newColumn] != 0) {
                    break;
                }
            }
        }
        return reachableSquares;
    }

    /**
     * Returns whether a square specified by row and column is outside the game board.
     *
     * This is Copied from BoardModel and only adapted to handle "future" grids
     *
     * @param row    The row the square is on.
     * @param column The column the square in on.
     * @param grid A virtual grid that represents the GameState after a move in the future.
     * @return if the square is outside the board.
     */
    private boolean squareOutsideBoard(int row, int column,String[][] grid) {
        return row < 0 || row >= grid.length || column < 0
                || column >= grid[0].length;
    }

    /**
     * Returns whether a square is reachable by a piece.
     *
     * This is Copied from BoardModel and only adapted to handle "future" grids
     *
     * @param row         The row the piece is on.
     * @param column      The column the piece in on.
     * @param attackPower The attack power of the piece.
     * @param grid         A virtual grid that represents the GameState after a move in the future.
     * @param playersPiece The piece of the current player.
     * @return if the square is reachable. -1: Square not reachable, 0: Square reachable (empty
     * square), 1: Square reachable (enemy piece)
     */
    private int squareReachable(int row, int column, int attackPower, String[][] grid, Piece playersPiece) {
        String team = playersPiece.getTeamId();

        // The specified square is outside the board.
        if (this.squareOutsideBoard(row, column, grid)) {
            return -1;
        }

        String squareString = grid[row][column];
        if (squareString.isEmpty()) {
            return 0;
        }
        if (squareString.equals("b")) {
            return -1;
        }
        if (squareString.startsWith("b:")) {
            if (squareString.endsWith(team)) {
                // Square not reachable if own base.
                return -1;
            } else {
                // Must be enemy base
                return 1;
            }
        }

        // Must be a piece
        Piece piece = this.board.getPieceByID(grid[row][column]);
        if (piece.getTeamId().equals(team)) {
            return -1;
        }

        // Must be enemy piece
        if (piece.getDescription().getAttackPower() <= attackPower) {
            return 1;
        } else {
            return -1;
        }

    }

        /**
         * Method to test functionality of the client during development: Simply edit the gameSessionID and start this method.
         */
    public static void main(String[] args) throws URISyntaxException {
        try {
            URI serverURL = new URI("http://localhost:8888"); // Replace with your server URL
            String gameSessionID = "ccdb2127-3d6c-4c3e-9efd-4d8c6449ceb3"; // Replace with your game session ID
            String team = "normal-BOT"; // Replace with your (wanted) team ID
            BoardModel boardModel = new BoardModel(serverURL,gameSessionID,team);

            NormalBot easy = new NormalBot(boardModel);
        } catch (IOException | InterruptedException | GameSessionNotFound | NoMoreTeamSlots e) {
            e.printStackTrace();
        }
    }
}
