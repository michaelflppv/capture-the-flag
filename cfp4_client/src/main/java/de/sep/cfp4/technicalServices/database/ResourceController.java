package de.sep.cfp4.technicalServices.database;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.sep.cfp4.technicalServices.resource.BoardTheme;
import de.sep.cfp4.technicalServices.database.interfaces.ResourceLoader;
import de.sep.cfp4.application.Launcher;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import com.google.gson.Gson;


public class ResourceController implements ResourceLoader {

  // A map to cache the images, so we don't have to reload them from disk every time.
  private final HashMap<String, Image> imageCache;
  private final Collection<PieceDescription> pieces;

  private final String PREDEFINED_PIECES_PATH = "pieces/predefinedPieces.json";

  public ResourceController() {
    // Initialize the HashMap on creation of the ResourceController.
    imageCache = new HashMap<>();
    pieces = loadPieces(PREDEFINED_PIECES_PATH);
    // Preload all piece images into the cache.
    preloadImages();
  }

  // This method loads all necessary images into the cache.
  private void preloadImages() {
    // Define the team numbers.
    int[] teamNumbers = {0, 1, 2, 3};
    String imageKey;

    // Define the PieceTypes of pieces available.
    String[] PieceTypes = {"Pawn", "Rook", "Knight", "Bishop", "Queen", "King", "Unknown", "Base"};

    // Iterate over all board themes to load Piece images for each theme.
    for (BoardTheme boardTheme : BoardTheme.values()) {

      //Load the Block Images. Key can only be constructed with teamNumber, so 0 is choosen.
      imageKey = constructImageKey(boardTheme, "Block", 0);
      try {
        // Load the image from the resources folder.
        Image originalImage = new Image(
            Launcher.class.getResource("BoardThemes/" + boardTheme.name() + "/Block/Block.png")
                .toExternalForm());
        // Resize the image to 100x100 pixels.
        Image resizedImage = resizeImageTo100x100(originalImage);
        // Add the resized image to the cache.
        imageCache.put(imageKey, resizedImage);
      } catch (NullPointerException e) {
        // Log an error if the image file is not found.
        System.err.println("Image not found for: " + imageKey);
      }

      // Iterate over all PieceTypes of pieces.
      for (String PieceType : PieceTypes) {
        // Iterate over all team numbers.
        for (int teamNumber : teamNumbers) {
          // Construct a unique key for each image to be used in the map.
          imageKey = constructImageKey(boardTheme, PieceType, teamNumber);
          try {
            // Load the image from the resources folder.
            Image originalImage = new Image(Launcher.class.getResource(
                "BoardThemes/" + boardTheme.name() + "/" + PieceType + "/" + PieceType + "_"
                    + teamNumber + ".png").toExternalForm());
            // Resize the image to 100x100 pixels.
            Image resizedImage = resizeImageTo100x100(originalImage);
            // Add the resized image to the cache.
            imageCache.put(imageKey, resizedImage);
          } catch (NullPointerException e) {
            // Log an error if the image file is not found.
            System.err.println("Image not found for: " + imageKey);
          }
        }
      }
    }
  }

  // Resize a given image to 100x100 pixels.
  private Image resizeImageTo100x100(Image originalImage) {
    // Set preserveRatio to false to force a 100x100 size regardless of original aspect ratio.
    // Set smooth to true for better image quality.
    // The constructor does not use a higher quality filtering algorithm to improve performance.
    Image resizedImage = new Image(originalImage.getUrl(), 100, 100, false, true, false);
    return resizedImage;
  }

  // Construct a unique key based on the Board theme, piece type and team number.
  private String constructImageKey(BoardTheme boardTheme, String Type, int teamNumber) {
    return boardTheme.name()+ "_" + Type + "_" + teamNumber;
  }

