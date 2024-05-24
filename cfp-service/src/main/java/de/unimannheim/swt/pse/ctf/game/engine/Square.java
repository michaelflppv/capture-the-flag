package de.unimannheim.swt.pse.ctf.game.engine;

/**
 * Class to represent a square in the grid, mainly for use in the RespawnHelperMethods class.
 * Besides their position, squares have additional attributes to save all the relevant data for BFS.
 */
public class Square implements Comparable{

    private int row;
    private int column;
    private int[] positionInGridArray;
    private int indexInAdjacencyList;
    //boolean is false by default
    private boolean isFreeSquare;
    private int distanceToBase = Integer.MAX_VALUE;
    private ColorForBFS colorForBFS = ColorForBFS.WHITE;


    public Square(int row, int column, int maxColumn, String entryInGrid){
        this.row = row;
        this.column = column;
        this.positionInGridArray = new int[]{row, column};
        this.indexInAdjacencyList = (row * maxColumn) + column;

        this.isFreeSquare = entryInGrid.isEmpty();
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int[] getPositionInGridArray() {
        return positionInGridArray;
    }

    public void setPositionInGridArray(int[] positionInGridArray) {
        this.positionInGridArray = positionInGridArray;
    }

    public int getIndexInAdjacencyList() {
        return indexInAdjacencyList;
    }

    public void setIndexInAdjacencyList(int indexInAdjacencyList) {
        this.indexInAdjacencyList = indexInAdjacencyList;
    }

    public boolean isFreeSquare() {
        return isFreeSquare;
    }

    public void setFreeSquare(boolean freeSquare) {
        isFreeSquare = freeSquare;
    }

    public int getDistanceToBase() {
        return distanceToBase;
    }

    public void setDistanceToBase(int distanceToBase) {
        this.distanceToBase = distanceToBase;
    }

    public ColorForBFS getColor() {
        return colorForBFS;
    }

    public void setColorGray() {
        this.colorForBFS = ColorForBFS.GRAY;
    }

    public void setColorBlack() {
        this.colorForBFS = ColorForBFS.BLACK;
    }

    public boolean getIsFreeSquare(){
        return this.isFreeSquare;
    }

    @Override
    public String toString(){
        String toString = "\n--------------";
        toString += "Square row " + this.getRow() + ", column " + this.getColumn();
        toString += "\tIndex in adjacency list: " + this.getIndexInAdjacencyList();
        toString += "\tFree square: " + this.getIsFreeSquare();
        return toString;
    }


    /*
    necessary so that the squares can be added to a queue in the BFS
    However, the compareTo() method is never used, hence the return value is arbitrary
     */
    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
