package com.tnove.algorthms;

import java.awt.image.PixelGrabber;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tnove.dao.*;

//import org.hibernate.Hibernate;


/**
 * @author ruibo
 * 
 */
public class CalculateFeatureSpatial extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public CalculateFeatureSpatial() {
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
	 * @throws
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");

		Blob blobQueried = null;
		Blob blobSave1 = null;
		Image pic = new Image();
		BaseDao dao = new BaseDao();
		List list = null;
		String hql = "from Image ";

		int id;
		int indexTable = 0;
		int amountQuery = 30;

		long startTime = System.currentTimeMillis();

		while (amountQuery == 30) {
			// TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index <
			// amountQuery)
			try {
				// list.clear();
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
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

				java.awt.Image image = null;
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

				int[] pixelsSource = new int[width * height];
				PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0,
						width, height, pixelsSource, 0, width);

				try {
					pixelGrabber.grabPixels();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				float[][] spatialHisto = getSpatialHistogram(pixelsSource,
						width, height);

				// ���л�
				try {
					ByteArrayOutputStream out1 = new ByteArrayOutputStream();

					ObjectOutputStream outputStream1 = new ObjectOutputStream(
							out1);

					outputStream1.writeObject(spatialHisto);
					byte[] bytes1 = out1.toByteArray();

					// blobSave1 = Hibernate.createBlob(bytes1);
					out1.close();
					outputStream1.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.toString();

				}

				// /��������ݿ⣻
				Feature feature = null;// new Feature();
				// feature.setId(id);
				try {
					feature = (Feature) dao.findById("Feature", id);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				feature.setSpatialHistogram(blobSave1);
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
		System.out.println("��ʱ��" + (endtime - startTime) / 1000 + "s");

		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * ����ռ���ɫֱ��ͼ
	 * 
	 * @param pixelsSource
	 *            ͼ������
	 * @param width
	 * @param height
	 * @return ���ؿռ���ɫֱ��ͼ
	 */
	@SuppressWarnings("unchecked")
	private float[][] getSpatialHistogram(int[] pixelsSource, int width,
			int height) {

		List[] Ak = new ArrayList[256];
		for (int i = 0; i < Ak.length; i++) {
			Ak[i] = new ArrayList();
		}
		int r, g, b, gray;
		int x = 0, y = 0;

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
			float sumX = 0.0f, sumY = 0.0f;
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
			double sumEuc = 0;
			if (Ak[i].size() == 0) {
				continue;
			}
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
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
