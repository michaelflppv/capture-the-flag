package de.unimannheim.swt.pse.ctf.game.map;

import de.unimannheim.swt.pse.ctf.game.state.Piece;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

/**
 * This class describes a {@link Piece} in a game.
 */
public class PieceDescription {

    @Schema(
            description = "unique piece type (e.g., 'Pawn' or 'Rook')"
    )
    private String type;
    @Schema(
            description = "attack power of piece (the higher, the more power it has)"
    )
    @Min(1)
    private int attackPower;
    @Schema(
            description = "the number of pieces of this type a team has"
    )
    @Min(1)
    private int count;
    @Schema(
            description = "possible movements of a piece"
    )
    private Movement movement;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }
}