  // Retrieve a piece image from the cache based on the piece type and team number.
  public Image getImage(BoardTheme boardTheme, String Type, int teamNumber) {
    // Construct the key to search in the map.
    String key = constructImageKey(boardTheme, Type, teamNumber);
    // Retrieve the image from the map.
    Image icon = imageCache.get(key);
    if (icon != null) {
      // Return the image if found.
      return icon;
    } else {
      // Return a default image for unknown piece PieceTypes.
      return imageCache.get(constructImageKey(boardTheme, "Unknown", teamNumber));
    }
  }

//  public List<MapItem> getMapItems() {
//    List<MapItem> mapItems = new ArrayList<>();
//    Gson gson = new Gson();
//    String directoryPath = "mapTemplates";
//    InputStream in = Launcher.class.getResourceAsStream(directoryPath);
//    if (in == null) {
//      System.err.println("Directory not found: " + directoryPath);
//      return mapItems;
//    }
//
//    // Get the real directory path from resources
//    File dir = new File(Launcher.class.getResource(directoryPath).getPath());
//    File[] files = dir.listFiles((dir1, name) -> name.endsWith(".json"));
//
//    if (files != null) {
//      for (File file : files) {
//        try (JsonReader reader = new JsonReader(
//            new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
//          MapTemplate template = gson.fromJson(reader, MapTemplate.class);
//          MapItem mapItem = new MapItem(file.getName(), template);
//          if (template != null) {
//            mapItems.add(mapItem);
//          }
//        } catch (IOException e) {
//          System.err.println("Error reading file: " + file.getName());
//          e.printStackTrace();
//        }
//      }
//    } else {
//      System.err.println("No JSON files found in the directory.");
//    }
//    return mapItems;
//  }

  static class MapStorage {
    private String mapName;
    private MapTemplate mapTemplate;
  }

  public List<MapItem> getMapItems() {
    Gson gson = new Gson();
    try (JsonReader reader = new JsonReader(new InputStreamReader(Launcher.class.getResourceAsStream("mapTemplates/predefinedMapTemplates.json")))) {
      List<MapStorage> mapStorages = gson.fromJson(reader, new TypeToken<List<MapStorage>>(){}.getType());
      return mapStorages.stream()
          .map(mapStorage -> new MapItem(mapStorage.mapName, mapStorage.mapTemplate))
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a collection of all images loaded.
   *
   * @return A collection of all images loaded.
   */
  public Collection<Image> getAllImages() {
    return imageCache.entrySet().stream()
        .filter(entry -> entry.getKey().endsWith("_0") && !entry.getKey().contains("_Block_") && !entry.getKey().contains("_Base_"))
        .map(Map.Entry::getValue)
        .toList();
  }

  /**
   * Adds a new Image to the cache with the specified key.
   *
   * @param key   The key for the image to be stored.
   * @param image The Image object to store.
   * @author dcebulla
   */
  public void storeImage(String key, Image image) {
    String imageKey = imageCache.entrySet().stream()
        .filter(entry -> entry.getValue().equals(image))
        .map(e -> e.getKey().substring(0, e.getKey().length() - 1))
        .findFirst()
        .get();

    for(BoardTheme theme : BoardTheme.values()) {
      for (int i = 0; i < 4; i++) {
        imageCache.put(constructImageKey(theme, key, i), imageCache.get(imageKey + i));
      }
    }
  }

  /**
   * Loads predefined pieces from a JSON file on the disk.
   * @return A collection of PieceDescriptions.
   * @author dcebulla
   */
  private Collection<PieceDescription> loadPieces(String path) {
    //Parse predefined pieces from JSON file
    Gson gson = new Gson();
    Type listType = new TypeToken<ArrayList<PieceDescription>>(){}.getType();
    try (JsonReader reader = new JsonReader(new InputStreamReader(Launcher.class.getResourceAsStream(path)))) {
      return gson.fromJson(reader, listType);
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  /**
   * Returns a collection of predefined pieces.
   * @return A collection of PieceDescriptions.
   * @author dcebulla
   */
  public Collection<PieceDescription> getPredefinedPieces() {
    return this.pieces;
  }

}