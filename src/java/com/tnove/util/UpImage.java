package com.tnove.util;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;

/**
 * ʵ��ͼƬ�ϴ���Ҳ�����ϴ������ļ�
 * 
 * @author ruibo
 *
 */

public class UpImage  {
    public static final int MAX_SIZE = 1024 * 1024*100;
  
    private int file_Size=0;
    private String file_Path = "";
    private HashMap hm = new HashMap();

    /**
     * �ļ��ϴ�
     * 
     * @param req �ͻ��˵�Request����
     * @param destFileDir �ϴ��ļ��ı��ر���·��
     * @return 
     */
    public String upLoad(HttpServletRequest req, String destFileDir) {
        String tmpString ="";
        String result = "";
        DataInputStream dis = null;

        try {
            dis = new DataInputStream(req.getInputStream());
            String content = req.getContentType();
            if (content != null && content.indexOf("multipart/form-data") != -1) {

                int reqSize = req.getContentLength();
                byte[] data = new byte[reqSize];
               
                int bytesRead = 0;
                int totalBytesRead = 0;
                int sizeCheck = 0;
                while (totalBytesRead < reqSize) {
                    // check for maximum file size violation
                    sizeCheck = totalBytesRead + dis.available();
                    if (sizeCheck > MAX_SIZE)
                        result = "�ļ�̫�����ϴ�...";

                    bytesRead = dis.read(data, totalBytesRead, reqSize);
                    totalBytesRead += bytesRead;
                }

                tmpString = new String(data);
                hm = parseAnotherParam(tmpString);
                int postion = arrayIndexOf(data, "\r\n".getBytes());
                byte[] split_arr = new byte[postion];
                System.arraycopy(data, 0, split_arr, 0, postion);


                postion = arrayIndexOf(data, "filename=\"".getBytes());
                byte[] dataTmp = new byte[data.length - postion];
                System.arraycopy(data, postion, dataTmp, 0, dataTmp.length);
                data = null;
                data = dataTmp.clone();


                String filePath =null;
                postion = arrayIndexOf(data, "Content-Type:".getBytes())-2;
                dataTmp = null;
                dataTmp = new byte[postion];
                System.arraycopy(data, 0, dataTmp, 0, dataTmp.length);
                filePath = new String(dataTmp);
                if (filePath==null && filePath.equals("")) return "";
//System.out.println("filename |"+filePath+"|");
               // ����contentType ����ֵ
                postion = arrayIndexOf(data, "Content-Type:".getBytes());
                dataTmp = null;
                dataTmp = new byte[data.length - postion];
                System.arraycopy(data, postion, dataTmp, 0, dataTmp.length);
                data = null;
                data = dataTmp.clone();
//System.out.println("src adatatmp |"+new String(data)+"|");
 
                postion = arrayIndexOf(data, "\n".getBytes()) + 1;
                dataTmp = null;
                dataTmp = new byte[data.length - postion];
                System.arraycopy(data, postion, dataTmp, 0, dataTmp.length);
                data = null;
                data = dataTmp.clone();
//System.out.println("datatmp |"+new String(data)+"|");

                // �����ļ���Ϣ ���������Ҫ���ֽ�
                postion = arrayIndexOf(data, split_arr);
                split_arr = null;
                dataTmp = null;
                dataTmp = new byte[postion - 2];
                System.arraycopy(data, 2, dataTmp, 0, dataTmp.length);
                data = null;
                data = dataTmp.clone();
//System.out.println("datatmp |"+new String(data)+"|");

                postion = arrayLastIndexOf(data, "\n".getBytes())-1;
                dataTmp = null;
                dataTmp = new byte[postion];
//System.out.println("postion:"+postion + " datalength:"+ data.length +" tmplength:" + dataTmp.length);
                System.arraycopy(data, 0, dataTmp, 0, dataTmp.length);

                data = null;
//System.out.println("data |"+new String(dataTmp)+"|");
                String file_path = getFileName(filePath);
//System.out.println("file_path:"+file_path);
                if(null != file_path) {//�ļ��ϴ�·��
                  if (writeFile(dataTmp, destFileDir + file_path)) {
                    this.file_Size = dataTmp.length;
                    this.file_Path = destFileDir + file_path;
                    result = "�ļ��ϴ����";
                  } else {
                    result = "�ļ��ϴ�ʧ��";
                  }
                }else{
                    result = "�ļ���Ϊ��";
                }
                dataTmp = null;
            } else {
                result = "content ����Ϊ multipart/form-data";
            }
        } catch (UnsupportedEncodingException ex4) {
            result = "UnsupportedEncodingException����";
        } catch (NullPointerException e) {
            result = "NullPointerException����";
        } catch (IOException ex1) {
            result = "IOException ���� ";
        }catch (Exception ex1) {
            result = "Exception ���� ";
        }

        return result;
    }

