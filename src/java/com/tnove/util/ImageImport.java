package com.tnove.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Blob;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;

import com.tnove.dao.*;


public class ImageImport extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static ArrayList filelist = new ArrayList(); 
	//private String path = null;
//	private String fileName = null;
	private StringBuffer pageBuffer = null;
	String featureFlie ;	
	int startIndex;
	int endIndex;
	/**
	 * Constructor of the object.
	 */
	public ImageImport() {
		super();
		pageBuffer = new StringBuffer();
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

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out
				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
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
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
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
		long a = System.currentTimeMillis();
        refreshFileList("G:/temp/TestImageSet/set");
        
        File   f   =   new   File("G:/temp/TestImageSet/set/annotation.txt");   
        if(f.exists()){ 
        BufferedReader reader =new BufferedReader(
 				new InputStreamReader( 
 						new FileInputStream("G:/temp/TestImageSet/set/annotation.txt")));
 		//StringBuffer pageBuffer = new StringBuffer();
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 	        pageBuffer.append(line+"\n");
 	      }
 		featureFlie = pageBuffer.toString();
 		reader.close();
        }else {
			featureFlie = null;
		}
 		
 		importImage(filelist);
 	//	System.out.println(pageBuffer.toString());
 		

        System.out.println("��ʱ��"+(System.currentTimeMillis() - a)/1000);
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * ��ø�·���������ļ����ļ�·������(���ļ������ļ�
	 * 
	 * @param strPath
	 */
	@SuppressWarnings({ "unchecked", "unchecked" })
	public  void refreshFileList(String strPath) { 
        File dir = new File(strPath); 
        File[] files = dir.listFiles(); 
        
        if (files == null) 
            return; 
        for (int i = 0; i < files.length; i++) { 
            if (files[i].isDirectory()) { 
                refreshFileList(files[i].getAbsolutePath()); 
            } else { 
       //         String strFileName = files[i].getAbsolutePath().toLowerCase();
             //   System.out.println("---"+strFileName);
               filelist.add(files[i].getAbsolutePath());                    
            } 
        } 
    }

 public  void importImage(ArrayList filePathList) { 
	 /**
	  * ע�⣺�˴��������}��InputStream������Blob����java.awt.Image����Ҫһ�������ͬһ��
	  * ���ʹ��һ��InputStream�ȴ���Blob�ڽ�java.awt.Image���������ݿ��е�Blob�ֶν�Ϊ��
	  * 
	  */
	 BaseDao dao = new BaseDao();
	 String path = null;
	 Blob blob = null;	    
	 Image  image = null;
	 InputStream imageis = null;
	 InputStream imageInputStream = null;
	
	 for (int i = 0; i < filePathList.size(); i++) {//filelist.size()
     	
     	//���?��
     	if(i%30==0) //��ÿ30�������Ϊһ���?Ԫ 
     	{
     	dao.clear();
     	System.out.println("д����ݿ�ͼ����"+i);
     	}

     	path = filelist.get(i).toString();
     	//System.out.println("���·��: "+path);
			if(!(path.endsWith(".jpg") || path.endsWith(".gif")|| path.endsWith(".bmp"))) {
				continue;
			}
		try{		    
		    image = new Image();
		    imageis = new   FileInputStream(path); 
		    imageInputStream =  new   FileInputStream(path);
		    //TODO Nevalosa Deng
//		    blob = (Blob)Hibernate.createBlob(imageis);
	        if(blob == null){
	    	    System.err.println("blobΪ��");
	        }
	        image.setValue(blob);
		    java.awt.Image   src   =   javax.imageio.ImageIO.read(imageInputStream);   //����Image���� 
		  //BufferedImage bufferedImage = 
	        int   width   = src.getWidth(null);   //�õ�Դͼ��   
	        int   height  = src.getHeight(null);//768;//tag1.getHeight();   //�õ�Դͼ�� 

	        String repath = path.substring(path.lastIndexOf("\\set\\")+5);
			// System.out.println("���·���� "+ repath);
			//  String ttString = pageBuffer.toString();
			 	//	  System.out.println("endstring: "+endString+" :filename: "+fileName);
			// System.out.println("featureFile���ȣ� "+featureFlie.length());
			 image.setImageName(repath); 
			 
			 String keyWord = null;			 
			 if (featureFlie != null) {
				
			 //����ʽת�����������ļ�һ�¡�			
			String fString = repath.replace('\\', '/');
		//	System.out.println(fString);
			fString = fString.substring(0, repath.indexOf("."));
		//	System.out.println(fString);			
			//��õ�ǰͼƬ�ؼ��ֵĿ�ʼ�ͽ���λ��
			startIndex = featureFlie.indexOf(fString);
			endIndex = featureFlie.indexOf("\n", startIndex);
			 		//  System.out.println("aj: "+startIndex+" :aj2: "+endIndex);
			if (startIndex < 0 || endIndex < 0) {
				keyWord = repath;
			}else{
				keyWord = featureFlie.substring(startIndex, endIndex);
			}
			
		//	System.out.println("����"+keyWord);
			 }else {
				keyWord = repath;
			}

	       image.setHeight(height);
	       image.setWidth(width);	       		    
	       image.setKeyword(keyWord);

	       dao.add(image);
	       imageis.close();
	       imageInputStream.close(); 
	    }catch (Exception e) {
			// TODO: handle exception
	    	System.out.println(e.getMessage());
	    	
		}
	 }   

 }
}
