package Controller;

import Model.*;
import View.ConfirmBox;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;

public class Main extends Application {

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    // Load or generate configuration for this program.
    File configFile = new File("Configuration.ser");
    if (configFile.exists()) {
      load(configFile);
    } else {
      configFile.createNewFile();
    }
    Scanner scanner = new Scanner(System.in);
    System.out.print("Type, 'console' or 'gui'");
    String input = scanner.nextLine();
    if (input.equals("console")) {
      new ConsoleController();
    } else if (input.equals("gui")) {
      launch(args);
    }
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("File Explorer");
    primaryStage.getIcons().add(new Image("/res/folder.png"));
    primaryStage.setOnCloseRequest(
        (event) -> {
          event.consume();
          Boolean answer = ConfirmBox.display("Alert", "Do you want to exit");
          if (answer) {
            try {
              save();
            } catch (IOException e) {
              e.printStackTrace();
            }
            primaryStage.close();
          }
        });
    try {
      FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/MainWindow.fxml"));
      AnchorPane pane = loader.load();
      Scene scene = new Scene(pane);
      primaryStage.setScene(scene);
      primaryStage.show();
    } catch (IOException e) {
      System.out.println("Crashed on Main");
      e.printStackTrace();
    }
  }

  /**
   * Save all the Photos generated and all tags to Configuration.ser source:
   * http://blademastercoder.github.io/2015/01/29/java-Serializable.html
   *
   * @throws IOException exception
   */
  public static void save() throws IOException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream("Configuration.ser");
      ObjectOutputStream oos = null;
      try {
        oos = new ObjectOutputStream(fos);
        oos.writeObject(Photo.getAllPhotos());
        oos.writeObject(TagManager.getAllTags()); // write
        oos.flush();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          assert oos != null;
          oos.close();
        } catch (IOException e) {
          System.out.println("cannot close oos：" + e.getMessage());
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("cannot find this file：" + e.getMessage());
    } finally {
      try {
        assert fos != null;
        fos.close();
      } catch (IOException e) {
        System.out.println("cannot close fos：" + e.getMessage());
      }
    }
  }

  /**
   * Load all the Photos and all tags from Configuration.ser
   *
   * @throws IOException exception
   * @throws ClassNotFoundException exception
   */
  private static void load(File file) throws IOException, ClassNotFoundException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      ObjectInputStream ois = null;
      try {
        ois = new ObjectInputStream(fis);
        try {
          Photo.setAllPhotos((ArrayList<Photo>) ois.readObject());
          TagManager.setAllTags((ArrayList<String>) ois.readObject()); // read
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          assert ois != null;
          ois.close();
        } catch (IOException e) {
          System.out.println("cannot close ois：" + e.getMessage());
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("cannot find this file：" + e.getMessage());
    } finally {
      try {
        assert fis != null;
        fis.close();
      } catch (IOException e) {
        System.out.println("cannot close fis：" + e.getMessage());
      }
    }
  }
}
