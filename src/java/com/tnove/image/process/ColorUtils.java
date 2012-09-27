package com.tnove.image.process;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * http://zh.wikipedia.org/zh/HSL%E5%92%8CHSV%E8%89%B2%E5%BD%A9%E7%A9%BA%E9%97%
 * B4 http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
 * 
 * @author nevalosa
 * 
 */
public class ColorUtils {
	
	private static int R = 23;
	
	public enum COLOR {
		WHITE, BLACK, RED, GREEN, CYAN, YELLOW
	};

	public static double[] RGBtoHSV(int rgb) {
		
		int red = (rgb >> 16) & 0xff;
	    int green = (rgb >> 8) & 0xff;
	    int blue = (rgb) & 0xff;

		return RGBtoHSV(red, green, blue);
	}

	public static double[] RGBtoHSV(double r, double g, double b) {

		double h, s, v;
		double min, max, delta;

		min = Math.min(Math.min(r, g), b);
		max = Math.max(Math.max(r, g), b);
		v = max;
		delta = max - min;

		// S
		if (max != 0)
			s = delta / max;
		else {
			s = 0;
			h = -1;
			return new double[] { h, s, v };
		}

		// H
		if (r == max)
			h = (g - b) / delta; // between yellow & magenta
		else if (g == max)
			h = 2 + (b - r) / delta; // between cyan & yellow
		else
			h = 4 + (r - g) / delta; // between magenta & cyan

		h *= 60; // degrees

		if (h < 0)
			h += 360;

		return new double[] { h, s, v };
	}

	public static int getHSVColorType(double r, double g, double b) {

		double[] hsv = RGBtoHSV(r, g, b);
		return getHSVColorType(hsv);
	}

	public static int getHSVColorType(double[] hsv) {
		if(hsv[2] < 0.15) return 6;//Black
		if(hsv[1]<0.1 && hsv[2] > 0.8) return 7;//White
		int h = (int) hsv[0] / 60;
		return (h>=6)? 5:h ;
	}

	/**
	 * 1.If H is 300 -> 60 , set R = V 2.If H is 60 -> 180, set G = V 3.If H is
	 * 180 -> 300, set B = V
	 * 
	 * @param hsv
	 * @return
	 */
	public static int getCenterPoint(double[] hsv) {

		if(hsv[2] < 0.15 || (hsv[1]<0.1 && hsv[2] > 0.8)) return -1;
		
		if (hsv[0] > 300 || hsv[0] <= 60)
			return 0;
		if (hsv[0] > 60 && hsv[0] <= 180)
			return 1;
		return 2;
	}
	
	public static int[] getImageCenter(BufferedImage bi){
		
		int[] mid = new int[2];
		mid[0] = bi.getWidth() / 2;
		mid[1] = bi.getHeight() / 2;
		return mid;
	}

	public static int[] getRGBCenter(BufferedImage bi) {
		// center_R:x,y
		int[][] center = new int[3][3];
		int w = bi.getWidth();
		int h = bi.getHeight();

		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				int rgb = bi.getRGB(x, y);
				double[] hsv = RGBtoHSV(rgb);
				int index = getCenterPoint(hsv);
				if (index < 0)
					continue;
				center[index][0]++;
				center[index][1] += x;
				center[index][2] += y;
			}

		for (int i = 0; i < center.length; i++) {
			if(center[i][0] == 0) continue;
			center[i][1] /= center[i][0];
			center[i][2] /= center[i][0];
		}
		
		int[] c = new int[2];
		c[0] = (center[0][1] + center[1][1] + center[2][1]) /3;
		c[1] = (center[0][2] + center[1][2] + center[2][2])/3;

		return c;
	}

	/**
	 * 
	 * @param bi
	 * @param center
	 * @param r
	 *            the base radius for controlling the size of encode. And
	 *            recommend value is not more then 23;
	 * @return
	 */
	public static int[][] calculateColor(BufferedImage bi, int[] mid) {
		
		int midX = mid[0];
		int midY = mid[1];
		double radius = Math.min(bi.getHeight() -midY, Math.min(Math.min(midX, midY),bi.getWidth() - midX))/2;
		double ratio = radius/R;
		
		// System.out.println("bi.getWidth()"+bi.getWidth()+"\tbi.getHeight()"+bi.getHeight());
		int minX = (int) Math.max(0, midX - 2 * radius);
		int maxX = (int) Math.min(bi.getWidth(), midX + 2 * radius);
		int minY = (int) Math.max(0, midY - 2 * radius);
		int maxY = (int) Math.min(bi.getHeight(), midY + 2 * radius);
		
//		System.out.println("Mid["+midX+","+midY+"] Radius :"+ radius+"  range :[" +minX+","+ maxX+","+minY+","+maxY+"]");
		
		// for 4 field, stats 6 color.
		int[][] stats = new int[4][8];
		double sqarR = Math.pow(radius, 2);
		
		for (int x = minX; x < maxX; x++)
			for (int y = minY; y < maxY; y++) {
				double D = Math.pow(x - midX, 2) + Math.pow(y - midY, 2);
				int field = (int) (D / sqarR);
				
				if (field >= 4)
					continue;

				int rgb = bi.getRGB(x, y);
				double[] hsv = RGBtoHSV(rgb);
				int type = getHSVColorType(hsv);
				stats[field][type]++;
			}
		
		
		for(int i = 0; i < stats.length; i++){
			for(int j = 0; j< stats[i].length; j++){
				if(stats[i][j] == 0)continue;
				stats[i][j] /= Math.pow((ratio), 2);
			}
		}
		
		return stats;
	}

	/**
	 * 
	 * @param stats
	 * @param lim
	 *            recommend value is 4;
	 * @return
	 */
	public static int[] getEncodeInLog(int[][] stats, int lim) {

		int[] codes = new int[4];

		for (int i = 0; i < stats.length; i++) {

			int code = 0;
//			System.out.print("field"+i);
			for (int j = 0; j < stats[i].length; j++) {

				code <<= 3;
				int c = 0;
				int orig = stats[i][j];
				
				while (orig != 0) {

					orig >>>= 1;
					c++;
				}

				c = Math.max(0, c - lim);
				code |= c;
//				System.out.print("["+stats[i][j]+","+c+"]\t");
			}
			codes[i] = code;
//			System.out.println();
		}
//		System.out.println("Encode :");
		return codes;
	}
	
	/**
	 * 
	 * @param stats
	 * @param lim
	 *            recommend is 1;
	 * @return
	 */
	public static int[] getEncodeInDivide(int[][] stats, int lim) {
		
		int[] codes = new int[4];
		
		for (int i = 0; i < stats.length; i++) {
			
			int code = 0;

			for (int j = 0; j < stats[i].length; j++) {

				code <<= 4;
				int c = 0;
				int orig = stats[i][j];

				while (orig > 0) {

					orig -= 100;
					c++;
				}

				c = Math.max(0, c - lim);
				code |= c;
			}
			codes[i] = code;
		}

		return codes;

	}

	public static int[] getImageEncode(File file) throws IOException {

		BufferedImage bi = ImageIO.read(file);
		int[] mid = getImageCenter(bi);
		int[][] stats = calculateColor(bi,mid);
		return getEncodeInLog(stats, 4);
	}
}
