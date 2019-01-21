package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class PhotoManager {
  private Photo photo;

  /** A logger for the photo. */
  private static final Logger LOGGER = Logger.getLogger(Photo.class.getName());

  public PhotoManager(Photo photo) {
    this.photo = photo;
  }

  /**
   * Returns the oldName of this Photo and renames the Photo by adding tags from the current tags to
   * the original name (without any tags).
   *
   * @return String
   * @throws IOException exception
   */
  public String rename() throws IOException {
    StringBuilder newName = new StringBuilder();
    for (String tag : photo.getTags().getCurrentTags()) {
      newName.append("@").append(tag).append(" ");
    }
    String oldName = photo.getTagName();

    if (photo.getTags().getCurrentTags().size() != 0) {
      photo.setTagName(photo.getName() + " " + newName.toString().trim());
    } else {
      photo.setTagName(photo.getName());
    }
    Path old = Paths.get(photo.getUrl());
    Files.move(old, old.resolveSibling(photo.getTagName() + photo.getExtension()));
    photo.setUrl(old.resolveSibling(photo.getTagName() + photo.getExtension()).toString());
    return oldName;
  }

  /**
   * Write a log message to PhotoRenaming.log
   *
   * @param oldName oldName of this Photo, an empty String will be passed in if rename failed.
   * @param changesLog a log of all changes of this Photo's tag set.
   * @throws IOException
   */
  public void writeLog(String oldName, ArrayList<String> changesLog) throws IOException {
    String logs = String.join("\n     ", changesLog);
    FileHandler handler = new FileHandler("PhotoRenaming.log", true);
    SimpleFormatter formatterTxt = new SimpleFormatter();
    handler.setFormatter(formatterTxt);
    LOGGER.addHandler(handler);
    if (oldName.equals("")) {
      LOGGER.info(
          "\n     File Path: "
              + photo.getUrl()
              + "\n "
              + "    Rename failed.\n     "
              + logs
              + "\n");
    } else {
      LOGGER.info(
          "\nFile Path:"
              + photo.getUrl()
              + "\n     Rename: "
              + oldName
              + " -> "
              + photo.getTagName()
              + "\n     "
              + logs
              + "\n");
    }
    handler.close();
  }

  /**
   * Reads logs from PhotoRenaming.log
   *
   * @return String
   * @throws IOException exception
   */
  public static String readLog() throws Exception {
    List<String> logsList = Files.readAllLines(new File("PhotoRenaming.log").toPath());
    return String.join("\n", logsList);
  }

  /**
   * Moves the photo to the selected directory.
   *
   * @param directory The directory that will be moved to.
   * @throws IOException exception
   */
  public void move(Directory directory) throws IOException {
    Path oldPath = Paths.get(photo.getUrl());
    Path newPath =
        Paths.get(directory.getPath() + File.separator + photo.getTagName() + photo.getExtension());
    if (oldPath != newPath) {
      Files.move(oldPath, newPath, ATOMIC_MOVE);
      photo.setUrl(newPath.toString());
      directory.add(photo);
    }
  }

  /**
   * Return a Photo related to file from Entry.allPhotos if this Photo has been viewed before.
   * Otherwise, a new Photo of the file will be generated.
   *
   * @param file an image file
   * @return Photo
   * @throws IOException exception
   */
  public static Photo photoOpenedBefore(File file) throws IOException {
    for (Photo p : Photo.getAllPhotos()) {
      if (p.getUrl().equals(file.getPath())) {
        return p;
      }
    }
    String name = file.getName();
    if (file.getName().contains("@")) {
      ArrayList<String> currTags = convertToTags(name);
      String tagName = name.substring(0, file.getName().indexOf("."));
      name = name.substring(0, file.getName().indexOf("@"));
      return new Photo(name, file.getPath(), currTags, tagName);
    } else {
      return new Photo(name, file.getPath());
    }
  }

  /**
   * Converts file name of an image to a set of tags.
   *
   * @param name the file name of an image
   * @return current tags of this image
   */
  private static ArrayList<String> convertToTags(String name) {
    String[] tags = name.split("@");
    ArrayList<String> currTags = new ArrayList<>(Arrays.asList(tags));
    currTags.remove(0);
    String lastOne = currTags.remove(currTags.size() - 1);
    lastOne = lastOne.substring(0, lastOne.lastIndexOf("."));
    currTags.add(lastOne);
    return currTags;
  }
}
