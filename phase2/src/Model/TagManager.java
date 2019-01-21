package Model;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * TagManager is a serializable class that manipulates tags of a Photo. It saves the current tags of
 * a Photo and stores all the historical sets of tags that the Photo has had. User can add new tags
 * or delete tags, or he can choose to go back to the older sets of tags. All these changes can be
 * cleared all at once or saved.
 */
public class TagManager implements Serializable {

  /** Current set of tags of a Photo. It will only be changed when the user saves his changes. */
  private ArrayList<String> currentTags;

  /**
   * Set of tags modified by the user. It will be changed when the user adds or deletes tags or
   * chooses to go back to the one of the older sets of tags. When the user wants to clear all the
   * changes, it will be reverted to the current tags version.
   */
  private ArrayList<String> modifiedTags;

  /**
   * A hashMap with Integers as keys and older sets of tags as values. Each key (say k) represents
   * the kth changes of tags the user saved.
   */
  private HashMap<Integer, ArrayList<String>> historyTags = new HashMap<>();

  /** Stores all the tags the user has added and saved to any Photo instances. */
  private static ArrayList<String> allTags = new ArrayList<>();

  /** Represents how many changes of tags have the user made and saved for a Photo. */
  private Integer num = 0;

  private ArrayList<String> changesLog = new ArrayList<>();
  /** Default Constructor of TagManager */
  public TagManager() {
    currentTags = new ArrayList<>();
    modifiedTags = new ArrayList<>();
  }

  /**
   * Creates a new TagManager with the given currTags. All tags should be added to AllTags if they
   * are not in AllTags.
   *
   * @param currTags current set of tags
   */
  public TagManager(ArrayList<String> currTags) {
    currentTags = new ArrayList<>(currTags);
    modifiedTags = new ArrayList<>(currTags);
    updateAllTags();
  }

  /**
   * A getter for the currentTags that a particular image has.
   *
   * @return An ArrayList that contains current tags.
   */
  ArrayList<String> getCurrentTags() {
    return currentTags;
  }

  /**
   * A getter for the modifiedTags.
   *
   * @return An ArrayList that contains modified tags.
   */
  public ArrayList<String> getModifiedTags() {
    return modifiedTags;
  }

  /**
   * A getter for the older sets of tags that the user had changed before.
   *
   * @return A HashMap of historyTags.
   */
  public HashMap<Integer, ArrayList<String>> getHistoryTags() {
    return historyTags;
  }

  /**
   * A getter for all the tags that the user has used.
   *
   * @return An ArrayList of allTags.
   */
  public static ArrayList<String> getAllTags() {
    return allTags;
  }

  /**
   * A setter for setting the AllTags ArrayList to all tags the user had used before the program is
   * turned off.
   *
   * @param loadTags An ArrayList of all tags the user had used before the program is turned off.
   */
  public static void setAllTags(ArrayList<String> loadTags) {
    allTags = loadTags;
  }

  /**
   * Adds tag to modifiedTags if tag is not in modifiedTags. Adds a log message to changesLog.
   *
   * @param tag a tag the user wants to add.
   * @return True if tag is added, false other wise
   */
  public boolean add(String tag) {
    if (!modifiedTags.contains(tag)) {
      this.modifiedTags.add(tag);
      this.changesLog.add("Tag added: @" + tag);
      return true;
    }
    return false;
  }

  /**
   * Deletes tag from modifiedTags if there is tag in it. Adds a log message to changesLog.
   *
   * @param tag a tag the user wants to delete.
   */
  public void delete(String tag) {
    modifiedTags.remove(tag);
    changesLog.add("Tag deleted: @" + tag);
  }

  /**
   * Goes back to the num(th) historical set of tags. Adds a log message to changesLog.
   *
   * @param num number of changes to go back
   */
  public void goBack(int num) {
    ArrayList<String> oldSetOfTags = historyTags.get(num);
    modifiedTags = oldSetOfTags;
    changesLog.add("Reverted to: " + oldSetOfTags.toString());
  }

  /**
   * Returns true and changesLog and saves current changes of tags iff modifiedTags and currentTags
   * are not the same.
   *
   * @return Pair<Boolean, ArrayList<String>>
   */
  public Pair<Boolean, ArrayList<String>> save() {
    if (!nothingChanges()) {
      num++;
      historyTags.put(num, currentTags);
      currentTags = new ArrayList<>(modifiedTags);
      updateAllTags();
      changesLog.add("Changes are saved successfully, this image has been renamed.");
      Pair<Boolean, ArrayList<String>> returnValue =
          new Pair<>(Boolean.TRUE, new ArrayList<>(changesLog));
      changesLog.clear();
      return returnValue;
    } else {
      changesLog.add("No changes detected. No need to rename this image.");
      Pair<Boolean, ArrayList<String>> returnValue =
          new Pair<>(Boolean.FALSE, new ArrayList<>(changesLog));
      changesLog.clear();
      return returnValue;
    }
  }

  /** Adds all tags in currentTags if they are not in AllTags. */
  private void updateAllTags() {
    for (String tag : currentTags) {
      if (!allTags.contains(tag)) {
        allTags.add(tag);
      }
    }
  }

  /**
   * Returns true iff there is any difference between currentTags and modifiedTags.
   *
   * @return boolean
   */
  private boolean nothingChanges() {
    if (currentTags.size() != modifiedTags.size()) {
      return false;
    } else {
      for (int i = 0; i < currentTags.size(); i++) {
        if (!currentTags.get(i).equals(modifiedTags.get(i))) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Clear all the changes made to modifiedTags. modifiedTags will be the same as the currentTags if
   * all the changes are removed. Adds a log message to changesLog.
   */
  public void clearChanges() {
    modifiedTags = new ArrayList<>(currentTags);
    changesLog.add("All changes above have been cleared.");
  }

  /**
   * Return an arrayList of String showing all the historical sets of tags in order.
   *
   * @return ArrayList<String></>
   */
  public ArrayList<String> showHistoryTags() {
    ArrayList<String> history = new ArrayList<>();
    for (int i = 1; i <= num; i++) {
      history.add(this.historyTags.get(i).toString());
    }
    return history;
  }

  /**
   * Return a string that represents what's in the currentTags ArrayList.
   *
   * @return A String.
   */
  @Override
  public String toString() {
    return "currentTags: " + currentTags.toString();
  }

  public boolean equals(TagManager tagManager) {
    if (tagManager != null && tagManager.currentTags.size() == currentTags.size()) {

      for (String tag : tagManager.currentTags) {
        if (!currentTags.contains(tag)) {
          return false;
        }
      }
      return true;
    } else return false;
  }
}
