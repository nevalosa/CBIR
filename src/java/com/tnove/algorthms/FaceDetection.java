/**
 * 
 */
package com.tnove.algorthms;


/**
 * @class FaceDetection TODO
 * @version 1.0
 * @author Nevalosa Deng
 * @mail NevalosaD@hz.webex.com
 * @date 2011-9-2
 */
public class FaceDetection {
	
	private JNIOpenCV myJNIOpenCV;
	
	private FaceDetection myFaceDetection;
	
	public FaceDetection(){
	
		myJNIOpenCV = new JNIOpenCV();
		String filename = "5.jpg";
		String cascade = "haarcascade_frontalface_default.xml";
		int[] detectedFaces = myJNIOpenCV.detectFace(cascade, filename);
		int numFaces = detectedFaces.length / 4;
		System.out.println("numFaces = " + numFaces);
		for (int i = 0; i < numFaces; i++) {
			System.out.println("Face " + i + ": " + detectedFaces[4 * i + 0] + " " + detectedFaces[4 * i + 1] + " " + detectedFaces[4 * i + 2] + " "
			        + detectedFaces[4 * i + 3]);
		}
	}
	
	public static void main(String args[]) {
		
		System.out.println("java.library.path="+System.getProperty("java.library.path"));
		FaceDetection myFaceDetection = new FaceDetection();
	}
}

//class JNIOpenCV {
//	
//	static {
//		System.loadLibrary("libcv200");
//	}
//	
//	public native int[] detectFace(int minFaceWidth, int minFaceHeight, String cascade, String filename);
//	
//}
