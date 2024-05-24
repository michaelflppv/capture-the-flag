package de.unimannheim.swt.pse.ctf.game.engine;

import de.unimannheim.swt.pse.ctf.game.PiecePlacement.PiecePlacement;
import de.unimannheim.swt.pse.ctf.game.exceptions.ForbiddenMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameOver;
import de.unimannheim.swt.pse.ctf.game.exceptions.InvalidMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import de.unimannheim.swt.pse.ctf.game.map.*;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameEngine implements Game{

    private GameState gameState;

    /*
     * hashmap, in which the pieces are saved with their name on the grid as key,
     * and reference to the orientedPiece as value
     * -
     * orientedPieces are the ones with possibly changed directions,
     * so that movements up and left intuitively mean approaching [0][0]
     */
    public HashMap<String, Piece> pieceByGridName = new HashMap<String,Piece>();

    /*
    idea: start is first move request, end is as soon as this.isGameOver() is true
    The Dates are set to 01.01.1970 by default -> new Date(0),
    to indicate an error if called before "actually" initialized
     */
    private Date startDate = new Date(0);
    private Date endDate = new Date(0);
    private Date dateOfLastMove = new Date(0);

    //to save the data from MapTemplate, as it would be lost after the this.create() method is done
    private int totalTimeLimitInSeconds, moveTimeLimitInSeconds;

    private int remainingTotalTimeInSeconds, remainingMoveTimeInSeconds;
    private int remainingTeamSlots;

    private String[] winners = new String[0];

    private ScheduledExecutorService scheduler;  // Executor service for handling scheduled tasks



    public GameEngine(){
        /*
        -
        do not call the this.create() in the constructor.
        -
        The GameSessionController creates a new instance of Game, then calls the create() method
        with a game session from a GameSessionRequest
        Then puts a new instance of GameSession (which handles the instance of Game (GameEngine, respectively) )
        in a map, with a randomly generated sessionId as it's key.
        Client communication with the instance of GameEngine is handled via the GameSessionController
        (sessionId and instance of request), who then calls the GameSession, whose only functions really are
        - to create team secrets
        - in order to check whether a team is allowed to make certain requests (called in GameSessionController
            lines 199 and 237 (makeMoveRequest and giveUpRequest), and the requests are answered with ForbiddenMoveException,
            if there is no match of actual teamSecret and given teamSecret.
         */
    }

    public void fillHashMap(){

        //is called from the joinGame method, as soon as all teams have joined

        ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();

        synchronized (this.gameState){
            for(Team t: gameState.getTeams()){
                for(Piece p: t.getPieces()){
                    String name = "p:" + p.getTeamId() + "_" + p.getId();
                    pieceByGridName.put(name, validMoveHelperMethods.orientedPiece(p, this.gameState));
                }
            }
        }
    }


    /**
     * Initializes a new game based on given {@link MapTemplate} configuration.
     *
     * @param template {@link MapTemplate}
     * @return GameState
     */

    @Override
    public GameState create(MapTemplate template) {
        //GameState gets initialized with the values from the MapTemplate
        this.gameState = new GameState();
        //this.gameState.setGrid(new String[template.getGridSize()[0]][template.getGridSize()[1]]);
        //this.gameState.setTeams(new Team[template.getTeams()]);
        this.gameState.setCurrentTeam(0);
        this.gameState.setLastMove(null);
        this.totalTimeLimitInSeconds = template.getTotalTimeLimitInSeconds();
        this.moveTimeLimitInSeconds = template.getMoveTimeLimitInSeconds();
        this.remainingTotalTimeInSeconds = this.totalTimeLimitInSeconds;
        this.remainingMoveTimeInSeconds = this.moveTimeLimitInSeconds;

        //Gets the number of Teams from the map Template
        int numberOfTeams = template.getTeams();
        this.remainingTeamSlots = numberOfTeams;
        /*
        Counts the number of total pieces of one team. In every piecedescription is an attribute count, which specifies the number of pieces of this type.
        We calculate the number of total pieces by adding all counts of all piecedescriptions together.
         */
        int numberOfPieces = 0;
        for(PieceDescription p :template.getPieces()) {
            numberOfPieces += p.getCount();
        }

        // Create a String array to store hexadecimal values of colors
        String[] colors = new String[4];

        // Assign hexadecimal values for each color
        colors[0] = "#FFFFFF"; // White
        colors[1] = "#000000"; // Black
        colors[2] = "#FF0000"; // Red
        colors[3] = "#FFFF00"; // Yellow

        /*
        Sets up the Teams. Gives every Team the pieces that are specified in the MapTemplate
        //FIXME Finish comment
         */
        Team[] teams = new Team[numberOfTeams];
        int pieceID = 1;
        //a represents Team 1, b represents Team 2, etc.
        String[] teamIdentfierChars = new String[]{"a","b","c","d"};
        for(int i = 0; i < numberOfTeams; i++) {
            Piece[] piecesForTeam = new Piece[numberOfPieces];
            for (int j = 0; j < template.getPieces().length; j++) {
                for (int k = 0; k < template.getPieces()[j].getCount(); k++) {
                    Piece piece = new Piece();
                    //This is a new representation of the piece ID: Its PieceID and a char that identifies the Team.
                    // This is done so that a piece can be uniquely identfied by its ID
                    piece.setId(String.valueOf(pieceID));
                    piece.setTeamId(String.valueOf(i));
                    piece.setDescription(template.getPieces()[j]);
                    piecesForTeam[pieceID - 1] = piece;
                    pieceID++;
                }
            }
            pieceID = 1;
            Team t = new Team();
            t.setPieces(piecesForTeam);
            t.setId(String.valueOf(i));
            t.setFlags(template.getFlags());

            //First Team is black, second team is white, third team is red, fourth team is yellow
            t.setColor(colors[i]);
            teams[i] = t;
        }
        this.gameState.setTeams(teams);

        /* Place the pieces on the grid
           @author mfilippo (Mikhail Filippov)
         */
        PlacementType placement = template.getPlacement();
        String[][] grid = new String[template.getGridSize()[0]][template.getGridSize()[1]];

        PiecePlacement piecePlacement = new PiecePlacement(template, grid, teams);
        piecePlacement.placePieces(teams[0].getPieces(), placement);
        this.gameState.setGrid(grid);

        //this.fillHashMap();
        /*
        cannot be called here, as the template (order of method calls see comment in the constructor) does
        not yet contain the teams.
        template.getTeams() only just returns the amount of teams (int value)
        -
        this.fillHashMap() is automatically called as soon as there are no more team slots available
         */

        return this.gameState;
    }

    /**
     * Get current state of the game
     *
     * @return GameState
     */
    @Override
    public GameState getCurrentGameState() {
        synchronized (this.gameState){
            return this.gameState;
        }
    }

    /**
     * Updates a game and its state based on team join request (add team).
     *
     * <ul>
     *     <li>adds team if a slot is free (array element is null)</li>
     *     <li>if all team slots are finally assigned, implicitly starts the game by picking a starting team at random</li>
     * </ul>
     *
     * @param teamId Team ID
     * @return Team
    //@throws NoMoreTeamSlots No more team slots available
     */

    @Override
    public Team joinGame(String teamId) throws NoMoreTeamSlots {
        if(this.getRemainingTeamSlots() <= 0){
            throw new NoMoreTeamSlots();
        }

        Team[] teams = this.getCurrentGameState().getTeams();
        int teamIndex = teams.length - this.getRemainingTeamSlots();
        Team joinedTeam = teams[teamIndex];

        //Check if there is already a team with the same ID.
        //Index for Team. Individual for each Team, ensures no duplicates.
        int index=0;
        for(Team t: teams) {
            if(t.getId().equals(teamId)){
                //This ensures that the teamID is always individual for a Team
                teamId = teamId + index;
            }
            index++;
        }

        joinedTeam.setId(teamId);
        Piece[] pieces = joinedTeam.getPieces();

        for (Piece piece : pieces) {
            piece.setTeamId(teamId);
        }

        String[][] grid = this.gameState.getGrid();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j].equals("b:" + teamIndex)) {
                    grid[i][j] = "b:" + teamId;
                } else if (grid[i][j].matches("p:" + teamIndex + "_.*")) {
                    grid[i][j] = grid[i][j].replaceFirst(String.valueOf(teamIndex), teamId);
                }
            }
        }

        this.remainingTeamSlots--;
        if(this.getRemainingTeamSlots() == 0){
            // Initialize the scheduler to manage timed tasks such as decrementing time limits
            scheduler = Executors.newSingleThreadScheduledExecutor();
            startScheduler();  // Start the scheduling of the time decrement tasks
            this.fillHashMap();
        }

        return teams[teamIndex];
    }

    /**
     * @return number of remaining team slots
     */
    @Override
    public int getRemainingTeamSlots() {
        return this.remainingTeamSlots;
    }

    /**
     * Make a move
     *
     * logic proceeds in the following steps:
     * check if the move is valid
     * updating the gameState in case of a valid move:
     * - clear former position of the moving piece in grid
     * - if necessary, update opponent piece list or reduce flag count by one
     * - - - if flag count is reduced and one or more flags remain,
     *       respawn piece on the next free square next to own base
     * - update position in the grid
     * - update position saved in the piece instance
     * update Date of the last move
     * check whether after the move, the game is over
     *
     *
     * @param move {@link Move}
     * @throws InvalidMove Requested move is invalid
     * @throws GameOver    Game is over
     */
    @Override
    public void makeMove(Move move) throws InvalidMove, GameOver {

        //Throw GameOver Exception if the Game is already finished
        if(this.isGameOver()){
            throw new GameOver();
        }

        //execution of method ends if any exception is thrown
        if(!this.isValidMove(move)){
            throw new InvalidMove();
        }

        if(this.startDate == null){
            /*
            null if it is the first valid move of the game,
            and this signals the game's starting time
             */
            this.startDate = new Date();
        }

        /*
        updating the gameState after a valid move
        - clear former position of the piece in grid
        - if necessary, update opponent piece list or reduce flag count by one
        - - - if flag count is reduced and one or more flags remain,
                respawn piece on the next free square next to own base
        - update position in the grid
        - update position saved in the piece instance
        - update Date of the last move
         */

        synchronized (this.gameState){

            //reference
            String[][] grid = this.gameState.getGrid();
            Team currentTeam = this.getCurrentGameState().getTeams()[this.getCurrentGameState().getCurrentTeam()];
            //clear former position
            A: for(int row = 0; row < grid.length; row++){
                for(int column = 0; column < grid[0].length; column++){
                    if(grid[row][column].equals("p:" + currentTeam.getId() + "_" + move.getPieceId())){
                        grid[row][column] = "";
                        break A;
                    }
                }
            }

            /*
            if applicable (changes for other team), update grid plus
            - move reaches opponent base: reduce number of flags by 1, respawn if necessary
            - move beats opponent piece: update HashMap, set piece in other team's array to null
             */
            String formerEntry = grid[move.getNewPosition()[0]][move.getNewPosition()[1]];
            //This flag indicates, if a piece captured the last flag from an opponent
            boolean lastFlagFromOpponentCaptured = false;
            if(formerEntry.matches("b:.+")) {
                // has to be an opponent base due to checks in the isValidMove() method
                String opponentTeamId = formerEntry.substring(2);
                //The Validmove method already made sure, that the move is allowed.
                Piece movingPiece = this.pieceByGridName.get("p:" + move.getTeamId() + "_" + move.getPieceId());

                for (int i = 0;i<this.gameState.getTeams().length;i++) {
                    Team t = this.gameState.getTeams()[i];
                    if (t != null && t.getId().equals(opponentTeamId)) {
                        t.setFlags(t.getFlags() - 1);

                        if (t.getFlags() > 0) {
                            //piece needs to respawn, and game is probably not yet over
                            int[] positionToRespawn = new RespawnHelperMethods().getSquareToSpawn(this.gameState.getGrid(), movingPiece.getTeamId());
                            this.gameState.getGrid()[positionToRespawn[0]][positionToRespawn[1]] = "p:" + move.getTeamId() + "_" + move.getPieceId();
                            movingPiece.setPosition(positionToRespawn);
                        }
                        else {
                            clearGridFromTeam(t);
                            lastFlagFromOpponentCaptured = true;
                            this.gameState.getTeams()[i] = null;
                        }

                        break;
                    }
                }
            } else if (formerEntry.matches("p:.+_.+")) {
                /*
                weaker or equally strong opponent piece,
                since if it was an own piece or stronger opponent move would have been invalid

                now the updating of the position is the same as for an empty square, and is done later
                 */

                //remove from the hashmap which saves all the pieces still on the board, and safe as reference
                Piece opponentPiece = this.pieceByGridName.remove(formerEntry);


                //update the opponent team's piece array
                //Create a new Piece Array without the piece that got captured
                //Go to all teams
                for (int i = 0; i < this.getCurrentGameState().getTeams().length; i++) {
                    //Find the team from which the piece has been captured
                    if (this.getCurrentGameState().getTeams()[i] != null && this.getCurrentGameState().getTeams()[i].getId().equals(opponentPiece.getTeamId())) {
                        Team t = this.getCurrentGameState().getTeams()[i];
                        //Create a new Piece Array without the piece that got captured
                        Piece[] newPieceArray = new Piece[t.getPieces().length - 1];
                        //Variable used for adding pieces into newPieceArray. Incremented everytime a new Piece is added into newPieceArray
                        int k = 0;
                        //Find the piece of the Team that has been captured
                        for (int j = 0; j < t.getPieces().length; j++) {
                            if (t.getPieces()[j].getId().equals(opponentPiece.getId())) {
                                /*
                                set the reference of the piece to null.
                                Procedure is coordinated with the isGameOver() method
                                The piece will not be in the new Piece Array of the Team
                                 */
                                //Removes the piece from the hashmap
                                pieceByGridName.remove("p:" + t.getId() + "_" + t.getPieces()[j].getId());
                                //Sets it to null
                                t.getPieces()[j] = null;
                            }
                            //If the team still has Pieces, then all the pieces that have not been captured will be added to the new Pieces Array and will remain in the Game
                            else if (newPieceArray.length != 0) {
                                newPieceArray[k] = t.getPieces()[j];
                                k++;
                            }
                        }

                        t.setPieces(newPieceArray);

                        //The last piece of the Team has been captured and the Team looses. The Team gets deleted from the GameState
                        if (t.getPieces().length == 0) {
                            clearGridFromTeam(t);
                            this.getCurrentGameState().getTeams()[i] = null;
                        }

                        break;
                    }
                }
            }

            //The Regex matches a single \ which is the empty field representation in the Grid
            if(formerEntry.isEmpty() || formerEntry.matches("p:.+_.+")||lastFlagFromOpponentCaptured){
                /*
                after updating the hashmap and the opponent team's piece array,
                it is the same procedure for free squares as for the ones where an opponent piece was
                 */

                //update new position in the grid
                grid[move.getNewPosition()[0]][move.getNewPosition()[1]] = "p:" + currentTeam.getId() + "_" + move.getPieceId();
                //update position of moving piece
                this.pieceByGridName.get("p:" + currentTeam.getId() + "_" + move.getPieceId()).setPosition(move.getNewPosition());

            }
            //Set the last Move in the GameState
            this.getCurrentGameState().setLastMove(move);
            //update the current team in gameState
            int nextTeam = (this.gameState.getCurrentTeam() + 1) % this.gameState.getTeams().length;
            this.gameState.setCurrentTeam(nextTeam);
            skipMove();

            //Reset the move timer
            this.remainingMoveTimeInSeconds = moveTimeLimitInSeconds;
        }

        /*
        as soon as processing is done and the new game state is sent,
        the Date of the last move is updated (latest possible occasion)
        -
        TODO: if there is any "more late" occasion, put this code there
         */
        this.dateOfLastMove = new Date();

    }
    /**
     * Method to check if the number of teams is valid
     *
     * @author mfilippo
     * @return true if the number of teams is valid, false otherwise
     */
    public boolean isValidTeamNumber() {
        return this.getRemainingTeamSlots() == 0;
    }


    /**
     * @return -1 if no total game time limit set, 0 if over, > 0 if seconds remain
     */
    @Override
    public int getRemainingGameTimeInSeconds() {
        return remainingTotalTimeInSeconds;
    }

    /**
     * @return -1 if no move time limit set, 0 if over, > 0 if seconds remain
     */
    @Override
    public int getRemainingMoveTimeInSeconds() {
        return remainingMoveTimeInSeconds;
    }

    /**
     * A team has to option to give up a game (i.e., losing the game as a result).
     * <p>
     * Assume that a team can only give up if it is its move (turn).
     *
     * @param teamId Team ID
     */
    @Override
    public void giveUp(String teamId) {

        /*
        the GameSessionController calls the isAllowed Method of the GameSession managing this instance of GameEngine,
        meaning that if this method is called by the GameSessionController the teamSecret has already been verified.
        Now need to check whether it is the teams turn.
         */

        //team giving up
        Team teamGivingUp = null;

        synchronized (this.gameState) {
            for (Team t : this.gameState.getTeams()) {
                if (t!=null && t.getId().equals(teamId)) {
                    teamGivingUp = t;
                    break;
                }
            }

            //id of the team whose turn it is
            String idOfCurrentTeam = this.gameState.getTeams()[this.gameState.getCurrentTeam()].getId();

            if (StringUtils.equals(idOfCurrentTeam, teamId)) {
            /*
            it is the teams turn
            -
            preliminary solution: remove the flags from the team's base,
            If there are more than two players *still in the game* (before the GiveUp),
            then delete the pieces and the base of the team which gave up.
             */
                teamGivingUp.setFlags(0);
                //Delete Pieces from the HashMap
                for(Piece p: teamGivingUp.getPieces()) {
                    pieceByGridName.remove("p:"+p.getTeamId() + "_" + p.getId());
                }
                //Clean up the grid and remove the pieces and the base of the team giving up from the board
                if (this.gameState.getTeams().length > 2) {
                    clearGridFromTeam(teamGivingUp);
                }
                //Null the team that gave up
                for(int i = 0;i<this.gameState.getTeams().length;i++){
                    if(this.gameState.getTeams()[i] != null && this.gameState.getTeams()[i].getId()==teamGivingUp.getId()) {
                        this.gameState.getTeams()[i] = null;
                    }
                }

                //Make it's the next teams turn
                int nextTeam = (this.gameState.getCurrentTeam() + 1) % this.gameState.getTeams().length;
                this.gameState.setCurrentTeam(nextTeam);
                //Check if the Team which turn it now is can make a Move. If not, the move needs to be skipped.
                //This can be skipped, if not all Teams have joined the Game
                if(isValidTeamNumber()) {
                    skipMove();
                }


            }
            else {
                throw new ForbiddenMove();
            }

            //Check if the game over requirements are fulfilled
            this.isGameOver();

        }

    }

    /**
     * Checks whether a move is valid based on the current game state.
     *
     * @param move {@link Move}
     * @return true if move is valid based on current game state, false otherwise
     */
    @Override
    public boolean isValidMove(Move move) {

        ValidMoveHelperMethods validMoveHelperMethods = new ValidMoveHelperMethods();

        //This is the case if not all teams have joined the game
        if(!this.isValidTeamNumber()) {
            return false;
        }

        synchronized (this.gameState) {
            String idOfCurrentTeam = this.gameState.getTeams()[this.gameState.getCurrentTeam()].getId();

            //We can now safely get the piece from the HashMap knowing that the Move came from the Team which turn it is
            String pieceGridName = "p:" + idOfCurrentTeam + "_" + move.getPieceId();
            Piece piece = pieceByGridName.get(pieceGridName);

            if(piece == null ||  !idOfCurrentTeam.equals(move.getTeamId())){
                return false;
            }


            /*
            Takes a look at the possibleSquares array and finds the value of the possibleSquares array where the move wants to go.
            */
            int[][] possibleSquares = validMoveHelperMethods.possibleSquares(piece,this.getCurrentGameState(),this.pieceByGridName);
            int valueOfTarget = possibleSquares[move.getNewPosition()[0]][move.getNewPosition()[1]];

            /*
            if value is -1, the square cannot be reached (out of range, own piece/base,
            block, opponent piece of greater attack power, Movement is not Shape but piece would
            have to jump...
             */

            return valueOfTarget != -1;

        }
    }

    /**
     * Checks whether the game is started based on the current {@link GameState}.
     *
     * <ul>
     *     <li>{@link Game#isGameOver()} == false</li>
     *     <li>{@link Game#getCurrentGameState()} != null</li>
     * </ul>
     *
     * @return isStarted
     */
    @Override
    public boolean isStarted() {
        //returns true if the first move has been made, i.e. startDate is already set to a date after 01.01.1970
        return this.startDate.after(new Date(0));
    }

    /**
     * Checks whether the game is over based on the current {@link GameState}.
     *
     * @return true if game is over, false if game is still running.
     */
    @Override
    public boolean isGameOver() {
         /*
        criterion from the requirements:
        a) - If a team's flag(s) are captured, the game is over and the team that captured the flag wins.
        b)- If all pieces on a team are captured, the game is over and the team with remaining pieces wins.
        c)- The game can also ends if the total time limit of the game is reached. The player with the most pieces wins.
         */

        synchronized (this.gameState) {
            Team[] teams = this.getCurrentGameState().getTeams();
            ArrayList<Team> losers = new ArrayList<>(); // Local list of loser Teams
            boolean isOver = false;

            // case a) and b)
            for (Team team : teams) {
                if (team == null) {
                    losers.add(null);
                }
            }

            // Measures the number of losers. If the number of losers is one less to the number of players, the game is over.
            if (teams.length - losers.size() == 1) {
                isOver = true;
                // Find the winner among the remaining teams
                for (Team team : teams) {
                    if (!(team == null)) {
                        winners = new String[]{team.getId()};
                        break;
                    }
                }
            }

            //When the time is up the player with the most pieces wins the Game
            else if (this.getRemainingGameTimeInSeconds() == 0) {
                isOver = true;
                // Determine the maximum amount of pieces a team has
                int maxPieces = 0;
                for (Team team : teams) {
                    if(team !=null && team.getPieces().length > maxPieces) {
                        maxPieces = team.getPieces().length;
                    }
                }

                ArrayList<String> winnersList = new ArrayList<String>();
                for(Team team : teams) {
                    if(team.getPieces().length==maxPieces) {
                        winnersList.add(team.getId());
                    }
                }
                winners = new String[winnersList.size()];
                for(int i = 0;i<winners.length;i++){
                    winners[i] = winnersList.get(i);
                }
            }

            else {
                //DRAW Check

                //Check if there is a draw
                int teamsCanMove = 0; // Counter for teams that can make a legal move
                // Iterate through each team to check for possible moves
                for (Team team : teams) {
                    if(team != null) {
                        for (Piece piece : team.getPieces()) {
                            if (piece != null) { // Ensure the piece is not null
                                try {
                                    int[][] possibleMoves = new ValidMoveHelperMethods().possibleSquares(piece, this.getCurrentGameState(), this.pieceByGridName);
                                    if (ValidMoveHelperMethods.hasLegalMove(possibleMoves)) {
                                        teamsCanMove++;
                                        break; // Move to the next team if a legal move is found
                                    }
                                } catch (NullPointerException e) {
                                    teamsCanMove = this.gameState.getTeams().length;
                                }
                            }
                        }
                        if (teamsCanMove > 1) {
                            break; // If more than one team can move, the game is not over yet
                        }
                    }
                }

                // If only one team can make a legal move, the game is a draw, and all teams are winners
                if (teamsCanMove == 1) {
                    winners = new String[this.getCurrentGameState().getTeams().length];
                    for (int i = 0; i < this.getCurrentGameState().getTeams().length; i++) {
                        winners[i] = this.getCurrentGameState().getTeams()[i].getId();
                    }
                    isOver = true;
                }
            }

            if (isOver) {
                this.endDate = new Date();
                //Shutsdown the Timer
                shutdownScheduler();
            }

            return isOver;
        }
    }

    /**
     * Get winner(s) (if any)
     *
     * @return {@link Team#getId()} if there is a winner
     */
    @Override
    public String[] getWinner() {
        try {
            return winners;
        }
        catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    /**
     * @return Start {@link Date} of game
     */
    @Override
    public Date getStartedDate() {
        //01.01.1970 if game has not started yet (i.e. first move has not been made)
        return this.startDate;
    }

    /**
     * @return End date of game
     */
    @Override
    public Date getEndDate() {
        //01.01.1970 if game has not ended yet
        return this.endDate;
    }

    private void clearGridFromTeam(Team t) {
        String baseregex = "b:" + t.getId();
        //Remove the base from the loosing Team from the board
        for (int i = 0; i < this.gameState.getGrid().length; i++) {
            for (int j = 0; j < this.gameState.getGrid()[i].length; j++) {
                //Replace the base of the team giving up with an empty space
                if (this.gameState.getGrid()[i][j].matches(baseregex)) {
                    this.gameState.getGrid()[i][j] = "";
                    break;
                }
            }
        }

        for (Piece p : t.getPieces()) {
            String pieceregex = "p:" + p.getTeamId() + "_" + p.getId();
            for (int i = 0; i < this.gameState.getGrid().length; i++) {
                for (int j = 0; j < this.gameState.getGrid()[i].length; j++) {
                    //Replace the base of the team giving up with an empty space
                    if (this.gameState.getGrid()[i][j].matches(pieceregex)) {
                        this.gameState.getGrid()[i][j] = "";
                    }
                }
            }
        }
    }

    /**
     * Skips the move for a team if:
     * a) The team does not have a legal move.
     * b) The team is null, implying the team has lost the game.
     *
     * Iterates over all teams to identify a team that has a legal move.
     */
    private void skipMove() {
        // Loop through all teams
        for (int i = 0; i < this.gameState.getTeams().length; i++) {
            // Get the current team based on the game state
            Team currentTeam = this.gameState.getTeams()[this.gameState.getCurrentTeam()];

            if (currentTeam == null) {
                // If the current team is null, move to the next team
                int nextTeam = (this.gameState.getCurrentTeam() + 1) % this.gameState.getTeams().length;
                this.gameState.setCurrentTeam(nextTeam);
            } else {
                // Check for any legal moves available for the current team
                for(int j = 0; j<currentTeam.getPieces().length;j++) {
                    Piece p = currentTeam.getPieces()[j];
                    if(p != null) {
                        int[][] possibleMoves = new ValidMoveHelperMethods().possibleSquares(p, this.getCurrentGameState(), this.pieceByGridName);
                        if (ValidMoveHelperMethods.hasLegalMove(possibleMoves)) {
                            // If a legal move is found, exit the method
                            return;
                        }
                    }
                }
                // No legal move found, move to the next team
                int nextTeam = (this.gameState.getCurrentTeam() + 1) % this.gameState.getTeams().length;
                this.gameState.setCurrentTeam(nextTeam);
            }
        }
    }
    public int getTurnTimeLimit(){
        return moveTimeLimitInSeconds;
    }

    /**
     * Initializes and starts the scheduler to decrement game time limits at fixed intervals.
     */
    private void startScheduler() {
        if (totalTimeLimitInSeconds != -1) {
            // Schedule the totalTimeLimit decreaser to run every second
            scheduler.scheduleAtFixedRate(this::decreaseTotalTime, 0, 1, TimeUnit.SECONDS);
        }
        if (moveTimeLimitInSeconds != -1) {
            // Schedule the remainingTotalTimeInSeconds decreaser to run every second
            scheduler.scheduleAtFixedRate(this::decreaseMoveTime, 0, 1, TimeUnit.SECONDS);
        }
    }


    /**
     * Gracefully shuts down the scheduler when the game ends or when the server is shutting down.
     */
    public void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();  // Request shutdown
            try {
                // Wait up to 60 seconds for existing tasks to terminate
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();  // Cancel currently executing tasks
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();  // Re-cancel if current thread also interrupted
                Thread.currentThread().interrupt();  // Preserve interrupt status
            }
        }
    }

    /**
     * Decreases the total time limit of the game by one second each call.
     * If the total time limit reaches zero, checks if the game should end.
     */
    private void decreaseTotalTime() {
        if (remainingTotalTimeInSeconds > 0) {
            remainingTotalTimeInSeconds -= 1;  // Decrement the total time by one second
            if (remainingTotalTimeInSeconds == 0) {
                System.out.println("The total time limit has expired.");
                isGameOver();
            }
        }
    }

    /**
     * Decreases the move time limit of the game by one second each call.
     * If the move time limit reaches zero, forces the move to skip to the next player.
     */
    private void decreaseMoveTime() {
        if (remainingMoveTimeInSeconds > 0) {
            remainingMoveTimeInSeconds -= 1;  // Decrement the move time by one second
            System.out.println(remainingMoveTimeInSeconds);
            if (remainingMoveTimeInSeconds == 0) {
                System.out.println("The move time limit has expired.");
                int nextTeam = (this.gameState.getCurrentTeam() + 1) % this.gameState.getTeams().length;
                this.gameState.setCurrentTeam(nextTeam);  // Skip the current team's move if they have not acted in time
                skipMove();
                //Reset the Timer
                this.remainingMoveTimeInSeconds = moveTimeLimitInSeconds;
            }
        }
    }
}
