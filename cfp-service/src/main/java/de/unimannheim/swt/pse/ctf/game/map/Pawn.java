package de.unimannheim.swt.pse.ctf.game.map;

/**
 * Class to describe the Type "Pawn" as a Description of a Piece
 * @author mfilippo
 * @version 15.03.2024
 */
public class Pawn extends PieceDescription {
    public Pawn() {
        // initializing the type, attack power and count of the pawn
        this.setType("Pawn");
        this.setAttackPower(1);
        this.setCount(10);

        // initializing the movement of the pawn
        Movement movement = new Movement();

        Directions directions = new Directions();
        directions.setUp(1);
        directions.setUpLeft(1);
        directions.setUpRight(1);

        movement.setDirections(directions);
        this.setMovement(movement);
    }
}
