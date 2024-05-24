package de.sep.cfp4.application.model;

import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.application.exceptions.MapTemplateNameTaken;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.sep.cfp4.application.model.listItems.PieceItem;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import java.util.Arrays;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model for the map editor view.
 *
 * @author dcebulla
 * @version 0.0.3
 */
public class MapEditorModel {

  private static final MapEditorModel mapEditorModel = new MapEditorModel();
  private final Database database = DatabaseHandler.getInstance();

  // Observable lists for the stored and selected pieces
  private final ObservableList<PieceItem> storedPiecesList;
  private final ObservableList<PieceItem> selectedPiecesList;
  private final ReadOnlyObjectWrapper<MapItem> selectedMap = new ReadOnlyObjectWrapper<>();

  private MapEditorModel() {
    //Set Count of all pieces to 1, then map them to a PieceItem
    this.storedPiecesList = this.database.getPredefinedPiecesList();
    this.selectedPiecesList = FXCollections.observableArrayList();
  }

  public static MapEditorModel getInstance() {
    return mapEditorModel;
  }

  /**
   * Adds a piece to the list of selected pieces.
   *
   * @param pieceItem The piece to be added
   */
  public void addSelectedPiece(PieceItem pieceItem) {
    if (!this.selectedPiecesList.contains(pieceItem)) {
      this.selectedPiecesList.add(pieceItem);
    }
  }

  /**
   * Tries to add a new map template to the databaseHandler.
   *
   * @param mapName   The name of the map template
   * @param rows      The number of rows of the map
   * @param columns   The number of columns of the map
   * @param teams     The number of teams
   * @param flags     The number of flags
   * @param blocks    The number of blocks
   * @param placement The placement strategy for pieces and blocks
   * @param totalTime The total game time limit in seconds
   * @param moveTime  The time limit for moves in seconds
   * @throws MapTemplateNameTaken If the map name is already taken
   */
  public void addMapTemplate(String mapName, int rows, int columns, int teams, int flags,
      int blocks, PlacementType placement, int totalTime, int moveTime)
      throws MapTemplateNameTaken {
    MapTemplate mapTemplate = createMapTemplate(rows, columns, teams, flags, blocks,
        placement, totalTime, moveTime);
    this.database.addMapItem(new MapItem(mapName, mapTemplate));
  }

  /**
   * Creates a new map template with the specified parameters.
   *
   * @param rows      The number of rows of the map
   * @param columns   The number of columns of the map
   * @param teams     The number of teams
   * @param flags     The number of flags
   * @param blocks    The number of blocks
   * @param placement The placement strategy for pieces and blocks
   * @param totalTime The total game time limit in seconds
   * @param moveTime  The time limit for moves in seconds
   * @return The map template
   */
  public MapTemplate createMapTemplate(int rows, int columns, int teams, int flags,
      int blocks, PlacementType placement, int totalTime, int moveTime) {
    MapTemplate mapTemplate = new MapTemplate();

    // Fill map template properties
    mapTemplate.setGridSize(new int[]{rows, columns});
    mapTemplate.setTeams(teams);
    mapTemplate.setFlags(flags);

    // Convert selected pieces (PieceItem) to piece descriptions (PieceDescription)
    PieceDescription[] pieces = selectedPiecesList
        .stream()
        .map(PieceItem::getPieceDescription)
        .toArray(PieceDescription[]::new);
    mapTemplate.setPieces(pieces);

    mapTemplate.setBlocks(blocks);
    mapTemplate.setPlacement(placement);
    mapTemplate.setTotalTimeLimitInSeconds(totalTime);
    mapTemplate.setMoveTimeLimitInSeconds(moveTime);
    return mapTemplate;
  }


  /**
   * Loads a map item into the map editor.
   *
   * @param mapItem The map item to be loaded
   */
  public void openInMapEditor(MapItem mapItem) {
    this.selectedPiecesList.clear();
    this.selectedPiecesList.addAll(
        Arrays.stream(mapItem.getMapTemplate().getPieces())
            .map(PieceItem::new)
            .toList()
    );
    // Clear the selected map and set the new one, notifies listeners of the change even if the map is the same.
    this.selectedMap.set(null);
    this.selectedMap.set(mapItem);
  }

  /**
   * Adds a new piece item to the stored pieces list.
   *
   * @param pieceItem The piece to be removed
   */
  public void addStoredPiece(PieceItem pieceItem) {
    this.database.storePieceImage(pieceItem.getPieceDescription().getType(), pieceItem.getPieceImage());
    this.database.addPieceItem(pieceItem);
  }

  /**
   * Removes a piece item from the stored pieces list.
   *
   * @param pieceItem The piece to be removed
   */
  public void removeStoredPiece(PieceItem pieceItem) {
    this.database.removePieceItem(pieceItem);
  }

  /**
   * Returns the list of stored pieces. The list is unmodifiable.
   *
   * @return The list of stored pieces
   */
  public ObservableList<PieceItem> getStoredPiecesList() {
    return this.storedPiecesList;
  }

  /**
   * Returns the list of selected pieces.
   *
   * @return The list of selected pieces
   */
  public ObservableList<PieceItem> getSelectedPiecesList() {
    return this.selectedPiecesList;
  }

  /**
   * Returns the last loaded map item.
   *
   * @return The loaded map item.
   */
  public ReadOnlyObjectProperty<MapItem> selectedMapProperty() {
    return this.selectedMap.getReadOnlyProperty();
  }

}
