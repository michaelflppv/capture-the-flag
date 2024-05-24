package de.sep.cfp4.application.model.listItems;

import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class to represent a map item in the map list view.
 * @author dcebulla
 * @version 0.0.1
 */
public class MapItem {

  private StringProperty mapName;
  private MapTemplate mapTemplate;
  private BooleanProperty selected;

  public MapItem(String mapName, MapTemplate mapTemplate) {
      this.mapName = new SimpleStringProperty(mapName);
      this.mapTemplate = mapTemplate;
      this.selected = new SimpleBooleanProperty(false);
  }

  // Getter- and setter-methods for the mapName, mapTemplate and selected properties
  public String getMapName() {
    return mapName.get();
  }

  public MapTemplate getMapTemplate() {
    return mapTemplate;
  }

  public boolean isSelected() {
    return selected.get();
  }

  public void setMapName(String mapName) {
    this.mapName.set(mapName);
  }

  public void setMapTemplate(MapTemplate mapTemplate) {
    this.mapTemplate = mapTemplate;
  }

  public void setSelected(boolean selected) {
    this.selected.set(selected);
  }

  public StringProperty mapNameProperty() {
    return mapName;
  }

  public BooleanProperty selectedProperty() {
    return selected;
  }

}
