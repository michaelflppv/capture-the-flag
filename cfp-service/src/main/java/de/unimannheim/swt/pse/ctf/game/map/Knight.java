package de.unimannheim.swt.pse.ctf.game.map;

/**
 * Class to describe the Type "Knight" as a Description of a Piece
 * @author mfilippo
 * @version 15.03.2024
 */
public class Knight extends PieceDescription {
public Knight() {
    // initializing the type, attack power and count of the rook
    this.setType("Knight");
    this.setAttackPower(3);
    this.setCount(2);

    // initializing the shape of the knight
    Movement movement = new Movement();

    Shape shape = new Shape();
    shape.setType(ShapeType.lshape);

    movement.setShape(shape);
    this.setMovement(movement);
    }
}
