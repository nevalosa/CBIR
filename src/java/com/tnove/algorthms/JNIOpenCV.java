/**
 * 
 */
package com.tnove.algorthms;

/**
 * @class JNIOpenCV TODO
 * @version 1.0
 * @author Nevalosa Deng
 * @mail NevalosaD@hz.webex.com
 * @date 2011-9-2
 */
public class JNIOpenCV extends JNIBase {
	
	public JNIOpenCV(String libraryName){
	
		super(libraryName);
	}
	
	public JNIOpenCV(){
	
		System.loadLibrary("libcv200");
	}
	
	public native int[] detectFace(int minFaceWidth, int minFaceHeight, String cascade, String filename);
	
	public native int[] detectFace(String cascade, String filename);
}
