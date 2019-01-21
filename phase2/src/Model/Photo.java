package Model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Photo is a subclass of Entry. Each Photo indicates a real image file in the file system. User can
 * add or delete tags to the Photo and can choose to go back to one of the historical sets of tags.
 * If the user chooses to save those changes, the photo will be renamed, else if he chooses to clear
 * all the changes, nothing will happen. User can also move the photo to another directory. All the
 * renaming ever done will be recorded in PhotoRenaming.log
 */
public class Photo extends Entry implements Serializable {

  /** The url of this photo. */
  private String url;

  /** Name of the tagged photo. */
  private String tagName;

  /** The TagManager that manages the tags. */
  private TagManager tags;

  /** The extension of the photo. */
  private String extension;

  /** A static variable storing all Photo instances create. */
  private static ArrayList<Photo> allPhotos = new ArrayList<>();

  /**
   * Default Constructor of Photo
   *
   * @param name file name of the Photo
   * @param url filepath of the Photo
   * @throws IOException exception
   */
  public Photo(String name, String url) throws IOException {
    super(name);
    setPhotoName();
    this.url = url;
    this.tags = new TagManager();
    this.tagName = this.name;
    if (url.lastIndexOf(".") != -1) {
      this.extension = url.substring(url.lastIndexOf("."), url.length());
    }
    allPhotos.add(this);
  }

  /**
   * Constructor of Photo that has some tags not added by this application.
   *
   * @param name file name of Photo without any tags
   * @param url filepath of the Photo
   * @param currTags the set of tags that this Photo already has
   * @param tagName file name of Photo with tags
   */
  public Photo(String name, String url, ArrayList<String> currTags, String tagName) {
    super(name);
    setPhotoName();
    this.url = url;
    this.tags = new TagManager(currTags);
    this.tagName = tagName;
    if (url.lastIndexOf(".") != -1) {
      this.extension = url.substring(url.lastIndexOf("."), url.length());
    }
    allPhotos.add(this);
  }

  /** Removes the extension of name if this Entry is a Photo. */
  public void setPhotoName() {
    if (name.lastIndexOf(".") != -1) {
      name = name.substring(0, name.lastIndexOf("."));
    }
  }

  /**
   * A getter for the ArrayList of all photo instances.
   *
   * @return An ArrayList of all photos.
   */
  public static ArrayList<Photo> getAllPhotos() {
    return allPhotos;
  }

  /**
   * A setter to set a new ArrayList to this entry's allPhotos ArrayList.
   *
   * @param Photos An arrayList of photo instances.
   */
  public static void setAllPhotos(ArrayList<Photo> Photos) {
    allPhotos = Photos;
  }

  /**
   * A getter for the url.
   *
   * @return This photo's url.
   */
  public String getUrl() {
    return url;
  }

  /** A getter for the url. */
  void setUrl(String newUrl) {
    url = newUrl;
  }

  /**
   * A getter for the tagName.
   *
   * @return The name of the tag.
   */
  public String getTagName() {
    return tagName;
  }

  /** A setter for the tagName. */
  void setTagName(String newTagName) {
    tagName = newTagName;
  }

  /**
   * A getter for the TagManager of this photo.
   *
   * @return The TagManager of this photo.
   */
  public TagManager getTags() {
    return this.tags;
  }

  /**
   * A getter for the extension of this photo.
   *
   * @return The extension of this photo.
   */
  public String getExtension() {
    return extension;
  }

  @Override
  public String toString() {
    return "Photo: " + tagName;
  }
}
