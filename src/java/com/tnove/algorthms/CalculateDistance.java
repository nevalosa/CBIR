package com.tnove.algorthms;

import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import com.tnove.dao.BaseDao;
import com.tnove.dao.Feature;

/**
 * CalculateDistance
 * 
 * @Description here:
 * @author Po Rui
 * 
 */
public class CalculateDistance {

	private final static int AMOUNT_RESULT = 50;

	private static float[][] M = null;

	public CalculateDistance() {

	}

	/**
	 * @param imagePath
	 * @param method
	 * @param methodDis
	 * @param greyCoMFeaure
	 * @return 
	 * @throws IOException
	 */
	public float[] getIndex(String imagePath, int method, int methodDis,
			int greyCoMFeaure) throws IOException {

		File imageFile = new File(imagePath);
		Image image = javax.imageio.ImageIO.read(imageFile);
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		int pixelsSource[];
		long starttime = System.currentTimeMillis();
		pixelsSource = new int[imageWidth * imageHeight];
		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, imageWidth,
				imageHeight, pixelsSource, 0, imageWidth);

		try {
			pixelGrabber.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// int[] pixelsSmooth = getSmoothPixels(pixelsSource, imageWidth,
		// imageHeight);

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

		double[] grayHisto = getGrayHistogram(pixelsSource);

		float[][] result = null;
		switch (method) {
		case 1:
			result = CalculatDistanceEntropy(grayHisto);
			break;
		case 2:
			result = CalculatDistanceGrayHisto(grayHisto, methodDis);
			break;
		case 3:
			if (M == null) {
				getM();
			}
			result = CalculatDistanceF(indexHisto);
			break;
		case 4:
			float Bi[][] = getSpatialHistogram(pixelsSource, imageWidth,
					imageHeight);
			result = CalculatDistanceSpatial(grayHisto, imageWidth,
					imageHeight, Bi);
			break;
		case 5:
			double[][][] greyCoMP = getCoMGrey(pixelsSource, imageWidth,
					imageHeight);
			greyCoMP = MatrixUnitary(greyCoMP, imageWidth, imageHeight);
			double[] featuresGreyCoM = getFeature(greyCoMP);
			if (greyCoMFeaure == 0) {
				result = CalculatDistanceGreyCoMatrix(featuresGreyCoM,
						methodDis, true);
			} else {
				result = CalculatDistanceGreyCoMatrix(featuresGreyCoM,
						greyCoMFeaure, false);
			}
			break;
		case 6: // FFT
			FFT myFft = new FFT(image);
			myFft.getOriginalPixels();
			myFft.getFFTPixels();
			double[] pr = myFft.getFFTPr();
			int[] pg = myFft.getFFTPg();
			result = CalculatDistanceFFT(pr, pg);
			break;
		case 7:
			
			/*
			 * if(M == null){ getM(); } result = CalculatDistanceF(indexHisto);
			 */
			// result = CalculatDistanceEntropy(grayHisto);

			
			result = CalculatDistanceGrayHisto(grayHisto, 2);
			FFT myFft1 = new FFT(image);
			myFft1.getOriginalPixels();
			myFft1.getFFTPixels();
			double[] pr1 = myFft1.getFFTPr();
			int[] pg1 = myFft1.getFFTPg();
			result = getSecondIndex1(result, pr1, pg1);
			break;
		case 8:
			FFT myFft2 = new FFT(image);
			myFft2.getOriginalPixels();
			myFft2.getFFTPixels();
			double[] pr2 = myFft2.getFFTPr();
			int[] pg2 = myFft2.getFFTPg();
			result = CalculatDistanceFFT(pr2, pg2);

			if (M == null) {
				getM();
			}
			result = getSecondIndex2(result, indexHisto);
			break;

		default:
			break;
		}
		/*
		 * for (int i = 0; i < result[0].length; i++) {
		 * System.out.println("ID:"+result[0][i]+" dis:"+result[1][i]); }
		 */
//		System.out.println(" -- "
//				+ (System.currentTimeMillis() - starttime) / 1000 + " ��");

		return result[0];
	}

	/**
	 * ����ģ����ɫֱ��ͼ�ľ���
	 * 
	 * @param H
	 *            ģ����ɫֱ��ͼ
	 * @return ���������ǰ30��ͼ������
	 */
	private float[][] CalculatDistanceF(int[] H) {

		Blob blobQueried = null;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		@SuppressWarnings("rawtypes")
		List list = null;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;
		ArrayList<Float> arrayListF = null;
		int indexResultSet = 0;
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}
		int sumI = 0;
		for (int i = 0; i < H.length; i++) {
			sumI += H[i];
		}
		float[] F = new float[M[0].length];
		float sum = 0;
		for (int i = 0; i < M[0].length; i++) {
			for (int j = 0; j < M.length; j++) {
				sum += (float) H[j] * M[j][i];
			}
			F[i] = sum;
		}

		int indexTable = 0;
		while (amountReadOnetime == 30) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
				amountReadOnetime = list.size();
				indexTable += amountReadOnetime;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ��ÿ��ͼ����F
			for (int j = 0; j < list.size(); j++) { // list.size()
				feature = (Feature) list.get(j);
				blobQueried = feature.getFuzzyHistogram();

				id = feature.getId();
				try {
					ObjectInputStream in = new ObjectInputStream(
							blobQueried.getBinaryStream());
					arrayListF = (ArrayList<Float>) in.readObject();
					in.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				float distance = 0.0f;
				float tt;
				for (int i = 0; i < arrayListF.size(); i++) {
					tt = (float) (arrayListF.get(i) - F[i]);
					distance += tt * tt;
				}

				if (indexResultSet < AMOUNT_RESULT) {
					result[1][indexResultSet] = distance;
					result[0][indexResultSet] = id;
					// System.out.println("�����ӡ�� "+indexResultSet);

				} else {// ��������
					float maxDis = 0.0f;
					int posMaxDis = 0;
					for (int k = 0; k < result[1].length; k++) {
						if (result[1][k] > maxDis) {
							maxDis = result[1][k];
							posMaxDis = k;
						}
					}
					if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
						result[0][posMaxDis] = id;
						result[1][posMaxDis] = distance;
					}
				}

				indexResultSet++;

			}// end of for()

