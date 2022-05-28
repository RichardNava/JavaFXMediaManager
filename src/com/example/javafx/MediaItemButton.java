package com.example.javafx;

import com.example.media.MediaItem;
import java.io.File;
import java.io.IOException;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class MediaItemButton extends Button {

    private final MediaItem mediaItem;
    public static final int iconSize = 80;
    private final VBox metaBox;
    private final StackPane modalDimmer;
    private long lastClickMillis = 0;

    public MediaItemButton(MediaItem mediaItem, VBox metaBox, StackPane modalDimmer) throws IOException {
        super(mediaItem.getTitle().trim(), getNode(mediaItem));
        this.mediaItem = mediaItem;
        this.metaBox = metaBox;
        this.modalDimmer = modalDimmer;
        init();
    }

    private void init() {
        int size = iconSize + 30;
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        setContentDisplay(ContentDisplay.TOP);
        getStyleClass().clear();
        getStyleClass().add("sample-tile");

        setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                long lTime = lastClickMillis;
                long currentTime = System.currentTimeMillis();
                lastClickMillis = currentTime;
                for (Node node : ((VBox) ((TilePane) getParent()).getParent()).getChildren()) {
                    if (node instanceof TilePane) {
                        TilePane tPane = (TilePane)node;
                        for (Node node2 : tPane.getChildren()) {
                            node2.getStyleClass().remove("selected");
                        }
                    }
                }
                getStyleClass().add("selected");
                if (currentTime - lTime > 500) {
                    //not a double click
                    ((Text) metaBox.getChildren().get(0)).setText("Name: " + mediaItem.getTitle());
                    ((Text) metaBox.getChildren().get(1)).setText("Date: " + mediaItem.getDate());
                    ((Text) metaBox.getChildren().get(2)).setText("Type: " + mediaItem.getType());
                    return;
                }

                switch (mediaItem.getType()) {
                    case FLASH_VIDEO:
                    case MP4_VIDEO:
                        FXPlayer fxPlayer = new FXPlayer(mediaItem.getId(), modalDimmer);
                        showModalMessage(fxPlayer);
                        break;
                    case IMAGE:
                        Image image = new Image("file:///"+mediaItem.getId(), modalDimmer.getWidth() - 30, modalDimmer.getHeight() - 30, true, true);
                        ImageView iv = new ImageView(image);
                        modalDimmer.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                            @Override
                            public void handle(MouseEvent t) {
                                t.consume();
                                hideModalMessage();
                                modalDimmer.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                            }
                        });
                        showModalMessage(iv);
                        break;
                    default:

                }
            }
        });

    }

    private static Node getNode(MediaItem mediaItem) throws IOException {
        switch (mediaItem.getType()) {
            case FLASH_VIDEO:
            case MP4_VIDEO:
                return createMovieThumbnail(mediaItem.getId());
                //return createMoviePreview(mediaItem.getId());
            case IMAGE:
                return createImageThumbnail(mediaItem.getId());
            default:
                throw new IllegalArgumentException();
        }
    }

    public static ImageView createImageThumbnail(String path) {
        String url = "file:///" + path;
        Image image = new Image(url, iconSize, iconSize, true, true);
        ImageView iv = new ImageView(image);
        return iv;
    }

    public static ImageView createMovieThumbnail(String path) throws IOException {
        //Play graphic from: http://openclipart.org/detail/127705/play-by-augustoschwartz
        Image image = new Image(MediaApplication.class.getResourceAsStream("play.png"), iconSize, iconSize, true, true);
        ImageView iv = new ImageView(image);
        return iv;
    }

    public static MediaView createMoviePreview(String path) throws IOException {
        final Media media = new Media(new File(path).toURI().toString());
        final MediaPlayer mediaPlayer = new MediaPlayer(media);
//        mediaPlayer.setOnError(new Runnable() {
//            public void run() {
//                System.out.println(mediaPlayer.getError());
//            }
//        });
        mediaPlayer.cycleCountProperty().setValue(MediaPlayer.INDEFINITE);
        mediaPlayer.muteProperty().setValue(true);
        mediaPlayer.setAutoPlay(true);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitHeight(iconSize);
        mediaView.setFitWidth(iconSize);
        return mediaView;
    }

    /**
     * Show the given node as a floating dialog over the whole application, with
     * the rest of the application dimmed out and blocked from mouse events.
     *
     * @param message
     */
    public void showModalMessage(Node message) {
        modalDimmer.getChildren().add(message);
        modalDimmer.setOpacity(0);
        modalDimmer.setVisible(true);
        modalDimmer.setCache(true);
        TimelineBuilder.create().keyFrames(
                new KeyFrame(Duration.seconds(1),
                new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t) {
                        modalDimmer.setCache(false);
                    }
                },
                new KeyValue(modalDimmer.opacityProperty(), 1, Interpolator.EASE_BOTH))).build().play();
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