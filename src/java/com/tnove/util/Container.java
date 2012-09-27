package com.tnove.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * ��Ϣ�࣬���Spider��ͼ��洢ģ�鹲ͬ��Ҫ�����
 * 
 * @author ruibo
 *
 */

public class Container {
	static final int MAX_IMG_URL=500;
    static final int MAX_IMAGE_NUM = 500;
     int downloadImgCount = 0;
     int crawledImgCount = 0;
     int crawledPageCount = 0;
     boolean isLimitHost ;
     boolean isCrawlFromStartURL;
	 boolean isCrawling = true;
	 boolean isSaving = true;
     String  startURL;
     String  currentPageMetaKey = null;
     String  currentPageTitle = null;
     String  fatherURLText = null;
     String  fatherURL = null;
     String  toCrawlURLFile = null;
     String  preImageTagName = null;
     String  preImageURL = null;

 //    HashMap disallowListCache ;
	
      PrintWriter urlLogWriter;
	  PrintWriter crawerLogWriter;
	  PrintWriter saveLogWriter;
//	  ArrayList<String> urList;
	  LinkedHashSet<String> toCrawlImageURLSet;
	  HashSet<String> crawledPageURLSet;
	  HashSet<String> crawledImageURLSet;
	  HashSet<String> crawledImageNameSet;
	  LinkedHashSet<String> toCrawlPageURLSet;
//	  LinkedBlockingQueue<String> imgURL;

	public Container(){
		
	}
	public Container(String startURLFile,String toCrawlURLFile,boolean isCrawlFromStartURL,boolean isLimitHost) throws IOException{
		//disallowListCache = new HashMap();
		crawledPageURLSet = new HashSet<String>();
		crawledImageURLSet = new HashSet<String>();
		crawledImageNameSet = new HashSet<String>();
		toCrawlPageURLSet = new LinkedHashSet<String>();
		toCrawlImageURLSet = new LinkedHashSet<String>();

		this.isCrawling = true;
		this.isSaving = true;
		this.isCrawlFromStartURL = isCrawlFromStartURL;
		this.toCrawlURLFile = toCrawlURLFile;
		this.isLimitHost = isLimitHost;
		
		RandomAccessFile randomAccessFile = null;
		//add URL to toCrawlPageURLSet
		try {
			//select the right file to read
			if(isCrawlFromStartURL){
			randomAccessFile = new RandomAccessFile(startURLFile, "r");
		}else {
			randomAccessFile = new RandomAccessFile(toCrawlURLFile, "r");
		}
			
           if(randomAccessFile.length()>0){
			String url=randomAccessFile.readLine();
			
			//the first startURL			
			toCrawlPageURLSet.add(url);
			int urlEndPos = url.indexOf("***");
			url = url.substring(0, urlEndPos);
			this.startURL = url;
			while( (url=randomAccessFile.readLine())!= null){
				toCrawlPageURLSet.add(url);
			}
			}
			randomAccessFile.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void setCrawling(boolean isCrawling){
		this.isCrawling = isCrawling;
	}
	public void clearCurrentPageKey() {
		this.currentPageMetaKey  = null;
		this.currentPageTitle = null;
		this.fatherURLText = null;
	}
	public String getStartURL() {
		return this.startURL;
	}
	public synchronized void crawerAddURL(String imgUrl) {
	
		while ( toCrawlImageURLSet.size()>MAX_IMG_URL) {
			if(isSaving){
				break;
			}
			try {//sleep(100);
				wait();
			} catch (Exception e) {
				// TODO: handle exception
				crawerLogWriter.println("ͬ������-crawer");
				e.printStackTrace();
			}
		}
		toCrawlImageURLSet.add(imgUrl);
		notify();
	}
	public synchronized String getImgURL() {
		while (toCrawlImageURLSet.size() == 0) {
			if(!isCrawling){
				break;
			}
			try {
				wait();	
			} catch (Exception e) {
				// TODO: handle exception
				saveLogWriter.println("ͬ������saveimage");
				e.printStackTrace();
			}		
		
		}
		notify();
		//have imageUrl
		return toCrawlImageURLSet.iterator().next();
	}

}
