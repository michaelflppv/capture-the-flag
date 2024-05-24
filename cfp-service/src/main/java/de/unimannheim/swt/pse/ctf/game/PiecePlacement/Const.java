package de.unimannheim.swt.pse.ctf.game.PiecePlacement;

import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;

/**
 * This class is being used to store the constants for the PiecePlacement class
 * The constants are used to define the number of bases and the minimum number of borders
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 02.05.2024
 */
public class Const {
    /* Bases have the highest priority by placing square objects, so their amount is constant
     * The quantity can be changed if necessary
     */
    public final int baseNumber;

    /* Placing the pieces has higher priority than placing the borders,
       so the minimal amount of borders can be 0 or more by certain conditions
    */
    public final int minBordersNumber;

    /**
     * This constructor is used to initialize the baseNumber and minBordersNumber
     *
     * @param mapTemplate {@link MapTemplate} the map template
     */
    public Const(MapTemplate mapTemplate) {
        this.baseNumber = 1;
        this.minBordersNumber = mapTemplate.getBlocks();
    }

    /**
     * This method is used to get the base number
     *
     * @return baseNumber
     */
    public int getBaseNumber() {
        return baseNumber;
    }

    /**
     * This method is used to get the minimum number of borders
     *
     * @return minBordersNumber
     */
    public int getMinBordersNumber() {
        return minBordersNumber;
    }
}
