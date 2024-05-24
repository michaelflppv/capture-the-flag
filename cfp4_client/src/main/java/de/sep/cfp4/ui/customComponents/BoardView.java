package de.sep.cfp4.ui.customComponents;

import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.model.BoardModel;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

/**
 * View class for the board element.
 * @version 0.0.1
 * @author dcebulla
 */
public class BoardView extends GridPane {

  // EventHandler used to handle mouse clicks on squares on the board. Default is an empty handler.
  private final ObjectProperty<EventHandler<MouseEvent>> propertyOnSquareClicked = new SimpleObjectProperty<>(e -> {});
  // The boardModel associated with this boardView.
  private BoardModel boardModel;
  // The squares the board consists of.
  private Square[][] squares;
  private final Database database = DatabaseHandler.getInstance();

  public BoardView(BoardModel boardModel) {
    this.initModel(boardModel);
  }

  // Do not delete !!!: This constructor is needed for the FXML loader
  public BoardView() {}

  /**
   * Initializes the boardModel associated with this boardView.
   * @param boardModel The boardModel to associate with this boardView.
   */
  public void initModel(BoardModel boardModel) {
    if(this.boardModel != null) {
      throw new IllegalStateException("BoardModel already initialized");
    }
    this.boardModel = boardModel;
    this.renderBoard();
  }

  /**
   * Renders the board based on the current state of the associated boardModel.
   */
  public void renderBoard() {
    String[][] grid = this.boardModel.getGrid();
    this.squares = new Square[grid.length][grid[0].length];
    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[0].length; j++) {
        String squareString = grid[i][j];
        Square square = new Square((i + j) % 2 == 0 ? this.database.getBoardTheme().getBoardColors()[0]
            : this.database.getBoardTheme().getBoardColors()[1]);

        if (squareString.startsWith("p:")) {
          square.setPiece(this.boardModel.getPieceByID(squareString));
        } else if (squareString.startsWith("b:")) {
          square.setBase(this.boardModel.getTeamNumberByID(squareString.substring(2)));
        } else if (squareString.equals("b")) {
          square.setBlock();
        }

        square.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClick -> this.getOnSquareClick().handle(mouseClick));

