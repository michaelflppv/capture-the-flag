package de.unimannheim.swt.pse.ctf.game.map;

/**
 * Class to describe the Type "King" as a Description of a Piece
 * @author mfilippo
 * @version 15.03.2024
 */
public class King extends PieceDescription {
    public King() {
        // initializing the type, attack power and count of the king
        this.setType("King");
        this.setAttackPower(1);
        this.setCount(1);

        // initializing the movement of the king
        Movement movement = new Movement();

        Directions directions = new Directions();
        directions.setLeft(1);
        directions.setRight(1);
        directions.setUp(1);
        directions.setDown(1);
        directions.setUpLeft(1);
        directions.setUpRight(1);
        directions.setDownLeft(1);
        directions.setDownRight(1);

        movement.setDirections(directions);
        this.setMovement(movement);
    }
}
