package com.tnove.algorthms;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ����e��Ⱦ���
 * 
 * @author ruibo
 *
 */
public class CalculateM extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Constructor of the object.
	 */
	public CalculateM() {
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
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=gb2312");
		//PrintWriter out = response.getWriter();
		ServletOutputStream out = response.getOutputStream();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
	//	out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=gb2312");
		//PrintWriter out = response.getWriter();
		ServletOutputStream out = response.getOutputStream();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ����");
		 		
		float[][] M = null; //�洢����
		/**
		 * ���ļ���M
		 */
/*		long starttime = System.currentTimeMillis();
 		M = new float[4096][120];
 		RandomAccessFile randomAccessFile = null;
 		randomAccessFile = new RandomAccessFile("G:/temp/M.log", "r");
 		if(randomAccessFile.length()>0){
			String element= null;
 			for (int i = 0; i < M.length; i++) {
				for (int  j = 0; j < M[0].length; j++) {
								
						element = randomAccessFile.readLine();
				  	   //System.out.println("ԭֵ��"+emul);
						if(element != null)
					    M[i][j] = Float.parseFloat(element);
					//    System.out.println("M["+i+"]["+j+"] = "+M[i][j]);

				}
				element = randomAccessFile.readLine();
			}	 			
 			}//end of if
 		randomAccessFile.close();
 		long endtime = System.currentTimeMillis();
 		System.out.println("�~���M��ʱ��"+(starttime-endtime)+"millis");
*/ 		
 		/**
 		 * ���¼���M
 		 */
		long startL = System.currentTimeMillis();
	 	if(M == null){
 		FuzzyCMeansClustering fuzzyCMeansClustering = new FuzzyCMeansClustering(16, 120, 1000, 1.9, 0.005);
 		fuzzyCMeansClustering.FCMClustring();
 		M = fuzzyCMeansClustering.getMembership();
 		PrintWriter MWriter = new PrintWriter(new FileWriter("G:/temp/M.log"));
 		for (int i = 0; i < M.length; i++) {
 			for (int j = 0; j < M[0].length; j++) {
 				MWriter.println(M[i][j]+" ");
 			}
 			MWriter.println();
 		}
 		MWriter.close();
 		}
	 	System.out.println("��ǰ��ʱΪ��"+(System.currentTimeMillis()-startL)/60000+" ����");
		
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}


	 		
	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occure
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
