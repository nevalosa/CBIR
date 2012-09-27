package com.tnove.util;



import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;


public class Crawer extends Thread  {
	Container container;

	public Crawer(Container container){
	//	parser = new Parser();
	
		this.container = container;
		 try {
		        this.container.crawerLogWriter = new PrintWriter(new FileWriter("G:/temp/crawerError.log"));
		      } catch (Exception e) {
		       e.printStackTrace();
		      }
	}

	/**
	 * ���ַ�ת��ΪURL����
	 * 
	 * @param url
	 * @return
	 */
		private URL verifyUrl(String url) {
			  // Only allow HTTP URLs.
			  if (!url.toLowerCase().startsWith("http://"))
			    return null;

			  // Verify format of URL.
			  URL verifiedUrl = null;
			  try {
			    verifiedUrl = new URL(url);
			  } catch (Exception e) {
				container.crawerLogWriter.println("ת��URLʱ���"+e.toString()+getDate());
			    return null;
			  }

			  return verifiedUrl;
			}

		/**
		 * �߳�ģ�顣��������Ѽ���Ϣ����
		 */
//		 Perform the actual crawling, searching for the search string.
		public void run ()
		{
			//  current URL
			  String scanURL = null;
			  Parser parser = new Parser();
			  int badLink = 0;
				
		  while (container.isSaving 
				  && container.isCrawling 
				  && container.toCrawlPageURLSet.size() > 0
				  && container.downloadImgCount < container.MAX_IMAGE_NUM
				   )//��10000��t�Ӿ�ͣ
		  {
			  //clear old page key
			  container.clearCurrentPageKey();
			  
			  if(container.toCrawlPageURLSet.iterator().hasNext()){
			  scanURL = container.toCrawlPageURLSet.iterator().next();//�õ���һ���¼��URL�����ڵ�t���ı�
			  container.toCrawlPageURLSet.remove(scanURL);
			  }else {
				System.out.println("toCrawlSet�Ѿ���û�п���t��");
				break;
			}
			  int fatherURLTextPos = scanURL.indexOf("***")+3;
			  container.fatherURLText = scanURL.substring(fatherURLTextPos).trim();
			  scanURL = scanURL.substring(0, fatherURLTextPos-3);//����ĵ�ǰҳ���URL
			  container.fatherURL = scanURL;
			  try {
				parser.setURL(scanURL);
			//	if(!errorFlag){
				parser.setEncoding("gb2312");
			//	}else {
				//	parser.setEncoding("")
			//	}
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				badLink++;
				e.printStackTrace();
				container.crawerLogWriter.println("�������ʼ��ʱ��?"+e.toString()+getDate()); 
			}
			
			container.crawledPageCount++;
			
		//	  container.imgURLSet.addAll(getImageVisitor(parser));
			 ////////////////////////////////////////////
	            NodeVisitor visitor = new NodeVisitor() {
	                public void visitTag(Tag tag) {
	                    if (tag instanceof HeadTag) {
	                    } else if (tag instanceof MetaTag) {
	                        MetaTag metaTag = new MetaTag();
	                        metaTag = (MetaTag)tag;
	                        String cont  = metaTag.getAttribute("name") ;   	                        
	                        if (cont!=null 	                       		
	                        		&& (cont.equalsIgnoreCase("description")
	                        				||cont.equalsIgnoreCase("keyword"))) { 
	                        	
	                        	//add meta-keyword to pageMetaKey 
	                        	 container.currentPageMetaKey += metaTag.getAttribute("content"); 
	                        }                           
	                    } else if (tag instanceof TitleTag) {
	                        container.currentPageTitle = tag.getText();
	                    } else if (tag instanceof LinkTag 
	                    		&&  container.toCrawlPageURLSet.size() < 5000) {//t�ӵ�5000ʱ�������t�ӡ�
	                    	LinkTag linkTag= new LinkTag();
	                    	 linkTag = (LinkTag)tag;
	                    	 String newPageURL = linkTag.getLink();
	                    	 String newPageURLText = linkTag.getLinkText();	
	                    	 if(newPageURL.length()>1){
	                    	 //is imageURL 
	                    	 //the text of this link relate to the image
	                    	 if(newPageURL.toLowerCase().endsWith(".jpg")||newPageURL.toLowerCase().endsWith(".jpeg")){
	                    		 //container.currentPageMetaKey++"\\"+container.currentPageTitle+"\\"
	                    		 String imageKeyword = container.fatherURLText+"\\"+newPageURLText;
	                    		 container.crawerAddURL(newPageURL+"***"+container.fatherURL+"***"+imageKeyword);
	                    	 }
	                    		
	                    	 //whether this page-URL is the pre-Image's discription or not
	                    	 if (container.preImageTagName==linkTag.getAttribute("name")) {
								//whether pre-Image crawled
	                    		 if(container.toCrawlImageURLSet.contains(container.preImageURL)){
	                    			 //remove old imageURL String
	                    			 container.toCrawlImageURLSet.remove(container.preImageURL);
	                    			 //renew the pre-image URL string,add more keyword
	                    			 container.preImageURL += "\\"+linkTag.getLinkText();
	                    			 //reload this new image URL 
	                    			 container.crawerAddURL(container.preImageURL);
	                    		 }
							}
	                    	 //is pageURL and doesn't crawled
	                    	 if(!container.crawledPageURLSet.contains(newPageURL)){
	                    	 //limit host
	                    	 if(container.isLimitHost){
	                    		 URL scanUrl = verifyUrl(container.getStartURL()); 
	                    		 URL newUrl = verifyUrl(newPageURL);	                    		 
	                    	 if(newUrl != null 
	 	                    		&&  scanUrl.getHost().toLowerCase().equals(
			                  newUrl.getHost().toLowerCase())){
	                    		 container.toCrawlPageURLSet.add(newPageURL+"***"+newPageURLText);
	                    		//add URL-text after URL 
	                    	 }
	                    	 }else{
	                    		 container.toCrawlPageURLSet.add(newPageURL+"***"+newPageURLText);
	                    		// add URL-text after URL
	                    	 }
	                    	 }
	                    	 }//end of the first if(newPageUrl.Length()>1)
	                    }  else if (tag instanceof ImageTag) {	                    	
	                    	ImageTag imageTag= new ImageTag();
	                    	 imageTag = (ImageTag)tag;
	                    	 String imageURL = imageTag.getImageURL();
	                    	 String imageURLText = imageTag.getAttribute("alt");

	        			    if (imageURL.length() > 0) 
	        			    	//is jpg/jpeg
	        				    if(imageURL.toLowerCase().endsWith(".jpg") || imageURL.toLowerCase().endsWith(".jpeg") )
	        				    // is not JavaScript links.
	        				    if (imageURL.toLowerCase().indexOf("javascript") == -1) {
	        				    	//container.currentPageMetaKey+"\\"+container.currentPageTitle+"\\"+
	        				    	String imageKeyword = container.fatherURLText+"\\"+imageURLText;
	                                
	        				    	//save imagetage infomation
	        				    	container.preImageTagName = imageTag.getAttribute("name");
	                                container.preImageURL = imageURL+"***"+imageKeyword;
	        				    	container.crawerAddURL(imageURL+"***"+container.fatherURL+"***"+imageKeyword);//
	        			    }
	                    }else {
	                    	
	                     } 

	                } 

	            }; 
	            try {
					parser.visitAllNodesWith(visitor);
				} catch (ParserException e) {
					// TODO Auto-generated catch block
					badLink++;
					e.printStackTrace();
					container.crawerLogWriter.println("������ҳ�ڵ�ʱ��?"+e.toString()+getDate()); 
				}
				container.crawledPageURLSet.add(scanURL);
		    
		    }		  
		    container.isCrawling = false;

		    try {	
			    
			    //recod end-mark and some other information 
			    container.crawerLogWriter.println("Crawer��������ҳ��\n"+container.crawledPageCount);
			    container.crawerLogWriter.println("Crawerδ���е�t����\n"+container.toCrawlPageURLSet.size());
			    container.crawerLogWriter.println("������Ļ�t����\n"+badLink + "��");
			    container.crawerLogWriter.println("Crawer����\n"+getDate()); 
		    	container.crawerLogWriter.close();
		    	
		    	//record the first 100 page-URL
		        RandomAccessFile randomAccessFile = new RandomAccessFile(container.toCrawlURLFile, "rw");
		        for(int i=0;i < 100 && container.toCrawlPageURLSet.iterator().hasNext(); i++){
		    	        String url = container.toCrawlPageURLSet.iterator().next();
		                randomAccessFile.writeBytes(url+"\n");
		                container.toCrawlPageURLSet.remove(url);
		              }
		              randomAccessFile.close();

			    } catch (Exception e) {
			           e.printStackTrace();
			    }
		}
		
		/**
		 * ���������Ϣ������log��¼
		 * 
		 * @return
		 */
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

