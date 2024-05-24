package de.sep.cfp4.application.controller.gameSetup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import de.sep.cfp4.application.Launcher;
import de.sep.cfp4.application.model.listItems.Server;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Controller for the server list view.
 * Allows the user to add, remove and select servers from a list of servers.
 * @author dcebulla
 * @version 0.0.1
 */

// It is possible that the server list will be implemented as a custom JavaFX component in the future.
public class ServerListController {

  @FXML
  private TableView<Server> serverTable;
  @FXML
  private TableColumn<Server, String> urlColumn;
  @FXML
  private TableColumn<Server, Boolean> statusColumn;
  @FXML
  private TableColumn<Server, Boolean> selectionColumn;
  @FXML
  private TableColumn<Server, Void> deleteColumn;
  @FXML
  private TextField urlField;

  private ObservableList<Server> serverList;


  /**
   * Initializes the server list and specifies how entries to the server list are to be displayed.
   */
  public void initialize() {
    // Initialize the server list with test servers
    this.serverList = FXCollections.observableArrayList(
        new Server("localhost:8888", false, false)
    );

    // Disable selection of rows
    this.serverTable.setSelectionModel(null);

    // Set up the columns
    this.urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
    this.statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    this.selectionColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));

    this.statusColumn.setCellFactory(column -> {
      TableCell<Server, Boolean> cell = new TableCell<>();
      cell.itemProperty().addListener((obs, wasStatus, isStatus) -> {
        if (isStatus != null) {
          Label statusLabel = new Label(isStatus ? "Online" : "Offline");

          Circle statusCircle = new Circle(5);
          statusCircle.setFill(isStatus ? Color.GREEN : Color.RED);

          statusLabel.setContentDisplay(ContentDisplay.RIGHT);
          statusLabel.setGraphic(statusCircle);

          cell.graphicProperty()
              .bind(Bindings.when(cell.emptyProperty()).then((Label) null).otherwise(statusLabel));
        }
      });
      return cell;
    });

    // Adapted from https://edencoding.com/tableview-customization-cellfactory/ and CellFactory Entity Cycle
    this.selectionColumn.setCellFactory(column -> {
      TableCell<Server, Boolean> cell = new TableCell<>();
      Button selectButton = new Button();
      cell.itemProperty().addListener((obs, wasSelected, isSelected) -> {
        if (isSelected != null) {
          if (isSelected) {
            selectButton.setText("Selected");
            selectButton.getStyleClass().remove("grey-button");
            selectButton.getStyleClass().add("black-button");
          } else {
            selectButton.setText("Select");
            selectButton.getStyleClass().remove("black-button");
            selectButton.getStyleClass().add("grey-button");
          }
        }
      });

      selectButton.setOnAction(event -> {
        Server serverClicked = this.serverTable.getItems().get(cell.getIndex());
        boolean selected = serverClicked.isSelected();
        this.serverTable.getItems().forEach(server -> server.setSelected(false));
        serverClicked.setSelected(!selected);
      });

      cell.graphicProperty()
          .bind(Bindings.when(cell.emptyProperty()).then((Button) null).otherwise(selectButton));
      return cell;
    });

    this.deleteColumn.setCellFactory(column -> {
      TableCell<Server, Void> cell = new TableCell<>();
      Button deleteButton = new Button();
      ImageView deleteIcon = new ImageView(
          new Image(Launcher.class.getResourceAsStream("images/Trash.png")));
      deleteIcon.setFitWidth(20);
      deleteIcon.setFitHeight(20);
      deleteButton.setGraphic(deleteIcon);
      deleteButton.getStyleClass().add("trash-button");

      deleteButton.setOnAction(event -> {
        Server server = this.serverTable.getItems().get(cell.getIndex());
        serverList.remove(server);
      });

      // The binding ensures that the button is deleted once the server entry is removed from the server list
      // It avoids the need to manually remove the button from the cell via a listener
      cell.graphicProperty()
          .bind(Bindings.when(cell.emptyProperty()).then((Button) null).otherwise(deleteButton));
      return cell;
    });

    // Set the server list to the table view
    serverTable.setItems(serverList);
  }


  /**
   * Checks whether the specified server url is valid and adds the server to the server list.
   */
  @FXML
  public void addServer() {
    String url = this.urlField.getText();
    this.urlField.clear();

    // Check if the URL is valid
    try {
      URI uri = new URL("http://" + url).toURI();
      System.out.println(uri.toString());
    } catch (Exception e) {
      return;
    }

    // Check if the server is already in the list
    Server serverCheck = this.serverList.stream().filter(server -> server.getUrl().equals(url))
        .findFirst().orElse(null);
    if (serverCheck != null) {
      System.out.println("Server already in list");
      return;
    }

    Server server = new Server(url, false, false);
    this.updateServerOnline(server);
    this.serverList.add(server);

    System.out.println("Added server: " + url);
  }



  /**
   * Returns the server that is currently selected in the server list.
   *
   * @return The selected server
   */
  public Server getSelectedServer() {
    return this.serverList.stream().filter(Server::isSelected).findFirst().orElse(null);
  }


  /**
   * Checks whether a given server url hosts a valid running capture the flag server
   * and updates the server stats accordingly.
   *
   * @param server The server object
   */
  public void updateServerOnline(Server server) {
    // Internal class which represents the capture the flag status page for invalid GET 404 requests.
    class ServerStatus {

      private String timestamp;
      private int status;
      private String error;
      private String path;
    }
    // Start a new Thread to handle the HttpRequest to avoid blocking the application thread.
    Thread thread = new Thread(() -> {
      // Adapted from https://baeldung.com/java-validate-json-string , validation with Gson, strict validation.
      TypeAdapter<ServerStatus> checkJson = new Gson().getAdapter(ServerStatus.class);
      HttpRequest httpRequest = HttpRequest.newBuilder()
          .uri(URI.create("http://" + server.getUrl() + "/api/gamesession/"))
          .header("Content-Type", "application/json")
          .GET()
          .build();
      HttpClient httpClient = HttpClient.newHttpClient();
      try {
        // Asynchronous HttpRequest updates the server status noticeably faster.
        CompletableFuture<HttpResponse<String>> httpResponse = httpClient.sendAsync(httpRequest,
            BodyHandlers.ofString());
        checkJson.fromJson(httpResponse.get().body());
        server.setStatus(true);
      } catch (JsonSyntaxException | IOException | InterruptedException | ExecutionException e) {
        server.setStatus(false);
      }
    });
    thread.setDaemon(true);
    thread.start();

  }


  /**
   * Refreshed the server status of each server currently stored in the server list.
   */
  @FXML
  public void refreshServerStatus() {
    this.serverList.forEach(this::updateServerOnline);
  }

}
