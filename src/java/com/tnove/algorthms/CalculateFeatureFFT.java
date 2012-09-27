package com.tnove.algorthms;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tnove.dao.BaseDao;
import com.tnove.dao.Feature;
import com.tnove.dao.Image;

//import org.hibernate.Hibernate;


/**
 * ��ø�bҶ�任��ص�����
 * 
 * @author ruibo
 * 
 */
public class CalculateFeatureFFT extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public CalculateFeatureFFT() {
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

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");

		Blob blobQueried = null;
		Blob blobSave1 = null;
		Blob blobSave2 = null;
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
				list = dao.findByHQL(hql, indexTable, 30); // һ��ȡ��30���¼
				amountQuery = list.size();
				indexTable += amountQuery;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int j = 0; j < list.size(); j++) { // list.size()
				pic = (Image) list.get(j);
				id = pic.getId();
				blobQueried = (Blob) pic.getValue();

				java.awt.Image image = null;
				if (blobQueried != null) {
					try {
						InputStream inputStream = new BufferedInputStream(
								blobQueried.getBinaryStream());
						image = javax.imageio.ImageIO.read(inputStream);
						inputStream.close();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}

				FFT myFft = new FFT(image);
				myFft.getOriginalPixels();
				myFft.getFFTPixels();
				double[] pr = myFft.getFFTPr();
				int[] pg = myFft.getFFTPg();

				try {
					ByteArrayOutputStream out1 = new ByteArrayOutputStream();
					ByteArrayOutputStream out2 = new ByteArrayOutputStream();
					ObjectOutputStream outputStream1 = new ObjectOutputStream(out1);
					ObjectOutputStream outputStream2 = new ObjectOutputStream(out2);
					outputStream1.writeObject(pr);
					outputStream2.writeObject(pg);
					byte[] bytes1 = out1.toByteArray();
					byte[] bytes2 = out2.toByteArray();
					// blobSave1 = Hibernate.createBlob(bytes1);
					// blobSave2 = Hibernate.createBlob(bytes2);
					out1.close();
					out2.close();
					outputStream1.close();
					outputStream2.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.toString();
				}

				Feature feature = null;// new Feature();
				// feature.setId(id);
				try {
					feature = (Feature) dao.findById("Feature", id);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				feature.setFftPr(blobSave1);
				feature.setFftPg(blobSave2);
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
		
		long endtime = System.currentTimeMillis();
		
		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
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
