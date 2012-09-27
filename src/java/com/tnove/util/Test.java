package com.tnove.util;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tnove.dao.*;

public class Test extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor of the object.
	 */
	public Test() {
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

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out
				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");

		File imageFile = new File("G:/temp/����4.gif");
		Image image= javax.imageio.ImageIO.read(imageFile);
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		int pixelsSource[]; 
		pixelsSource = new int[imageWidth * imageHeight];
		PixelGrabber  pixelGrabber = new PixelGrabber(image,0,0,imageWidth,imageHeight,pixelsSource,0,imageWidth);
		 
		try
		{
			pixelGrabber.grabPixels ();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		
	//	 getM();
		 
		int[] H = new int[4096];
		int r, g, b, index;
			for (int i = 0; i < pixelsSource.length; i++) //pixelsSource.length
			{
				int pixels =pixelsSource[i];
		    r = (pixels >> 20) & 0xf;
		    g = (pixels >> 12) & 0xf;
		    b = (pixels >> 4)  & 0xf;
//		      System.out.println("�ض���λr1g1b1��"+r+","+g+","+b);
		    index = (r<<8)+(g<<4)+b;
//		     System.out.println("����"+index);
		    H[index]++;
			}
			
		int[] grayHisto = getGrayHistogram(pixelsSource);
		float[][] Bi =  getSpatialHistogram(pixelsSource, imageWidth, imageHeight);
		float[][] result = CalculatDistanceSpa(grayHisto, imageWidth, imageHeight, Bi); 
	//	float[][] pp = CalculatDistanceH(grayHisto);
	//	float[][] result =  CalculatDistanceEntropy(H, imageHeight*imageWidth);
/*		double[][] pp = new double[3][3];
		pp[0][0] = 1.0;
		pp[0][1] = 2.0;
		pp[0][2] = -1.0;
		pp[1][0] = 3.0;
		pp[1][1] = 1.0;
		pp[1][2] = 0.0;
		pp[2][0] = -1.0;
		pp[2][1] = 0.0;
		pp[2][2] = -2.0;
		if (invertGaussJordan(pp)) {
			for (int i = 0; i < pp.length; i++) {
				for (int j = 0; j < pp.length; j++) {
					System.out.println("i:"+i+"j:"+j+"  /"+pp[i][j]);
				}
			}
		}
*/		
		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	private float[][] CalculatDistanceSpa(int[] H,int width, int height, float[][] Bi) {
		
		Blob blobQueriedSpatial = null ;
		Blob blobQueriedGrayH = null ;
		Feature feature = null;
		BaseDao dao = new BaseDao();
		List list=null ;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;
//		ArrayList<Float> arrayListF = null;
		float[][] spatialHisto = null;
		int[] grayHisto = null;
		int indexResultSet = 0;
		float[][] result = new float[2][30];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}
		

	int indexTable = 0;
	while(amountReadOnetime == 30 ){
		//TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index < amountQuery)
		try {
			list = dao.findByHQL(hql,446 , 5); //һ��ȡ��30���¼
			amountReadOnetime = list.size() ;
			indexTable += amountReadOnetime;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(list.size());
	///��ÿ��ͼ����F
		for(int j=0; j < list.size(); j++ ){ //list.size()
			   feature = (Feature)list.get(j);			
			   blobQueriedSpatial = feature.getSpatialHistogram();	
			   blobQueriedGrayH = feature.getGrayHistogram();
			
			id = feature.getId();
			
			int qWidth = feature.getWidth();
			int qHeight = feature.getHeight();
			
		//	�����л�
			try {
	            ObjectInputStream in1 = new ObjectInputStream(blobQueriedSpatial.getBinaryStream());
	            ObjectInputStream in2 = new ObjectInputStream(blobQueriedGrayH.getBinaryStream());
	            
	            //�õ���ݿ����
	            spatialHisto = (float[][])in1.readObject();
	            grayHisto = (int[])in2.readObject(); 
	             
	            in1.close(); 	
	            in2.close();
	        } catch (Exception e) {
	             // TODO: handle exception 
	            e.printStackTrace();	            
	        } 
        System.out.println("Id:"+id);
        
        //����ռ�ֱ��ͼ���룻
        float distance = 0.0f;
        double distanceEuc = 0.0;
        float div = 0.0f;
        double sqrt2 = Math.sqrt(2);
        int totlePixelsH = width*height;
        int totlePixelsQ = qWidth*qHeight;
        for (int i = 0; i < grayHisto.length; i++) {
        	
        	if ((Bi[2][i] == 0) && (spatialHisto[2][i] == 0)) {
				continue;
			}
        	distanceEuc = Math.sqrt((spatialHisto[0][i]-Bi[0][i])*(spatialHisto[0][i]-Bi[0][i]) + (spatialHisto[1][i]-Bi[1][i])*(spatialHisto[1][i]-Bi[1][i]));
        	if (spatialHisto[2][i] >= Bi[2][i]) {
				div = Bi[2][i] / spatialHisto[2][i];
			} else {
				div = spatialHisto[2][i] / Bi[2][i];
			}
        //	System.out.println(grayHisto[i] > H[i]?((double)H[i]/totlePixelsH):((double)grayHisto[i]/totlePixelsQ));
        //	System.out.println(((sqrt2 - distanceEuc)/sqrt2 + div));
        	distance +=(grayHisto[i] >= H[i]?((double)H[i]/totlePixelsH):((double)grayHisto[i]/totlePixelsQ)) * ((sqrt2 - distanceEuc)/sqrt2 + div);
		    System.out.println("dis"+distance);
        }
	
    	if ( indexResultSet < 30 ) {
			result[1][indexResultSet] =(float) distance;
			result[0][indexResultSet] = id;
			//System.out.println("�����ӡ�� "+indexResultSet);
			
		}else {//��������
			float maxDis = 0.0f;
			int posMaxDis = 0;
			for (int k = 0; k < result[1].length; k++) {
				if (result[1][k] > maxDis) {
					maxDis = result[1][k];
					posMaxDis = k;
				}
			}
			if(distance < result[1][posMaxDis]){//�����������ȵ�ǰ������򽻻�
			result[0][posMaxDis] = id;
			result[1][posMaxDis] =(float) distance;
			}
		}

    	
    	indexResultSet++;

	}//end of for()
	
	list.clear();
	dao.clear();
	} //end of while

//	System.out.println("�ܼ�¼����: "+indexTable);
	//����
	  float stmp;
	          for (int i = 1; i< result[0].length; i++)
	          {
	              for(int j=0; j<i;j++)
	              {
	                  if(result[1][i]<result[1][j])
	                  {
	                      stmp = result[1][i];
	                      result[1][i] = result[1][j];
	                      result[1][j] = stmp;  
	                      
	                      stmp = result[0][i];
	                      result[0][i] = result[0][j];
	                      result[0][j] = stmp;
	                  }    
	              }    		      
	          }
	          for (int i = 0; i < result[0].length; i++) {
				System.out.println(result[0][i]);
			}
	          return result;
	}
	
	private float[][] CalculatDistanceEntropy(int[] H,int totlePixels) {
		

		Feature feature = null;
		BaseDao dao = new BaseDao();
		List list=null ;
		String hql = "from Feature ";
		int amountReadOnetime = 30;
		int id = 0;
		int indexResultSet = 0;
		float[][] result = new float[2][30];
		for (int i = 0; i < result[1].length; i++) {
			result[1][i] = Float.MAX_VALUE;
		}
		

		
		double entropy = 0;
		double p = 0;
		for (int i = 0; i < H.length; i++) {
//			System.out.println(H[i]);
			if (H[i] == 0) {
				continue; //P����Ϊ0.log��0����NaN
			}
//			p = H[i]/totlePixels;
			p = (double)H[i]/totlePixels;
			entropy -= p*(Math.log(p)/Math.log(2));
		}

	int indexTable = 0;
	while(amountReadOnetime == 30 ){
		//TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index < amountQuery)
		try {
			list = dao.findByHQL(hql, 207, 5); //һ��ȡ��30���¼
			amountReadOnetime = list.size() ;
			indexTable += amountReadOnetime;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(list.size());
	///��ÿ��ͼ����F
	for(int j=0; j < list.size(); j++ ){ //list.size()
		   feature = (Feature)list.get(j);				
		   double entropyi = feature.getEntropy();
		   id = feature.getId();	     
		

        double distance = Math.abs(entropy - entropyi);
        System.out.println("ID:"+id+"entropy="+entropy+"entropyi"+entropyi);
         System.out.println("����:"+distance);
	
    	if ( indexResultSet < 30 ) {
			result[1][indexResultSet] =(float) distance;
			result[0][indexResultSet] = id;
			//System.out.println("�����ӡ�� "+indexResultSet);
			
		}else {//��������
			float maxDis = 0.0f;
			int posMaxDis = 0;
			for (int k = 0; k < result[1].length; k++) {
				if (result[1][k] > maxDis) {
					maxDis = result[1][k];
					posMaxDis = k;
				}
			}
			if(distance < result[1][posMaxDis]){//�����������ȵ�ǰ������򽻻�
			result[0][posMaxDis] = id;
			result[1][posMaxDis] =(float) distance;
			}
		}

    	
    	indexResultSet++;

	}//end of for()
	
	list.clear();
	dao.clear();
	} //end of while

//	System.out.println("�ܼ�¼����: "+indexTable);
	//����
	  float stmp;
	          for (int i = 1; i< result[0].length; i++)
	          {
	              for(int j=0; j<i;j++)
	              {
	                  if(result[1][i]<result[1][j])
	                  {
	                      stmp = result[1][i];
	                      result[1][i] = result[1][j];
	                      result[1][j] = stmp;  
	                      
	                      stmp = result[0][i];
	                      result[0][i] = result[0][j];
	                      result[0][j] = stmp;
	                  }    
	              }    		      
	          }
	          for (int i = 0; i < result[0].length; i++) {
				System.out.println(result[0][i]);
			}
	          return result;
	}
	
	private boolean invertGaussJordan(double[][] Matrix) {
		int i, j, k, l;
		double d = 0, p = 0;
		
		int  numCols = Matrix.length;
		int[] pnRow = new int[numCols];
		int[] pnCol = new int[numCols];
		
		for (k = 0; k <= numCols-1; k++) {
			d = 0.0;
			for (i = k; i <= numCols-1; i++) {
				for (j = k; j <= numCols-1; j++) {
				//	l = i*numCols+j;
					p = Math.abs(Matrix[i][j]);
					if (p > d) {
						d = p;
						pnRow[k] = i;
						pnCol[k] = j;
					}
				}
			}
			
			//shibai
			if (d == 0.0) {
				return false;
			}
			
			if (pnRow[k] != k) {
				for (j = 0; j <= numCols-1; j++) {
					//v = pnRow[k]*numCols+j;
					p = Matrix[k][j];
					Matrix[k][j] = Matrix[pnRow[k]][j];
					Matrix[pnRow[k]][j] = p;
				}
			}
			
			if (pnCol[k] != k) {
				for ( i = 0; i <= numCols-1; i++) {
					p = Matrix[i][k];
					Matrix[i][k] = Matrix[i][pnCol[k]];
					Matrix[i][pnCol[k]] = p;
				}
			}
			
			l = k;
			Matrix[k][k] = 1/Matrix[k][k];
			for (j = 0; j <= numCols-1; j++) {
				if (j != k) {
					Matrix[k][j] = Matrix[k][j]*Matrix[k][k];
				}
			}
			
			for ( i = 0; i <= numCols-1; i++) {
				if (i != k) {
					for ( j = 0; j < numCols-1; j++) {
						if (j != k) {
							Matrix[i][j] = Matrix[i][j] - Matrix[i][k]*Matrix[k][j];
						}
					}
				}
			}
			
			for (i = 0; i <= numCols-1; i++) {
				if (i != k) {
					Matrix[i][k] = -Matrix[i][k]*Matrix[l][l]; 
				}
			}
		}
		
		//�ָ����д���
		for ( k = numCols-1; k >= 0; k--) {
			if (pnCol[k] != k) {
				for ( j = 0; j <= numCols-1; j++) {
					p = Matrix[k][j];
					Matrix[k][j] = Matrix[pnCol[k]][j];
					Matrix[pnCol[k]][j] = p;
				}
			}
			
			if (pnRow[k] != k) {
				for ( i = 0; i <= numCols-1; i++) {
					p = Matrix[i][k];
					Matrix[i][k] = Matrix[i][pnRow[k]];
					Matrix[i][pnRow[k]] = p;
				}
			}
		}
		return true;
	}
	
	  private int[] getGrayHistogram(int[] pixelsSource ) {
			
			int[] grayHisto = new int[256];
			int r, g, b, gray;
			
			for (int i = 0; i < pixelsSource.length; i++)
			{
				//	int alpha = pixelsSource[i]>>24;
					 r = (pixelsSource[i] >> 16) & 0xff;
					 g = (pixelsSource[i] >> 8) & 0xff;
					 b = (pixelsSource[i]) & 0xff ;

					gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
					
					grayHisto[gray]++;
				
			}
	 		return grayHisto;
}

		private double getEucDistance(int[] Hp,int[] Hq) {
			double dis = 0.0;
			for (int i = 0; i < Hq.length; i++) {
				dis += (Hp[i] - Hq[i])*(Hp[i] - Hq[i]);
			}
			dis = Math.sqrt(dis);
			System.out.println("juli:"+dis);
			return dis;
		}
		
	private float[][] getSpatialHistogram(int[] pixelsSource, int width, int height ) {
			
			//	int[] grayHisto = new int[256];
				List[] Ak = new ArrayList[256];
				for (int i = 0; i < Ak.length; i++) {
					Ak[i] = new ArrayList();
				}
				int r, g, b, gray;
				int x, y ;
		//		float[] point = new float[2]; 
				
				for (int i = 0; i < pixelsSource.length; i++)
				{
					//	int alpha = pixelsSource[i]>>24;
						 r = (pixelsSource[i] >> 16) & 0xff;
						 g = (pixelsSource[i] >> 8) & 0xff;
						 b = (pixelsSource[i]) & 0xff ;

						 //point[0] 
						 x = i / width;
						 //point[1] 
						 y = i % width;
						 gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
						
						 Ak[gray].add(x);
						 Ak[gray].add(y);
						// grayHisto[gray]++;
					
				}
				
				float[][] Bi = new float[3][256];
				for (int i = 0; i < Bi[0].length; i++) {//ʹ��|Ak|ΪAk�����ص���
					if (Ak[i].size() == 0) {
						continue;
					}
					float sumX = 0, sumY = 0;
					for (int j = 0; j < Ak[i].size(); j++) {
						//point = (float[])Ak[i].get(j);
						x = (Integer)Ak[i].get(j );
						y = (Integer)Ak[i].get(++j );						
						sumX += x;
						sumY += y;
					}
					Bi[0][i] = 1.0f/(width*Ak[i].size()) * sumX; 
					Bi[1][i] = 1.0f/(height*Ak[i].size()) * sumY;
				}
				
			//	float[] Qk = new float[256];
				for (int i = 0; i < Bi[0].length; i++) {
					if (Ak[i].size() == 0) {
						continue;
					}
					double sumEuc = 0;
					for (int j = 0; j < Ak[i].size(); j++) {
						//point = (float[])Ak[i].get(j);
						x = (Integer)Ak[i].get(j );
						y = (Integer)Ak[i].get(++j );
						sumEuc += (Bi[0][i] - x)*(Bi[0][i] - x)+(Bi[1][i] - y)*(Bi[1][i] - y);
						}
					Bi[2][i] = (float)Math.sqrt(sumEuc/Ak[i].size());				
				}
		 		return Bi;
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
