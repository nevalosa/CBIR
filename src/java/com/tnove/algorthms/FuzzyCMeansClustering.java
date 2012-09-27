package com.tnove.algorthms;


import java.util.Random;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * �ó���ʵ�ֶ�RGBɫ�ʿռ�ľ��࣬
 * ��Ϊ��120�࣬��ʼ����ɫ��Ϊ4096����16λɫ��,mȡ1.9
 * @author ruibo
 *
 */
public class FuzzyCMeansClustering {

	static Logger myLogger = Logger.getLogger(FuzzyCMeansClustering.class.getName());


	/**
	 * 
	 * 
	 */
	private int  numBands;
	private int  dataSize;
	private int  maxIterations,numClusters; 
    // FCM ���� m
    private double fuzziness; // "m" 
    //��ϵ����Ŀ�����
    private float[][] membership;
    
    //������
    private int iteration; 
    //  A metric of clustering "quality", called "j" as in the equations. 
    private double j = Float.MAX_VALUE; 
    
    // �����������
    private double epsilon; 
 
    private long position; 
    // The cluster centers. 
    private float[][] clusterCenters; 
    // A big array with all the input data and a small one for a single pixel. 
    private int[] inputData; 
    private float[] aPixel; 
    
    public FuzzyCMeansClustering(int colorDepth, int numClusters,int maxIterations, double fuzziness,double epsilon) {

    	System.out.println("��ʼ��");
    	this.dataSize = colorDepth*colorDepth*colorDepth;
    	this.numBands = 3; //��ɫ�ռ����
//    	 Get some clustering parameters. 
    	this.numClusters = numClusters; 
    	this.maxIterations = maxIterations; 
    	this.fuzziness = fuzziness; 
    	this.epsilon = epsilon; 
    	iteration = 0; 
    //	hasFinished = false;
//    	 We need arrays to store the clusters' centers, validity tags and membership values. 
    	clusterCenters = new float[numClusters][numBands]; 
    	membership = new float[dataSize][numClusters]; 
//    	 Gets the whole image data on memory. Get memory for a single pixel too. 
    	this.inputData = new int[dataSize*numBands]; 
  //  	System.out.println("����"+4);
//    	��ʼ���������
    	for (int i = 0; i < inputData.length; i++) { 
			inputData[i] = i << 4;
		}
    	System.out.println("datasize:"+dataSize);
    	aPixel = new float[numBands]; 
//    	 Initialize the membership functions randomly. 
    	Random generator = new Random(); // easier to debug if a seed is used 
//    	 For each data point (in the membership function table) 
    	for(int i=0;i<dataSize;i++) 
    	{ 
//    	 For each cluster's membership assign a random value. 
    	float sum = 0f; 
    	for(int c=0;c<numClusters;c++) 
    	{
    	membership[i][c] = 0.01f+generator.nextFloat(); 
    	sum += membership[i][c]; 
    	} 
//    	 Normalize so the sum of MFs for a particular data point will be equal to 1. 
    	for(int c=0;c<numClusters;c++){
    	membership[i][c] /=sum;
    	}
    	} 
//    	 Initialize the global position value. 
    	position = 0; 
    	System.out.println("��ʼ������");
    }
    
    /**
     * the most important method ,cala FCM
     *
     */
    public void FCMClustring() 
    { 
    	//Logger myLogger = Logger.getLogger("myLogger");    
    	long startL=System.currentTimeMillis();
        
        //Get an instance of the childLogger    
        Logger mySonLogger = Logger.getLogger("myLogger.mySonLogger");   
    	PropertyConfigurator.configure("G:/temp/log4j.properties");
     //   myLogger.debug("Entering application.");
    //	mySonLogger.debug("");
        double lastJ = 0; 
    // Calculate the initial objective function just for kicks. 
//    lastJ = calculateObjectiveFunction(); 
    // Do all required iterations (until the clustering converges) 
      for(iteration=0;iteration<maxIterations;iteration++) 
      { 
    	//System.out.println("������:"+iteration);
      // Calculate cluster centers from MFs. 
      calculateClusterCentersFromMFs(); 
      // Then calculate the MFs from the cluster centers ! 
      calculateMFsFromClusterCenters(); 
      // Then see how our objective function is going. 
//      j = calculateObjectiveFunction(); 
//      if (Math.abs(lastJ-j) < epsilon) break; 
//      lastJ = j; 
    //  System.out.println("���д���: "+ iteration );
      
      //ÿ100����һ�������һ��Ҳ����
      if ((iteration%100 == 0) || (iteration%100 == 99)) {
    	  j = calculateObjectiveFunction(); 
    	  System.out.println("���"+Math.abs(lastJ-j));
    	  if (Math.abs(lastJ-j)<epsilon) { //С�����ͽ���
			break;
		}
    	  myLogger.debug("ѭ������Ϊ��"+iteration+" ���Ϊ�� "+(lastJ-j));
      	  mySonLogger.debug("ѭ������Ϊ��"+iteration+" ���Ϊ�� "+(lastJ-j));
      	 lastJ = j; 
   	  }

      } // end of the iterations loop.
    
  //  j = calculateObjectiveFunction(); 
    myLogger.debug("��ʱΪ��"+(System.currentTimeMillis()-startL)/60000+"���ӣ� ���Ϊ�� "+j);
	mySonLogger.debug("��ʱΪ��"+(System.currentTimeMillis()-startL)/60000+"���ӣ� ���Ϊ�� "+j);
     System.out.println("���Ϊ�� "+(lastJ-j));
    // Means that all calculations are done, too. 
    position = getSize(); 
    } 
  
