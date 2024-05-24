package de.unimannheim.swt.pse.ctf.game.map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class represents possible piece movements
 * in terms of squares to move in one or more direction(s).
 */
public class Directions {

    @Schema(
            description = "move N squares left"
    )
    private int left = 0;
    @Schema(
            description = "move N squares right"
    )
    private int right = 0;
    @Schema(
            description = "move N squares up"
    )
    private int up = 0;
    @Schema(
            description = "move N squares down"
    )
    private int down = 0;
    @Schema(
            description = "move N squares up-left (diagonal)"
    )
    private int upLeft = 0;
    @Schema(
            description = "move N squares up-right (diagonal)"
    )
    private int upRight = 0;
    @Schema(
            description = "move N squares down-left (diagonal)"
    )
    private int downLeft = 0;
    @Schema(
            description = "move N squares down-right (diagonal)"
    )
    private int downRight = 0;

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public int getDown() {
        return down;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public int getUpLeft() {
        return upLeft;
    }

    public void setUpLeft(int upLeft) {
        this.upLeft = upLeft;
    }

    public int getUpRight() {
        return upRight;
    }

    public void setUpRight(int upRight) {
        this.upRight = upRight;
    }

    public int getDownLeft() {
        return downLeft;
    }

    public void setDownLeft(int downLeft) {
        this.downLeft = downLeft;
    }

    public int getDownRight() {
        return downRight;
    }

    public void setDownRight(int downRight) {
        this.downRight = downRight;
    }

    /*
    added for practicality, yet does not endanger the functionality of the API
    and is in accordance with the requirements
     */
    public int[] getDirectionsAsArray(){
        int[] valueOfDirections = new int[8];
        valueOfDirections[0] = this.getLeft();
        valueOfDirections[1] = this.getRight();
        valueOfDirections[2] = this.getUp();
        valueOfDirections[3] = this.getDown();
        valueOfDirections[4] = this.getUpLeft();
        valueOfDirections[5] = this.getUpRight();
        valueOfDirections[6] = this.getDownLeft();
        valueOfDirections[7] = this.getDownRight();

        return valueOfDirections;
    }
}
