package com.example.javafx;

import java.io.File;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class FXPlayer extends BorderPane {

    private final StackPane modalDimmer;
    private MediaPlayer mp;
    private MediaView mediaView;
    private final boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Slider volumeSlider;
    private HBox mediaBar;
    private HBox closeBar;
    private static final Image PlayButtonImage = new Image(FXPlayer.class.getResourceAsStream("playbutton.png"));
    private static final Image PauseButtonImage = new Image(FXPlayer.class.getResourceAsStream("pausebutton.png"));
    ImageView imageViewPlay = new ImageView(PlayButtonImage);
    ImageView imageViewPause = new ImageView(PauseButtonImage);

    @Override
    protected void layoutChildren() {
        if (mediaView != null && getBottom() != null) {
            mediaView.setFitWidth(getWidth());
            mediaView.setFitHeight(getHeight() - getBottom().prefHeight(-1));
        }
        super.layoutChildren();
        if (mediaView != null) {
            mediaView.setTranslateX((((Pane) getCenter()).getWidth() - mediaView.prefWidth(-1)) / 2);
            mediaView.setTranslateY((((Pane) getCenter()).getHeight() - mediaView.prefHeight(-1)) / 2);
        }
    }

    @Override
    protected double computeMinWidth(double height) {
        return closeBar.prefWidth(-1);
    }

    @Override
    protected double computeMinHeight(double width) {
        return 200;
    }

    @Override
    protected double computePrefWidth(double height) {
        return Math.max(mp.getMedia().getWidth(), closeBar.prefWidth(height));
    }

    @Override
    protected double computePrefHeight(double width) {
        return mp.getMedia().getHeight() + closeBar.prefHeight(width);
    }

    @Override
    protected double computeMaxWidth(double height) {
        return modalDimmer.getWidth() - 60;
    }

    @Override
    protected double computeMaxHeight(double width) {
        return modalDimmer.getHeight() - 60;
    }

    public FXPlayer(final String filePath, final StackPane modalDimmer) {
        this.modalDimmer = modalDimmer;
        Media media = new Media(new File(filePath).toURI().toString());
        mp = new MediaPlayer(media);

        setStyle("-fx-background-color: #bfc2c7;");
        final Pane mvPane = new Pane();
        mvPane.setStyle("-fx-background-color: black;");
        setCenter(mvPane);
        closeBar = new HBox();
        closeBar.setPadding(new Insets(5, 10, 5, 10));
        closeBar.setAlignment(Pos.CENTER_RIGHT);
        BorderPane.setAlignment(closeBar, Pos.CENTER);
        final Button closeButton = new Button("X");
        closeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                mp.stop();
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
        });

        closeBar.getChildren().add(closeButton);
        setTop(closeBar);

        final Runnable readyHandler = new Runnable() {

            public void run() {
                duration = mp.getMedia().getDuration();
                updateValues();
                mediaView = new MediaView(mp);
                mvPane.getChildren().add(mediaView);

                mediaBar = new HBox();
                mediaBar.setPadding(new Insets(5, 10, 5, 10));
                mediaBar.setAlignment(Pos.CENTER_LEFT);
                BorderPane.setAlignment(mediaBar, Pos.CENTER);

                final Button playButton = new Button();
                playButton.setGraphic(imageViewPlay);
                playButton.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent e) {
                        updateValues();
                        Status status = mp.getStatus();
                        if (status == Status.UNKNOWN
                                || status == Status.HALTED) {
                            // don't do anything in these states
                            return;
                        }

                        if (status == Status.PAUSED
                                || status == Status.READY
                                || status == Status.STOPPED) {
                            // rewind the movie if we're sitting at the end
                            if (atEndOfMedia) {
                                mp.seek(mp.getStartTime());
                                atEndOfMedia = false;
                                playButton.setGraphic(imageViewPlay);
                                //playButton.setText(">");
                                updateValues();
                            }
                            mp.play();
                            playButton.setGraphic(imageViewPause);
                            //playButton.setText("||");
                        } else {
                            mp.pause();
                        }
                    }
                });
                mp.currentTimeProperty().addListener(new ChangeListener<Duration>() {

                    @Override
                    public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                        updateValues();
                    }
                });
                mp.setOnPlaying(new Runnable() {

                    @Override
                    public void run() {
                        //System.out.println("onPlaying");
                        if (stopRequested) {
                            mp.pause();
                            stopRequested = false;
                        } else {
                            playButton.setGraphic(imageViewPause);
                            //playButton.setText("||");
                        }
                    }
                });
                mp.setOnPaused(new Runnable() {

                    @Override
                    public void run() {
                        //System.out.println("onPaused");
                        playButton.setGraphic(imageViewPlay);
                        //playButton.setText("||");
                    }
                });


                mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
                mp.setOnEndOfMedia(new Runnable() {

                    @Override
                    public void run() {
                        if (!repeat) {
                            playButton.setGraphic(imageViewPlay);
                            //playButton.setText(">");
                            stopRequested = true;
                            atEndOfMedia = true;
                        }
                    }
                });


                mediaBar.getChildren().add(playButton);
                // Add spacer
                Label spacer = new Label("   ");
                mediaBar.getChildren().add(spacer);
                // Time label
                Label timeLabel = new Label("Time: ");
                mediaBar.getChildren().add(timeLabel);
                // Time slider
                timeSlider = new Slider();
                HBox.setHgrow(timeSlider, Priority.ALWAYS);
                timeSlider.setMinWidth(50);
                timeSlider.setMaxWidth(Double.MAX_VALUE);
                timeSlider.valueProperty().addListener(new InvalidationListener() {

                    @Override
                    public void invalidated(Observable ov) {
                        if (timeSlider.isValueChanging()) {
                            // multiply duration by percentage calculated by slider position
                            if (duration != null) {
                                mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
                            }
                            updateValues();

                        }
                    }
                });
                mediaBar.getChildren().add(timeSlider);
                // Play label
                playTime = new Label();
                playTime.setPrefWidth(130);
                playTime.setMinWidth(50);
                mediaBar.getChildren().add(playTime);
                // Volume label
                Label volumeLabel = new Label("Vol: ");
                mediaBar.getChildren().add(volumeLabel);
                // Volume slider
                volumeSlider = new Slider();
                volumeSlider.setPrefWidth(70);
                volumeSlider.setMaxWidth(100);
                volumeSlider.setMinWidth(30);
                volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {

                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (volumeSlider.isValueChanging()) {
                            mp.setVolume(volumeSlider.getValue() / 100.0);
                        }
                    }
                });
                mediaBar.getChildren().add(volumeSlider);
                setBottom(mediaBar);
                mp.setAutoPlay(true);
            }
        };

        mp.setOnReady(readyHandler);
        mp.setOnError(new Runnable() {

            private int attempts;

            @Override
            public void run() {
                mp.stop();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                }
                if (attempts < 10) {
                    attempts++;
                    Media media = new Media(new File(filePath).toURI().toString());
                    mp = new MediaPlayer(media);
                    mp.setOnError(this);
                    mp.setOnReady(readyHandler);
                } else {
                    Text t = new Text("Failed to play video. Try:\n* Not using a remote connection (local playback)\n* A different video file\n* Updating your video card driver");
                    setCenter(t);
                }
            }
        });


    }

    protected void updateValues() {
        if (playTime != null && timeSlider != null && volumeSlider != null && duration != null) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    Duration currentTime = mp.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration.toMillis()).toMillis() * 100.0);
                    }
                    if (!volumeSlider.isValueChanging()) {
                        volumeSlider.setValue((int) Math.round(mp.getVolume() * 100));
                    }
                }
            });
        }
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,
                        durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                        elapsedMinutes, elapsedSeconds);
            }
        }
    }
}