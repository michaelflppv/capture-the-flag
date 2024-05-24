package de.sep.cfp4.application.model.listItems;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class to represent a server item in the server list view.
 * @author dcebulla
 * @version 0.0.1
 */
public class Server {
  private StringProperty url;
  private BooleanProperty status;
  private BooleanProperty selected;

  public Server(String url, boolean status, boolean selected) {
    this.url = new SimpleStringProperty(url);
    this.status = new SimpleBooleanProperty(status);
    this.selected = new SimpleBooleanProperty(selected);
  }

  // getters and setters

    // getters for String and Boolean values
    public String getUrl() {
      return url.get();
    }

    public boolean getStatus() {
      return status.get();
    }

    public boolean isSelected() {
      return selected.get();
    }

    // setters for String and Boolean values
    public void setUrl(String url) {
      this.url.set(url);
    }

    public void setStatus(boolean status) {
      this.status.set(status);
    }

    public void setSelected(boolean selected) {
      this.selected.set(selected);
    }

    // getters for StringProperty and BooleanProperty
    public StringProperty urlProperty() {
      return url;
    }

    public BooleanProperty statusProperty() {
      return status;
    }

    public BooleanProperty selectedProperty() {
      return selected;
    }

}