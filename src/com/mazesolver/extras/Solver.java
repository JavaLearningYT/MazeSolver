package com.mazesolver.extras;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.mazesolver.listener.StatusListener;

import javafx.embed.swing.SwingFXUtils;
import javafx.util.Pair;

public class Solver {
	String location;
	StatusListener listener;
	BufferedImage image = null;
	private int lines;
	private long[][] maze;
	private int space;
	private Pair<Integer, Integer> start;
	private Pair<Integer, Integer> end;
	
	/**
	 * Constructs a solver for the image at this location
	 * @param location - the location of the image
	 */
	public Solver(String location) {
		this.location=location;
	}
	/**
	 * sets the listener
	 * @param listener - The listener to be called when the image or status should be updated
	 */
	public void setListener(StatusListener listener) {
		this.listener=listener;
	}
	/**
	 * Sets the start and end points of the maze
	 * @param start - starting x y pair
	 * @param end - ending x y pair
	 */
	public void setPoints(Pair<Integer,Integer>start,Pair<Integer,Integer> end) {
		this.start=start;
		this.end=end;
	}
	/**
	 * Sets the color of the walls and spaces
	 * @param walls - the color of the walls
	 * @param spaces - the color of the corridors
	 */
	public void setColors(int walls, int spaces) {
		this.lines=walls;
		this.space=spaces;
	}
	/**
	 * Begin solving the maze 
	 */
	public void execute() {
		try {
		image = ImageIO.read(new File(location));
		}catch(IOException iox) {
			iox.printStackTrace();
			listener.updateText("Invalid file supplied");
			return;
		}
		parse();
		solve();

	}
	/**
	 * Solves the array finding the shortest route from start to end
	 */
	private void solve() {
		listener.updateText("Finding Solution");
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
		listener.updateText("Drawing Path");
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
		listener.updateImage(SwingFXUtils.toFXImage(image,null));
	}

	/**
	 * Parses the image into a array based on pixel colors
	 */
	private void parse() {
		maze = new long[image.getHeight()][image.getWidth()];
		listener.updateText("Parsing Image");
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
					//print.print(1);
					continue;
				}
				if (col == space) {
					maze[i][a] = -2;
					//print.print(0);
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
					//print.print(0);
				} else {
					maze[i][a] = -1;
					//print.print(1);
				}

			}
			//print.println();
		}
		//print.flush();
		//print.close();
	}
}
