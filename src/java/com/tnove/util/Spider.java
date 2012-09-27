package com.tnove.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ����֩���Sverletģ�飬ͨ�����Crawer����
 * @author ruibo
 *
 */
public class Spider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String START_URL_FILE = "G:/temp/startURL.log";
	private static final String TO_CRAWL_URL_FILE = "G:/temp/toCrawlURL.log";
	private static final boolean IS_CRAWL_FROM_STARTURL = true;
	private static final boolean IS_LIMIT_HOST = true;
	
	/**
	 * Constructor of the object.
	 */
	public Spider() {
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
		doPost(request, response);
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
		
		response.setContentType("text/html;charset=GBK");
	//	response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out
				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>Spider startup servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.println("  <BR><BR><BR><hr><BR><BR><center>");
	    try {
	    	
			Container container = new Container(START_URL_FILE,TO_CRAWL_URL_FILE, IS_CRAWL_FROM_STARTURL, IS_LIMIT_HOST);
			Crawer crawer = new Crawer(container);
			SaveImage saveImage = new SaveImage(container);
			crawer.start();
			Thread.sleep(100);
			saveImage.start();
		
			out.print("�����3��Ѿ�����ѰͼƬ����b��ݿ⣡");
			 // Close matches log file.				
		    } catch (Exception e) {
			// TODO: handle exception
			e.toString();
			out.println("Spider��?<BR>");
		}
		out.println("  <BR>");
		response.encodeRedirectURL("Yo/index.jsp");
		out.println("<a href='javascript:history.back();'>����</a></center>");
//		out.println("��ݿ⽨b��ϡ�<BR><BR>");
		out.println("  <hr></BODY>");
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