   /** 
    * This method calculates the cluster centers from the membership 
    * functions. 
    */ 
  private void calculateClusterCentersFromMFs() 
    { 
    float top,bottom; 
    // For each band and cluster 
    for(int b=0;b<numBands;b++) 
      for(int c=0;c<numClusters;c++) 
        { 
        // For all data points calculate the top and bottom parts of the equation. 
        top = bottom = 0;  
          for(int w=0;w<dataSize;w++) 
            { 
            // Index will help locate the pixel data position. 
            int index = (w)*numBands; 
            top += Math.pow(membership[w][c],fuzziness)*inputData[index+b]; 
            bottom += Math.pow(membership[w][c],fuzziness); 
            } 
        // Calculate the cluster center. 
        clusterCenters[c][b] = top/bottom; 
        // Upgrade the position vector (batch). 
        position += dataSize; 
        } 
    } 
  
 /** 
  * This method calculates the membership functions from the cluster 
  * centers. 
  */ 
  private void calculateMFsFromClusterCenters() 
    { 
    float sumTerms; 
    // For each cluster and data point 
    for(int c=0;c<numClusters;c++) 
        for(int w=0;w<dataSize;w++) 
          { 
          // Get a pixel (as a single array). 
          int index = (w)*numBands; 
          for(int b=0;b<numBands;b++) aPixel[b] = inputData[index+b]; 
          // Top is the distance of this data point to the cluster being read. 
          float top = calcDistance(aPixel,clusterCenters[c]); 
          // Bottom is the sum of distances from this data point to all clusters. 
          sumTerms = 0f; 
          for(int ck=0;ck<numClusters;ck++) 
            { 
            float thisDistance = calcDistance(aPixel,clusterCenters[ck]); 
            sumTerms += Math.pow(top/thisDistance,(2f/(fuzziness-1f))); 
            } 
          // Then the MF can be calculated as... 
          membership[w][c] = (float)(1f/sumTerms); 
          // Upgrade the position vector (batch). 
          position += (numBands+numClusters); 
          } 
    } 
    
   /** 
    * This method calculates the objective function ("j") which reflects the 
    * quality of the clustering. 
    */ 
  private double calculateObjectiveFunction() 
    { 
    double j = 0; 
    // For all data values and clusters 
      for(int w=0;w<dataSize;w++) 
        for(int c=0;c<numClusters;c++) 
          { 
          // Get the current pixel data. 
          int index = (w)*numBands; 
          for(int b=0;b<numBands;b++) 
            aPixel[b] = inputData[index+b]; 
          // Calculate the distance between a pixel and a cluster center. 
          float distancePixelToCluster = calcDistance(aPixel, clusterCenters[c]); 
          j += distancePixelToCluster*Math.pow(membership[w][c], fuzziness); 
          // Upgrade the position vector (batch). 
          position += (2*numBands); 
          } 
     return j; 
     } 
  
  /** 
   * This method calculates the Euclidean distance between two N-dimensional 
   * vectors. 
   * @param a1 the first data vector. 
   * @param a2 the second data vector. 
   * @return the Euclidean distance between those vectors. 
   */ 
   private float calcDistance(float[] a1,float[] a2) 
     { 
     float distance = 0f; 
     for(int e=0;e<a1.length;e++) distance += (a1[e]-a2[e])*(a1[e]-a2[e]); 
     // Sanity check, avoid singularities 
     if (distance == 0.0) distance = Float.MIN_VALUE; 
     return (float)Math.sqrt(distance); 
     } 
   
   /**
    * ����e��Ⱦ���
    * 
    * @return ����e��Ⱦ���
    */
   public float[][] getMembership() {
		return membership;
	}
   
   /**
    * ���ؾ�������
    * 
    * @return
    */
   public float[][] getCenterClusters() {
	
	   return clusterCenters;
}
   /** 
    * This method returns the estimated size (steps) for this task. 
    * The value is, of course, an approximation, just so we will be able to 
    * give the user a feedback on the processing time. In this case, the value 
    * is calculated as the number of loops in the run() method. 
    */ 
    public long getSize() 
      { 
      // Return the estimated size for this task: 
      return (long)maxIterations* // The maximum number of iterations times 
        ( 
             (numClusters*dataSize*(2*numBands))+ // Step 0 of method run() 
            (dataSize*numBands*numClusters)+ // Step 1 of method run()   
            (numClusters*dataSize*(numBands+numClusters))+ // Step 2 of run() 
            (numClusters*dataSize*(2*numBands))  // Step 3 of method run() 
        ); 
      } 
     
   /** 
    * This method returns a measure of the progress of the algorithm. 
    */ 
    public long getPosition() 
      { 
      return position; 
      }    
}

