package com.tnove.algorthms;

import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//import javax.media.jai.Histogram;
//import javax.media.jai.JAI;
//import javax.media.jai.PlanarImage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tnove.dao.*;
//import org.hibernate.Hibernate;


/**
 * ���3��������أ��Ҷ�ֱ��ͼ���ۼӻҶ�ֱ��ͼ��
 * 
 * @author ruibo
 * 
 */
public class CalculateFeature extends HttpServlet {

	private final static int AMOUNT_QUERY_ONETIME = 30;
	private float[][] M = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public CalculateFeature() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=gb2312");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");

		Blob blobQueried = null;
		Blob blobSave1 = null;
		Blob blobSave2 = null;
		Blob blobSave3 = null;
		Image pic = new Image();
		BaseDao dao = new BaseDao();
		ArrayList<Float> arrayListF = new ArrayList<Float>();// ��ͨ��ɫֱ��ͼ��F
		double entropy = 0;
		List list = null;
		String hql = "from Image ";

		int id;
		int indexTable = 0;
		int amountQuery = AMOUNT_QUERY_ONETIME;

		long startTime = System.currentTimeMillis();

		// /�õ�Me��Ⱦ���
		getM();

		while (amountQuery == AMOUNT_QUERY_ONETIME) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				list = dao.findByHQL(hql, indexTable, AMOUNT_QUERY_ONETIME); // һ��ȡ��30���¼
				amountQuery = list.size();
				indexTable += amountQuery;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("���ֲ�ѯ����" + indexTable);
			// /��ÿ���¼����
			for (int j = 0; j < list.size(); j++) { // list.size()
				pic = (Image) list.get(j);
				id = pic.getId();
				blobQueried = (Blob) pic.getValue();

				int width = pic.getWidth();
				int height = pic.getHeight();

				int[] indexHisto = getHistogram(blobQueried, 2);
				// �õ��Ҷ�ֱ��ͼ
				int[] grayHisto = getHistogram(blobQueried, 1);
				// �õ��Ҷ�ֱ��ͼ����
				entropy = getEntropy(grayHisto);
				// �õ��Ҷ�ֱ��ͼ��Ӧ���ۻ���ɫֱ��ͼ��
				int[] grayHistoC = getGrayHistoCumulation(grayHisto);

				// ����F���󣬴���arraylist��,ģ����ɫֱ��ͼ
				float sumF = 0;
				arrayListF.clear();
				for (int i = 0; i < M[0].length; i++) {
					for (int k = 0; k < M.length; k++) {
						sumF += (float) indexHisto[k] * M[k][i];
					}
					arrayListF.add(sumF);
				}

				// ���л�
				try {
					ByteArrayOutputStream out1 = new ByteArrayOutputStream();
					ByteArrayOutputStream out2 = new ByteArrayOutputStream();
					ByteArrayOutputStream out3 = new ByteArrayOutputStream();
					ObjectOutputStream outputStream1 = new ObjectOutputStream(out1);
					ObjectOutputStream outputStream2 = new ObjectOutputStream(out2);
					ObjectOutputStream outputStream3 = new ObjectOutputStream(out3);
					outputStream1.writeObject(arrayListF);
					outputStream2.writeObject(grayHisto);
					outputStream3.writeObject(grayHistoC);
					byte[] bytes1 = out1.toByteArray();
					byte[] bytes2 = out2.toByteArray();
					byte[] bytes3 = out3.toByteArray();

					//Commend by Nevalosa.
					
//					blobSave1 = Hibernate.createBlob(bytes1);
//					blobSave2 = Hibernate.createBlob(bytes2);
//					blobSave3 = Hibernate.createBlob(bytes3);
					out1.close();
					out2.close();
					out3.close();
					outputStream1.close();
					outputStream2.close();
					outputStream3.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.toString();
				}

				// /��������ݿ⣻
				// Feature feature = new Feature();//��һ��ʹ��
				// ��һ���Ժ�ֻ�޸�
				Feature feature = null;
				try {
					feature = (Feature) dao.findById("Feature", id);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (feature == null) {
					feature = new Feature();
					feature.setId(id);
					feature.setWidth(width);
					feature.setHeight(height);
				}

				feature.setEntropy(entropy);
				feature.setFuzzyHistogram(blobSave1);
				feature.setGrayHistogram(blobSave2);
				feature.setGrayCumHistogram(blobSave3);
				try {
					dao.saveOrUpdate(feature);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}// end of for(;i < list.size();)

			dao.clear();
			list.clear();
		}// end of while()

		dao.clear();
		System.out.println("��¼�ĸ���Ϊ�� " + indexTable);
		/**                      */
		long endtime = System.currentTimeMillis();
		System.out.println("��ʱ��" + (endtime - startTime) + "millis");

		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * ���RGB��ɫ�ռ��e��Ⱦ��� ���þ����Ѿ������4���Ĭ��λ�ö����ڴ棬����ͼ���þ���
	 * 
	 * @throws IOException
	 */
	private void getM() throws IOException {

		long starttime = System.currentTimeMillis();
		M = new float[4096][120];
		RandomAccessFile randomAccessFile = null;
		randomAccessFile = new RandomAccessFile("G:/temp/M.log", "r");
		if (randomAccessFile.length() > 0) {
			String element = null;
			for (int i = 0; i < M.length; i++) {
				for (int j = 0; j < M[0].length; j++) {

					element = randomAccessFile.readLine();
					if (element != null)
						M[i][j] = Float.parseFloat(element);

				}
				element = randomAccessFile.readLine();
			}
		}// end of if
		randomAccessFile.close();
		long endtime = System.currentTimeMillis();
		System.out.println("�~���M��ʱ��" + (endtime - starttime) + "millis");
		// û�и��ļ����������¼���
		/*
		 * if(M == null){ FuzzyCMeansClustering fuzzyCMeansClustering = new
		 * FuzzyCMeansClustering(16, 120, 100, 1.9, 0.005);
		 * fuzzyCMeansClustering.FCMClustring(); M =
		 * fuzzyCMeansClustering.getMembership(); PrintWriter MWriter = new
		 * PrintWriter(new FileWriter("G:/temp/M.log")); for (int i = 0; i <
		 * M.length; i++) { for (int j = 0; j < M[0].length; j++) {
		 * MWriter.println(M[i][j]+" "); } MWriter.println(); } MWriter.close();
		 * }
		 */
	}

	/**
	 * ����Ҷ�ֱ��ͼ����Ϣ��
	 * 
	 * @param H
	 *            �Ҷ�ֱ��ͼ
	 * @return
	 */
	private double getEntropy(int[] H) {
		double entropy = 0;
		double p = 0;
		double log2 = Math.log(2);
		for (int i = 0; i < H.length; i++) {
			if (H[i] == 0) {
				continue; // P����Ϊ0.log��0����NaN
			}
			p = (double) H[i];
			entropy -= p * (Math.log(p) / log2);
		}
		return entropy;
	}

	/**
	 * ����Ҷ�ֱ��ͼ
	 * 
	 * @param blobQueried
	 *            ��ݿ�洢��BLOB�������
	 * @param gray1Index2RGB3
	 *            ѡ��ֱ��ͼ����1�Ҷ�ֱ��ͼ��2����ֱ��ͼ��3rgbֱ��ͼ
	 * @return
	 * @throws IOException
	 */
	private int[] getHistogram(Blob blobQueried, int gray1Index2RGB3)
			throws IOException {

		java.awt.Image image = null;
		int[] result = null;
		if (blobQueried != null) {
			try {
				InputStream inputStream = new BufferedInputStream(
						blobQueried.getBinaryStream());
				image = javax.imageio.ImageIO.read(inputStream);
				inputStream.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		int[] pixelsSource = new int[imageWidth * imageHeight];
		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, imageWidth,
				imageHeight, pixelsSource, 0, imageWidth);

		try {
			pixelGrabber.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// ƽ�����
		// int[] pixelsSmooth = getSmoothPixels(pixelsSource, imageWidth,
		// imageHeight);
		switch (gray1Index2RGB3) {
		case 1:
			result = getGrayHistogram(pixelsSource);
			break;
		case 2:
			result = getIndexHistogram(pixelsSource);
			break;
		case 3:
			// result = getRGBHistogram(pixelsSource);
			break;

		default:
			break;
		}

		return result;
	}

	/**
	 * ��������ֱ��ͼ����Ҫ�Ƚ�����ɫ��������ʹ��ͳһ������4096ɫ
	 * 
	 * @return ��������ֱ��ͼ
	 */
	private int[] getIndexHistogram(int[] pixelsSource) throws IOException {

		int[] indexHisto = new int[4096];
		int r, g, b, index;
		for (int i = 0; i < pixelsSource.length; i++) // pixelsSource.length
		{
			int pixels = pixelsSource[i];
			r = (pixels >> 20) & 0xf;
			g = (pixels >> 12) & 0xf;
			b = (pixels >> 4) & 0xf;
			index = (r << 8) + (g << 4) + b;
			indexHisto[index]++;
		}
		return indexHisto;
	}

	/**
	 * �����ۻ���ɫֱ��ͼ
	 * 
	 * @param H
	 *            �Ҷ�ֱ��ͼ
	 * @return �����ۻ�ֱ��ͼ
	 */
	private int[] getGrayHistoCumulation(int[] H) {
		int[] histoC = new int[H.length];
		int hI;

		for (int i = 0; i < H.length; i++) {
			hI = 0;
			for (int j = 0; j <= i; j++) {
				hI += H[j];
			}

			histoC[i] = hI;
		}
		return histoC;
	}

	/**
	 * ����Ҷ�ֱ��ͼ
	 * 
	 * @param pixelsSource
	 *            ͼ������
	 * @return ���ػҶ�ֱ��ͼ
	 */
	private int[] getGrayHistogram(int[] pixelsSource) {

		int[] grayHisto = new int[256];
		int r, g, b, gray;

		for (int i = 0; i < pixelsSource.length; i++) {
			r = (pixelsSource[i] >> 16) & 0xff;
			g = (pixelsSource[i] >> 8) & 0xff;
			b = (pixelsSource[i]) & 0xff;

			gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);

			grayHisto[gray]++;

		}
		return grayHisto;
	}

	/**
	 * ��ͼ�����ƽ����ʹ�ø�˹ģ��ƽ��
	 * 
	 * @param pixels
	 *            ͼ������
	 * @param width
	 * @param height
	 * @return ����ƽ���������
	 */
	public int[] getSmoothPixels(int[] pixels, int width, int height) {

		// ��ͼ�����ƽ�������?Alphaֵ���ֲ���
		ColorModel cm = ColorModel.getRGBdefault();
		for (int i = 1; i < height - 1; i++) {
			for (int j = 1; j < width - 1; j++) {
				int alpha = cm.getAlpha(pixels[i * width + j]);
				int red = cm.getRed(pixels[i * width + j]) << 2;
				int green = cm.getGreen(pixels[i * width + j]) << 2;
				int blue = cm.getBlue(pixels[i * width + j]) << 2;

				// ��ͼ�����ƽ��
				int red1 = cm.getRed(pixels[(i - 1) * width + j - 1]);
				int red2 = cm.getRed(pixels[(i - 1) * width + j]) << 1;
				int red3 = cm.getRed(pixels[(i - 1) * width + j + 1]);
				int red4 = cm.getRed(pixels[i * width + j - 1]) << 1;
				int red6 = cm.getRed(pixels[i * width + j + 1]) << 1;
				int red7 = cm.getRed(pixels[(i + 1) * width + j - 1]);
				int red8 = cm.getRed(pixels[(i + 1) * width + j]) << 1;
				int red9 = cm.getRed(pixels[(i + 1) * width + j + 1]);
				int averageRed = (red + red1 + red2 + red3 + red4 + red6 + red7
						+ red8 + red9) >> 4;

				int green1 = cm.getGreen(pixels[(i - 1) * width + j - 1]);
				int green2 = cm.getGreen(pixels[(i - 1) * width + j]) << 1;
				int green3 = cm.getGreen(pixels[(i - 1) * width + j + 1]);
				int green4 = cm.getGreen(pixels[i * width + j - 1]) << 1;
				int green6 = cm.getGreen(pixels[i * width + j + 1]) << 1;
				int green7 = cm.getGreen(pixels[(i + 1) * width + j - 1]);
				int green8 = cm.getGreen(pixels[(i + 1) * width + j]) << 1;
				int green9 = cm.getGreen(pixels[(i + 1) * width + j + 1]);
				int averageGreen = (green + green1 + green2 + green3 + green4
						+ green6 + green7 + green8 + green9) >> 4;

				int blue1 = cm.getBlue(pixels[(i - 1) * width + j - 1]);
				int blue2 = cm.getBlue(pixels[(i - 1) * width + j]) << 1;
				int blue3 = cm.getBlue(pixels[(i - 1) * width + j + 1]);
				int blue4 = cm.getBlue(pixels[i * width + j - 1]) << 1;
				int blue6 = cm.getBlue(pixels[i * width + j + 1]) << 1;
				int blue7 = cm.getBlue(pixels[(i + 1) * width + j - 1]);
				int blue8 = cm.getBlue(pixels[(i + 1) * width + j]) << 1;
				int blue9 = cm.getBlue(pixels[(i + 1) * width + j + 1]);
				int averageBlue = (blue + blue1 + blue2 + blue3 + blue4 + blue6
						+ blue7 + blue8 + blue9) >> 4;

				pixels[i * width + j] = alpha << 24 | averageRed << 16
						| averageGreen << 8 | averageBlue;
			}
		}
		return pixels;

	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
