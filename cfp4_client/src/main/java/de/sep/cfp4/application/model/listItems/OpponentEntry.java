package de.sep.cfp4.application.model.listItems;

import de.sep.cfp4.technicalServices.resource.PlayerType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class to represent an opponent Entry in the OpponentList
 * All methods are GENERATED by CHATGPT
 * @author jgroehl
 */
public class OpponentEntry {
    private final StringProperty opponentName;
    private final ObjectProperty<PlayerType> opponentType;

    public OpponentEntry(String opponentName, PlayerType opponentType) {
        this.opponentName = new SimpleStringProperty(opponentName);
        this.opponentType = new SimpleObjectProperty<>(opponentType);
    }

    public String getOpponentName() {
        return opponentName.get();
    }

    public void setOpponentName(String opponentName) {
        this.opponentName.set(opponentName);
    }

    public StringProperty opponentNameProperty() {
        return opponentName;
    }

    public PlayerType getOpponentType() {
        return opponentType.get();
    }

    public void setOpponentType(PlayerType opponentType) {
        this.opponentType.set(opponentType);
    }

    public ObjectProperty<PlayerType> opponentTypeProperty() {
        return opponentType;
    }
}
