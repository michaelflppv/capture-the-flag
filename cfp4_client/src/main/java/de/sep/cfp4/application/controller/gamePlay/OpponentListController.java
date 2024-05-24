package de.sep.cfp4.application.controller.gamePlay;

import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.resource.PlayerType;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.sep.cfp4.application.model.listItems.OpponentEntry;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.util.StringConverter;

/**
 * Controller for the opponent list view.
 * @author jgroehl
 * @version 0.0.4
 */
public class OpponentListController {
    @FXML
    private TableView<OpponentEntry> opponentTable;

    @FXML
    private TableColumn<OpponentEntry, String> opponentNameColumn;

    @FXML
    private TableColumn<OpponentEntry, PlayerType> opponentTypeColumn;

    private final ObservableList<OpponentEntry> opponentEntries = FXCollections.observableArrayList();
    private final Database database = DatabaseHandler.getInstance();
    private final ObservableList<String> options = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.opponentTable.setSelectionModel(null);

        setupTableColumns();

        // Set initial items
        opponentEntries.add(new OpponentEntry("Yourself", PlayerType.HUMAN_PLAYER));
        opponentTable.setItems(opponentEntries);
    }


    private void setupTableColumns() {
        // Set cell value factories
        opponentNameColumn.setCellValueFactory(new PropertyValueFactory<>("opponentName"));
        opponentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("opponentType"));

        // Set cell factories
        opponentTypeColumn.setCellFactory(column -> new TableCell<OpponentEntry, PlayerType>() {
            @Override
            protected void updateItem(PlayerType aiType, boolean empty) {
                super.updateItem(aiType, empty);
                if (aiType == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ComboBox<PlayerType> box = new ComboBox<>();
                    box.getItems().addAll(PlayerType.values());
                    box.getSelectionModel().select(aiType);

                    box.setCellFactory(param -> new ListCell<PlayerType>() {
                        @Override
                        protected void updateItem(PlayerType item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(database.getBoardTheme().getPlayerTypeNames()[item.ordinal()]);
                            }
                        }
                    });
                    box.setConverter(new StringConverter<PlayerType>() {
                        @Override
                        public String toString(PlayerType playerType) {
                            return database.getBoardTheme().getPlayerTypeNames()[playerType.ordinal()];
                        }

                        @Override
                        public PlayerType fromString(String s) {
                            // Position of the player type in the array
                            int index = Arrays.asList(database.getBoardTheme().getPlayerTypeNames()).indexOf(s);
                            if (index == -1) {
                                return null;
                            }
                            return PlayerType.values()[index];
                        }
                    });

                    OpponentEntry opponentEntry = getTableView().getItems().get(getIndex());
                    box.setOnAction(event -> {
                        opponentEntry.setOpponentType(box.getValue());
                    });
                    setGraphic(box);
                }
            }
        });
    }

    public void updateBasedOnMapSelection(MapItem selectedMap) {
        // Clear existing entries and add "Yourself"

        opponentEntries.clear();
        if (selectedMap != null) {
            opponentEntries.add(new OpponentEntry("Yourself", PlayerType.HUMAN_PLAYER));
            for (int i = 1; i < selectedMap.getMapTemplate().getTeams(); i++) {
                opponentEntries.add(new OpponentEntry("Opponent " + i, PlayerType.HUMAN_PLAYER));
            }
        } else {
            System.out.println("No map selected.");
        }
    }

    public PlayerType[] getPlayers(){
        return opponentTable.getItems().stream().map(OpponentEntry::getOpponentType).toArray(PlayerType[]::new);
    }
}
