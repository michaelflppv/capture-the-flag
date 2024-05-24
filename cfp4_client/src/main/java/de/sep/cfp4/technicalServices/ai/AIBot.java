package de.sep.cfp4.technicalServices.ai;

import de.unimannheim.swt.pse.ctf.game.state.Move;

import java.util.Collection;

/**
 * Interface for the different AI Bot difficulties
 *
 * @author jgroehl
 * @version 0.0.4
 */
public interface AIBot {
    Move getNextMove();

    Collection<RankedMove> calculateMoves();
}
