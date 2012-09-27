package com.tnove.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.tnove.dao.*;


/**
 * ͼ���ѯ�󣬽����ʾʹ�ø�ģ�顣
 * ��ģ�齫��ѯ���ص�ǰ30��ͼ����ݣ��4�����������ʽ���ص��û�����
 * ͼ����ص���Ϣ�򵥶7���
 * 
 * @author ruibo
 *
 */
public class ImageZoom {
	
    public static void zoom() {
        // Ŀ��ͼƬ
        File input = new File("G:/temp/6672290.jpg");
        // ����λ��
        String output = "G:/temp/11/";
        try {
            InputStream imageStream = new FileInputStream(input);
            // ���Ŀ��ͼƬ��bһ���ͼƬ
            com.sun.image.codec.jpeg.JPEGImageDecoder decoderFile = JPEGCodec
                    .createJPEGDecoder(imageStream);
            BufferedImage imageFile = decoderFile.decodeAsBufferedImage();
            float zoom = 0.5F;// ��Ҫ����ı���
            // ���Ŀ��ͼƬ�Ŀ�ߣ�ͬʱ���Է������õ���ͼƬ��С
            int w = (int) (imageFile.getWidth() * zoom);
            int h = (int) (imageFile.getHeight() * zoom);
            // ��bһ����ͼƬ�Ļ���ͼƬ
            BufferedImage bufImage = new BufferedImage(w, h,
                    BufferedImage.TYPE_INT_RGB);
            FileOutputStream out = new FileOutputStream(output
                    + input.getName());
            // ��Ŀ��ͼƬ�ϻ��Graphics�Ա㻭����ͼƬ�ϣ����һ��������ڲ������࣬������null����
            Graphics g = bufImage.getGraphics();
            g.drawImage(imageFile, 0, 0, w, h, new ImageObserver() {
                public boolean imageUpdate(java.awt.Image img, int infoflags, int x,
                        int y, int width, int height) {
                    return true;
                }
            });
            // �������
            JPEGImageEncoder jpeg = JPEGCodec.createJPEGEncoder(out);
            jpeg.encode(bufImage);
            out.flush();
            out.close();
            imageStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getImage(HttpServletRequest request,
            HttpServletResponse response, String index) throws Exception {
 //       File input = new File(file);
        try {

        	Image pic = new Image();
    		BaseDao dao = new BaseDao();
    		Blob blobImage = null;
    		int id = Integer.parseInt(index);
    		
        //    response.reset();  
            /*response.setContentType("image/jpeg");*/ 
            ServletOutputStream out = response.getOutputStream();
            pic = (Image)dao.findById("Image",(Serializable)id); //	
			response.setContentType("image/jpg"); 
			response.reset();
			blobImage = (Blob)pic.getValue();
			byte[] data = blobImage.getBytes(1, (int)blobImage.length());
			InputStream inputStream =  new   BufferedInputStream(blobImage.getBinaryStream());
		//	ServletOutputStream outImage=response.getOutputStream();
			 while(inputStream.read(data)!=-1){
	              out.write(data);
	          }
			 dao.clear();
			 
            out.flush();
 
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ImageFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public static List<String> getImageInfo(float[] result) throws Exception {
 //       File input = new File(file);
    	String keyAndURL = null;
        try {

        	
    		BaseDao dao = new BaseDao();
    		List<String> rstList = new ArrayList<String>();

    		for (int i = 0; i < result.length; i++) {
				
    		int id = (int)result[i];
    		Image pic = new Image();
            pic = (Image)dao.findById("Image",(Serializable)id); //	
            
            //ʵ����ݿ�ʹ��
            keyAndURL = pic.getFatherUrl()+"***"+pic.getKeyword();
            //������ݿ�ʹ��
//            keyAndURL = pic.getImageName()+"***"+pic.getKeyword();//
            
			rstList.add(keyAndURL);

    		}
    		dao.clear();
			return rstList;
//          imageStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ImageFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
}