    /**
     * �����ļ��ı���·��
     * 
     * @return
     */
    public String getFilePath(){
        return this.file_Path;
    }

    /**
     * �����ļ���С
     * 
     * @return
     */
    public int getFileSize(){
        return this.file_Size;
    }

    /**
     * �����ļ�
     * 
     * @param data
     * @param path
     * @return
     */
    public boolean writeFile(byte[] data, String path) {
        File f = null;
        FileOutputStream fos = null;
        try {
            f = new File(path);
            f.createNewFile();
            fos = new FileOutputStream(f);
            fos.write(data, 0, data.length);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public String getFileName(String arg) {
        String path = "";
        if(arg.equals("\"\"")) {
            return null;
        }

        if (arg.indexOf("\"") > -1)
            path = arg.substring(arg.indexOf("\"") + 1, arg.lastIndexOf("\""));
        else
            path = arg;
//System.out.println("file_path:"+arg);
        path = path.substring(path.lastIndexOf("\\") + 1);
        return path;
    }


    /**
     * �ж�}��byte�����ֵ�Ƿ����
     */
    private boolean arrayEquals(byte[] src, byte[] value){
        if(src == null || value == null) 
            return false;
        if(src.length != value.length) 
            return false;

        for(int i=0; i<src.length; i++) {
            if(src[i] != value[i]) 
                return false;
        }
        return true;
    }

    /**
     * �ҳ�value������src�е�λ��, ��ǰ���
     * @param src
     * @param value
     * @return
     */
    private int arrayIndexOf(byte[] src, byte[] value){
        if(src == null || value == null) 
            return -1;
        if(src.length < value.length) 
            return -1;

        int postion = -1;

        for(int i=0; i<src.length - value.length; i++) {
            postion = i;
            byte[] tmp = new byte[value.length];
            System.arraycopy(src, i, tmp, 0, tmp.length);
            if(arrayEquals(tmp, value)) {
                tmp = null;
                return postion;
            }else{
                postion = -1;
                tmp = null;
            }
        }

        return postion;
    }

    /**
     * �ҳ�value������src�е�λ��
     * 
     * @param src
     * @param value
     * @return
     */
    private int arrayLastIndexOf(byte[] src, byte[] value){
        if(src == null || value == null) 
            return -1;
        if(src.length < value.length) 
            return -1;

        int postion = -1;

        for(int i=src.length - value.length ; i >-1; i--) {
            postion = i;

            byte[] tmp = new byte[value.length];
            System.arraycopy(src, i, tmp, 0, tmp.length);
//System.out.println(i);
//Common.PrintDataHex(tmp, " ");
//Common.PrintDataHex(value, " ");

            if(arrayEquals(tmp, value)) {
                tmp = null;
                return postion;
            }else{
                postion = -1;
                tmp = null;
            }
        }
        //System.out.println("debug");
        return postion;
    }
    

    public HashMap parseAnotherParam(String str){
      HashMap<String, String> hm= new HashMap<String, String>();
      String key="";
      String value="";
      int startindex = 0;
      int endindex = 0;

      startindex = str.indexOf("Content-Disposition: form-data; name=\"") 
                 + "Content-Disposition: form-data; name=\"".length();
      endindex = str.indexOf("\"\r\n\r\n");

      while ( startindex >-1 && endindex > -1 ){
        key = str.substring(startindex, endindex);

        if(!str.substring(endindex , endindex + 5).equals("\"\r\n\r\n")  ){//ȥ��û��value��Ԫ��
            str = str.substring(endindex);
            startindex = str.indexOf("Content-Disposition: form-data; name=\"") 
                       + "Content-Disposition: form-data; name=\"".length();
            endindex = str.indexOf("\"\r\n\r\n");
            continue;
        }
        if( key.indexOf("\";") > -1){//ȥ���ϴ��ļ��Ĳ����Լ�����
           str = str.substring(str.indexOf("\";") + 2);
           startindex = str.indexOf("Content-Disposition: form-data; name=\"") 
                      + "Content-Disposition: form-data; name=\"".length();
           endindex = str.indexOf("\"\r\n\r\n");

           continue;
        } else
            str = str.substring(endindex + 5);

        value = str.substring(0, str.indexOf("\r\n"));
        str = str.substring(str.indexOf("\r\n") + 2);
        //System.out.println("key:"+key+" value:"+value);
        hm.put(key,value);

        startindex = str.indexOf("Content-Disposition: form-data; name=\"") 
                   + "Content-Disposition: form-data; name=\"".length();
        endindex = str.indexOf("\"\r\n\r\n");

      }
      return hm;
    }

    public String getParameter(String param){
        //System.out.println(hm.toString());
      return (String)hm.get(param);
    }
}
