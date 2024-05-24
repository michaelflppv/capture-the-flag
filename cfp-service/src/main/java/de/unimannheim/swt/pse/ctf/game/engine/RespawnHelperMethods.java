package de.unimannheim.swt.pse.ctf.game.engine;

import java.util.*;


public class RespawnHelperMethods {

    /**
     * The requirements demand that in a game with more than one flag in each base, a piece capturing an
     * opponent flag should respawn on a free square next to it's own base,
     * if the game is not over after capturing the flag (one or more flags remain in the opponent base).
     * Next to the piece's own base means directly adjacent (any of the 8 squares) or the next = closest possible
     * square.
     *-
     * Approach:
     * - represent squares of the grid as their own class, with attributes for relevant data
     * - represent the grid as a graph via an adjacency list
     * - calculate the shortest path from the pieces own base to the next nodes using breadth first search
     *   and save the distance
     * - among those which the minimal distance which are free chose one randomly
     */

    private HashMap<String, Square> squareByGridName = new HashMap<>();
    private String[][] grid;
    private String teamId;
    private Square baseOfTeam;
    private List<List<Square>> adjacencyList;
    private List<Square> candidatesForSpawning;

    /*
    getters and setters primarily for testing,
    may be removed afterwards
     */
    public HashMap<String, Square> getSquareByGridName() {
        return squareByGridName;
    }

    public void setSquareByGridName(HashMap<String, Square> squareByGridName) {
        this.squareByGridName = squareByGridName;
    }

    public String[][] getGrid() {
        return grid;
    }

    public void setGrid(String[][] grid) {
        this.grid = grid;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Square getBaseOfTeam() {
        return baseOfTeam;
    }

    public void setBaseOfTeam(Square baseOfTeam) {
        this.baseOfTeam = baseOfTeam;
    }

    public List<List<Square>> getAdjacencyList() {
        return adjacencyList;
    }

    public void setAdjacencyList(List<List<Square>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    /**
     * the main (in the sense of most important) method of the class, returning the position of the square in the grid,
     * on which the piece can spawn. teamId ist the id of the team
     * whose piece has landed on the opponent base in the last move
     * --- working partly as a constructor of the class, to make handling even more easy
     * this way, int[] positionToRespawn = new RespawnHelperMethods().getSquareToSpawn(grid, teamId);
     * is the only line we need, all the rest is handled in the class
     * -
     * @param grid
     * @param teamId
     * @return
     */
    public int[] getSquareToSpawn(String[][] grid, String teamId){
        this.grid = grid;
        this.teamId = teamId;
        fillAdjacencyList();
        return this.getSquareToSpawn();
    }

    public boolean isInsideGrid(int row, int column){
        //checking row and column coordinate
        if(row < 0 || row >= this.grid.length || column < 0 || column >= this.grid[0].length){
            return false;
        }
        return true;
    }

    /**
     * returns an adjacency list (list of lists), with each list containing the adjacent squares to
     * a given square. The order of the squares in the list is obtained by traversing the grid
     * through the rows, and in each row throw the columns.
     * The instances of square also safe the index of the list of their adjacent squares
     * @return
     */
    public List<List<Square>> fillAdjacencyList(){
        /*
        for each field in the grid,
        create an instance of Square
        calculate the max. 8 adjacent squares
        update the adjacencyList by adding a list of adjacent squares
        -
        along the way, determine position of base, next to which we want to respawn
         */

        //new linkedList for each call of the method, mainly for testing
        this.adjacencyList = new LinkedList<>();
        //new baseOfTeam, mainly for testing
        this.baseOfTeam = null;

        //generate the squares, and use references from here on
        for(int row = 0; row < this.grid.length; row++) {
            for (int column = 0; column < this.grid[0].length; column++) {
                squareByGridName.put((row + "_" + column), new Square(row, column, this.grid[0].length, this.grid[row][column]));
            }
        }


        LinkedList<Square> adjacentToNodeList;
        //idea is the same as in the ValidMoveHelperMethods
        int[][] stepToAdjacentSquares = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        int rowOfPossibleAdjacentSquare, columnOfPossibleAdjacentSquare;

        for(int row = 0; row < this.grid.length; row++){
            for(int column = 0; column < this.grid[0].length; column++){

                //search for base of team
                if(grid[row][column].equals("b:" + this.teamId)){
                    this.baseOfTeam = this.squareByGridName.get(row + "_" + column);
                }

                //doing the following procedure for each square in the grid
                adjacentToNodeList = new LinkedList<>();

                for(int[] step: stepToAdjacentSquares){
                    //visit all possible 8 adjacent squares
                    rowOfPossibleAdjacentSquare = row + step[0];
                    columnOfPossibleAdjacentSquare = column + step[1];

                    if(this.isInsideGrid(rowOfPossibleAdjacentSquare, columnOfPossibleAdjacentSquare)){
                        /*
                        found an adjacent square within the borders of the grid
                        add to list of adjacent squares of the current square
                         */
                        adjacentToNodeList.addLast(this.squareByGridName.get(rowOfPossibleAdjacentSquare + "_" + columnOfPossibleAdjacentSquare));
                    }
                }

                adjacencyList.add(adjacentToNodeList);
            }
        }

        return adjacencyList;
    }


    /**
     * performs a breath first search starting from the base of the team.
     * Each time we enter a new "layer" of nodes, i.e. the distance from the base increases by one,
     * check whether we already found at least one candidate for a square to spawn.
     * If so, choose one of the candidates randomly and return.
     * @return
     */
    public int[] getSquareToSpawn(){
        /*
        adjacency list needs to be filled before this method

        BFS
        and if the distance to the base increases by one, check whether we already can choose from the visited squares
        (return early, computation can probably terminate after the first loop pass in most cases)
         */
        Queue<Square> queue = new LinkedList<>();
        candidatesForSpawning = new LinkedList<>();

        this.baseOfTeam.setColorGray();
        this.baseOfTeam.setDistanceToBase(0);

        queue.add(this.baseOfTeam);

        while(!queue.isEmpty()){
            Square s = queue.remove();
            for(Square square: this.adjacencyList.get( s.getIndexInAdjacencyList() )){
                if(square.getColor().equals(ColorForBFS.WHITE)){
                    square.setColorGray();
                    square.setDistanceToBase(s.getDistanceToBase() + 1);
                    queue.add(square);

                    if(square.isFreeSquare()){
                        candidatesForSpawning.add(square);
                    }
                }
            }
            s.setColorBlack();
            /*
            at this point, candidatesForSpawning should contain >= 0 free squares
            which all have the same distance to the base
             */

            /*
            for testing purposes, put the following if-statement in a comment
             */
            if(!candidatesForSpawning.isEmpty()){
                //have found at least one square of the distance of s + 1
                break;
            }

        }

        //randomly choose a square out of the candidates
        int chosenSquare = (int) (Math.random() * candidatesForSpawning.size());
        try{
            return candidatesForSpawning.get(chosenSquare).getPositionInGridArray();
        }catch(IndexOutOfBoundsException indexOutOfBoundsException){
            //no square to respawn, as no square was empty. (the .get above does not work)
            return null;
        }
    }

    public List<Square> getCandidatesForSpawning() {
        return candidatesForSpawning;
    }
}
