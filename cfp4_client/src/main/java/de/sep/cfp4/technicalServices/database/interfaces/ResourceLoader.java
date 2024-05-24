package de.sep.cfp4.technicalServices.database.interfaces;

import de.sep.cfp4.technicalServices.resource.BoardTheme;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import java.util.Collection;
import java.util.List;
import javafx.scene.image.Image;

/**
 * Interface for the ResourceLoader class.
 * @version 0.0.1
 * @author dcebulla
 */
public interface ResourceLoader {
  Image getImage(BoardTheme boardTheme, String pieceType, int teamNumber);
  List<MapItem> getMapItems();
  Collection<Image> getAllImages();
  void storeImage(String key, Image image);
  Collection<PieceDescription> getPredefinedPieces();

}
