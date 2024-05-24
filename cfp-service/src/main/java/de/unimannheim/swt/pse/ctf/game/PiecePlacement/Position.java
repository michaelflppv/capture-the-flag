package de.unimannheim.swt.pse.ctf.game.PiecePlacement;

/**
 * This class is being used to store the position of a piece on the grid
 * It contains the row index, column index and the piece id
 *
 * @author mfilippo
 * @version 25.03.2024
 */
public class Position {
    // row index of the piece
    private int rowIndex;

    // column index of the piece
    private int columnIndex;

    // id of the piece
    private String pieceId;

    /**
     * Constructor for the Position class
     *
     * @param rowIndex {@link int} the row index of the piece
     * @param columnIndex {@link int} the column index of the piece
     * @param pieceId {@link String} the id of the piece
     */
    public Position(int rowIndex, int columnIndex, String pieceId) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.pieceId = pieceId;
    }

    /**
     * Getter for the row index
     *
     * @return int the row index of the piece
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     * Getter for the column index
     *
     * @return int the column index of the piece
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * Getter for the piece id
     *
     * @return String the id of the piece
     */
    public String getPieceId() {
        return this.pieceId;
    }
}