			list.clear();
			dao.clear();
		} // end of while

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;
	}

	/**
	 * ����ռ���ɫֱ��ͼ��ľ��룬���ռ���ɫֱ��ͼ�������Զ�
	 * 
	 * @param H
	 *            �Ҷ���ɫֱ��ͼ
	 * @param width
	 * @param height
	 * @param Bi
	 *            �ռ���ɫֱ��ͼ
	 * @return ���ز�ѯ��������ǰ30��ͼ������
	 */
	private float[][] CalculatDistanceSpatial(double[] H, int width,
			int height, float[][] Bi) {

		Blob blobQueriedSpatial = null;
		Blob blobQueriedGrayH = null;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		List list = null;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;
		// ArrayList<Float> arrayListF = null;
		float[][] spatialHisto = null;
		int[] grayHisto = null;
		int indexResultSet = 0;
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}

		int indexTable = 0;
		while (amountReadOnetime == 30) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
				amountReadOnetime = list.size();
				indexTable += amountReadOnetime;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(list.size());
			// /��ÿ��ͼ����
			for (int j = 0; j < list.size(); j++) { // list.size()
				feature = (Feature) list.get(j);
				blobQueriedSpatial = feature.getSpatialHistogram();
				blobQueriedGrayH = feature.getGrayHistogram();

				id = feature.getId();

				int qWidth = feature.getWidth();
				int qHeight = feature.getHeight();

				// �����л�
				try {
					ObjectInputStream in1 = new ObjectInputStream(
							blobQueriedSpatial.getBinaryStream());
					ObjectInputStream in2 = new ObjectInputStream(
							blobQueriedGrayH.getBinaryStream());

					// �õ���ݿ����
					spatialHisto = (float[][]) in1.readObject();
					grayHisto = (int[]) in2.readObject();

					in1.close();
					in2.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				// ����ռ�ֱ��ͼ���룻
				float distance = 0.0f;
				double distanceEuc = 0.0;
				float div = 0.0f;
				double sqrt2 = Math.sqrt(2);
				int totlePixelsH = width * height;
				int totlePixelsQ = qWidth * qHeight;
				for (int i = 0; i < grayHisto.length; i++) {

					if ((Bi[2][i] == 0) && (spatialHisto[2][i] == 0)) {
						continue;
					}
					distanceEuc = Math.sqrt((spatialHisto[0][i] - Bi[0][i])
							* (spatialHisto[0][i] - Bi[0][i])
							+ (spatialHisto[1][i] - Bi[1][i])
							* (spatialHisto[1][i] - Bi[1][i]));
					if (spatialHisto[2][i] >= Bi[2][i]) {
						div = Bi[2][i] / spatialHisto[2][i];
					} else {
						div = spatialHisto[2][i] / Bi[2][i];
					}
					distance += (grayHisto[i] >= H[i] ? ((double) H[i] / totlePixelsH)
							: ((double) grayHisto[i] / totlePixelsQ))
							* ((sqrt2 - distanceEuc) / sqrt2 + div);
				}

				System.out.println("ID:" + id + " ÿ���ľ���:" + distance);
				distance = 2 - distance;

				if (indexResultSet < AMOUNT_RESULT) {
					result[1][indexResultSet] = distance;
					result[0][indexResultSet] = id;

				} else {// ��������
					float maxDis = 0.0f;
					int posMaxDis = 0;
					for (int k = 0; k < result[1].length; k++) {
						if (result[1][k] > maxDis) {
							maxDis = result[1][k];
							posMaxDis = k;
						}
					}
					if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
						result[0][posMaxDis] = id;
						result[1][posMaxDis] = distance;
					}
				}

				indexResultSet++;

			}// end of for()

			list.clear();
			dao.clear();
		} // end of while

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;
	}

	/**
	 * ����Ҷ�ֱ��ͼ֮��ľ��롣
	 * 
	 * @param H
	 *            �Ҷ���ɫֱ��ͼ
	 * @param methodDis
	 *            ������㷨
	 * @return ���ز�ѯ��������ǰ30��ͼ������
	 */
	private float[][] CalculatDistanceGrayHisto(double[] H, int methodDis) {

		Blob blobQueried = null;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		List list = null;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;
		int[] HistoI = null; // �Ҷ�ֱ��ͼ
		int indexResultSet = 0;
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}

		int indexTable = 0;
		while (amountReadOnetime == 30) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
				amountReadOnetime = list.size();
				indexTable += amountReadOnetime;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// /��ÿ������������
			for (int j = 0; j < list.size(); j++) { // list.size()
				feature = (Feature) list.get(j);
				if (1 == 1) {
					blobQueried = feature.getGrayHistogram();// ��ûҶ�ֱ��ͼ

				} else {
					// blobQueried = feature.getGraycumhistogram();
					// //��ûҶ��ۼ�ֱ��ͼ
				}

				id = feature.getId();
				try {
					ObjectInputStream in = new ObjectInputStream(
							blobQueried.getBinaryStream());
					HistoI = (int[]) in.readObject(); // �����л�
					in.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				double[] Histo = new double[HistoI.length];
				for (int i = 0; i < Histo.length; i++) {
					Histo[i] = (double) HistoI[i];
				}
				// ������룬1-���� 2-ŷʽ 3-���� 4-K-L 5-Jeffrey 6-���� 7-����
				double distance = 0.0;
				switch (methodDis) {
				case 1:
					distance = getCrossDistance(H, Histo);// �������
					break;
				case 2:
					distance = getEucDistance(H, Histo);// ŷ�Ͼ���
					break;
				case 3:
					distance = getKaDistance(H, Histo);// ��������
					break;
				case 4:
					distance = getK_LDistance(H, Histo);// K-L����
					break;
				case 5:
					distance = getJeffreyDistance(H, Histo);// Jeffrey����
					break;
				case 6:
					distance = getCosineDistance(H, Histo);// ���Ҿ���
					break;
				case 7:
					distance = getMahalaDis(H, Histo);// ���Ͼ���
					break;

				default:
					break;
				}

				if (indexResultSet < AMOUNT_RESULT) {
					result[1][indexResultSet] = (float) distance;
					result[0][indexResultSet] = id;

				} else {// ��������
					float maxDis = 0.0f;
					int posMaxDis = 0;
					for (int k = 0; k < result[1].length; k++) {
						if (result[1][k] > maxDis) {
							maxDis = result[1][k];
							posMaxDis = k;
						}
					}
					if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
						result[0][posMaxDis] = id;
						result[1][posMaxDis] = (float) distance;
					}
				}

				indexResultSet++;

			}// end of for()

			list.clear();
			dao.clear();
		} // end of while

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;

	}

	/**
	 * ������Ϣ�ؼ�ľ���
	 * 
	 * @param H
	 *            �Ҷ�ֱ��ͼ
	 * @return ���ز�ѯ��������ǰ30��ͼ������
	 */
	private float[][] CalculatDistanceEntropy(double[] H) {

		double entropyi = 0;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		List list = null;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;
		int indexResultSet = 0;
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}

		// ������,��256
		double entropy = 0;
		double p = 0;
		for (int i = 0; i < H.length; i++) {
			if (H[i] == 0) {
				continue; // P����Ϊ0.log��0����NaN
			}
			p = (double) H[i];
			entropy -= p * (Math.log(p) / Math.log(2));
		}

		int indexTable = 0;
		while (amountReadOnetime == 30) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
				amountReadOnetime = list.size();
				indexTable += amountReadOnetime;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// /��ÿ��ͼ��ȡ��
			for (int j = 0; j < list.size(); j++) { // list.size()
				feature = (Feature) list.get(j);

				id = feature.getId();
				entropyi = feature.getEntropy();

				float distance = (float) (entropy - entropyi);
				distance = Math.abs(distance);

				if (indexResultSet < AMOUNT_RESULT) {
					result[1][indexResultSet] = (float) distance;
					result[0][indexResultSet] = id;

				} else {// ��������
					float maxDis = 0.0f;
					int posMaxDis = 0;
					for (int k = 0; k < result[1].length; k++) {
						if (result[1][k] > maxDis) {
							maxDis = result[1][k];
							posMaxDis = k;
						}
					}
					if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
						result[0][posMaxDis] = id;
						result[1][posMaxDis] = (float) distance;
					}
				}

				indexResultSet++;

			}// end of for()

			list.clear();
			dao.clear();
		} // end of while

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;
	}

	/**
	 * ����Ҷȹ�������ľ���
	 * 
	 * @param featuresGreyCoM
	 *            �Ҷȹ������
	 * @param greyCoMFeature
	 *            �Ҷȹ�����������
	 * @param isUseAll
	 *            �Ƿ�ʹ�����е�����
	 * @return ���ز�ѯ��������ǰ30��ͼ������
	 */
	private float[][] CalculatDistanceGreyCoMatrix(double[] featuresGreyCoM,
			int greyCoMFeature, boolean isUseAll) {

		Blob blobQueried = null;
		double[] featuresi = null;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		List list = null;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;
		int indexResultSet = 0;
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}

		int indexTable = 0;
		while (amountReadOnetime == 30) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
				amountReadOnetime = list.size();
				indexTable += amountReadOnetime;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// /��ÿ��ͼ��ȡ
			for (int j = 0; j < list.size(); j++) { // list.size()
				feature = (Feature) list.get(j);

				id = feature.getId();
				blobQueried = feature.getGreyCoMatrix();
				try {
					ObjectInputStream in = new ObjectInputStream(
							blobQueried.getBinaryStream());
					featuresi = (double[]) in.readObject(); // �����л�
					in.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				// �������
				double distance = 0;
				if (isUseAll) {
					// ������룬1-���� 2-ŷʽ 3-���� 4-K-L 5-Jeffrey 6-���� 7-����
					switch (greyCoMFeature) {
					case 1:
						distance = getCrossDistance(featuresGreyCoM, featuresi);// �������
						break;
					case 2:
						distance = getEucDistance(featuresGreyCoM, featuresi);// ŷ�Ͼ���
						break;
					case 3:
						distance = getKaDistance(featuresGreyCoM, featuresi);// ��������
						break;
					case 4:
						distance = getK_LDistance(featuresGreyCoM, featuresi);// K-L����
						break;
					case 5:
						distance = getJeffreyDistance(featuresGreyCoM,
								featuresi);// Jeffrey����
						break;
					case 6:
						distance = getCosineDistance(featuresGreyCoM, featuresi);// ���Ҿ���
						break;
					case 7:
						distance = getMahalaDis(featuresGreyCoM, featuresi);// ���Ͼ���
						break;

					default:
						break;
					}
					distance = Math.abs(distance);
				} else {
					/**
					 * ע���в����1��8ת��Ϊ��0��7
					 */
					int CoMFeature = greyCoMFeature;//
					CoMFeature--;
					distance = featuresGreyCoM[CoMFeature]
							- featuresi[CoMFeature];
					distance = Math.abs(distance);
				}

				if (indexResultSet < AMOUNT_RESULT) {
					result[1][indexResultSet] = (float) distance;
					result[0][indexResultSet] = id;

				} else {// ��������
					float maxDis = 0.0f;
					int posMaxDis = 0;
					for (int k = 0; k < result[1].length; k++) {
						if (result[1][k] > maxDis) {
							maxDis = result[1][k];
							posMaxDis = k;
						}
					}
					if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
						result[0][posMaxDis] = id;
						result[1][posMaxDis] = (float) distance;
					}
				}

				indexResultSet++;

			}// end of for()

			list.clear();
			dao.clear();
		} // end of while

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;
	}

	/**
	 * ���㸵bҶ�任��������ľ���
	 * 
	 * @param pr
	 *            ��״����
	 * @param pg
	 *            Ш״����
	 * @return ���ز�ѯ��������ǰ30��ͼ������
	 */
	private float[][] CalculatDistanceFFT(double[] pr, int[] pg) {

		Blob blobQueriedPr = null;
		Blob blobQueriedPg = null;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		List list = null;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;

		double[] Pri = null;
		double[] Pgi = null;
		int indexResultSet = 0;
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}

		int indexTable = 0;
		while (amountReadOnetime == 30) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
				amountReadOnetime = list.size();
				indexTable += amountReadOnetime;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// /��ÿ��ͼ����
			for (int j = 0; j < list.size(); j++) { // list.size()
				feature = (Feature) list.get(j);
				blobQueriedPr = feature.getFftPr();
				blobQueriedPg = feature.getFftPg();

				id = feature.getId();

				// �����л�
				try {
					ObjectInputStream in1 = new ObjectInputStream(
							blobQueriedPr.getBinaryStream());
					ObjectInputStream in2 = new ObjectInputStream(
							blobQueriedPg.getBinaryStream());

					// �õ���ݿ����
					Pri = (double[]) in1.readObject();
					Pgi = (double[]) in2.readObject();

					in1.close();
					in2.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				// ����FFT���룻
				float distance = 0;
				double tt = 0;
				double sumPr = 0;
				double sumPg = 0;
				for (int i = 0; i < Pri.length; i++) {
					tt = pr[i] - Pri[i];
					sumPr += tt * tt;
				}
				for (int i = 0; i < Pgi.length; i++) {
					tt = pg[i] - Pgi[i];
					sumPg += tt * tt;
				}
				// Ȩ��w1=w2=1
				distance = (float) Math.sqrt(sumPr) + (float) Math.sqrt(sumPg);

				if (indexResultSet < AMOUNT_RESULT) {
					result[1][indexResultSet] = distance;
					result[0][indexResultSet] = id;
					// System.out.println("�����ӡ�� "+indexResultSet);

				} else {// ��������
					float maxDis = 0.0f;
					int posMaxDis = 0;
					for (int k = 0; k < result[1].length; k++) {
						if (result[1][k] > maxDis) {
							maxDis = result[1][k];
							posMaxDis = k;
						}
					}
					if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
						result[0][posMaxDis] = id;
						result[1][posMaxDis] = distance;
					}
				}

				indexResultSet++;

			}// end of for()

			list.clear();
			dao.clear();
		} // end of while

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;
	}

	/**
	 * �������Ͼ���
	 * 
	 * @param Hp
	 *            ����p
	 * @param Hq
	 *            ����q
	 * @return
	 */
	private double getMahalaDis(double[] Hp, double[] Hq) {

		double[] U = new double[Hp.length];
		for (int i = 0; i < U.length; i++) {
			U[i] = (Hp[i] + Hq[i]) / 2;
		}
		double[][] S = new double[Hp.length][Hp.length];
		for (int i = 0; i < S.length; i++) {
			// S(i,j)={[His1(i)-u(i)]*[His1(j)-u(j)]+[His2(i)-u(i)]*[His2(j)-u(j)]}/2
			for (int j = 0; j < S.length; j++) {
				S[i][j] = ((Hp[i] - U[i]) * (Hp[j] - U[j]) + (Hq[i] - U[i])
						* (Hq[j] - U[j])) / 2;
			}
		}
		// ��S�������
		// D=sqrt{[His1-His2] * S^(-1) * [(His1-His2)��ת������]}
		double[] pp = new double[256];
		double dis = 0;
		boolean dd = invertGaussJordan(S);
		System.out.println("�Ƿ�ɹ���" + dd);
		if (dd) {
			int k = 0;
			for (int i = 0; i < Hp.length; i++) {
				for (int j = 0; j < Hp.length; j++) {
					k += (Hp[j] - Hq[j]) * S[j][i];
				}
				pp[i] = k;
			}

			for (int i = 0; i < pp.length; i++) {
				dis += pp[i] * (Hp[i] - Hq[i]);
			}
		}
		dis = Math.sqrt(dis);
		return dis;
	}

	/**
	 * ��˹ת��������Э�������
	 * 
	 * @param Matrix
	 * @return
	 */
	private boolean invertGaussJordan(double[][] Matrix) {
		int i, j, k, l;
		double d = 0, p = 0;

		int numCols = Matrix.length;
		int[] pnRow = new int[numCols];
		int[] pnCol = new int[numCols];

		for (k = 0; k <= numCols - 1; k++) {
			d = 0.0;
			for (i = k; i <= numCols - 1; i++) {
				for (j = k; j <= numCols - 1; j++) {
					// l = i*numCols+j;
					p = Math.abs(Matrix[i][j]);
					if (p > d) {
						d = p;
						pnRow[k] = i;
						pnCol[k] = j;
					}
				}
			}

			// shibai
			if (d == 0.0) {
				return false;
			}

			if (pnRow[k] != k) {
				for (j = 0; j <= numCols - 1; j++) {
					p = Matrix[k][j];
					Matrix[k][j] = Matrix[pnRow[k]][j];
					Matrix[pnRow[k]][j] = p;
				}
			}

			if (pnCol[k] != k) {
				for (i = 0; i <= numCols - 1; i++) {
					p = Matrix[i][k];
					Matrix[i][k] = Matrix[i][pnCol[k]];
					Matrix[i][pnCol[k]] = p;
				}
			}

			l = k;
			Matrix[k][k] = 1 / Matrix[k][k];
			for (j = 0; j <= numCols - 1; j++) {
				if (j != k) {
					Matrix[k][j] = Matrix[k][j] * Matrix[k][k];
				}
			}

			for (i = 0; i <= numCols - 1; i++) {
				if (i != k) {
					for (j = 0; j < numCols - 1; j++) {
						if (j != k) {
							Matrix[i][j] = Matrix[i][j] - Matrix[i][k]
									* Matrix[k][j];
						}
					}
				}
			}

			for (i = 0; i <= numCols - 1; i++) {
				if (i != k) {
					Matrix[i][k] = -Matrix[i][k] * Matrix[l][l];
				}
			}
		}

		// �ָ����д���
		for (k = numCols - 1; k >= 0; k--) {
			if (pnCol[k] != k) {
				for (j = 0; j <= numCols - 1; j++) {
					p = Matrix[k][j];
					Matrix[k][j] = Matrix[pnCol[k]][j];
					Matrix[pnCol[k]][j] = p;
				}
			}

			if (pnRow[k] != k) {
				for (i = 0; i <= numCols - 1; i++) {
					p = Matrix[i][k];
					Matrix[i][k] = Matrix[i][pnRow[k]];
					Matrix[i][pnRow[k]] = p;
				}
			}
		}
		return true;
	}

	/**
	 * ���㽻���
	 * 
	 * @param Hp
	 * @param Hq
	 * @return
	 */
	private double getCrossDistance(double[] Hp, double[] Hq) {
		double dis = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < Hp.length; i++) {// ���Ż�
			sum1 += Hp[i];
		}
		for (int i = 0; i < Hq.length; i++) {
			sum2 += Hp[i] > Hq[i] ? Hq[i] : Hp[i];
		}

		dis = 1 - sum2 / sum1;
		return dis;
	}

	/**
	 * �����˹��
	 * 
	 * @param Hp
	 * @param Hq
	 * @return
	 */
	private double getEucDistance(double[] Hp, double[] Hq) {
		double dis = 0.0;
		for (int i = 0; i < Hq.length; i++) {
			dis += (Hp[i] - Hq[i]) * (Hp[i] - Hq[i]);
		}

		dis = Math.sqrt(dis);
		return dis;
	}

	private double getKaDistance(double[] Hp, double[] Hq) {
		double dis = 0.0;
		double mean = 0.0;
		for (int i = 0; i < Hq.length; i++) {
			mean = (Hp[i] + Hq[i]) / 2;
			if (mean == 0) {
				continue;
			}
			dis += (Hp[i] - mean) * (Hp[i] - mean) / mean;
		}

		return dis;
	}

	/**
	 * ����K_L��
	 * 
	 * @param Hp
	 * @param Hq
	 * @return
	 */
	private double getK_LDistance(double[] Hp, double[] Hq) {
		double dis = 0.0;
		double lg = 0.0;
		double pD = 0.0;
		for (int i = 0; i < Hq.length; i++) {
			if (Hq[i] == 0) {
				// dis += 20000;
				continue;
			}
			if ((pD = Hp[i] / Hq[i]) == 0) {
				// dis = 0;
				continue;
			}
			lg = Math.log10(pD);
			dis += Hp[i] * lg;
		}

		return dis;
	}

	/**
	 * ����JEFFREY��
	 * 
	 * @param Hp
	 * @param Hq
	 * @return
	 */
	private double getJeffreyDistance(double[] Hp, double[] Hq) {
		double dis = 0.0;
		double mean = 0.0;
		double lg1 = 0.0;
		double lg2 = 0.0;
		for (int i = 0; i < Hq.length; i++) {
			mean = (Hp[i] + Hq[i]) / 2;
			if (mean == 0) {
				// dis += 20000;
				continue;
			}
			if ((lg1 = Hp[i] / mean) == 0 || (lg2 = Hq[i] / mean) == 0) {
				continue;
			}
			lg1 = Math.log10(lg1);
			lg2 = Math.log10(lg2);
			dis += Hp[i] * lg1 + Hq[i] * lg2;
		}

		return dis;
	}

	/**
	 * �������Ҿ�
	 * 
	 * @param Hp
	 * @param Hq
	 * @return
	 */
	private double getCosineDistance(double[] Hp, double[] Hq) {
		double dis = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		double mult = 0.0;
		for (int i = 0; i < Hp.length; i++) {// ���Ż�
			sum1 += Hp[i];
		}
		for (int i = 0; i < Hq.length; i++) {
			sum2 += Hq[i];
		}
		for (int i = 0; i < Hq.length; i++) {
			mult += (double) Hp[i] * Hq[i];
		}
		dis = 1 - mult / (sum2 * sum1);
		return dis;
	}

	/**
	 * ͼ��ƽ����ʹ�ø�˹ģ�巨
	 * 
	 * @param pixels
	 *            Դͼ������
	 * @param width
	 * @param height
	 * @return ����ͼ��ƽ��������
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
	 * ���RGB�ռ���ɫe��Ⱦ���M
	 * 
	 * @throws IOException
	 */
	private static void getM() throws IOException {

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
		System.out.println("�~���M��ʱ��" + (starttime - endtime) + "millis");
	}

	/**
	 * ����Ҷ�ֱ��ͼ
	 * 
	 * @param pixelsSource
	 * @return
	 */
	private double[] getGrayHistogram(int[] pixelsSource) {

		double[] grayHisto = new double[256];
		int r, g, b, gray;

		for (int i = 0; i < pixelsSource.length; i++) {
			// int alpha = pixelsSource[i]>>24;
			r = (pixelsSource[i] >> 16) & 0xff;
			g = (pixelsSource[i] >> 8) & 0xff;
			b = (pixelsSource[i]) & 0xff;

			gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);

			grayHisto[gray]++;

		}
		return grayHisto;
	}

	/**
	 * ����ռ���ɫֱ��ͼ
	 * 
	 * @param pixelsSource
	 * @param width
	 * @param height
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private float[][] getSpatialHistogram(int[] pixelsSource, int width,
			int height) {

		List[] Ak = new ArrayList[256];
		for (int i = 0; i < Ak.length; i++) {
			Ak[i] = new ArrayList();
		}
		int r, g, b, gray;
		int x, y;

		for (int i = 0; i < pixelsSource.length; i++) {
			r = (pixelsSource[i] >> 16) & 0xff;
			g = (pixelsSource[i] >> 8) & 0xff;
			b = (pixelsSource[i]) & 0xff;

			x = i / width;
			y = i % width;
			gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);

			Ak[gray].add(x);
			Ak[gray].add(y);

		}

		float[][] Bi = new float[3][256];
		for (int i = 0; i < Bi[0].length; i++) {// ʹ��|Ak|ΪAk�����ص���
			if (Ak[i].size() == 0) {
				continue;
			}
			float sumX = 0, sumY = 0;
			for (int j = 0; j < Ak[i].size(); j++) {
				x = (Integer) Ak[i].get(j);
				y = (Integer) Ak[i].get(++j);
				sumX += x;
				sumY += y;
			}
			Bi[0][i] = 1.0f / (width * Ak[i].size()) * sumX;
			Bi[1][i] = 1.0f / (height * Ak[i].size()) * sumY;
		}

		for (int i = 0; i < Bi[0].length; i++) {
			if (Ak[i].size() == 0) {
				continue;
			}
			double sumEuc = 0;
			for (int j = 0; j < Ak[i].size(); j++) {
				x = (Integer) Ak[i].get(j);
				y = (Integer) Ak[i].get(++j);
				sumEuc += (Bi[0][i] - x) * (Bi[0][i] - x) + (Bi[1][i] - y)
						* (Bi[1][i] - y);
			}
			Bi[2][i] = (float) Math.sqrt(sumEuc / Ak[i].size());
		}
		return Bi;
	}

	/**
	 * ����Ҷȹ������
	 * 
	 * @param pixels
	 *            ͼ������
	 * @param width
	 * @param height
	 * @return
	 */
	private double[][][] getCoMGrey(int[] pixels, int width, int height) {

		double[][][] greyCoMP = new double[4][256][256];
		int r, g, b, gray;

		// �Ȱ�ͼ��ҶȻ�����
		for (int i = 0; i < pixels.length; i++) {
			r = (pixels[i] >> 16) & 0xff;
			g = (pixels[i] >> 8) & 0xff;
			b = (pixels[i]) & 0xff;

			gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);

			pixels[i] = gray;

		}

		// ��Ҷȹ������d=3,�ĸ��
		int gray1 = 0;
		int gray2 = 0;
		int d = 1; // ����ѡ�����Ϊ3
		for (int i = d; i < height - d; i++) {
			for (int j = d; j < width - d; j++) {

				// 0�ȷ����
				gray1 = pixels[i * width + j];
				gray2 = pixels[i * width + j + d];
				greyCoMP[0][gray1][gray2]++;
				gray2 = pixels[i * width + j - d];
				greyCoMP[0][gray1][gray2]++;

				// 45��
				gray1 = pixels[i * width + j];
				gray2 = pixels[(i - d) * width + j + d];
				greyCoMP[1][gray1][gray2]++;
				gray2 = pixels[(i + d) * width + j - d];
				greyCoMP[1][gray1][gray2]++;

				// 90�ȷ���
				gray1 = pixels[i * width + j];
				gray2 = pixels[(i + d) * width + j];// ����
				greyCoMP[2][gray1][gray2]++;
				gray2 = pixels[(i - d) * width + j];// ����
				greyCoMP[2][gray1][gray2]++;

				// 135��
				gray1 = pixels[i * width + j];
				gray2 = pixels[(i - d) * width + j - d];// ����
				greyCoMP[3][gray1][gray2]++;
				gray2 = pixels[(i + d) * width + j + d];// ����
				greyCoMP[3][gray1][gray2]++;

			}
		}

		return greyCoMP;
	}

	/**
	 * �����һ��
	 * 
	 * @param greyCoMP
	 * @param width
	 * @param height
	 * @return
	 */
	private double[][][] MatrixUnitary(double[][][] greyCoMP, int width,
			int height) {

		/*
		 * double[] sum = new double[4];
		 * 
		 * for (int i = 0; i < greyCoMP.length; i++) { for (int j = 0; j <
		 * greyCoMP[i].length; j++) { for (int k = 0; k < greyCoMP[i][j].length;
		 * k++) { sum[i] += greyCoMP[i][j][k];//���ľ���ĺ� } } }
		 */
		int degree45 = 2 * (height - 2) * (width - 2);
		// for (int i = 0; i < greyCoMP.length; i++) {
		for (int j = 0; j < greyCoMP[0].length; j++) {
			for (int k = 0; k < greyCoMP[0][j].length; k++) {
				greyCoMP[0][j][k] /= degree45;// ��һ��
				greyCoMP[1][j][k] /= degree45;
				greyCoMP[2][j][k] /= degree45;
				greyCoMP[3][j][k] /= degree45;
			}
		}
		// }

		return greyCoMP;
	}

	/**
	 * ����Ҷȹ����������
	 * 
	 * @param greyCoMP
	 * @return
	 */
	private double[] getFeature(double[][][] greyCoMP) {

		// ���Ƕ��׾أ�����
		double moment1 = 0;
		double moment2 = 0;
		double moment3 = 0;
		double moment4 = 0;
		double mean1 = 0, mean2 = 0, mean3 = 0, mean4 = 0;
		double[] PxPulsy1 = new double[512];
		double[] PxPulsy2 = new double[512];
		double[] PxPulsy3 = new double[512];
		double[] PxPulsy4 = new double[512];
		// ���Աȶ�(g���Ծ�)
		double contrast1 = 0;
		double contrast2 = 0;
		double contrast3 = 0;
		double contrast4 = 0;
		// �����أ���
		double correlation1 = 0;
		double correlation2 = 0;
		double correlation3 = 0;
		double correlation4 = 0;
		double[] ux = new double[4];
		double[] uy = new double[4];
		double[][] uyp = new double[4][256];
		// ����
		double squares1 = 0;
		double squares2 = 0;
		double squares3 = 0;
		double squares4 = 0;

		// �������
		double inverseDifMoment1 = 0;
		double inverseDifMoment2 = 0;
		double inverseDifMoment3 = 0;
		double inverseDifMoment4 = 0;
		// ���ƽ���
		double averageSum1 = 0;
		double averageSum2 = 0;
		double averageSum3 = 0;
		double averageSum4 = 0;
		// �����
		double squaresSum1 = 0;
		double squaresSum2 = 0;
		double squaresSum3 = 0;
		double squaresSum4 = 0;
		// �����
		double log2 = Math.log(2);
		double entropy1 = 0;
		double entropy2 = 0;
		double entropy3 = 0;
		double entropy4 = 0;
		for (int i = 0; i < greyCoMP[0].length; i++) {
			double[] sumj = new double[4];
			for (int j = 0; j < greyCoMP[0][i].length; j++) {
				// &&�Ƕ��׾أ�����d������һ��Ӱ�죬�Ƕȶ���Ҳ��һ��Ӱ��p(i, j)^2
				moment1 += greyCoMP[0][i][j] * greyCoMP[0][i][j];
				moment2 += greyCoMP[1][i][j] * greyCoMP[1][i][j];
				moment3 += greyCoMP[2][i][j] * greyCoMP[2][i][j];
				moment4 += greyCoMP[3][i][j] * greyCoMP[3][i][j];

				// ����е�ux��uy
				sumj[0] += greyCoMP[0][i][j];
				sumj[1] += greyCoMP[1][i][j];
				sumj[2] += greyCoMP[2][i][j];
				sumj[3] += greyCoMP[3][i][j];
				uyp[0][j] += greyCoMP[0][i][j];
				uyp[1][j] += greyCoMP[1][i][j];
				uyp[2][j] += greyCoMP[2][i][j];
				uyp[3][j] += greyCoMP[3][i][j];

				// �Աȶȵ�P��i,j��*|i-j|^2
				int tt = (i - j) * (i - j);
				contrast1 += tt * greyCoMP[0][i][j];
				contrast2 += tt * greyCoMP[1][i][j];
				contrast3 += tt * greyCoMP[2][i][j];
				contrast4 += tt * greyCoMP[3][i][j];

				// ����
				tt = 1 / (1 + tt);
				inverseDifMoment1 += tt * greyCoMP[0][i][j];
				inverseDifMoment2 += tt * greyCoMP[0][i][j];
				inverseDifMoment3 += tt * greyCoMP[0][i][j];
				inverseDifMoment4 += tt * greyCoMP[0][i][j];

				// �� (:ע��log����Ϊ0��
				if (greyCoMP[0][i][j] != 0) {
					entropy1 -= greyCoMP[0][i][j] * Math.log(greyCoMP[0][i][j])
							/ log2;
				}
				if (greyCoMP[1][i][j] != 0) {
					entropy2 -= greyCoMP[1][i][j] * Math.log(greyCoMP[1][i][j])
							/ log2;
				}
				if (greyCoMP[2][i][j] != 0) {
					entropy3 -= greyCoMP[2][i][j] * Math.log(greyCoMP[2][i][j])
							/ log2;
				}
				if (greyCoMP[3][i][j] != 0) {
					entropy4 -= greyCoMP[3][i][j] * Math.log(greyCoMP[3][i][j])
							/ log2;
				}

				// ��ֵ
				mean1 += greyCoMP[0][i][j];
				mean2 += greyCoMP[1][i][j];
				mean3 += greyCoMP[2][i][j];
				mean4 += greyCoMP[3][i][j];

				// ��Pk
				PxPulsy1[i + j] += greyCoMP[0][i][j];
				PxPulsy2[i + j] += greyCoMP[1][i][j];
				PxPulsy3[i + j] += greyCoMP[2][i][j];
				PxPulsy4[i + j] += greyCoMP[3][i][j];
			}
			ux[0] += i * sumj[0];
			ux[1] += i * sumj[1];
			ux[2] += i * sumj[2];
			ux[3] += i * sumj[3];
		}

		// ����uy
		for (int i = 0; i < uyp[0].length; i++) {
			uy[0] += i * uyp[0][i];
			uy[1] += i * uyp[1][i];
			uy[2] += i * uyp[2][i];
			uy[3] += i * uyp[3][i];
		}

		// ����Qx��Qy
		// ��256��254�ε�ѭ��ʱ�任��4��256�Ŀռ����uxʱ��
		double[] Qx = new double[4];
		double[] Qy = new double[4];
		double[][] Qyp = new double[4][256];
		for (int i = 0; i < greyCoMP[0].length; i++) {
			double[] sumj = new double[4];
			for (int j = 0; j < greyCoMP[0][i].length; j++) {
				sumj[0] += greyCoMP[0][i][j];
				sumj[1] += greyCoMP[1][i][j];
				sumj[2] += greyCoMP[2][i][j];
				sumj[3] += greyCoMP[3][i][j];
			}
			Qx[0] += (i - ux[0]) * (i - ux[0]) * sumj[0];
			Qx[1] += (i - ux[1]) * (i - ux[1]) * sumj[1];
			Qx[2] += (i - ux[2]) * (i - ux[2]) * sumj[2];
			Qx[3] += (i - ux[3]) * (i - ux[3]) * sumj[3];
			// ���ڵ�һ��ѭ���Ѿ�������j�еĺ�uyp[]��,���Կ���ֱ�Ӽ���
			Qy[0] += (i - uy[0]) * (i - uy[0]) * uyp[0][i];
			Qy[1] += (i - uy[1]) * (i - uy[1]) * uyp[1][i];
			Qy[2] += (i - uy[2]) * (i - uy[2]) * uyp[2][i];
			Qy[3] += (i - uy[3]) * (i - uy[3]) * uyp[3][i];
		}

		// �����ʽ�������
		ux[0] = ux[0] * uy[0];
		ux[1] = ux[0] * uy[1];
		ux[2] = ux[0] * uy[2];
		ux[3] = ux[0] * uy[3];
		Qx[0] = Math.sqrt(Qx[0]) * Math.sqrt(Qy[0]);
		Qx[1] = Math.sqrt(Qx[1]) * Math.sqrt(Qy[1]);
		Qx[2] = Math.sqrt(Qx[2]) * Math.sqrt(Qy[2]);
		Qx[3] = Math.sqrt(Qx[3]) * Math.sqrt(Qy[3]);
		for (int i = 0; i < greyCoMP[0].length; i++) {
			for (int j = 0; j < greyCoMP[0][i].length; j++) {
				correlation1 += (i * j * greyCoMP[0][i][j] - ux[0]) / Qx[0];
				correlation2 += (i * j * greyCoMP[1][i][j] - ux[1]) / Qx[1];
				correlation3 += (i * j * greyCoMP[2][i][j] - ux[2]) / Qx[2];
				correlation4 += (i * j * greyCoMP[3][i][j] - ux[3]) / Qx[3];
			}
		}

		// ���ֵ
		mean1 /= 65536;// (256*256);
		mean2 /= 65536;
		mean3 /= 65536;
		mean4 /= 65536;

		// �����������d�ͽǶ�Ӱ��
		for (int i = 0; i < greyCoMP[0].length; i++) {
			for (int j = 0; j < greyCoMP[0][i].length; j++) {
				squares1 += (i - mean1) * (i - mean1) * greyCoMP[0][i][j];
				squares2 += (i - mean2) * (i - mean2) * greyCoMP[1][i][j];
				squares3 += (i - mean3) * (i - mean3) * greyCoMP[2][i][j];
				squares4 += (i - mean4) * (i - mean4) * greyCoMP[3][i][j];
			}
		}

		// ���ƽ��ͣ�����(����d�ͷ����Ӱ��)
		for (int i = 2; i < PxPulsy1.length; i++) {
			averageSum1 += i * PxPulsy1[i];
			averageSum2 += i * PxPulsy2[i];
			averageSum3 += i * PxPulsy3[i];
			averageSum4 += i * PxPulsy4[i];
		}

		// ����ͣ�������һ��Ӱ��
		for (int i = 2; i < PxPulsy1.length; i++) {
			squaresSum1 += (i - averageSum1) * (i - averageSum1) * PxPulsy1[i];
			squaresSum2 += (i - averageSum2) * (i - averageSum2) * PxPulsy2[i];
			squaresSum3 += (i - averageSum3) * (i - averageSum3) * PxPulsy3[i];
			squaresSum4 += (i - averageSum4) * (i - averageSum4) * PxPulsy4[i];
		}

		// �������ľ�ֵ��ʹ�ý��Է������
		double[] featureCoM = new double[8];
		featureCoM[0] = (moment1 + moment2 + moment3 + moment4) / 4;// ���׽Ǿ�
		featureCoM[1] = (contrast1 + contrast2 + contrast3 + contrast4) / 4;// �Աȶ�
		featureCoM[2] = (correlation1 + correlation2 + correlation3 + correlation4) / 4;// ���
		featureCoM[3] = (squares1 + squares2 + squares3 + squares4) / 4;// ����
		featureCoM[4] = (inverseDifMoment1 + inverseDifMoment2
				+ inverseDifMoment3 + inverseDifMoment4) / 4;// ����
		featureCoM[5] = (averageSum1 + averageSum2 + averageSum3 + averageSum4) / 4;// ƽ���
		featureCoM[6] = (squaresSum1 + squaresSum2 + squaresSum3 + squaresSum4) / 4;// �����
		featureCoM[7] = (entropy1 + entropy2 + entropy3 + entropy4) / 4;// ��

		return featureCoM;
	}

	/**
	 * ��ʹ�õ�һ����ɫ�㷨֮����ʹ�������㷨��һ������
	 * 
	 * @param result1
	 *            ��һ�β�ѯ�Ľ�����飬������ǰ50����ɫ�����ͼ��
	 * @param pr1
	 *            ��bҶ�任�Ļ�״������
	 * @param pg1
	 *            ��bҶ�任��Ш״������
	 * @return ���ض��μ���Ľ��
	 * @throws IOException
	 */
	public float[][] getSecondIndex1(float[][] result1, double[] pr1, int[] pg1)
			throws IOException {

		Blob blobQueriedPr = null;
		Blob blobQueriedPg = null;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		// List list=null ;
		// String hql = "from Feature ";
		// int amountReadOnetime = 30;

		double[] Pri = null;
		double[] Pgi = null;
		int indexResultSet = 0; // result�����б��������
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result1[0].length; i++) {

			int idResult1 = (int) result1[0][i];
			feature = new Feature();
			try {
				feature = (Feature) dao.findById("Feature",
						(Serializable) idResult1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //

			blobQueriedPr = feature.getFftPr();
			blobQueriedPg = feature.getFftPg();

			// �����л�
			try {
				ObjectInputStream in1 = new ObjectInputStream(
						blobQueriedPr.getBinaryStream());
				ObjectInputStream in2 = new ObjectInputStream(
						blobQueriedPg.getBinaryStream());

				// �õ���ݿ����
				Pri = (double[]) in1.readObject();
				Pgi = (double[]) in2.readObject();

				in1.close();
				in2.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			// ����FFT���룻
			float distance = 0;
			double tt = 0;
			double sumPr = 0;
			double sumPg = 0;
			for (int j = 0; j < Pri.length; j++) {
				tt = pr1[j] - Pri[j];
				sumPr += tt * tt;
			}
			for (int j = 0; j < Pgi.length; j++) {
				tt = pg1[j] - Pgi[j];
				sumPg += tt * tt;
			}
			// Ȩ��w1=w2=1
			distance = (float) Math.sqrt(sumPr) + (float) Math.sqrt(sumPg);

			if (indexResultSet < AMOUNT_RESULT) {
				result[1][indexResultSet] = distance;
				result[0][indexResultSet] = idResult1;
				// System.out.println("�����ӡ�� "+indexResultSet);

			} else {// ��������
				float maxDis = 0.0f;
				int posMaxDis = 0;
				for (int k = 0; k < result[1].length; k++) {
					if (result[1][k] > maxDis) {
						maxDis = result[1][k];
						posMaxDis = k;
					}
				}
				if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
					result[0][posMaxDis] = idResult1;
					result[1][posMaxDis] = distance;
				}
			}
			indexResultSet++;
		}
		dao.clear();

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;

	}

	/**
	 * ������ʹ����������������ʹ����ɫ�㷨����μ���
	 * 
	 * @param result1
	 *            �����������ĵ���ǰ50�����ͼ��Ľ������
	 * @param H
	 *            ֱ��ͼ
	 * @return ���ض��μ���Ľ��
	 * @throws IOException
	 */
	public float[][] getSecondIndex2(float[][] result1, int[] H)
			throws IOException {

		Blob blobQueried = null;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		int id = 0;
		ArrayList<Float> arrayListF = null;
		int indexResultSet = 0;
		float[][] result = new float[2][AMOUNT_RESULT];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}
		int sumI = 0;
		for (int i = 0; i < H.length; i++) {
			sumI += H[i];
		}
		float[] F = new float[M[0].length];
		float sum = 0;
		for (int i = 0; i < M[0].length; i++) {
			for (int j = 0; j < M.length; j++) {
				sum += (float) H[j] * M[j][i];
			}
			F[i] = sum;
		}

		// int indexTable = 0;

		// �ֱ����
		for (int i = 0; i < result1[0].length; i++) {

			int idResult1 = (int) result1[0][i];
			feature = new Feature();
			try {
				feature = (Feature) dao.findById("Feature",
						(Serializable) idResult1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //

			blobQueried = feature.getFuzzyHistogram();

			id = feature.getId();
			try {
				ObjectInputStream in = new ObjectInputStream(
						blobQueried.getBinaryStream());
				arrayListF = (ArrayList<Float>) in.readObject();
				in.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			float distance = 0.0f;
			float tt;
			for (int j = 0; j < arrayListF.size(); j++) {
				tt = (float) (arrayListF.get(j) - F[j]);
				distance += tt * tt;
			}

			if (indexResultSet < AMOUNT_RESULT) {
				result[1][indexResultSet] = distance;
				result[0][indexResultSet] = id;
				// System.out.println("�����ӡ�� "+indexResultSet);

			} else {// ��������
				float maxDis = 0.0f;
				int posMaxDis = 0;
				for (int k = 0; k < result[1].length; k++) {
					if (result[1][k] > maxDis) {
						maxDis = result[1][k];
						posMaxDis = k;
					}
				}
				if (distance < result[1][posMaxDis]) {// �����������ȵ�ǰ������򽻻�
					result[0][posMaxDis] = id;
					result[1][posMaxDis] = distance;
				}
			}

			indexResultSet++;
		}
		dao.clear();

		// ����
		float stmp;
		for (int i = 1; i < result[0].length; i++) {
			for (int j = 0; j < i; j++) {
				if (result[1][i] < result[1][j]) {
					stmp = result[1][i];
					result[1][i] = result[1][j];
					result[1][j] = stmp;

					stmp = result[0][i];
					result[0][i] = result[0][j];
					result[0][j] = stmp;
				}
			}
		}

		return result;

	}

}
