package com.tnove.algorthms;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * ��bҶ�任
 * @author ruibo
 *
 */
public class FFT 
{

	/**
	 * ��bҶ�任
	 */
	private static final long serialVersionUID = 1L;

	Image im;

	int iw, ih;
	int fw = 1;//ft image
	int fh = 1;
	
	int[] pixels;
	int[] newPixels;

	OneFft of;

	// ���췽��
	public FFT(Image image)
	{
		this.im = image;
	}

	public void setIm(Image image) {
		this.im = image;
	}
	
	/**
	 * ��ʼ��ͼ������
	 * 
	 * @param pixels
	 */
	public void setPixcles(int[] pixels) {
		this.pixels = pixels;
	}
	
	/**
	 * ��ȡͼ�����أ�
	 *
	 */
	public void getOriginalPixels()
	{


		// ��ȡͼ��Ŀ��iw�͸߶�ih
		iw = im.getWidth(null);
		ih = im.getHeight(null);
		pixels = new int[iw * ih];

		// ��ȡͼ�������pixels
		try
		{
			PixelGrabber pg = new PixelGrabber(im, 0, 0, iw, ih, pixels, 0, iw);
			pg.grabPixels();
		}
		catch (InterruptedException e3)
		{
			e3.printStackTrace();
		}

	
	}

	/**
	 * ����FFT�任���õ��任�����������
	 *
	 */
	public void getFFTPixels()
	{
		// �����ȼ���ͼ��,Ȼ��ſ��Խ���FFT�任
		if (pixels != null)
		{

			// ����ֵ
			int w = 1;
			int h = 1;
			int wp = 0;
			int hp = 0;

			// ������и�bҶ�任�Ŀ�Ⱥ͸߶ȣ�2������η���
			while (w * 2 <= iw)
			{
				w *= 2;
				wp++;
			}
			while (h * 2 <= ih)
			{
				h *= 2;
				hp++;
			}

			fw = w;
			fh = h;
			// �����ڴ�
			Complex[] td = new Complex[h * w];
			Complex[] fd = new Complex[h * w];

			newPixels = new int[h * w];

			// ��ʼ��newPixels
			for (int i = 0; i < h; i++)
			{
				for (int j = 0; j < w; j++)
				{
					//ȡ�ûҶ�ֵ
					newPixels[i * w + j] = pixels[i * iw + j] & 0xff;
				}
			}

			// ��ʼ��fd,td
			for (int i = 0; i < h; i++)
			{
				for (int j = 0; j < w; j++)
				{
					fd[i * w + j] = new Complex();//ʵ�鶼Ϊ0
					td[i * w + j] = new Complex(newPixels[i * w + j], 0);//ʵ���Ҷ�ֵ
				}
			}

			// ��ʼ���м��
			Complex[] tempW1 = new Complex[w];
			Complex[] tempW2 = new Complex[w];
			for (int j = 0; j < w; j++)
			{
				tempW1[j] = new Complex(0, 0);
				tempW2[j] = new Complex(0, 0);
			}

			// ��y�����Ͻ��п��ٸ�bҶ�任
			for (int i = 0; i < h; i++)
			{
				// ÿһ����bҶ�任
				for (int j = 0; j < w; j++)
				{
					tempW1[j] = td[i * w + j];
				}

				// ����һάFFT�任
				of = new OneFft();
				of.setData(tempW1, wp);
				tempW2 = of.getData();

				for (int j = 0; j < w; j++)
				{
					fd[i * w + j] = tempW2[j];
				}
			}

			// ����任���
			for (int i = 0; i < h; i++)
			{
				for (int j = 0; j < w; j++)
				{
					td[j * h + i] = fd[i * w + j];
				}
			}

			// ��ʼ���м��
			tempW1 = new Complex[h];
			tempW2 = new Complex[h];
			for (int j = 0; j < h; j++)
			{
				tempW1[j] = new Complex(0, 0);
				tempW2[j] = new Complex(0, 0);
			}

			// ��x������и�bҶ�任
			for (int i = 0; i < w; i++)
			{
				// ÿһ����bҶ�任
				for (int j = 0; j < h; j++)
				{
					tempW1[j] = td[i * h + j];
				}

				// ����һάFFT�任
				of = new OneFft();
				of.setData(tempW1, hp);
				tempW2 = of.getData();

				for (int j = 0; j < h; j++)
				{
					fd[i * h + j] = tempW2[j];
				}
			}

			// ����Ƶ��
			for (int i = 0; i < h; i++)
			{
				for (int j = 0; j < w; j++)
				{
					double re = fd[j * h + i].re;
					double im = fd[j * h + i].im;

					int ii = 0, jj = 0;
					int temp = (int) (Math.sqrt(re * re + im * im) / 100);
					if (temp > 255)
					{
						temp = 255;
					}

					// ��i�У�j�У���Ϊ��ii�У���jj��
					if (i < h / 2)
					{
						ii = i + h / 2;
					}
					else
					{
						ii = i - h / 2;
					}
					if (j < w / 2)
					{
						jj = j + w / 2;
					}
					else
					{
						jj = j - w / 2;
					}

					newPixels[ii * w + jj] = temp * temp;//������
				}
			}
		}	
		//�Եõ��ı任����������˹��һ��
		//int[] sumx = new int[iw];

		
	}
	
