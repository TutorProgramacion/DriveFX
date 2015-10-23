package tutor.programacion;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Carmelo Marín Abrego on 22/10/2015.
 */
public class DriveFXController implements Initializable {

    @FXML
    public ProgressIndicator progres;
    @FXML
    public ListView<File> listView;

    private static final String ROOT = "root";

    private static List<File> listFilesInFolder(Drive service, String folderId) throws IOException {
        List<File> archivos = new ArrayList<>();
        Drive.Children.List request = service.children().list(folderId);

        try {
            ChildList children = request.execute();

            for (ChildReference child : children.getItems()) {
                File file = service.files().get(child.getId()).execute();
                archivos.add(file);
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
            request.setPageToken(null);
        }

        return archivos;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listView.setCellFactory(param -> new TextFieldListCell<File>(
                new StringConverter<File>() {
                    @Override
                    public String toString(File object) {
                        return object.getTitle();
                    }

                    @Override
                    public File fromString(String string) {
                        return null;
                    }
                }
        ));

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                File file = listView.getSelectionModel().getSelectedItem();
                if (file != null) {
                    explorarFolder(file.getId());
                }
            }
        });

        explorarFolder(ROOT);
    }

    private void explorarFolder(String folderID) {
        try {
            Drive drive = DriveFiles.getDriveService();
            Service<ObservableList<File>> service = new Service<ObservableList<File>>() {
                @Override
                protected Task<ObservableList<File>> createTask() {
                    return new Task<ObservableList<File>>() {
                        @Override
                        protected ObservableList<File> call() throws Exception {
                            List<File> files = listFilesInFolder(drive, folderID);
                            return FXCollections.observableArrayList(files);
                        }
                    };
                }
            };

            listView.itemsProperty().unbind();
            progres.visibleProperty().unbind();

            listView.itemsProperty().bind(service.valueProperty());
            progres.visibleProperty().bind(service.runningProperty());

            service.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onRegresar(ActionEvent actionEvent) {
        explorarFolder(ROOT);
    }
}
