package com.tnove.util;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class DrawInImg {

	private boolean isDone = false;
	private String inputFileName = "";
	private String outputFileName = "";
	private int[][] RectInt;

	public DrawInImg(String inputFileName, String outputFileName,
			int[][] RectInt) {
		this.inputFileName = inputFileName;
		this.outputFileName = outputFileName;
		this.RectInt = RectInt;
	}

	public boolean DrawRect() {
		try {
			FileInputStream fis = new FileInputStream(inputFileName);
			FileOutputStream fos = new FileOutputStream(outputFileName);
			BufferedImage img = ImageIO.read(fis);
			Graphics g = img.getGraphics();
			g.setColor(Color.GREEN);
			for (int i = 0; i < RectInt.length; i++) {
				for (int j = 0; j < RectInt[i].length; j++) {
					g.drawRect(RectInt[i][0], RectInt[i][1], RectInt[i][2],
							RectInt[i][3]);
					// System.out.print(RectInt[i][j]+" ");
				}
				System.out.print("\r\n");
			}

			img.flush();
			g.dispose();
			ImageIO.write(img, "JPG", fos);

			while (true) {
				if (new File(outputFileName).exists()) {
					this.isDone = true;
					break;
				}
			}

		} catch (IOException ioe) {
			ioe = null;
		}

		return this.isDone;
	}

}