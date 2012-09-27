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
public class CalculateFeatureGreyCoMatrix extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public CalculateFeatureGreyCoMatrix() {
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
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {

		response.setContentType("text/html;charset=gb2312");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");

		Blob blobQueried = null ;
		Blob blobSave1 = null;
		Image pic = new Image();		
		BaseDao dao = new BaseDao();
        List list=null ;
		String hql = "from Image ";

		 int id ;
		 int indexTable = 0;
		 int amountQuery = 30;

         
		long startTime = System.currentTimeMillis();
		
		
		while (amountQuery == 30) {		
			//TODO �Ӹ�ѭ������һ�ζ�30���¼��ֱ����������ݿ� while(index < amountQuery)
			try {
			//	list.clear();
				list = dao.findByHQL(hql, indexTable, 30); //һ��ȡ��30���¼
				amountQuery = list.size();
				indexTable += amountQuery;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("���ֲ�ѯ����"+indexTable);
		///��ÿ���¼����
		for(int j=0; j < list.size(); j++ ){ //list.size()
		pic = (Image)list.get(j);
		id = pic.getId();
		blobQueried = (Blob)pic.getValue();
		
		int width = pic.getWidth();
		int height = pic.getHeight();

		 java.awt.Image image = null;
			if(blobQueried != null){			  
				try {
					InputStream inputStream   =   new   BufferedInputStream(blobQueried.getBinaryStream());
					image = javax.imageio.ImageIO.read(inputStream);
					inputStream.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				}
		         int[] pixelsSource = new int[width * height];
				PixelGrabber  pixelGrabber = new PixelGrabber(image,0,0,width,height,pixelsSource,0,width);
				 
				try
				{
					pixelGrabber.grabPixels ();
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				
		double[][][] greyCoMP = getCoMGrey(pixelsSource, width, height);
		greyCoMP = MatrixUnitary(greyCoMP, width, height);
		double[] featureCoM = getFeature(greyCoMP);

 	    
 	    //���л�
 	   try {
           ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        
           ObjectOutputStream outputStream1 = new ObjectOutputStream(out1);
           
           outputStream1.writeObject(featureCoM);
            byte [] bytes1 = out1.toByteArray();
           
//            blobSave1 =  Hibernate.createBlob(bytes1);
           out1.close();
           outputStream1.close();
         } catch (Exception e) {
            // TODO: handle exception 
     	  e.toString();
       } 

 	    /**
 	     * ��������ݿ⣻��ӵķ�ʽ���ڶ����Ժ�ʹ�ã�
 	     */ 
        Feature feature = null;//new Feature();
 	  //  feature.setId(id);
 	    try {
			feature = (Feature)dao.findById("Feature", id);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

        /**
         * ��������ݿ⣺�½��ķ�ʽ����һ��ʱʹ�ã�
         */
/*        Feature feature = new Feature();
        feature.setId(id);
        feature.setWidth(width);
        feature.setHeight(height);
*/    
		
        feature.setGreyCoMatrix(blobSave1);
 	     try {
			dao.saveOrUpdate(feature);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		}//end of for(;i < list.size();)
		
		dao.clear();
		list.clear();
		}//end of while()

		dao.clear();
		System.out.println("��¼�ĸ���Ϊ�� "+indexTable);
 	    /**                      */
		long endtime = System.currentTimeMillis();
		System.out.println("��ʱ��"+(endtime - startTime)/1000+"s");

		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * ����Ҷȹ������
	 * @param pixels  ͼ������
	 * @param width
	 * @param height
	 * @return  ���ػҶȹ������
	 */
	  private double[][][] getCoMGrey(int[] pixels, int width, int height) {
			
			double[][][] greyCoMP = new double[4][256][256];
			int r, g, b, gray;
			
			//�Ȱ�ͼ��ҶȻ�����
			for (int i = 0; i < pixels.length; i++)
			{
					 r = (pixels[i] >> 16) & 0xff;
					 g = (pixels[i] >> 8) & 0xff;
					 b = (pixels[i]) & 0xff ;

					gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
					
					pixels[i] = gray;
				
			}
			
			//��Ҷȹ������d=3,�ĸ��
			int gray1 = 0;
			int gray2 = 0;
			int d = 1; //����ѡ�����Ϊ3
			for (int i = d; i < height - d; i++)
			{
				for (int j = d; j < width - d; j++)
				{
			//���㷨��45��135�������˱�Ե��2(W+H)-7��ԡ���0��90�������˱��ص�2(w+h-4)���
			//�����ĸ�������ȣ�
					//0�ȷ���� 
						gray1 = pixels[i * width + j];
						gray2 = pixels[i*width + j + d];					
						greyCoMP[0][gray1][gray2]++;
						gray2 = pixels[i*width + j - d];
						greyCoMP[0][gray1][gray2]++;
					
                     //45��
						gray1 = pixels[i * width + j];
						gray2 = pixels[(i - d)*width + j + d];					
						greyCoMP[1][gray1][gray2]++;
						gray2 = pixels[(i + d)*width + j - d];
						greyCoMP[1][gray1][gray2]++;
						
                     //90�ȷ���
						gray1 = pixels[i * width + j];
						gray2 = pixels[(i + d)*width + j];//����					
						greyCoMP[2][gray1][gray2]++;
						gray2 = pixels[(i - d)*width + j];//����
						greyCoMP[2][gray1][gray2]++;

					//135��
						gray1 = pixels[i * width + j];
						gray2 = pixels[(i - d)*width + j - d];//����					
						greyCoMP[3][gray1][gray2]++;
						gray2 = pixels[(i + d)*width + j + d];//����
						greyCoMP[3][gray1][gray2]++;					
					
				}
			}

	 		return greyCoMP;
}
	
	  /**
	   * �Ҷȹ������Ĺ�һ�����?
	   * @param greyCoMP  �������
	   * @param width
	   * @param height
	   * @return  ���ع�һ����Ĺ������
	   */
	private double[][][] MatrixUnitary(double[][][] greyCoMP, int width, int height) {
				
		int degree45 = 2*(height-2)*(width-2);

			for (int j = 0; j < greyCoMP[0].length; j++) {
				for (int k = 0; k < greyCoMP[0][j].length; k++) {
					greyCoMP[0][j][k] /= degree45;//��һ��
					greyCoMP[1][j][k] /= degree45;
					greyCoMP[2][j][k] /= degree45;
					greyCoMP[3][j][k] /= degree45;
				}
			}
		
		return greyCoMP;
	}
	
	/**
	 * ������ڻҶȹ������Ķ���������
	 * 
	 * @param greyCoMP  ��һ����ĻҶȹ������
	 * @return ���ض��������ɵ�����
	 */
	private double[] getFeature(double[][][] greyCoMP) {
		
		//���Ƕ��׾أ�����
		double moment1 = 0;
		double moment2 = 0;
		double moment3 = 0;
		double moment4 = 0;
		double mean1 = 0, mean2 = 0, mean3 = 0, mean4 = 0;
		double[] PxPulsy1 = new double[512];
		double[] PxPulsy2 = new double[512];
		double[] PxPulsy3 = new double[512];
		double[] PxPulsy4 = new double[512];
       //���Աȶ�(g���Ծ�)
		double contrast1 = 0;
		double contrast2 = 0;
		double contrast3 = 0;
		double contrast4 = 0;
		//�����أ���
		double correlation1 = 0;
		double correlation2 = 0;
		double correlation3 = 0;
		double correlation4 = 0;
		double[] ux = new double[4];
		double[] uy = new double[4];
		double[][] uyp = new double[4][256];
		//����
		double squares1 = 0;
		double squares2 = 0;
		double squares3 = 0;
		double squares4 = 0;
		
		//�������
		double inverseDifMoment1 = 0;
		double inverseDifMoment2 = 0;
		double inverseDifMoment3 = 0;
		double inverseDifMoment4 = 0;
		//���ƽ���
		double averageSum1 = 0;
		double averageSum2 = 0;
		double averageSum3 = 0;
		double averageSum4 = 0;
		//�����
		double squaresSum1 = 0;
		double squaresSum2 = 0;
		double squaresSum3 = 0;
		double squaresSum4 = 0;
		//�����
		double log2 = Math.log(2);
		double entropy1 = 0;
		double entropy2 = 0;
		double entropy3 = 0;
		double entropy4 = 0;
		for (int i = 0; i < greyCoMP[0].length; i++) {
			double[] sumj = new double[4];
			for (int j = 0; j < greyCoMP[0][i].length; j++) {
               //&&�Ƕ��׾أ�����d������һ��Ӱ�죬�Ƕȶ���Ҳ��һ��Ӱ��p(i, j)^2
				moment1 += greyCoMP[0][i][j] * greyCoMP[0][i][j];
				moment2 += greyCoMP[1][i][j] * greyCoMP[1][i][j];
				moment3 += greyCoMP[2][i][j] * greyCoMP[2][i][j];
				moment4 += greyCoMP[3][i][j] * greyCoMP[3][i][j];
				
				//����е�ux��uy
				sumj[0] += greyCoMP[0][i][j];
				sumj[1] += greyCoMP[1][i][j];
				sumj[2] += greyCoMP[2][i][j];
				sumj[3] += greyCoMP[3][i][j];
				uyp[0][j] += greyCoMP[0][i][j];
				uyp[1][j] += greyCoMP[1][i][j];
				uyp[2][j] += greyCoMP[2][i][j];
				uyp[3][j] += greyCoMP[3][i][j];
				
				//�Աȶȵ�P��i,j��*|i-j|^2
				int tt = (i - j)*(i - j);
				contrast1 += tt * greyCoMP[0][i][j] ;
				contrast2 += tt * greyCoMP[1][i][j] ;
				contrast3 += tt * greyCoMP[2][i][j] ;
				contrast4 += tt * greyCoMP[3][i][j] ;
				
				//����
				tt = 1/(1 + tt);
				inverseDifMoment1 += tt * greyCoMP[0][i][j];
				inverseDifMoment2 += tt * greyCoMP[0][i][j];
				inverseDifMoment3 += tt * greyCoMP[0][i][j];
				inverseDifMoment4 += tt * greyCoMP[0][i][j];
				
				//��	(:ע��log����Ϊ0��
				if (greyCoMP[0][i][j] != 0) {
					entropy1 -=  greyCoMP[0][i][j] * Math.log(greyCoMP[0][i][j])/log2;
				}
				if (greyCoMP[1][i][j] != 0 ) {
					entropy2 -=  greyCoMP[1][i][j] * Math.log(greyCoMP[1][i][j])/log2;
				}
				if (greyCoMP[2][i][j] != 0) {
					entropy3 -=  greyCoMP[2][i][j] * Math.log(greyCoMP[2][i][j])/log2;
				}
				if (greyCoMP[3][i][j] != 0) {
					entropy4 -=  greyCoMP[3][i][j] * Math.log(greyCoMP[3][i][j])/log2;
				}				
				
				//��ֵ
				mean1 += greyCoMP[0][i][j];
				mean2 += greyCoMP[1][i][j];
				mean3 += greyCoMP[2][i][j];
				mean4 += greyCoMP[3][i][j];
				
				//��Pk
				PxPulsy1[i+j] +=greyCoMP[0][i][j];
				PxPulsy2[i+j] +=greyCoMP[1][i][j];
				PxPulsy3[i+j] +=greyCoMP[2][i][j];
				PxPulsy4[i+j] +=greyCoMP[3][i][j];
			}
			ux[0] += i*sumj[0];
			ux[1] += i*sumj[1];
			ux[2] += i*sumj[2];
			ux[3] += i*sumj[3];
		}
		
		//����uy
		for (int i = 0; i < uyp[0].length; i++) {
			uy[0] += i * uyp[0][i];
			uy[1] += i * uyp[1][i];
			uy[2] += i * uyp[2][i];
			uy[3] += i * uyp[3][i];
		}
		
		//����Qx��Qy
		//��256��254�ε�ѭ��ʱ�任��4��256�Ŀռ����uxʱ��
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
			Qx[0] += (i-ux[0]) * (i-ux[0]) * sumj[0];
			Qx[1] += (i-ux[1]) * (i-ux[1]) * sumj[1];
			Qx[2] += (i-ux[2]) * (i-ux[2]) * sumj[2];
			Qx[3] += (i-ux[3]) * (i-ux[3]) * sumj[3];
//			���ڵ�һ��ѭ���Ѿ�������j�еĺ�uyp[]��,���Կ���ֱ�Ӽ���
			Qy[0] += (i-uy[0]) * (i-uy[0]) * uyp[0][i];
			Qy[1] += (i-uy[1]) * (i-uy[1]) * uyp[1][i];
			Qy[2] += (i-uy[2]) * (i-uy[2]) * uyp[2][i];
			Qy[3] += (i-uy[3]) * (i-uy[3]) * uyp[3][i];
		}
		
		//�����ʽ�������
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
				correlation1 += (i*j*greyCoMP[0][i][j] - ux[0]) / Qx[0];
				correlation2 += (i*j*greyCoMP[1][i][j] - ux[1]) / Qx[1];
				correlation3 += (i*j*greyCoMP[2][i][j] - ux[2]) / Qx[2];
				correlation4 += (i*j*greyCoMP[3][i][j] - ux[3]) / Qx[3];
			}
		}
		
		//���ֵ
		mean1 /= 65536;//(256*256);
		mean2 /= 65536;
		mean3 /= 65536;
	    mean4 /= 65536;
	    
		//�����������d�ͽǶ�Ӱ��
		for (int i = 0; i < greyCoMP[0].length; i++) {
			for (int j = 0; j < greyCoMP[0][i].length; j++) {
				squares1 += (i - mean1) * (i - mean1) * greyCoMP[0][i][j];
				squares2 += (i - mean2) * (i - mean2) * greyCoMP[1][i][j];
				squares3 += (i - mean3) * (i - mean3) * greyCoMP[2][i][j];
				squares4 += (i - mean4) * (i - mean4) * greyCoMP[3][i][j];
			}
		}
		
		//���ƽ��ͣ�����(����d�ͷ����Ӱ��)
		for (int i = 2; i < PxPulsy1.length; i++) {
			averageSum1 += i * PxPulsy1[i];
			averageSum2 += i * PxPulsy2[i];
			averageSum3 += i * PxPulsy3[i];
			averageSum4 += i * PxPulsy4[i];
		}
		
		//����ͣ�������һ��Ӱ��
		for (int i = 2; i < PxPulsy1.length; i++) {
			squaresSum1 += (i - averageSum1)*(i - averageSum1)*PxPulsy1[i];
			squaresSum2 += (i - averageSum2)*(i - averageSum2)*PxPulsy2[i];
			squaresSum3 += (i - averageSum3)*(i - averageSum3)*PxPulsy3[i];
			squaresSum4 += (i - averageSum4)*(i - averageSum4)*PxPulsy4[i];
		}
		
		//�������ľ�ֵ��ʹ�ý��Է������
		double[] featureCoM = new double[8];
		featureCoM[0] = (moment1 + moment2 + moment3 + moment4) / 4;//���׽Ǿ�
		featureCoM[1] = (contrast1 + contrast2 + contrast3 + contrast4) / 4;//�Աȶ�
		featureCoM[2] = (correlation1 + correlation2 + correlation3 + correlation4) / 4;//���
		featureCoM[3] = (squares1 + squares2 + squares3 + squares4) / 4;//����
		featureCoM[4] = (inverseDifMoment1 + inverseDifMoment2 + inverseDifMoment3 + inverseDifMoment4) / 4;//����
		featureCoM[5] = (averageSum1 + averageSum2 + averageSum3 + averageSum4) / 4;//ƽ���
		featureCoM[6] = (squaresSum1 + squaresSum2 + squaresSum3 + squaresSum4) / 4;//�����
		featureCoM[7] = (entropy1 + entropy2 + entropy3 + entropy4) / 4;//��
		
		return featureCoM;
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
