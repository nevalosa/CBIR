/**
 * 
 */
package com.tnove.algorthms;

/**
 * @class JNIBase
 * TODO
 * @version 1.0 
 * @author	Nevalosa Deng
 * @mail   	NevalosaD@hz.webex.com
 * @date 	2011-9-2
 */
public class JNIBase {
	
	public JNIBase(){}
	
	public JNIBase(String libraryName){
		loadLibrary(libraryName);
	}
	
	private static void loadLibrary(String libraryName){
		System.loadLibrary(libraryName);
	}
 
} 


