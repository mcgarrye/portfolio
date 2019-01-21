package Controller;

import Model.*;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleController {
  private Directory folder;
  private Scanner scanner = new Scanner(System.in);
  private String mainMenu =
      "If you'd like to enter a directory, type 'enter'"
          + "\n"
          + "If you'd like to manage an Image, type 'image'"
          + "\n"
          + "If you'd like to search, type 'search'"
          + "\n"
          + "To go back to the parent directory, type 'back'"
          + "\n"
          + "If you'd like to view a list of all images anywhere under this directory, type 'listing'"
          + "\n"
          + "If you'd like to view a log of all changes, type 'log'"
          + "\n"
          + "If you'd like to manage the set of all existing tags, type 'tags'"
          + "\n"
          + "'return' and 'close' can be typed at any time to return to main directory or to close the program"
          + " respectively"
          + "\n";
  private String imageMenu =
      "If you'd like to add/delete/view/clear/save tags, type 'tags'"
          + "\n"
          + "If you'd like to move the image, type 'move'"
          + "\n"
          + "If you'd like to view the log, type 'log'"
          + "\n"
          + "If you'd like to revert to a previous tag/name, type 'revert'"
          + "\n";

  /**
   * Constructor of ConsoleController
   *
   * <p>folder is set to a directory named "Home"
   */
  public ConsoleController() throws IOException {
    folder = generateDirectory("Home", getPath(), null);
    startOrReturn();
  }

  /**
   * Returns a Directory object that can be viewed in the console with all it's subdirectories and
   * images using a String representation of the path and then also naming the directory and its
   * parent.
   *
   * @param name directory name
   * @param path string representation of the file path
   * @param ancestor the parent directory or null if no directory above
   * @return instance of Directory created using parameters
   */
  private Directory generateDirectory(String name, String path, Directory ancestor) {
    try {
      File directory = new File(path);

      Directory parent = new Directory(name, path, ancestor);

      File[] fileList = directory.listFiles();
      if (fileList != null) {
        for (File file : fileList) {
          if (file.isDirectory()) {
            Directory child = generateDirectory(file.getName(), file.getAbsolutePath(), parent);
            parent.add(child);
          }
          if (file.isFile()) {
            String[] extension = file.getName().split("\\.");
            if (extension.length != 0 && extension[extension.length - 1].equals("jpg")) {
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
   * Starts the console by printing the master directory, the main menu and then waits for a
   * response. Also creates same display for when user wants to return to main menu.
   */
  private void startOrReturn() throws IOException {
    printToConsole(folder);
    System.out.println(mainMenu);
    listener(folder);
  }

  /**
   * Prints out the directory passed in to the console for user to view.
   *
   * @param directory directory to be printed to the console
   */
  private void printToConsole(Directory directory) {
    System.out.println("================");
    directory.printDirectory();
    System.out.println("================" + "\n");
  }

  /**
   * Returns a String object of the file's path.
   *
   * @return String file's path
   */
  private static String getPath() {
    String current = "";
    try {
      current = new java.io.File(".").getCanonicalPath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return current;
  }

  /** Saves any changes made during session and then exits program. */
  private void closer() {
    try {
      Main.save();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.exit(0);
  }

  /**
   * Takes in a directory to use for function options ('enter', 'image', 'back', 'listing'). Scans
   * in user input and calls associated function based on the users demands.
   *
   * @param directory directory to be potentially used if user sees fit
   */
  private void listener(Directory directory) throws IOException {
    String input = scanner.nextLine();
    switch (input) {
      case "enter":
        directoryMethod(directory);
        break;
      case "image":
        imageMethod(directory);
        break;
      case "search":
        searchMethod();
        break;
      case "back":
        System.out.println("Directory: " + directory.getParent().getName() + "\n" + mainMenu);
        listener(directory.getParent());
        break;
      case "listing":
        listingMethod(directory);
        break;
      case "log":
        logAll();
        break;
      case "tags":
        tagSet();
        break;
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        System.out.println("Not a command" + "\n");
        listener(directory);
        break;
    }
  }

  /**
   * If user has chosen to enter a directory, the current directory is passed in and a list of
   * subdirectories directly below it are printed and stored for them to choose from if there are
   * any. Once a directory is chosen then it calls enterDirectory to move to that directory.
   *
   * @param directory the current directory the user is in
   */
  private void directoryMethod(Directory directory) throws IOException {
    ArrayList<Directory> dList = new ArrayList<>();
    // for loop adds to list all directories
    for (Entry entry : directory.getEntries()) {
      if (entry instanceof Directory) {
        dList.add((Directory) entry);
      }
    }
    if (dList.size() > 0) {
      System.out.println(dList);
      System.out.println("Type the name of the directory you'd like to enter");
      String input = scanner.nextLine();
      switch (input) {
        case "return":
          startOrReturn();
          break;
        case "close":
          closer();
          break;
        default:
          enterDirectory(dList, input);
          break;
      }
    } else {
      System.out.println("No directories here" + "\n" + mainMenu);
      listener(directory);
    }
  }

  /**
   * A list of possible directories to enter is passed in along with the user's input for selecting
   * a directory. If the input matches a directory, it is then entered and the main menu is
   * displayed for this directory. If the input does not match then the user has the option to try
   * again, return or close.
   *
   * @param dList ArrayList of enterable directories
   * @param input String input by the user
   */
  private void enterDirectory(ArrayList<Directory> dList, String input) throws IOException {
    boolean found = false;
    // for loop checks for a directory with the same name as the input
    for (Directory directory : dList) {
      if (directory.getName().equals(input)) {
        printToConsole(directory);
        found = true;
        System.out.println("What do you want to do in " + directory.getName() + "?\n" + mainMenu);
        listener(directory);
        break;
      }
    }
    if (!found) {
      System.out.println("Directory not found, please try again");
      input = scanner.nextLine();
      switch (input) {
        case "return":
          startOrReturn();
          break;
        case "close":
          closer();
          break;
        default:
          enterDirectory(dList, input);
          break;
      }
    }
  }

  /**
   * If the user has chosen to manage images directly under the given directory, an ArrayList of
   * Photos directly under is then created if there are any and from that a ArrayList of the Photos
   * toString's which is then printed for the user to view to then chose which image to manage. The
   * list and their response are then passed to manageImage.
   *
   * @param directory current directory to look for images under
   */
  private void imageMethod(Directory directory) throws IOException {
    ArrayList<Photo> pList = new ArrayList<>();
    // for loop adds to list all photos below this directory
    for (Entry entry : directory.getEntries()) {
      if (entry instanceof Photo) {
        pList.add((Photo) entry);
      }
    }
    if (pList.size() > 0) {
      ArrayList<String> stringList = new ArrayList<>();
      for (Photo image : pList) {
        stringList.add(image.toString());
      }
      System.out.println(stringList);
      System.out.println("Type the name of the image you'd like to manage");
      String input = scanner.nextLine();
      switch (input) {
        case "return":
          startOrReturn();
          break;
        case "close":
          closer();
          break;
        default:
          manageImage(pList, input);
          break;
      }
    } else {
      System.out.println("No images here" + "\n" + mainMenu);
      listener(directory);
    }
  }

  /**
   * A list of possible Photos to manage is passed in along with the user's input for selecting a
   * photo. If the input matches a photo, it is then stored and the image menu is displayed for this
   * photo. imageListener is the called for the user to manage the image. If the input does not
   * match then the user has the option to try again, return or close.
   *
   * @param pList ArrayList of manageable Photos
   * @param input String input by the user
   */
  private void manageImage(ArrayList<Photo> pList, String input) throws IOException {
    boolean found = false;
    Photo pic = null;
    // for loop adds to list all photos names
    for (Photo image : pList) {
      if (image.getTagName().equals(input)) {
        System.out.println(image.toString());
        pic = image;
        found = true;
        break;
      }
    }
    if (found) {
      System.out.println(imageMenu);
      imageListener(pic);
    } else {
      System.out.println("Image not found, please try again");
      input = scanner.nextLine();
      switch (input) {
        case "return":
          startOrReturn();
          break;
        case "close":
          closer();
          break;
        default:
          manageImage(pList, input);
          break;
      }
    }
  }

  /**
   * Takes in a photo to use for function options. Scans in user input and calls associated function
   * based on the users demands.
   *
   * @param image Photo to be managed by user
   */
  private void imageListener(Photo image) throws IOException {
    String input = scanner.nextLine();
    switch (input) {
      case "tags":
        tagger(image);
        break;
      case "move":
        move(image);
        break;
      case "log":
        viewLog(image);
        break;
      case "revert":
        revert(image);
        break;
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        System.out.println("Not a command");
        imageListener(image);
        break;
    }
  }

  /**
   * If the user has decided to chose 'tags' they are then given the tag menu for them to decide on
   * what they would like to do with this image's tags. It then scans in user input and calls
   * associated function based on the users demands.
   *
   * @param image Photo to be managed by user
   */
  private void tagger(Photo image) throws IOException {
    System.out.println(
        "If you'd like to add a tag, type 'add'"
            + "\n"
            + "If you'd like to delete a tag, "
            + "type 'delete'"
            + "\n"
            + "If you'd like to view the tags, type 'view'"
            + "\n"
            + "If you'd like to clear your changes, type 'clear'"
            + "\n"
            + "If you'd like to save your changes, type 'save'"
            + "\n");
    String input = scanner.nextLine();
    switch (input) {
      case "add":
        addTag(image);
        break;
      case "delete":
        deleteTag(image);
        break;
      case "view":
        viewTags(image);
        break;
      case "clear":
        clearChanges(image);
        break;
      case "save":
        saveChanges(image);
        break;
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        System.out.println("Not a command");
        tagger(image);
        break;
    }
  }

  /**
   * If the user has decided to chose 'add' they are then given the current tags of the image, the
   * overall existing tag and then are asked to enter the new tag which is then added to the image.
   * The user is then prompted with the image menu for any more things they would like to do with
   * the image.
   *
   * @param image Photo for user to add tags to
   */
  private void addTag(Photo image) throws IOException {
    System.out.println("Here are the current tags on this image");
    System.out.println(image.getTags().getModifiedTags());
    System.out.println("Here are all the existing tags");
    System.out.println(TagManager.getAllTags());
    System.out.println("Please enter the new tag");
    String input = scanner.nextLine();
    switch (input) {
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        image.getTags().add(input);
        // image.getTags().save();
        //        try {
        //            PhotoManager pm = new PhotoManager(image);
        //          pm.rename();
        //        } catch (IOException e) {
        //          e.printStackTrace();
        //        } finally {
        System.out.println("What next with this image?" + "\n" + "\n" + imageMenu);
        imageListener(image);
        //        }
        break;
    }
  }

  /**
   * If the user has decided to chose 'delete' they are then given the current tags of the image and
   * then are asked to enter the tag which is then deleted from the image. The user is then prompted
   * with the image menu for any more things they would like to do with the image.
   *
   * @param image Photo for user to delete tags from
   */
  private void deleteTag(Photo image) throws IOException {
    System.out.println(image.getTags());
    System.out.println("Please enter tag to delete");
    String input = scanner.nextLine();
    switch (input) {
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        image.getTags().delete(input);
        //        image.getTags().save();
        System.out.println(image.getTags().getModifiedTags());
        //        try {
        //            PhotoManager pm = new PhotoManager(image);
        //          pm.rename();
        //        } catch (IOException e) {
        //          e.printStackTrace();
        //        } finally {
        System.out.println("What next with this image?" + "\n" + "\n" + imageMenu);
        imageListener(image);
        //        }
        break;
    }
  }

  /**
   * If the user has decided to chose 'view' they are then given the current tags of the image. The
   * user is then prompted with the image menu for any more things they would like to do with the
   * image.
   *
   * @param image Photo which tags will be displayed
   */
  private void viewTags(Photo image) throws IOException {
    System.out.println(image.getTags().getModifiedTags());
    System.out.println("What next with image:" + image.getTagName() + "?\n" + "\n" + imageMenu);
    imageListener(image);
  }

  /**
   * If the user has decided to chose 'save', all the changes of tags he made before will be saved.
   * The user is then prompted with the image menu for any more things they would like to do with
   * the image.
   *
   * @param image Photo which tags will be displayed
   * @throws IOException exception
   */
  private void saveChanges(Photo image) throws IOException {
    Pair<Boolean, ArrayList<String>> isSaved = image.getTags().save();
    PhotoManager pm = new PhotoManager(image);
    if (isSaved.getKey()) {
      String oldName = pm.rename();
      pm.writeLog(oldName, isSaved.getValue());
    } else {
      pm.writeLog("", isSaved.getValue());
    }
    System.out.println("What next with this image?" + "\n" + "\n" + imageMenu);
    imageListener(image);
  }

  /**
   * If the user has decided to chose 'clear', all the changes of tags he made (after the last time
   * all changes has been saved) will be cleared. The user is then prompted with the image menu for
   * any more things they would like to do with the image.
   *
   * @param image Photo which tags will be displayed
   * @throws IOException exception
   */
  private void clearChanges(Photo image) throws IOException {
    image.getTags().clearChanges();
    System.out.println("What next with this image?" + "\n" + "\n" + imageMenu);
    imageListener(image);
  }

  /**
   * If the user has chosen 'move' an ArrayList of all existing directories is then created and then
   * printed out for the user to choose from. They then input their choice and the given image is
   * moved to this new directory if their choice was valid. If not they are asked to try again. Once
   * moved, they are taken back to the main directory by startOrReturn.
   *
   * @param image Photo to be moved by user
   */
  private void move(Photo image) throws IOException {
    ArrayList<Directory> dList = folder.directoryList();
    boolean found = false;
    System.out.println(dList);
    System.out.println("Please enter directory you'd like to move to for those listed above");
    String input = scanner.nextLine();
    switch (input) {
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        // for loop checks for a directory with the same name as the input
        for (Directory directory : dList) {
          if (directory.getName().equals(input)) {
            try {
              PhotoManager pm = new PhotoManager(image);
              pm.move(directory);
            } catch (IOException e) {
              e.printStackTrace();
            }
            found = true;
          }
        }
        break;
    }
    if (!found) {
      System.out.println("Directory not found, please try again");
      move(image);
    }
    startOrReturn();
  }

  /**
   * If the user has decided to chose 'log', the history of this image's tags is printed. The user
   * is then prompted with the image menu for any more things they would like to do with the image.
   *
   * @param image Photo which log is being displayed
   */
  private void viewLog(Photo image) throws IOException {
    System.out.println(image.getTags().getHistoryTags());
    System.out.println("What next with image:" + image.getTagName() + "?\n" + "\n" + imageMenu);
    imageListener(image);
  }

  /**
   * If the user has decided to chose 'revert', the current name is printed, then the history of
   * this image's tags is printed. The user is then prompted to chose by number which naming to
   * revert to. The user is then prompted with the image menu for any more things they would like to
   * do with the image.
   *
   * @param image Photo which is being reverted
   */
  private void revert(Photo image) throws IOException {
    System.out.println("Here is the current naming");
    System.out.println(image.getTagName());
    System.out.println("Here is the history");
    System.out.println(image.getTags().getHistoryTags());
    System.out.println("Enter the number you'd like to revert to");
    String input = scanner.nextLine();
    switch (input) {
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        try {
          // attempts to change input to int if input can be changed
          int num = Integer.parseInt(input);
          image.getTags().goBack(num);
          //          try {
          //              PhotoManager pm = new PhotoManager(image);
          //            pm.rename();
          //          } catch (IOException e) {
          //            e.printStackTrace();
          //          }
          System.out.println(
              "What next with image:" + image.getTagName() + "?\n" + "\n" + imageMenu);
          imageListener(image);
        } catch (NumberFormatException e) {
          System.out.println("This is not a number, try again?");
          revert(image);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
    }
  }

  /**
   * If the user has decided to search for an image they are given all tags ever created and then
   * are asked to input the tag they'd like to search by. If they don't return or close,
   * searchListener is called on the Home directory searching the input by Directory.searchTag().
   */
  private void searchMethod() throws IOException {
    System.out.println("Here are all the existing tags");
    System.out.println(TagManager.getAllTags());
    System.out.println("Please enter the tag you would like to search by");
    String input = scanner.nextLine();
    switch (input) {
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        searchListener(folder.searchTag(input));
        break;
    }
  }

  /**
   * The user is provided a list of all Photos that have this tag and then likeToManage is called on
   * this list. If none existed they are sent back into searchMethod to try again.
   *
   * @param pList ArrayList of the Photos that have this tag
   */
  private void searchListener(ArrayList<Photo> pList) throws IOException {
    if (pList.size() > 0) {
      System.out.println(pList);
      likeToManage(pList);
    } else {
      System.out.println("No image is tagged with this" + "\n");
      searchMethod();
    }
  }

  /**
   * If the user has decided to list all images below the directory they are in, Directory.photoList
   * is called to create an ArrayList of all Photos which is then printed for the user. They are
   * then passed to likeToManage for any possible management of these images they'd like to do.
   *
   * @param directory Directory to list all Photos anywhere below that directory
   */
  private void listingMethod(Directory directory) throws IOException {
    ArrayList<Photo> pList = directory.photoList();
    System.out.println("Here are all images anywhere under " + directory.getName());
    System.out.println(pList);
    likeToManage(pList);
  }

  /**
   * The user is offered the option to manage any of the images previously found with the
   * searchMethod or by listingMethod. If they choose 'yes' then they asked to input the name of the
   * image they want to manage, which then calls manageImage using the pList and the input
   *
   * @param pList ArrayList of the Photos with a tag in common for the user to possibly manage
   */
  private void likeToManage(ArrayList<Photo> pList) throws IOException {
    System.out.println(
        "Would you like to manage one of these images? If so, type 'yes'. Otherwise you will be "
            + "returned to main directory"
            + "\n");
    String input = scanner.nextLine();
    switch (input) {
      case "yes":
        System.out.println("Type the name of the image you'd like to manage");
        input = scanner.nextLine();
        switch (input) {
          case "return":
            startOrReturn();
            break;
          case "close":
            closer();
            break;
          default:
            manageImage(pList, input);
            break;
        }
        break;
      case "close":
        closer();
        break;
      default:
        startOrReturn();
        break;
    }
  }

  /**
   * If the user has decided to have a log of all changes made in the system, the log is then read
   * out to them and they are then return to the start menu.
   */
  private void logAll() throws IOException {
    System.out.println("Here is the log of all changes ever made using this program");
    try {
      System.out.println(PhotoManager.readLog() + "\n");
    } catch (Exception e) {
      System.out.println("No log because no changes have been made yet");
    }

    startOrReturn();
  }

  /**
   * If the user has decided to manage the set of existing tags they are given the option to add or
   * delete a tag, and are then taken back to the main menu
   */
  private void tagSet() throws IOException {
    System.out.println("Here are all the existing tags");
    System.out.println(TagManager.getAllTags());
    System.out.println(
        "If you'd like to add a tag, type 'add'"
            + "\n"
            + "If you'd like to delete a tag, "
            + "type 'delete'"
            + "\n");
    String input = scanner.nextLine();
    switch (input) {
      case "add":
        adder();
        break;
      case "delete":
        deleter();
        break;
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        System.out.println("Not a command");
        tagSet();
        break;
    }
    startOrReturn();
  }

  /**
   * If the user has decided to add a tag to the set of tags, they are asked to input the new tag
   * and if it doesn't already exist then it is added. If it does then they are sent back to the set
   * of tags.
   */
  private void adder() throws IOException {
    System.out.println("Please enter the new tag");
    String input = scanner.nextLine();
    switch (input) {
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        if (!TagManager.getAllTags().contains(input)) TagManager.getAllTags().add(input);
        else {
          System.out.println("This tag is already in the set, back to the existing tags");
          tagSet();
        }
        break;
    }
  }

  /**
   * If the user has decided to delete a tag from the set of tags, they are asked to input the tag
   * and if it does exist then it is deleted. If it doesn't then they are sent back to the set of
   * tags.
   */
  private void deleter() throws IOException {
    System.out.println("Please enter the tag you wish to delete");
    String input = scanner.nextLine();
    switch (input) {
      case "return":
        startOrReturn();
        break;
      case "close":
        closer();
        break;
      default:
        if (TagManager.getAllTags().contains(input)) TagManager.getAllTags().remove(input);
        else {
          System.out.println("There is no such tag in this set, back to the existing tags");
          tagSet();
        }
        break;
    }
  }
}
