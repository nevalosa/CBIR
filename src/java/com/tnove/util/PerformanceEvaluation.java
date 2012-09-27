package com.tnove.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tnove.algorthms.*;
import com.tnove.dao.*;


/**
 * x�����9�ģ�飬�����openͼ���϶�����ʵ�ֵ��㷨���бȽϷ���
 * 
 * @author ruibo
 *
 */
public class PerformanceEvaluation extends HttpServlet {

	private static ArrayList filePathList = new ArrayList();

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public PerformanceEvaluation() {
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

		response.setContentType("text/html;charset=gb2312");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ���ܼ���<BR>");

		refreshFileList("G:/temp/TestImageSet/testset");//����ͼƬ��Ŀ¼��������в���ͼƬ��·��
		String method = request.getParameter("method");
		String feat = method.substring( 2);
		method = method.substring(0, 1);
		int feature = Integer.parseInt(feat);
		feature++;

		//�������ļ�
		String featureFlie = null;
		File   f   =   new   File("G:/temp/TestImageSet/testset/annotation.txt");   
        if(f.exists()){ 
        BufferedReader reader =new BufferedReader(
 				new InputStreamReader( 
 						new FileInputStream("G:/temp/TestImageSet/testset/annotation.txt")));
 		//StringBuffer pageBuffer = new StringBuffer();
 		String line = null;
 		StringBuffer pageBuffer = new StringBuffer();
 		while ((line = reader.readLine()) != null) {
 	        pageBuffer.append(line+"\n");
 	      }
 		featureFlie = pageBuffer.toString();
 		reader.close();
        }else {
			featureFlie = null;
		}
        
 		//ͳ������
		CalculateDistance calculateDistance = new CalculateDistance();
		 BaseDao dao = new BaseDao();
		 String path = null;	    
		 Image  image = null;//POJO
		 int rankSumAll = 0;
		 int sumImage = 0;
		 int sumRankImage = 0; 
		 int sumRelateImage = 0;
			
		 for (int i = 0; i < filePathList.size(); i++) {//filelist.size()
			 
			 path = filePathList.get(i).toString();
			 //ֻ����JPG
				if(!(path.endsWith(".jpg") || path.endsWith(".gif") || path.endsWith(".bmp"))) {
					continue;
				}	 	
//			�ڶ������ѡ������1���Ҷ�ֱ��ͼ��Ϣ�أ�2���Ҷȡ��ۻ�ֱ��ͼ��3��ģ����ɫֱ��ͼ 4���ռ���ɫֱ��ͼ 5-�Ҷȹ������
	         //�������ֻ�ڵڶ�����Ϊ2ʱ��Ч������������⣨����null����
	         //�������ѡ������������1-���� 2-ŷʽ 3-���� 4-K-L 5-Jeffrey 6-���� 7-����
//				���Ĳ���ֵ���ڶ�����Ϊ2����Ч��ʹ�õĻҶȹ���������֤���ͣ�
		         //1���Ƕ��׾� 2���Աȶ� 3����� 4������ 5������ 6����ƽ�� 7���ͷ��� 8����
             float[] result = calculateDistance.getIndex(path, feature, 1, 2); //�õ�ͼ������
		    sumImage++;  
				 
		    String repath = path.substring(path.lastIndexOf("\\testset\\")+9);
			// System.out.println("���·���� "+ repath);
			//  String ttString = pageBuffer.toString();
			 	//	  System.out.println("endstring: "+endString+" :filename: "+fileName);
			// System.out.println("featureFile���ȣ� "+featureFlie.length());

			 
			 //����ʽת�����������ļ�һ�¡�			
			String flagString = repath.replace('\\', '/');
		//	System.out.println(fString);
			flagString = flagString.substring(0, repath.indexOf("."));//��ѯͼ����ļ����޺�׺
			String pathBlong = flagString.substring(0, flagString.indexOf("/")); 
		
			int startIndex;
			String[] takens = null;
			if (featureFlie != null) {
			startIndex = featureFlie.indexOf(flagString);
			int endIndex = featureFlie.indexOf("\n", startIndex);
			 		//  System.out.println("aj: "+startIndex+" :aj2: "+endIndex);
			String keyWord = featureFlie.substring(startIndex, endIndex);//���ؼ���
			takens = keyWord.split(" ");
				//	System.out.println("����"+keyWord);
			}

			//out.println("����ʹ�õ�����Ϊ: "+FEATRUE+" �����Ϊ��"+DISTANCEMETHOD);
			out.println("<BR>��ͼ��"+path+"��ѯ������£�");
			out.println("<HR>");
			out.println("�����  <pre style='display:inline'>     </pre> rank   <pre style='display:inline'>     </pre>     �ļ��� <BR>");
			boolean flag = false;
			int sumOne = 0; //���ͼ����
			int rankSumOne = 0;
			for (int j = 0; j < result.length; j++) {//30����
		    	String queryKey = null;
				try {
					image = (Image)dao.findById("Image", (int)result[j]);
					queryKey = image.getKeyword();
					flag = false; //�Ƿ���صı�־
					int numKey = 0;
					if(method.equals("0")){
						for(int k = 1; k <takens.length; k++){
							if (queryKey.contains(takens[k])) {
								numKey++;
								//flag = false;
								//break;
							}
						}
					}
				    
				    if (method.equals("0")) {//ʹ�ñ�ע������
				    	if ((takens.length > 7 && numKey > ((takens.length >> 1) + 1))
	 							|| (takens.length < 8) && numKey > ((takens.length >> 1) - 1)) {
							flag = true;
						}					
				    }else {
						if (method.equals("1")) {//���ñ�ע������
							String imageNamei = image.getImageName();
							if (imageNamei.contains(pathBlong)) {
								flag = true;
							}
						}
					}
 					if (flag) {//result[j]��� i ���ѯͼ�����
 					//	out.println("&nbsp;"+result[j]+"<pre style='display:inline'>      </pre>"+j+"<pre style='display:inline'>        </pre>"+image.getImageName()+"<BR>");
 						sumOne++;
 						rankSumOne += j;
					}
					//out.println(", using the POST method");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			sumRelateImage +=sumOne;//��¼��������ͼ����
		    out.println("<BR>����ͼ������"+sumOne+"<BR>");
		    out.println("��ȷ�ȣ�"+ (float)sumOne/30);
		    if (sumOne != 0) {
		    	rankSumOne /= sumOne;
		    	sumRankImage++;
		    	 out.println("ƽ������"+ rankSumOne+"<BR><HR>");
			}
		    rankSumAll += rankSumOne; 
			    
			 } 
		 //���filepathlist����
		 filePathList.clear();
		out.println("<BR><HR><BR>��ѯͼ�������Ϊ��"+ sumImage+"<BR>����ͼƬRankSum��ƽ��Ϊ��" + (float)rankSumAll/sumRankImage);
		out.println("<BR>ƽ��ÿ��ͼ�������ͼ����"+ ((float)sumRelateImage/sumRankImage));
		out.println("<BR>�����ƽ���ͼ����"+ (sumImage - sumRankImage));
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	@SuppressWarnings("unchecked")
	public  void refreshFileList(String strPath) { 
        File dir = new File(strPath); 
        File[] files = dir.listFiles(); 
        
        if (files == null) 
            return; 
        for (int i = 0; i < files.length; i++) { 
            if (files[i].isDirectory()) { 
                refreshFileList(files[i].getAbsolutePath()); 
            } else { 
                @SuppressWarnings("unused")
				String strFileName = files[i].getAbsolutePath().toLowerCase();
             //   System.out.println("---"+strFileName);
                filePathList.add(files[i].getAbsolutePath());                    
            } 
        } 
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
