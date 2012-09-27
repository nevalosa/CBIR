package com.tnove.util;


//import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import org.hibernate.Hibernate;

import com.tnove.dao.*;



public class SaveImage extends Thread{
	
	 Container container;
	 

	 /**
	  * �ù�����ʹ��Ĭ�ϵ�ֵ
	  * @param container  ���������߹������
	  */
	public SaveImage(Container container){
		
		this.container = container;
		 try {
		        this.container.saveLogWriter = new PrintWriter(new FileWriter("G:/temp/saveError.log"));
		      } 
		 catch (Exception e) {
		       e.printStackTrace();
		      } 

	}
	
	/**
	 * 
	 * @param container  ���������߹������
	 * @param numsQuantizedColor ��ɫ����������256ɫ
	 * @param numsCluster ���������ò����д�ѡ�����еĴ���
	 * @param numsIteration ����ʱ�ľ���������ظ����������
	 * @param m FCM�㷨�Ĳ���
	 * @param epsilon ���ϵ�����С�ڸò���ʱ���ཫ����
	 */
/*	public SaveImage(Container container, int numsQuantizedColor, int numsCluster ,int numsIteration ,int m ,double epsilon){
		
		this.container = container;
		 try {
		        this.container.saveLogWriter = new PrintWriter(new FileWriter("G:/temp/saveError.log"));
		      } 
		 catch (Exception e) {
		       e.printStackTrace();
		      }
		 histogramData = new HistogramData( numsCluster );
		 qImage = new QuantizingImage(); 
		 this.numsQuantizedColor = numsQuantizedColor;
		 this.numsCluster = numsCluster;
		 this.numsIteration = numsIteration;
		 this.m = m;
		 this.epsilon = epsilon;
	}
*/
	/**
	 * ÿ��̳�Thread��������ʵ�ֵĺ���
	 */
	public void run(){
		container.isSaving = true;

		//ArrayList<String> imageList = String [] imageHaSet.toArray();	
    	while(container.downloadImgCount < Container.MAX_IMAGE_NUM){

    		//    		��imgURLSet������crawer����,�����ѭ��,�����ͼ����    		
    		if(!container.isCrawling && container.toCrawlImageURLSet.size()==0){
    			break;
    				}
    		String imageURL = container.getImgURL();
    	    container.toCrawlImageURLSet.remove(imageURL);
    		int imageKeywordPos = imageURL.indexOf("***")+3;
    		String imageFather = imageURL.substring(imageKeywordPos);   		
    		imageURL = imageURL.substring(0, imageKeywordPos-3);
    		
    		imageKeywordPos = imageFather.indexOf("***")+3;
    		String imageKeyword = imageFather.substring(imageKeywordPos);   		
    		String fatherURL = imageFather.substring(0, imageKeywordPos-3);
    		
    			try{
			//skip the crawled image url.it's the same picture
    			//	System.out.println("t��"+imageURL+"����ʣ�"+container.crawledImageURLSet.contains(imageURL));
    				if(container.crawledImageURLSet.contains(imageURL)){
    					continue;
    				}
    URL    url=verifyUrl(imageURL); 
    container.crawledImageURLSet.add(imageURL);
    container.crawledImgCount++; 
    
    java.awt.Image   src   =   javax.imageio.ImageIO.read(url);   //����Image���� 
   // PlanarImage src = JAI.create("url", url);
    if(src == null){
    	continue;
    }
    int   width   =   src.getWidth(null);   //�õ�Դͼ��   
    int   height  =   src.getHeight(null);   //�õ�Դͼ�� 
//    String fileName =  src.getSource();
    if(width < 300 || height < 300 || (width > 2500 && height >2500)){
    	continue;
    }
    BufferedImage   tag   =   new   BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    //tag.getGraphics();
    tag.getGraphics().drawImage(src, 0, 0, width, height, null);   //���Ի�����С���ͼ   
    
    //get file name
    String file = url.getFile();
    int p=file.lastIndexOf('/');
    String fileName = file.substring(p+1);
    if (container.crawledImageNameSet.contains(fileName)) {
		continue;
	}else {
		container.crawledImageNameSet.add(fileName);
	}
    
/*   FileOutputStream   out   =   new   FileOutputStream("G:/temp/DownloadImage1/"+fileName);   //����ļ���   
    JPEGImageEncoder   encoder   =   JPEGCodec.createJPEGEncoder(out);   
    encoder.encode(tag);   //��JPEG����   	
    out.close();  
*/    				
    container.downloadImgCount++;   
    
    // save image object to MySQL
    try{
    BaseDao dao = new BaseDao();
    com.tnove.dao.Image image = new com.tnove.dao.Image();
    image.setHeight(height);
    image.setWidth(width);
    if(fileName.length() > 100){
    	int pos = fileName.length()-100+1;
    	fileName = fileName.substring(pos);
    	
    }
    if(imageKeyword.length() > 300){
    	int pos = imageKeyword.length()-100+1;
    	imageKeyword = imageKeyword.substring(pos);
    }
    image.setImageName(fileName);
    image.setKeyword(imageKeyword);
    image.setUrl(imageURL); 
    image.setFatherUrl(fatherURL);
    
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(tag,"jpg", bas);
    byte[] data = bas.toByteArray();
    InputStream tt = new ByteArrayInputStream(data);
//    InputStream imageis =new   FileInputStream("G:/temp/DownloadImage1/"+fileName);    
    
    //TODO Nevalosa Deng
//    Blob blob = (Blob)Hibernate.createBlob(tt);
    bas.close();
    tt.close();
    
//    image.setValue(blob);
    dao.add(image);
    }catch (Exception e) {
		// TODO: handle exception
    	System.out.println(e.getMessage());
    	container.saveLogWriter.println("����ݿ�ʱ��?"+e.toString()+getDate());
	}

    	}
    	catch (IOException e) {
    		// TODO: handle exception
    		System.out.println(e.getMessage());
    		container.saveLogWriter.println("����ͼƬʱ��?"+e.toString()+getDate());
    	}
    	catch (Exception e) {
    		// TODO: handle exception
    		System.out.println(e.getMessage());
    		container.saveLogWriter.println("����ͼƬʱ���:"+e.toString()+getDate());
    	}
    	      
    }
    	container.isSaving = false;
    	container.saveLogWriter.println("�ܹ�ɨ��ͼ��t����\n"+container.crawledImgCount+" ��");
    	container.saveLogWriter.println("δɨ��ͼ��t����\n"+container.toCrawlImageURLSet.size()+" ��");
    	container.saveLogWriter.println("�ܹ���������ͼ����\n"+container.downloadImgCount+" ��");
    	container.saveLogWriter.println("saveimage����\n"+getDate());
    	container.saveLogWriter.close();
	}
	private URL verifyUrl(String url) {
		  // Only allow HTTP URLs.
		  if (!url.toLowerCase().startsWith("http://"))
		    return null;

		  // Verify format of URL.
		  URL verifiedUrl = null;
		  try {
		    verifiedUrl = new URL(url);
		  } catch (Exception e) {
			  container.saveLogWriter.println("ת��URLʱ���"+e.toString()+getDate());
		    return null;
		  }

		  return verifiedUrl;
		}
	private   String   getDate()     {   
		 // Date   date=null; 
		  SimpleDateFormat format;
		  Calendar   MyDate = Calendar.getInstance();   
		  MyDate.setTime(new java.util.Date());   
		  Date date = MyDate.getTime(); 
		  format = new SimpleDateFormat("yyyy-mm-dd HH:mm");
		  String datestr = format.format(date);
		  return   datestr;   
		  }   
	


}

