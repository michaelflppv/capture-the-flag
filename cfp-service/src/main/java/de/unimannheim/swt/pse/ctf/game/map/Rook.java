package de.unimannheim.swt.pse.ctf.game.map;

/**
 * Class to describe the Type "Rook" as a Description of a Piece
 * @author mfilippo
 * @version 15.03.2024
 */
public class Rook extends PieceDescription {
    public Rook() {
        // initializing the type, attack power and count of the rook
        this.setType("Rook");
        this.setAttackPower(5);
        this.setCount(2);

        // initializing the movement of the rook
        Movement movement = new Movement();

        Directions directions = new Directions();
        directions.setLeft(2);
        directions.setRight(2);
        directions.setUp(2);
        directions.setDown(2);

        movement.setDirections(directions);
        this.setMovement(movement);
    }
}