        squares[i][j] = square;
        this.add(square, j, i);
      }
    }
  }

  /**
   * Property for the onSquareClick event handler.
   * @return The onSquareClick event handler property.
   */
  public final ObjectProperty<EventHandler<MouseEvent>> onSquareClickProperty() {
    return this.propertyOnSquareClicked;
  }

  /**
   * Sets the onSquareClick event handler.
   * @param handler The onSquareClick EventHandler to set.
   */
  public final void setOnSquareClick(EventHandler<MouseEvent> handler) {
    this.propertyOnSquareClicked.set(handler);
  }

  /**
   * Gets the onSquareClick event handler.
   * @return The onSquareClick EventHandler.
   */
  public final EventHandler<MouseEvent> getOnSquareClick() {
    return this.propertyOnSquareClicked.get();
  }

  /**
   * Puts a marker on a square of the board.
   * @param row The row of the square to put the marker on.
   * @param column The column of the square to put the marker on.
   */
  public void setMarker(int row, int column) {
    this.squares[row][column].setMarker();
  }

  /**
   * Removes the marker from a square of the board.
   * @param row The row of the square to remove the marker from.
   * @param column The column of the square to remove the marker from.
   */
  public void removeMarker(int row, int column) {
    this.squares[row][column].removeMarker();
  }

  /**
   * Selects/Highlights a square on the board.
   * @param row The row of the square to select.
   * @param column The column of the square to select.
   */
  public void selectSquare(int row, int column) {
    this.squares[row][column].selectSquare();
  }

  /**
   * Deselects/De-highlights a square on the board.
   * @param row The row of the square to deselect.
   * @param column The column of the square to deselect.
   */
  public void deselectSquare(int row, int column) {
    this.squares[row][column].deselectSquare();
  }

  /**
   * Inner class representing a square on the board.
   * @version 0.0.1
   * @author dcebulla
   */
  private class Square extends StackPane {
    private final Rectangle square;
    private final Color squareColor;
    private ImageView pieceView;
    private Shape marker;

    /**
     * Creates an instance of a square with the specified base color.
     * @param color The base color of the square.
     */
    Square(Color color) {
      this.squareColor = color;
      this.square = new Rectangle(50, 50, color);

      // Set outline of individual squares
      this.square.setStroke(Color.BLACK);
      this.square.setStrokeType(StrokeType.INSIDE);
      this.square.setStrokeWidth(0.5);

      this.square.heightProperty()
          .bind(BoardView.this.heightProperty().divide(boardModel.getGrid().length));
      this.square.widthProperty()
          .bind(BoardView.this.widthProperty().divide(boardModel.getGrid()[0].length));
      this.getChildren().add(this.square);
    }

    /**
     * Puts a marker on a square of the board. Uses a circle for empty squares and a window for
     * squares with enemy pieces.
     */
    public void setMarker() {
      if (this.pieceView == null) {
        Circle circle = new Circle();
        circle.radiusProperty().bind(this.square.heightProperty().divide(6));
        circle.setFill(database.getBoardTheme().getMarkerColor());
        circle.setOpacity(0.5);
        this.marker = circle;
        this.getChildren().add(circle);
      } else {
        Shape window = Shape.subtract(new Rectangle(20, 20), new Circle(10, 10, 11));
        window.setFill(database.getBoardTheme().getMarkerColor());
        window.setOpacity(0.5);

        window.scaleYProperty().bind(this.square.heightProperty().divide(20));
        window.scaleXProperty().bind(this.square.widthProperty().divide(20));

        this.marker = window;
        this.getChildren().add(window);
      }
    }

    /**
     * Removes the marker from the square.
     */
    public void removeMarker() {
      this.getChildren().remove(this.marker);
      this.marker = null;
    }

    /**
     * Selects a square on the board.
     */
    public void selectSquare() {
      this.square.setFill(database.getBoardTheme().getMarkerColor());
      this.square.setOpacity(0.5);
    }

    /**
     * Deselects a square on the board.
     */
    public void deselectSquare() {
      this.square.setFill(this.squareColor);
      this.square.setOpacity(1);
    }

    /**
     * Puts a piece on a square of the board
     *
     * @param piece The piece to put on the square.
     */
    public void setPiece(Piece piece) {
      String pieceType = piece.getDescription().getType();

      // Find the team number of the piece
      int teamNumber = boardModel.getTeamNumberByID(piece.getTeamId());

      //NullPointerException is handled in the resourceController class
      this.pieceView = new ImageView(database.getPieceImage(database.getBoardTheme(),pieceType,teamNumber));

      this.pieceView.fitWidthProperty().bind(this.square.widthProperty());
      this.pieceView.fitHeightProperty().bind(this.square.heightProperty());
      this.getChildren().add(this.pieceView);
    }

    /**
     * Puts a base on a square of the board
     */
    public void setBase(int teamNumber) {
      this.pieceView = new ImageView(database.getPieceImage(database.getBoardTheme(),"Base",teamNumber));
      this.pieceView.fitWidthProperty().bind(this.square.widthProperty());
      this.pieceView.fitHeightProperty().bind(this.square.heightProperty());
      this.getChildren().add(this.pieceView);
    }

    /**
     * Puts a block on a square of the board
     */
    public void setBlock() {
      this.pieceView = new ImageView(database.getPieceImage(database.getBoardTheme(),"Block",0));
      this.pieceView.fitWidthProperty().bind(this.square.widthProperty());
      this.pieceView.fitHeightProperty().bind(this.square.heightProperty());
      this.getChildren().add(this.pieceView);
    }

  }

}
