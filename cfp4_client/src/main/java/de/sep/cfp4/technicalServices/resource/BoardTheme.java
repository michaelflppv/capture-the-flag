package de.sep.cfp4.technicalServices.resource;

import javafx.scene.paint.Color;


/**
 * Enum for the different themes of the game board.
 * @author dcebulla
 * @version 0.0.2
 */

public enum BoardTheme {
  DEFAULT(Color.rgb(222,227,230), Color.rgb(140,162,173), Color.DARKGREEN, new String[]{"Human Player", "Easy Bot", "Normal Bot", "Hard Bot"}),
  OLYMPIC(Color.rgb(222,227,230), Color.rgb(140,162,173), Color.DARKGREEN, new String[]{"Human Player", "Bronze Bot", "Silver Bot", "Gold Bot"});
  private final Color[] boardColors;
  private final Color markerColor;
  private final String[] playerTypeNames;

  BoardTheme(Color boardColor1, Color boardColor2, Color markerColor, String[] playerTypeNames) {
    this.boardColors = new Color[]{boardColor1, boardColor2};
    this.markerColor = markerColor;
    this.playerTypeNames = playerTypeNames;
  }

  public Color[] getBoardColors() {
    return this.boardColors;
  }

  public Color getMarkerColor() {
    return this.markerColor;
  }

  public String[] getPlayerTypeNames() {
    return this.playerTypeNames;
  }

}
