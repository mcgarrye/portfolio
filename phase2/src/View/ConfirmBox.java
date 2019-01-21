package View;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** ConfirmBox is a class that will only be called to let user make sure what they clicked. */
public class ConfirmBox {

  /**
   * Since the user choose to close that confirmBox, which is equivalent to answer no. And it will
   * only become true when the user confirms.
   */
  private static Boolean answer = false;

  /**
   * An answer we get from the user. source:
   * https://www.youtube.com/watch?v=ZuHcl5MmRck&index=7&list=PL6gx4Cwl9DGBzfXLWLSYVy8EbTdpGbUIG.html
   *
   * @param title The title of the confirm box.
   * @param message The message that the user will see to confirm their changes.
   * @return A boolean that represents the user's answer to the message.
   */
  public static boolean display(String title, String message) {
    Stage window = new Stage();
    window.initModality(Modality.APPLICATION_MODAL); // make the user answer the message first
    window.setTitle(title);
    window.setMinWidth(300);
    Label label = new Label();
    label.setText(message);

    Button yesButton = new Button("Yes");
    Button noButton = new Button("No");
    yesButton.setOnAction(
        event -> {
          answer = true;
          window.close();
        });
    noButton.setOnAction(
        event -> {
          answer = false;
          window.close();
        });

    VBox layout = new VBox(10);
    layout.getChildren().addAll(label, yesButton, noButton);
    layout.setAlignment(Pos.CENTER);
    Scene scene = new Scene(layout);
    window.setScene(scene);
    window.showAndWait();
    return answer;
  }
}
