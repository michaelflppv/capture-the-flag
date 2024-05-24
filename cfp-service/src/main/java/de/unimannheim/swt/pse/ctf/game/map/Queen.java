package de.unimannheim.swt.pse.ctf.game.map;

/**
 * Class to describe the Type "Queen" as a Description of a Piece
 * @author mfilippo
 * @version 15.03.2024
 */
public class Queen extends PieceDescription {
    public Queen() {
        // initializing the type, attack power and count of the queen
        this.setType("Queen");
        this.setAttackPower(5);
        this.setCount(1);

        // initializing the movement of the queen
        Movement movement = new Movement();

        Directions directions = new Directions();
        directions.setLeft(2);
        directions.setRight(2);
        directions.setUp(2);
        directions.setDown(2);
        directions.setUpLeft(2);
        directions.setUpRight(2);
        directions.setDownLeft(2);
        directions.setDownRight(2);

        movement.setDirections(directions);
        this.setMovement(movement);
    }
}
