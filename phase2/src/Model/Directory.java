package Model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Directory extends Entry implements Serializable {

  /** An ArrayList to hold Entries created. */
  private ArrayList<Entry> entries = new ArrayList<>();

  private Directory parent;

  /**
   * A constructor of directory.
   *
   * @param name The name of this directory.
   * @param path The name of this directory.
   * @param parent The parent directory of this directory.
   * @throws IOException exception
   */
  public Directory(String name, String path, Directory parent) throws IOException {
    super(name);
    this.parent = parent;
    this.path = path;
  }

  /**
   * A constructor of directory.
   *
   * @param name The name of this directory.
   * @param parent The parent directory of this directory.
   * @throws IOException exception
   */
  public Directory(String name, Directory parent) throws IOException {
    super(name);
    parent.add(this);
    this.parent = parent;
    this.path = parent.path + File.separator + name;
  }

  /**
   * A getter for the parent directory.
   *
   * @return A parent directory of this directory.
   */
  public Directory getParent() {
    return parent;
  }

  /**
   * A getter for the path.
   *
   * @return A String that represents the path of this directory.
   */
  public String getPath() {
    return path;
  }

  /**
   * A getter for the ArrayList of created entries.
   *
   * @return An ArrayList of created entries.
   */
  public ArrayList<Entry> getEntries() {
    return entries;
  }

  /**
   * A String that represents the directory's name.
   *
   * @return The name of this directory.
   */
  public String toString() {
    return name;
  }

  /**
   * Add an created entry to the entries ArrayList.
   *
   * @param entry The created entry.
   */
  public void add(Entry entry) {
    entries.add(entry);
  }

  /**
   * Add an ArrayList that contains entries to the entries ArrayList.
   *
   * @param entries The ArrayList of entries need to be added.
   */
  public void add(ArrayList<Entry> entries) {
    try {
      this.entries.addAll(entries);
    } catch (UnsupportedOperationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Remove the entry from the entries ArrayList.
   *
   * @param entry The entry need to be removed.
   */
  public void remove(Entry entry) {
    entries.remove(entry);
  }

  /** Print the Directory. */
  public void printDirectory() {
    System.out.println(fileTree());
  }

  /**
   * Returns an ArrayList of Strings, each location holds either a Directory String or Photo String.
   * Program runs recursively so that subdirectories can also be displayed.
   *
   * @return Returns an ArrayList of Strings.
   */
  public ArrayList<String> fileTree() {
    ArrayList<String> directory = new ArrayList<>();
    if (entries != null) {
      for (Entry entry : entries) {
        if (entry instanceof Directory) {
          directory.add("Directory: " + entry.getName() + ((Directory) entry).fileTree());
        } else if (entry instanceof Photo) {
          directory.add("Image: " + entry.getName());
        }
      }
    }
    return directory;
  }

  /**
   * @param name String name of the Directory being generated
   * @param directoryName String representation of the Directory path being generated
   * @param ancestor Directory to serve as the parent directory
   * @return Directory that has just been generated
   */
  public static Directory generateDirectory(String name, String directoryName, Directory ancestor) {
    try {
      File directory = new File(directoryName);
      Directory parent = new Directory(name, directory.getAbsolutePath(), ancestor);
      File[] fileList = directory.listFiles();
      if (fileList != null) {
        for (File file : fileList) {
          if (file.isDirectory()) {
            Directory child = new Directory(file.getName(), file.getAbsolutePath(), parent);
            parent.add(child);
          }
          if (file.isFile()) {
            String[] extension = file.getName().split("\\.");
            String[] accepted = {"bmp", "jpg", "jpeg", "gif", "png"};
            if (extension.length != 0
                && Arrays.asList(accepted).contains(extension[extension.length - 1])) {
              Photo photo = PhotoManager.photoOpenedBefore(file);
              parent.add(photo);
            }
          }
        }
      }
      return parent;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns an ArrayList of all Photos that match the tag searched. Program runs recursively so
   * that subdirectories can searched.
   *
   * @return Returns an ArrayList of Photos.
   */
  public ArrayList<Photo> searchTag(String tag) {
    ArrayList<Photo> results = new ArrayList<>();
    Directory allDir = generateDirectory("Search", this.getPath(), null);
    if (allDir != null) {
      for (Entry entry : allDir.getEntries()) {
        if (entry instanceof Directory) results.addAll(((Directory) entry).searchTag(tag));
        else {
          if (entry.toString().contains("@" + tag)) results.add((Photo) entry);
        }
      }
    }
    return results;
  }

  public Directory searchResult(String tag) throws IOException {
    Directory searchDirectory = new Directory("Results", this);
    for (Photo p : this.searchTag(tag)) {
      searchDirectory.add(p);
    }
    return searchDirectory;
  }

  /**
   * Returns an ArrayList of all Photos below the current directory. Program runs recursively so
   * that subdirectories can searched.
   *
   * @return Returns an ArrayList of Photos.
   */
  public ArrayList<Photo> photoList() {
    ArrayList<Photo> results = new ArrayList<>();
    Directory allDir = generateDirectory("List", this.getPath(), null);
    if (allDir != null) {
      for (Entry entry : allDir.getEntries()) {
        if (entry instanceof Directory) results.addAll(((Directory) entry).photoList());
        else {
          results.add((Photo) entry);
        }
      }
    }
    return results;
  }
  /**
   * Returns an ArrayList of all Directories below the current directory. Program runs recursively
   * so that subdirectories can be listed.
   *
   * @return Returns an ArrayList of Photos.
   */
  public ArrayList<Directory> directoryList() {
    ArrayList<Directory> allDirectories = new ArrayList<>();
    for (Entry entry : entries) {
      if (entry instanceof Directory) {
        allDirectories.addAll(((Directory) entry).directoryList());
        allDirectories.add((Directory) entry);
      }
    }
    return allDirectories;
  }

  /**
   * Returns an HashMap with a String of the tag used and an ArrayList of all Photos that contain
   * that tag.
   *
   * @return Returns an HashMap of (Strings and ArrayLists of Photos).
   */
  public HashMap<String, ArrayList<Photo>> sortImagesByTag() {
    HashMap<String, ArrayList<Photo>> sortedImages = new HashMap<>();
    for (String tag : getAllTagsFromCurrentDirectory()) {
      ArrayList<Photo> tempImages = new ArrayList<>();
      for (Entry entry : entries) {
        if (entry instanceof Photo) {
          if (((Photo) entry).getTags().getCurrentTags().contains(tag))
            tempImages.add((Photo) entry);
        }
      }
      sortedImages.put(tag, tempImages);
    }
    return sortedImages;
  }

  /**
   * Returns an ArrayList of all Strings of all the tags in use by any Photos in directory.
   *
   * @return Returns an ArrayList of Strings.
   */
  private ArrayList<String> getAllTagsFromCurrentDirectory() {
    ArrayList<String> tags = new ArrayList<>();
    for (Entry entry : entries) {
      if (entry instanceof Photo) {
        for (String tag : ((Photo) entry).getTags().getCurrentTags()) {
          if (!tags.contains(tag)) {
            tags.add(tag);
          }
        }
      }
    }
    return tags;
  }
}
