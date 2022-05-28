package com.example.javafx;

import com.example.media.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class MediaApplication extends Application {

    private VBox metaBox = new VBox();
    private StackPane modalDimmer = new StackPane();
    public File selectedFile = null;
    public final VBox vbox = new VBox(8) {
        // stretch to allways fill height of scrollpane

        @Override
        protected double computePrefHeight(double width) {
            return Math.max(
                    super.computePrefHeight(width),
                    getParent().getBoundsInLocal().getHeight());
        }
    };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        metaBox.getStyleClass().add("metadata");
        Text t = new Text("Name: ");
        t.getStyleClass().add("white-text");
        metaBox.getChildren().add(t);
        t = new Text("Date: ");
        t.getStyleClass().add("white-text");
        metaBox.getChildren().add(t);
        t = new Text("Type: ");
        t.getStyleClass().add("white-text");
        metaBox.getChildren().add(t);

        StackPane layerPane = new StackPane();


        primaryStage.setTitle("Media Viewer");
        vbox.getStyleClass().add("category-page");

        File startingDir = new File(System.getProperty("user.home") + "/Desktop");
        try {
            if (startingDir.exists() && startingDir.isDirectory()) {
                fillContentBox(startingDir);
            } else {
                fillContentBox(new File(System.getProperty("user.home")));
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Error reading the starting dir");
        }


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("noborder-scroll-pane");
        scrollPane.setFitToWidth(true);

        scrollPane.setContent(vbox);


        BorderPane borderPaneLayer = new BorderPane();

        borderPaneLayer.setBottom(metaBox);

        SplitPane sp = new SplitPane();
        StackPane sp1 = new StackPane();
        sp1.getChildren().add(buildFileSystemBrowser());
        StackPane sp2 = new StackPane();
        sp2.getChildren().add(scrollPane);
        sp.getItems().addAll(sp1, sp2);
        sp.setDividerPositions(0.3f, 0.6f);

        borderPaneLayer.setCenter(sp);

        modalDimmer.setVisible(false);

        borderPaneLayer.setId("main");
        modalDimmer.setId("ModalDimmer");
        layerPane.setDepthTest(DepthTest.DISABLE);
        layerPane.getChildren().add(borderPaneLayer);
        layerPane.getChildren().add(modalDimmer);


        Scene scene = new Scene(layerPane, 800, 600, Color.BLACK);
        scene.getStylesheets().addAll(MediaApplication.class.getResource("mediamanager.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.show();
    }

    public void fillContentBox(File dir) throws FileNotFoundException {
        MediaManager mm;
        try {
            mm = new FileMediaManager(dir, FileMediaManager.IdFormat.FX);
        } catch (IllegalArgumentException iae) {
            return;
        }
        List<MediaGroup> mediaGroups = mm
                .listMediaItems(new MediaQualifier()
                .setTypes(MediaType.IMAGE, MediaType.FLASH_VIDEO, MediaType.MP4_VIDEO)
                .setSortOrder(MediaOrder.DATE_DESC));

        vbox.getChildren().clear();

        int picCount = 0;
        int vidCount = 0;

        // create header
        //Label header = new Label(getName());
        Label header = new Label("0 Videos, 0 Pictures");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setMinHeight(Control.USE_PREF_SIZE); // Workaround for RT-14251
        header.getStyleClass().add("page-header");
        vbox.getChildren().add(header);

        Label categorySubHeader = new Label(dir.getAbsolutePath());
        categorySubHeader.setMaxWidth(Double.MAX_VALUE);
        categorySubHeader.setMinHeight(Control.USE_PREF_SIZE); // Workaround for RT-14251
        categorySubHeader.getStyleClass().add("page-subheader");
        vbox.getChildren().add(categorySubHeader);

        if (mediaGroups.size() > 0) {
            for (MediaGroup group : mediaGroups) {
                Label categoryHeader = new Label(group.getTitle());
                categoryHeader.setMaxWidth(Double.MAX_VALUE);
                categoryHeader.setMinHeight(Control.USE_PREF_SIZE); // Workaround for RT-14251
                categoryHeader.getStyleClass().add("category-header");
                vbox.getChildren().add(categoryHeader);
                // add direct children
                TilePane tiles = new TilePane(8, 8);
                tiles.setPrefColumns(1);
                tiles.getStyleClass().add("category-page-flow");
                vbox.getChildren().add(tiles);
                for (MediaItem mediaItem : group.getItems()) {
                    try {
                        switch (mediaItem.getType()) {
                            case FLASH_VIDEO:
                            case MP4_VIDEO:
                                vidCount++;
                                break;
                            case IMAGE:
                                picCount++;
                                break;
                        }
                        MediaItemButton mediaItemButton = new MediaItemButton(mediaItem, metaBox, modalDimmer);
                        tiles.getChildren().add(mediaItemButton);
                        String vidCntStr = vidCount == 1 ? "1 Video, " : vidCount + " Videos, ";
                        String picCntStr = picCount == 1 ? "1 Picture" : picCount + " Pictures";
                        header.setText(vidCntStr + picCntStr);
                    } catch (Exception io) {
                        // Failed to build button, skip adding
                    }
                }
            }
        } else {
            TilePane tiles = new TilePane(8, 8);
            tiles.setPrefColumns(1);
            tiles.getStyleClass().add("category-page-flow");
            vbox.getChildren().add(tiles);
        }
    }

    public TreeView buildFileSystemBrowser() {
        TreeItem<File> rootItem = new TreeItem<>();
        final TreeView<File> view = new TreeView<>(rootItem);
        view.setShowRoot(false);

        view.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {

            @Override
            public TreeCell<File> call(TreeView<File> list) {
                FileFormatCell ffCell = new FileFormatCell();

                return ffCell;
            }
        });


        view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<File>>() {

            @Override
            public void changed(ObservableValue<? extends TreeItem<File>> observable, TreeItem<File> oldValue, TreeItem<File> newValue) {
                if (view.getSelectionModel().getSelectedItem() != null) {
                    File selectedDir = view.getSelectionModel().getSelectedItem().getValue();
                    ((Text) metaBox.getChildren().get(0)).setText("Name:");
                    ((Text) metaBox.getChildren().get(1)).setText("Date:");
                    ((Text) metaBox.getChildren().get(2)).setText("Type:");
                    try {
                        fillContentBox(selectedDir);
                    } catch (FileNotFoundException ex) {
                        System.err.println("Error filling content box with items in " + selectedDir.getAbsolutePath());
                    }
                }
            }
        });

        for (File root : File.listRoots()) {
            rootItem.getChildren().add(createNode(root));
        }

        return view;
    }

    public class FileFormatCell extends TreeCell<File> {

        @Override
        protected void updateItem(File file, boolean empty) {
            // calling super here is very important - don't skip this!
            super.updateItem(file, empty);
            setText(file == null ? "" : file.getParent() == null ? file.toString() : file.getName());
        }
    }

    // This method creates a TreeItem to represent the given File. It does this
    // by overriding the TreeItem.getChildren() and TreeItem.isLeaf() methods 
    // anonymously, but this could be better abstracted by creating a 
    // 'FileTreeItem' subclass of TreeItem. However, this is left as an exercise
    // for the reader.
    public TreeItem<File> createNode(final File f) {
        return new TreeItem<File>(f) {

            // We cache whether the File is a leaf or not. A File is a leaf if
            // it is not a directory and does not have any files contained within
            // it. We cache this as isLeaf() is called often, and doing the 
            // actual check on File is expensive.
            private boolean isLeaf;
            // We do the children and leaf testing only once, and then set these
            // booleans to false so that we do not check again during this
            // run. A more complete implementation may need to handle more 
            // dynamic file system situations (such as where a folder has files
            // added after the TreeView is shown). Again, this is left as an
            // exercise for the reader.
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;

                    // First getChildren() call, so we actually go off and 
                    // determine the children of the File contained in this TreeItem.
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    File f = (File) getValue();
                    if (f != null && f.isDirectory()) {
                        File[] files = f.listFiles();
                        if (files != null) {
                            for (File childFile : files) {
                                if (childFile.isDirectory()) {
                                    isLeaf = false;
                                    return isLeaf;
                                }
                            }
                        }
                    }
                    isLeaf = true;
                }

                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
                File f = TreeItem.getValue();
                if (f != null && f.isDirectory()) {
                    File[] files = f.listFiles();
                    if (files != null) {
                        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();

                        final ListView<TreeItem<File>> listView = new ListView<>(children);

                        for (File childFile : files) {
                            if (childFile.isDirectory()) {
                                children.add(createNode(childFile));
                            }
                        }

                        return children;
                    }
                }

                return FXCollections.emptyObservableList();
            }
        };
    }

    /**
     * Hide any modal message that is shown
     */
    public void hideModalMessage() {
        modalDimmer.setCache(true);
        TimelineBuilder.create().keyFrames(
                new KeyFrame(Duration.seconds(1),
                new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t) {
                        modalDimmer.setCache(false);
                        modalDimmer.setVisible(false);
                        modalDimmer.getChildren().clear();
                    }
                },
                new KeyValue(modalDimmer.opacityProperty(), 0, Interpolator.EASE_BOTH))).build().play();
    }
}