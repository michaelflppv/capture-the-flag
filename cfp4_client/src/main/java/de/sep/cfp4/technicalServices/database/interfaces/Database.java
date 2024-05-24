package de.sep.cfp4.technicalServices.database.interfaces;

import de.sep.cfp4.technicalServices.resource.BoardTheme;
import de.sep.cfp4.application.exceptions.MapTemplateNameTaken;
import de.sep.cfp4.application.model.BoardModel;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.sep.cfp4.application.model.listItems.PieceItem;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javafx.beans.value.ChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public interface Database {
  BoardModel createGameSession(URI serverURL, String teamID, MapTemplate mapTemplate) throws IOException, InterruptedException;
  BoardModel joinGameSession(URI serverURL, String sessionID, String teamID)
      throws IOException, InterruptedException;
  void deleteGameSession(String sessionID, String teamID);
  void removeGameSessionFromDatabase(String sessionID, String teamID);
  void addListenerBoardModelList(MapChangeListener<String, BoardModel> listener);
  ObservableList<MapItem> getMapTemplateList();
  void addMapItem(MapItem mapItem) throws MapTemplateNameTaken;
  void removeMapItem(MapItem mapItem);
  ObservableList<PieceItem> getPredefinedPiecesList();
  Image getPieceImage(BoardTheme boardTheme, String type, int teamNumber);
  Collection<Image> getAllPieceImages();
  void storePieceImage(String key, Image image);
  void setBoardTheme(BoardTheme boardTheme);
  BoardTheme getBoardTheme();
  void addBoardThemeListener(ChangeListener<BoardTheme> listener);
  void addPieceItem(PieceItem pieceItem);
  void removePieceItem(PieceItem pieceItem);
  void joinAsSpectator(URI uri, BoardModel bm);
}
