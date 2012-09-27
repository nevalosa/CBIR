package com.tnove.util;
//http://lveyo.com/java-jni-opencv-21-face-detection.html
class JNIOpenCV {
	static {
		System.loadLibrary("JNI2OpenCV");
	}

	public native int[] detectFace(int minFaceWidth, int minFaceHeight,
			String cascade, String filename);
}

public class FaceDetection {
	private JNIOpenCV myJNIOpenCV;
	private FaceDetection myFaceDetection;

	public FaceDetection(String intputFileName, String outputFileName) {
		myJNIOpenCV = new JNIOpenCV();
		String filename = intputFileName;
		String outputFilename = outputFileName;
		String cascade = "D:\\workspace\\jee\\similar-image\\src\\haarcascade_frontalface_alt.xml";

		int[] detectedFaces = myJNIOpenCV.detectFace(40, 40, cascade, filename);
		int numFaces = detectedFaces.length / 4;

		System.out.println("numFaces = " + numFaces);
		for (int i = 0; i < numFaces; i++) {
			System.out
					.println("Face " + i + ": " + detectedFaces[4 * i + 0]
							+ " " + detectedFaces[4 * i + 1] + " "
							+ detectedFaces[4 * i + 2] + " "
							+ detectedFaces[4 * i + 3]);
		}
		int[][] RectInt = new int[numFaces][4];
		for (int i = 0; i < numFaces; i++) {
			RectInt[i][0] = detectedFaces[4 * i + 0];
			RectInt[i][1] = detectedFaces[4 * i + 1];
			RectInt[i][2] = detectedFaces[4 * i + 2];
			RectInt[i][3] = detectedFaces[4 * i + 3];
		}
		if (new DrawInImg(filename, outputFilename, RectInt).DrawRect())
			System.out.println("File create success ! ");
		else
			System.out.println("File create error ! ");
	}

	public static void main(String args[]) {

		args = new String[] { "D:\\test\\1-colore1.JPG",
				"D:\\test\\1-colore1-1.JPG" };
		FaceDetection myFaceDetection = new FaceDetection(args[0], args[1]);
	}
}