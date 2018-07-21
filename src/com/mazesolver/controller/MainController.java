package com.mazesolver.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.awt.Color;
import javax.imageio.ImageIO;

import com.mazesolver.extras.PixelatedImageView;
import com.mazesolver.extras.Solver;
import com.mazesolver.listener.StatusListener;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;

import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainController {
	final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);
	private Image tim;
	private BufferedImage image;
	private Pair<Integer, Integer> start;
	private Pair<Integer, Integer> end;
	private int lines;
	private File file;
	private int space;
	private String oLocation;
	private int click;
	
	PixelatedImageView iv = new PixelatedImageView();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button resetButton;
    
    @FXML
    private BorderPane borderPane;

    @FXML
    private Rectangle wallRect;

    @FXML
    private Rectangle spaceRect;

    @FXML
    private TextField fileLocation;

    @FXML
    private Button loadButton;

    @FXML
    private Button solveButton;

    @FXML
    private Button saveButton;

    @FXML
    private Label stateLabel;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    void help(ActionEvent event) {

    }

    @FXML
    void load(ActionEvent event) {
    	FileChooser chooser = new FileChooser();
    	String check = fileLocation.getText();
    	if(new File(check).exists()) {
    		chooser.setInitialDirectory(new File(check).getParentFile());
    	}
    	chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images","*.gif","*.png","*.jpg","*.jpeg"));
    	Stage stage = new Stage();
    	file = chooser.showOpenDialog(stage);
    	if(file!= null) {
    		try {
				tim = new Image(file.toURI().toURL().toString());
				iv.setImage(tim);
				image = ImageIO.read(file);
				fileLocation.setText(file.toString());
				stateLabel.setText("Click on a Wall");
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    }

    @FXML
    void reset(ActionEvent event) {
    	click=0;
    	wallRect.setFill(javafx.scene.paint.Color.DODGERBLUE);
    	spaceRect.setFill(javafx.scene.paint.Color.DODGERBLUE);
    	stateLabel.setText("Select Image");
    	saveButton.setDisable(true);
    	solveButton.setDisable(true);
    	try {
			tim = new Image(file.toURI().toURL().toString());
			iv.setImage(tim);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    @FXML
    void save(ActionEvent event) {

    }

    @FXML
    void solve(ActionEvent event) {
    	solveButton.setDisable(true);
    	resetButton.setDisable(true);
    	Solver solver = new Solver(file.toString());
    	solver.setListener(new StatusListener() {
    		@Override
    		public void updateText(String text) {
				Platform.runLater(new Runnable() {
					@Override 
					public void run() {
						stateLabel.setText(text);
					}
				});
    		}

			@Override
			public void updateImage(Image image) {
				tim = image;
				iv.setImage(tim);
			}
    	});
    	solver.setColors(lines, space);
    	solver.setPoints(start, end);
    	new Thread(new Runnable() {

			@Override
			public void run() {
				solver.execute();
				resetButton.setDisable(false);
				click=0;
				saveButton.setDisable(false);
				Platform.runLater(new Runnable() {
					@Override 
					public void run() {
						stateLabel.setText("Solved");
					}
				});
			}
    		
    	}).start();;
    }

    @FXML
    void initialize() {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert wallRect != null : "fx:id=\"wallRect\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert spaceRect != null : "fx:id=\"spaceRect\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert fileLocation != null : "fx:id=\"fileLocation\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert loadButton != null : "fx:id=\"loadButton\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert solveButton != null : "fx:id=\"solveButton\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert stateLabel != null : "fx:id=\"stateLabel\" was not injected: check your FXML file 'MainStage.fxml'.";
        assert scrollPane != null : "fx:id=\"scrollPane\" was not injected: check your FXML file 'MainStage.fxml'.";
        setup();
    }

	private void setup() {
		scrollPane.setContent(iv);
		iv.setPickOnBounds(true);
		iv.requestFocus();
		iv.setOnMouseClicked(e -> {
		switch (click) {
			case 0:
				lines = image.getRGB((int) Math.round(e.getX() * (image.getWidth() / iv.getLayoutBounds().getWidth())),
						(int) Math.round(e.getY() * (image.getHeight() / iv.getLayoutBounds().getHeight())));
				Color lColH = new Color(lines);
				javafx.scene.paint.Color lCol = javafx.scene.paint.Color.rgb(lColH.getRed(), lColH.getGreen(), lColH.getBlue(), lColH.getTransparency());
				wallRect.setFill(lCol);
				click++;
				e.consume();
				stateLabel.setText("Click on a corrider");
				break;
			case 1:
				space = image.getRGB((int) Math.round(e.getX() * (image.getWidth() / iv.getLayoutBounds().getWidth())),
						(int) Math.round(e.getY() * (image.getHeight() / iv.getLayoutBounds().getHeight())));
				click++;
				Color sColH = new Color(space);
				javafx.scene.paint.Color sCol = javafx.scene.paint.Color.rgb(sColH.getRed(), sColH.getGreen(), sColH.getBlue(), sColH.getTransparency());
				spaceRect.setFill(sCol);
				e.consume();
				stateLabel.setText("Choose start location");
				break;
			case 2:
				start = new Pair<Integer, Integer>(
						(int) Math.round(e.getX() * (image.getWidth() / iv.getLayoutBounds().getWidth())),
						(int) Math.round(e.getY() * (image.getHeight() / iv.getLayoutBounds().getHeight())));
				image.setRGB(start.getKey(), start.getValue(), Color.BLUE.getRGB());
				iv.setImage(SwingFXUtils.toFXImage(image, null));
				click++;
				stateLabel.setText("Choose end location");
				e.consume();
				break;
			case 3:
				end = new Pair<Integer, Integer>(
						(int) Math.round(e.getX() * (image.getWidth() / iv.getLayoutBounds().getWidth())),
						(int) Math.round(e.getY() * (image.getHeight() / iv.getLayoutBounds().getHeight())));
				e.consume();
				image.setRGB(end.getKey(), end.getValue(), Color.BLUE.getRGB());
				iv.setImage(SwingFXUtils.toFXImage(image, null));
				click++;
				solveButton.setDisable(false);
				stateLabel.setText("Ready to solve");
				break;
			}
		});
		zoomProperty.addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable arg0) {
				iv.setFitWidth(zoomProperty.get() * 4);
				iv.setFitHeight(zoomProperty.get() * 3);

			}
		});

		scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				if (event.getDeltaY() > 0) {
					zoomProperty.set(zoomProperty.get() * 1.1);
				} else if (event.getDeltaY() < 0) {
					zoomProperty.set(zoomProperty.get() / 1.1);
				}
			}

		});
		iv.setSmooth(false);
		iv.preserveRatioProperty().set(true);
	}
}