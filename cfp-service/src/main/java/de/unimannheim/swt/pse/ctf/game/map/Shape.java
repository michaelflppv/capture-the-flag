package de.unimannheim.swt.pse.ctf.game.map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class represents a shape movement (e.g., L-shape as known from chess).
 */
public class Shape {

    @Schema(
            description = "the type of movement"
    )
    private ShapeType type;

    public ShapeType getType() {
        return type;
    }

    public void setType(ShapeType type) {
        this.type = type;
    }
}
