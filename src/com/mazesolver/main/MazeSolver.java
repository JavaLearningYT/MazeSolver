package com.mazesolver.main;
//remove
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Optional;
import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MazeSolver extends Application {
	private int lines;
	private int space;
	private Pair<Integer, Integer> start;
	private Pair<Integer, Integer> end;
	private BufferedImage image;
	private Image tim;
	private Label status = new Label("Waiting");
	PixelatedImageView iv = new PixelatedImageView();
	long[][] maze;
	File iLoc;
	Stage stage = new Stage();
	private int click = 0;
	private BorderPane border = new BorderPane();
	private ScrollPane scrollPane = new ScrollPane();
	final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);
	Rectangle lineColor = new Rectangle(50,50);
	Rectangle spaceColor= new Rectangle(50,50);
	private Button startBut = new Button("start");
	private Button resetBut = new Button("reset");
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage tage) {
		TextInputDialog dialog = new TextInputDialog("D:/maze.png");
		dialog.setTitle("Image Selection");
		dialog.setHeaderText("Maze Selector");
		dialog.setContentText("Please enter the maze location:");
		startBut.setDisable(true);
		Optional<String> result = dialog.showAndWait();
		if (!result.isPresent()) {
			System.exit(0);
		}
		String location = result.get();

		iLoc = new File(location);

		load(location);
		System.out.println(image == null);
		iv.setPickOnBounds(true);
		iv.requestFocus();
		iv.setOnMouseClicked(e -> {
			switch (click) {
			case 0:
				lines = image.getRGB((int) Math.round(e.getX() * (image.getWidth() / iv.getLayoutBounds().getWidth())),
						(int) Math.round(e.getY() * (image.getHeight() / iv.getLayoutBounds().getHeight())));
				Color lColH = new Color(lines);
				javafx.scene.paint.Color lCol = javafx.scene.paint.Color.rgb(lColH.getRed(), lColH.getGreen(), lColH.getBlue(), lColH.getTransparency());
				lineColor.setFill(lCol);
				click++;
				e.consume();
				break;
			case 1:
				space = image.getRGB((int) Math.round(e.getX() * (image.getWidth() / iv.getLayoutBounds().getWidth())),
						(int) Math.round(e.getY() * (image.getHeight() / iv.getLayoutBounds().getHeight())));
				click++;
				Color sColH = new Color(space);
				javafx.scene.paint.Color sCol = javafx.scene.paint.Color.rgb(sColH.getRed(), sColH.getGreen(), sColH.getBlue(), sColH.getTransparency());
				spaceColor.setFill(sCol);
				e.consume();
				break;
			case 2:
				start = new Pair<Integer, Integer>(
						(int) Math.round(e.getX() * (image.getWidth() / iv.getLayoutBounds().getWidth())),
						(int) Math.round(e.getY() * (image.getHeight() / iv.getLayoutBounds().getHeight())));
				image.setRGB(start.getKey(), start.getValue(), Color.BLUE.getRGB());
				iv.setImage(SwingFXUtils.toFXImage(image, null));
				click++;
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
				startBut.setDisable(false);
				System.out.println("good");
				status.setText("Ready");
				break;
			}
		});
		startBut.setOnAction(e->{
			stage.close();
			try {
				image = ImageIO.read(new File(location));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			startBut.setDisable(true);
			resetBut.setDisable(true);
			parse();
			try {
				solve();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			resetBut.setDisable(false);
		});
		resetBut.setOnAction(e->{
			startBut.setDisable(true);
			click=0;
			load(location);
			status.setText("Waiting");
		});
		HBox buttons = new HBox(2);
		buttons.getChildren().addAll(startBut,resetBut);
		buttons.setSpacing(30);
		border.setBottom(buttons);
		VBox colors = new VBox(4);
		colors.getChildren().addAll(new Label("Line Color"),lineColor,new Label("Space Color"),spaceColor);
		colors.setSpacing(10);
		border.setRight(colors);
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
		scrollPane.setContent(iv);
		border.setCenter(scrollPane);
		scrollPane.setMaxSize(800, 800);
		VBox statusB = new VBox(2);
		statusB.getChildren().addAll(new Label("Status"),status);
		border.setTop(new Label("Maze Solver"));
		border.setLeft(statusB);
		stage.setScene(new Scene(border));
		stage.show();

		//System.out.println("done");
	}
	private void load(String location) {
		try {
			tim = new Image(iLoc.toURI().toURL().toString());
			iv.setImage(tim);
			image = ImageIO.read(new File(location));
			//image = convertToARGB(image);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	public static BufferedImage convertToARGB(BufferedImage image)
	{
	    BufferedImage newImage = new BufferedImage(
	        image.getWidth(), image.getHeight(),
	        BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = newImage.createGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	    return newImage;
	}
	private void solve() throws IOException {
		System.out.println("Solving");
		System.out.flush();
		maze[end.getValue()][end.getKey()] = 0;
		int on = 0;
		while (maze[start.getValue()][start.getKey()] == -2) {
			boolean found = false;
			for (int i = 0; i < maze.length; i++) {
				for (int a = 0; a < maze[i].length; a++) {
					if (maze[i][a] == on) {
						try {
							if (maze[i - 1][a] == -2) {
								maze[i - 1][a] = on + 1;
								found =true;
							}
						} catch (IndexOutOfBoundsException iore) {
						}
						try {
							if (maze[i + 1][a] == -2) {
								maze[i + 1][a] = on + 1;
								found = true;
							}
						} catch (IndexOutOfBoundsException iore) {

						}
						try {
							if (maze[i][a - 1] == -2) {
								maze[i][a - 1] = on + 1;
								found = true;
							}
						} catch (IndexOutOfBoundsException iore) {

						}
						try {
							if (maze[i][a + 1] == -2) {
								maze[i][a + 1] = on + 1;
								found = true;
							}
						} catch (IndexOutOfBoundsException iore) {

						}
					}

				}
			}
			if(!found) {
				System.out.println("Out of locations");
				System.out.flush();
				System.exit(0);
			}
			on++;

			if (on%1000==0) {
				System.out.println("on:"+on);
			}
		}
		int x = start.getKey();
		int y = start.getValue();
		long numb = maze[y][x];
		System.out.println("Drawing");
		System.out.flush();
		while (numb >= 2) {

			numb = maze[y][x];
			maze[y][x] = 0;
			try {
				if (maze[y - 1][x] < numb && maze[y - 1][x] > 0) {
					
					image.setRGB(x, y - 1, Color.RED.getRGB());
					y = y - 1;
					continue;
				}
			} catch (IndexOutOfBoundsException iore) {

			}
			try {
				if (maze[y + 1][x] < numb && maze[y + 1][x] > 0) {
					
					image.setRGB(x, y + 1, Color.RED.getRGB());
					y = y + 1;
					continue;
				}
			} catch (IndexOutOfBoundsException iore) {

			}
			try {
				if (maze[y][x - 1] < numb && maze[y][x - 1] > 0) {

					// System.out.println("left");
					image.setRGB(x - 1, y, Color.RED.getRGB());
					x = x - 1;
					continue;
				}
			} catch (IndexOutOfBoundsException iore) {

			}
			try {
				if (maze[y][x + 1] < numb && maze[y][x + 1] > 0) {
					
					image.setRGB(x + 1, y, Color.RED.getRGB());
					x = x + 1;
					continue;
				}
			} catch (IndexOutOfBoundsException iore) {

			}
		}
		String extension = "";

		int i = iLoc.getName().lastIndexOf('.');
		if (i > 0) {
		    extension = iLoc.getName().substring(i+1);
		}
		RenderedImage a = (RenderedImage) image;
		File holder = new File("D:/a."+extension);
		ImageIO.write(a, extension, new FileOutputStream("D:/a."+extension));
		tim = new Image(holder.toURI().toURL().toString());
		iv.setImage(tim);
		stage.show();
		status.setText("Done");
	}

	private void parse() {
		maze = new long[image.getHeight()][image.getWidth()];
		PrintWriter print =null;
		try {
			print = new PrintWriter(new File("D:/moutput.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Color line = new Color(lines);
		Color spac = new Color(space);
		for (int i = 0; i < image.getHeight(); i++) {
			for (int a = 0; a < image.getWidth(); a++) {
				int col = 0;
				try {
					col = image.getRGB(a, i);
				} catch (ArrayIndexOutOfBoundsException g) {
					g.printStackTrace();
				}
				if (col == lines) {
					maze[i][a] = -1;
					print.print(1);
					continue;
				}
				if (col == space) {
					maze[i][a] = -2;
					print.print(0);
					continue;
				}
				maze[i][a] = -9;
				Color color = new Color(col);
				int red = color.getRed();
				int green = color.getGreen();
				int blue = color.getBlue();

				int r1 = Math.abs(red - line.getRed());
				int r2 = Math.abs(red - spac.getRed());

				int g1 = Math.abs(green - line.getGreen());
				int g2 = Math.abs(green - spac.getGreen());

				int b1 = Math.abs(blue - line.getBlue());
				int b2 = Math.abs(blue - spac.getBlue());

				int l = r1 + g1 + b1;
				int s = r2 + g2 + b2;

				if (l > s) {
					maze[i][a] = -2;
					print.print(0);
				} else {
					maze[i][a] = -1;
					print.print(1);
				}

			}
			print.println();
		}
		print.flush();
		print.close();
	}

}
