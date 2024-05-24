package de.sep.cfp4.technicalServices.ai;

import de.sep.cfp4.application.model.BoardModel;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.exceptions.InvalidMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;


/**
 * Class for the easy AI Bot
 *
 * @author jgroehl
 * @version 0.0.4
 */
public class EasyBot implements AIBot{

    private final BoardModel board;
    private final String teamID;
    private Move nextMove;
    private Piece enemyPiece;
    private RankedMove rankedMove;

    public EasyBot(BoardModel boardModel)
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
        while (this.board.isGameInProgress()) {
            synchronized (this.board.getLock()) {
                if(this.board.getCurrentTeam() == this.board.getTeamNumberByID(teamID)){
                   try {
                       // Get next move and the corresponding piece coordinates
                       nextMove = getNextMove();
                       int pieceRow = 0;
                       int pieceColumn = 0;
                       for (int x = 0; x < board.getGrid().length; x++) {
                           for (int y = 0; y < board.getGrid()[x].length; y++) {
                               if (board.getGrid()[x][y].equals(
                                   "p:" + teamID + "_" + nextMove.getPieceId())) {
                                   pieceRow = x;
                                   pieceColumn = y;
                               }
                           }
                       }

                       // Make move request
                       board.makeMove(pieceRow, pieceColumn, nextMove.getNewPosition()[0],
                           nextMove.getNewPosition()[1]);
                       this.board.getLock().wait(1000);
                   } catch (InvalidMove e) {
                        System.out.println("EasyBot made an invalid move.");
                   } catch (IOException | InterruptedException e) {
                       throw new RuntimeException(e);
                   }
                }
            }
        }
    }

    /**
     * Returns a random move for the easy AI bot, considering all possible moves.
     *
     * @return random (but possible) move of the AI bot
     */
    @Override
    public Move getNextMove() {
        ArrayList<RankedMove> rankedMoves = calculateMoves();
        int random = (int)(Math.random() * rankedMoves.size());
        TreeSet<RankedMove> bestRankedMoves = new TreeSet<>(new Comparator<RankedMove>() {
            @Override
            public int compare(RankedMove m1, RankedMove m2) {
                return (int)(m2.getRank() - m1.getRank()) == 0 ? Integer.parseInt(m2.getMove().getPieceId()) - Integer.parseInt(m1.getMove().getPieceId()): (int)(m2.getRank() - m1.getRank());
            }
        });

        // If flag or opponent can be captured, execute the best possible move
        for(RankedMove m : rankedMoves){
            if(m.getRank() > 0){
                bestRankedMoves.addAll(rankedMoves);
                return bestRankedMoves.first().getMove();
            }
        }

        // Else execute a random (but possible) move
        RankedMove randMove = rankedMoves.get(random);
        return randMove.getMove();
    }

    /**
     * Returns a ranked ArrayList of all possible Moves.
     *
     * @return list of ranked moves
     */
    @Override
    public ArrayList<RankedMove> calculateMoves() {
        int[][] reachableSquares;
        ArrayList<RankedMove> rankedMoves = new ArrayList<>();
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
                                if(reachableSquares[row][column] > 0){
                                    if(board.getGrid()[row][column].startsWith("b:")){
                                        rankedMove.setRank(1000.0);
                                    }else{
                                        enemyPiece = board.getPieceByID(board.getGrid()[row][column]);
                                        rankedMove.setRank(enemyPiece.getDescription().getAttackPower());
                                    }
                                    rankedMoves.add(rankedMove);
                                }else {
                                    rankedMoves.add(new RankedMove(move));
                                }
                            }
                        }
                    }
                }
            }
        }
        return rankedMoves;
    }

    /**
     * Method to test functionality of the client during development: Simply edit the gameSessionID and start this method.
   */
    public static void main(String[] args) throws URISyntaxException {
        try {
            URI serverURL = new URI("http://localhost:8888"); // Replace with your server URL
            String gameSessionID = "279093b9-7c64-4a00-9942-d503549177bf"; // Replace with your game session ID
            String team = "easy-BOT"; // Replace with your (wanted) team ID
            BoardModel boardModel = new BoardModel(serverURL, gameSessionID, team);

            EasyBot easy = new EasyBot(boardModel);
        } catch (IOException | InterruptedException | GameSessionNotFound | NoMoreTeamSlots e) {
            e.printStackTrace();
        }
    }
}
