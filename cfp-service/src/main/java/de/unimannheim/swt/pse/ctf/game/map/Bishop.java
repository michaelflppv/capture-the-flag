package de.unimannheim.swt.pse.ctf.game.map;

/**
 * Class to describe the Type "Bishop" as a Description of a Piece
 * @author mfilippo
 * @version 15.03.2024
 */
public class Bishop extends PieceDescription {
    public Bishop() {
        // initializing the type, attack power and count of the bishop
        this.setType("Bishop");
        this.setAttackPower(3);
        this.setCount(2);

        // initializing the movement of the bishop
        Movement movement = new Movement();

        Directions directions = new Directions();
        directions.setUpLeft(2);
        directions.setUpRight(2);
        directions.setDownLeft(2);
        directions.setDownRight(2);

        movement.setDirections(directions);
        this.setMovement(movement);
    }
}
