package de.sep.cfp4.technicalServices.ai;

import de.unimannheim.swt.pse.ctf.game.state.Move;

/**
 * Class for the Ranked Moves
 *
 * This class allows to have a representation of a move that also has a rank.
 *
 * @author jgroehl
 * @version 0.0.4
 */
public class RankedMove extends Move {

    public RankedMove(Move move){
        this.setPieceId(move.getPieceId());
        this.setNewPosition(move.getNewPosition());
        this.rank = 0.0;
    }
    private double rank;


    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public Move getMove() {
        Move move = new Move();
        move.setNewPosition(this.getNewPosition());
        move.setPieceId(this.getPieceId());
        return move;
    }
}
