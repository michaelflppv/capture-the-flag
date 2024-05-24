package de.sep.cfp4.application.model.listItems;

import de.sep.cfp4.technicalServices.resource.BoardTheme;
import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class PieceItem {
  private final StringProperty pieceName;
  private final ObjectProperty<Image> pieceImage;
  private final IntegerProperty pieceCount;
  private PieceDescription pieceDescription;

  private final Database database = DatabaseHandler.getInstance();

  public PieceItem(PieceDescription pieceDescription) {
    this.pieceDescription = pieceDescription;
    this.pieceName = new SimpleStringProperty(pieceDescription.getType());
    this.pieceCount = new SimpleIntegerProperty(1);

    Image image = this.database.getPieceImage(BoardTheme.DEFAULT, pieceDescription.getType(), 0);
    this.pieceImage = new SimpleObjectProperty<>(image);

    this.database.addBoardThemeListener((observable, oldValue, newValue) -> {
      Image newImage = this.database.getPieceImage(newValue, pieceDescription.getType(), 0);
      this.pieceImage.set(newImage);
    });
  }

  /**
   * Manual constructor for PieceItem. With no reference to the database.
   * @param pieceDescription The piece description
   * @param image The image of the piece
   */
  public PieceItem(PieceDescription pieceDescription, Image image) {
    this.pieceDescription = pieceDescription;
    this.pieceName = new SimpleStringProperty(pieceDescription.getType());
    this.pieceCount = new SimpleIntegerProperty(1);
    this.pieceImage = new SimpleObjectProperty<>(image);

    // Add listener to change the image when the board theme changes
    // Null check is necessary because this constructor is also used during initialization of the database
    if (this.database != null) {
      this.database.addBoardThemeListener((observable, oldValue, newValue) -> {
        Image newImage = this.database.getPieceImage(newValue, pieceDescription.getType(), 0);
        this.pieceImage.set(newImage);
      });
    }
  }


  public String getPieceName() {
    return this.pieceName.get();
  }

  public Image getPieceImage() {
    return this.pieceImage.get();
  }

  public PieceDescription getPieceDescription() {
    return this.pieceDescription;
  }

  public int getPieceCount() {
    return this.pieceCount.get();
  }

  public void setPieceName(String pieceName) {
    this.pieceName.set(pieceName);
  }

  public void setPieceView(Image pieceImage) {
    this.pieceImage.set(pieceImage);
  }

  public void setPiece(PieceDescription pieceDescription) {
    this.pieceDescription = pieceDescription;
  }

  public void setPieceCount(int pieceCount) {
    this.pieceCount.set(pieceCount);
  }

  public StringProperty pieceNameProperty() {
    return this.pieceName;
  }

  public ObjectProperty<Image> pieceViewProperty() {
    return this.pieceImage;
  }

  public IntegerProperty pieceCountProperty() {
    return this.pieceCount;
  }

}
