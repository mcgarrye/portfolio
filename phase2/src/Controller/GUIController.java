package Controller;

import Model.*;
import View.ConfirmBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class GUIController {

  private int gridRow = 3;
  private final int gridColumn = 5;
  private int currRow = 0;
  private int currColumn = 0;
  private Directory currDirectory;
  private Entry currImage;
  private PhotoManager movingPhoto;
  private int tempTagsIndex = 0;

  @FXML private ImageView myImage;
  @FXML private GridPane gridPane;
  @FXML private TextField newFolderText;
  @FXML private AnchorPane imagePane;
  @FXML private AnchorPane newFolderPane;
  @FXML private AnchorPane displayPane;
  @FXML private AnchorPane allTagsPane;
  @FXML private ComboBox comboBox;
  @FXML private Button addTagButton;
  @FXML private TextField searchTextbox;
  @FXML private TextField filePathTextbox;
  @FXML private Label absolutePath;
  @FXML private Button moveButton;
  @FXML private Button dropButton;
  @FXML private Button saveButton;
  @FXML private Button clearButton;
  @FXML private Button revertButton;
  @FXML private ListView<String> tagList;
  @FXML private GridPane tempTagsGrid;
  @FXML private AnchorPane tempTagsPane;
  @FXML private ListView imageList;
  @FXML private ListView allTagsList;
  @FXML private TextField allTagsTextbox;
  /** Initialize the default path when program opens */
  public GUIController() {
    String testImagePath =
        File.separator + "phase2" + File.separator + "src" + File.separator + "TestImages";
    currDirectory = Directory.generateDirectory("Home", getPath() + testImagePath, null);
    tagList = new ListView<>();
    absolutePath = new Label();
    filePathTextbox = new TextField(getPath());
  }

  // The functions below handles button clicking events

  @FXML
  private void findImageButtonClick() {
    // source: https://www.youtube.com/watch?v=hNz8Xf4tMI4.html
    FileChooser fc = new FileChooser();
    fc.setInitialDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
    fc.getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter(
                "Image Files", "*.bmp", "*.png", "*.jpg", "*.jpeg", "*.gif"));
    File selectedFile = fc.showOpenDialog(null);
    if (selectedFile != null) {
      absolutePath.setText(selectedFile.getAbsolutePath());
      try {
        Photo photo = PhotoManager.photoOpenedBefore(selectedFile);
        try {
          String parentDir =
              selectedFile
                  .getPath()
                  .substring(0, selectedFile.getPath().lastIndexOf(File.separator));
          currDirectory = new Directory("Home", parentDir, null);
          updateScreen(currDirectory);
          currDirectory = Directory.generateDirectory("Home", filePathTextbox.getText(), null);
          updateScreen(currDirectory);
        } catch (IOException e) {
          e.printStackTrace();
        }
        imageClicked(photo);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("file is not valid");
    }
  }

  @FXML
  private void readLogClick() throws InvocationTargetException {
    Stage window = new Stage();
    window.initModality(Modality.APPLICATION_MODAL); // make the user answer the message first
    window.setTitle("Log Information");
    window.setMinWidth(500);
    window.setMinHeight(100);
    Label label = new Label();
    try {
      label.setText(PhotoManager.readLog());
    } catch (Exception e) {
      label.setText("No logs.");
    }
    VBox layout = new VBox(10);
    layout.getChildren().addAll(label);
    layout.setAlignment(Pos.CENTER);
    Scene scene = new Scene(layout);
    window.setScene(scene);
    window.showAndWait();
  }

  @FXML
  private void sortButtonClick() throws IOException {
    Directory sorted = new Directory("sorted", currDirectory);
    if (new File(sorted.getPath()).mkdir()) {
      for (String tag : currDirectory.sortImagesByTag().keySet()) {
        Directory d = new Directory(tag.trim(), sorted);
        if (new File(d.getPath()).mkdir()) {
          for (Photo p : currDirectory.sortImagesByTag().get(tag)) {
            // copy original file into the folder with the sorted tag
            Path original = Paths.get(p.getUrl());
            Path copy = Paths.get((d.getPath() + File.separator + p.getName() + p.getExtension()));
            Files.copy(original, copy, REPLACE_EXISTING);
          }
        }
      }
    }
    goToPathButtonClick();
  }

  public static String getPath() {
    String current = "";
    try {
      current = new File(".").getCanonicalPath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // System.out.println("Current dir:"+current);

    return current;
  }

  @FXML
  private void searchButtonClick() throws IOException {
    String tag = searchTextbox.getText();
    if (currDirectory.getName().equals("Results")) {
      currDirectory = currDirectory.getParent();
    }
    updateScreen(currDirectory.searchResult(tag));
  }

  @FXML
  private void newFolderOkButtonClick() throws IOException {
    if (!newFolderText.getText().trim().equals("")) {
      String name = newFolderText.getText().trim();
      Directory d = new Directory(name, currDirectory);
      if (new File(d.getPath()).mkdir()) {
        bringToFront(displayPane);
        updateScreen(currDirectory);
      }
    }
  }

  @FXML
  private void newFolderCancelButtonClick() {
    bringToFront(displayPane);
  }

  @FXML
  private void returnButtonClick() {
    bringToFront(displayPane);
    updateScreen(currDirectory);
  }

  @FXML
  private void quickHelpButtonClick() {
    JOptionPane.showMessageDialog(
        new JFrame(),
        "You can manage multiple tags at once, changes will not "
            + "be saved until Save Changes has been clicked");
  }

  @FXML
  private void dropButtonClick() throws IOException {
    movingPhoto.move(currDirectory);
    updateScreen(currDirectory);
    dropButton.setDisable(true);
    movingPhoto = null;
  }

  @FXML
  private void manageTagsButtonClick() {
    bringToFront(allTagsPane);
    ObservableList<String> allTags = FXCollections.observableArrayList(TagManager.getAllTags());
    allTagsList.getItems().clear();
    allTagsList.getItems().addAll(allTags);
  }

  @FXML
  private void allTagsDeleteButtonClick() {
    TagManager.getAllTags().remove(allTagsList.getSelectionModel().getSelectedItems().get(0));
    // refresh the list
    manageTagsButtonClick();
  }

  @FXML
  private void allTagsAddButtonClick() {
    if (!allTagsTextbox.getText().equals("")) {
      if (!TagManager.getAllTags().contains(allTagsTextbox.getText()))
        TagManager.getAllTags().add(allTagsTextbox.getText());
      // refresh the list
      manageTagsButtonClick();
    } else {
      JOptionPane.showMessageDialog(new JFrame("Error"), "Tag name cannot be empty!");
    }
  }

  @FXML
  private void listImageButtonClick() {
    imageList.getItems().clear();
    imageList.getItems().addAll(currDirectory.photoList());
  }

  @FXML
  private void newFolderButtonClick() {
    bringToFront(newFolderPane);
    // Another way to do it:
    // String name = JOptionPane.showInputDialog(new JFrame(), "Enter a name for the folder");
  }

  @FXML
  private void deleteButtonClick() {
    Boolean answer = ConfirmBox.display("Alert", "Are you sure to delete the picture?");
    if (answer) {
      currDirectory.remove(currImage);
      if (new File(((Photo) currImage).getUrl()).delete()) {
        updateScreen(currDirectory);
        bringToFront(displayPane);
      }
    }
  }

  @FXML
  private void goToPathButtonClick() {
    currDirectory = Directory.generateDirectory("Home", filePathTextbox.getText(), null);
    updateScreen(currDirectory);
  }

  @FXML
  private void backButtonClick() throws IOException {
    String previousDir =
        currDirectory.getPath().substring(0, currDirectory.getPath().lastIndexOf(File.separator));
    Directory home = new Directory("Home", previousDir, null);
    currDirectory = home;
    updateScreen(home);

    home = Directory.generateDirectory("Home", filePathTextbox.getText(), null);
    currDirectory = home;
    updateScreen(home);
  }

  @FXML
  private void initialize() {
    comboBox.setEditable(true);
    updateScreen(currDirectory);
    bringToFront(displayPane);
  }

  /**
   * @param d the directory to load the files/images from
   *     <p>This method will refresh the main gridPane according to the given directory
   */
  private void updateScreen(Directory d) {
    imageList.getItems().clear();
    currDirectory = d;
    filePathTextbox.setText(d.getPath());

    // clears the grid
    gridPane.getChildren().clear();
    currColumn = 0;
    currRow = 0;
    gridRow = 3;

    // redraw the elements onto the grid from the directory d
    for (Entry entry : d.getEntries()) {
      if (entry instanceof Directory) makeDirectory(entry);
      if (entry instanceof Photo) makeImage(entry);
    }

    // For the moving Images feature, only enable it when there's images to move
    if (movingPhoto != null) dropButton.setDisable(false);
    else dropButton.setDisable(true);
  }

  /**
   * @param entry A Directory Entry to draw on the screen
   *     <p>This method draws the folder icon with name onto the main gridPane
   */
  private void makeDirectory(Entry entry) {
    // Make the folder icon into an ImageView and make it fit
    ImageView image = new ImageView("/res/folder.png");
    Label label = new Label(entry.getName());
    image.setFitHeight(80);
    image.setFitWidth(80);
    label.setPrefHeight(20);
    // finding a place to put this icon
    if (currColumn > gridColumn) {
      if (currRow == gridRow) {
        gridPane.setMinHeight(gridPane.getMinHeight() + gridPane.getVgap() + 100);
        gridPane.getRowConstraints().add(new RowConstraints(100));
        gridRow++;
      }
      currColumn = 0;
      currRow++;
    }
    // add them to the pane with some constraints on min heights
    gridPane.add(image, currColumn, currRow);
    gridPane.setConstraints(image, currColumn, currRow, 1, 1, HPos.CENTER, VPos.TOP);
    gridPane.add(label, currColumn, currRow);
    gridPane.setConstraints(label, currColumn, currRow, 1, 1, HPos.CENTER, VPos.BOTTOM);
    currColumn++;

    // get a handle on when the folder icon is clicked
    image.setOnMouseClicked(
        (event) -> {
          // updates the grid with the new directory
          Directory d =
              Directory.generateDirectory(
                  entry.getName(), entry.getPath(), ((Directory) entry).getParent());
          updateScreen(d);
        });
  }

  /**
   * @param entry A Photo entry to draw on the screen
   *     <p>This method draws the image with name onto the main gridPane
   */
  private void makeImage(Entry entry) {
    // Make the image into an ImageView and make it fit
    Image photo = new Image("file:" + ((Photo) entry).getUrl());
    ImageView image = new ImageView(photo);
    Label label = new Label(((Photo) entry).getTagName());

    image.setFitHeight(80);
    image.setFitWidth(80);
    label.setPrefHeight(20);

    // finding a place to put this image on the main grid
    if (currColumn > gridColumn) {
      if (currRow == gridRow) {
        gridPane.setMinHeight(gridPane.getMinHeight() + gridPane.getVgap() + 100);
        gridPane.getRowConstraints().add(new RowConstraints(100));
        gridRow++;
      }
      currColumn = 0;
      currRow++;
    }

    // adding this image and label to the grid with some min height constraints
    gridPane.add(image, currColumn, currRow);
    gridPane.setConstraints(image, currColumn, currRow, 1, 1, HPos.CENTER, VPos.TOP);
    gridPane.add(label, currColumn, currRow);
    gridPane.setConstraints(label, currColumn, currRow, 1, 1, HPos.CENTER, VPos.BOTTOM);
    currColumn++;

    // get an image clicking handle
    image.setOnMouseClicked(
        (event) -> {
          imageClicked(entry);
        });
  }

  /**
   * @param entry the image which has been clicked
   *     <p>this methods handles all events from clicking an image
   */
  private void imageClicked(Entry entry) {
    // load image tags to gui
    updateComboBox();
    updateHistory(entry);
    comboBox.setEditable(true);
    Image myPhoto = new Image("file:" + ((Photo) entry).getUrl());
    myImage.setImage(myPhoto);
    absolutePath.setText(((Photo) entry).getUrl());
    bringToFront(imagePane);
    currImage = entry;

    // this grid contains all the tags
    tempTagsGrid.getChildren().clear();
    tempTagsIndex = 0;

    // updates the tag grid from saved tags
    updateTagGrid((Photo) entry);

    // Click on the move button
    moveButton.setOnAction(
        (clickEvent) -> {
          movingPhoto = new PhotoManager((Photo) entry);
          JOptionPane.showMessageDialog(
              new JFrame("On its way!"),
              "Go to a different directory and drop button will be enabled!");
          bringToFront(displayPane);
        });

    // Click on the revert button
    revertButton.setOnAction((clickEvent) -> revertToHistory(entry));

    // Click on the add button
    addTagButton.setOnAction((clickEvent) -> addTag(entry));

    // Click on the clear button
    clearButton.setOnAction((clickEvent) -> clearAllChanges(entry));

    // Click on the save button
    saveButton.setOnAction(
        (clickEvent) -> {
          try {
            saveChanges(entry);
          } catch (IOException e) {
            e.printStackTrace();
          }
          updateHistory(entry);
          updateComboBox();
          absolutePath.setText(((Photo) entry).getUrl());
        });
  }

  /**
   * @param entry the image entry that we have been working with
   *     <p>this method handles when the revert button has been clicked
   */
  private void revertToHistory(Entry entry) {
    // gets the selected item in the comboBox
    if (tagList.getSelectionModel().getSelectedItem() != null) {
      ((Photo) entry).getTags().goBack(tagList.getSelectionModel().getSelectedIndex() + 1);
      updateTagGrid((Photo) entry);
    }
  }

  private void updateTagGrid(Photo entry) {
    // redraws the tags grid
    tempTagsGrid.getChildren().clear();
    tempTagsIndex = 0;

    // goes through tag manager for each photo and draws it to the grid
    for (String tag : (entry).getTags().getModifiedTags()) {
      tempTagsGrid.getColumnConstraints().add(new ColumnConstraints(100));
      ImageView icon = new ImageView("/res/X.png");
      icon.setFitHeight(15);
      icon.setFitWidth(15);
      Button tempButton = new Button(tag);
      tempButton.setGraphic(icon);
      tempTagsGrid.add(tempButton, tempTagsIndex, 0);
      tempTagsIndex++;

      tempButton.setOnAction(
          (XClick) -> {
            (entry).getTags().delete(tempButton.getText());
            tempTagsGrid.getChildren().remove(tempButton);
          });
    }
  }

  /**
   * @param entry the image entry that we have been working with
   *     <p>this method handles when the add button has been clicked
   */
  private void addTag(Entry entry) {
    // the add function will return true with the tags is successfully added
    if (((Photo) entry).getTags().add(comboBox.getValue().toString().trim())
        && !comboBox.getValue().toString().equals("")) {
      // add this tag button to the tags grid
      tempTagsGrid.getColumnConstraints().add(new ColumnConstraints(100));
      tempTagsPane.setMinWidth(tempTagsPane.getMinWidth() + tempTagsGrid.getHgap() + 100);
      ImageView icon = new ImageView("/res/X.png");
      icon.setFitHeight(15);
      icon.setFitWidth(15);
      Button tempButton = new Button(comboBox.getValue().toString().trim());
      tempButton.setGraphic(icon);
      tempTagsGrid.add(tempButton, tempTagsIndex, 0);
      tempTagsIndex++;

      tempButton.setOnAction(
          (XClick) -> {
            ((Photo) entry).getTags().delete(tempButton.getText());
            tempTagsGrid.getChildren().remove(tempButton);
          });
    }
  }

  /**
   * @param entry the image entry that we have been working with
   *     <p>this method handles when the clear button has been clicked
   */
  private void clearAllChanges(Entry entry) {
    // clear all temporary tags added to the tags grid pane
    ((Photo) entry).getTags().clearChanges();

    updateTagGrid((Photo) entry);
  }

  /**
   * @param entry the image entry that we have been working with
   *     <p>this method handles when the save button has been clicked
   */
  private void saveChanges(Entry entry) throws IOException {
    Pair<Boolean, ArrayList<String>> isSaved = ((Photo) entry).getTags().save();
    updateTagGrid((Photo) entry);
    PhotoManager pm = new PhotoManager((Photo) entry);
    if (isSaved.getKey()) {
      String oldName = pm.rename();
      pm.writeLog(oldName, isSaved.getValue());
    } else {
      pm.writeLog("", isSaved.getValue());
    }
  }

  /**
   * @param visiblePane the pane that program wants to display
   *     <p>This methods make everything but the selected pane invisible
   */
  private void bringToFront(AnchorPane visiblePane) {
    // making everything invisible first, then make the selected pane visible
    tempTagsGrid.getChildren().clear();
    imagePane.setVisible(false);
    newFolderPane.setVisible(false);
    displayPane.setVisible(false);
    allTagsPane.setVisible(false);
    if (visiblePane == imagePane) imagePane.setVisible(true);
    if (visiblePane == displayPane) displayPane.setVisible(true);
    if (visiblePane == newFolderPane) newFolderPane.setVisible(true);
    if (visiblePane == allTagsPane) allTagsPane.setVisible(true);
  }

  /** Load all tags from TagManager and puts it in the combobox */
  private void updateComboBox() {
    ObservableList<String> allTags = FXCollections.observableArrayList(TagManager.getAllTags());
    comboBox.setItems(allTags);
  }

  /**
   * @param entry the image entry that we have been working with
   *     <p>load tagging history from the current image
   */
  private void updateHistory(Entry entry) {
    ObservableList<String> historicalTags =
        FXCollections.observableArrayList(((Photo) entry).getTags().showHistoryTags());
    tagList.setItems(historicalTags);
    if (historicalTags.size() > 0) revertButton.setDisable(false);
    else revertButton.setDisable(true);
  }
}