	/**
	 * ���л�״�����û�״����
	 * 
	 * @return
	 */
	public double[] getFFTPr() {
		
		double[] Pr = new double[1024];
		int index = 0;
		//ͼ������
		int ox = fw >> 1;
		int oy = fh >> 1;
		int x = 0;
		int y =0;
		for (int i = 0; i < fw; i++) {
			for (int j = 0; j < fh; j++) {
				//��ͼ���е�Ϊԭ��
				x = i - ox;
				y = j - oy;
				index = (int)Math.sqrt(x*x + y*y);
				Pr[index] += newPixels[j*fw + i];
			}
		}
		return Pr;
	}

	/**
	 * ����Ш״������Ш״����
	 * 
	 * @return
	 */
	public int[] getFFTPg() {
		
		int[] angeleP = new int[360];
		int index = 0;
		//ͼ������
		int ox = fw >> 1;
		int oy = fh >> 1;
		int x = 0;
		int y =0;
		double tan = 0;
		for (int i = 0; i < fw; i++) {
			for (int j = 0; j < fh; j++) {
				//��ͼ���е�Ϊԭ��
				x = i - ox;
				y = j - oy;
				if (x ==0 ) {
					index = y > 0? 90:270;
				}
				if(y == 0){//y==0 or x==0&&y==0
					index = x > 0? 0:180;
				}
				if (x != 0 && y != 0) {
					tan = (double)y / x;
					index = (int)Math.toDegrees(Math.atan(tan));
					if (x > 0 ) {//ת������ռ�
						index += y > 0? 0:359;
					}
					if (x < 0) {//y>0ʱ�Ƕ�Ϊ������180����2���ޣ�.y<0ʱ�Ƕ����180����3���ޣ�
						index += 180; 
					}
				}
											
				angeleP[index] += newPixels[j*fw + i];
			}
		}
		
		
		return angeleP;
	}
	
	public static void main(String[] args) throws IOException {

		String[] file = new String[]{"1.bmp","2.bmp","3.bmp","4.jpg"};
		
		int[][] features = new int[file.length][];
		
		for(int i = 0; i< file.length; i++){
			features[i] = getFeature(file[i]);
		}
		
		for(int i = 0; i < features.length; i++)
			for(int j = i+1; j< features.length; j++){
				System.out.println(String.format("Feature %d:%d = %s", i,j, distance(features[i], features[j])));
			}
	
    }
	
	public static int[] getFeature(String file) throws IOException{
		
		BufferedImage bi = ImageIO.read(new File("D:\\case\\",file));
		FFT fft = new FFT(bi);
		fft.getOriginalPixels();
		fft.getFFTPixels();
		
		return fft.getFFTPg();
	}
	
	public static double distance(int[] x, int[] y){
		
		double d = 0;
		for(int i = 0; i < x.length; i++){
			
			d += Math.pow((x[i] -y[i]), 2);
		}
		
		return Math.sqrt(d);
	}
}