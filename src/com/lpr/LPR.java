package com.lpr;

import java.io.File;
import java.io.IOException;

public class LPR {
	
	public native byte[]  DetectLPR(short[] pixs, int width, int height, int maxsize);//

	static {
		System.loadLibrary("TelLPRecognition");//
	//	System.out.println(System.getProperty("java.library.path"));
	//	System.setProperty("java.library.path", ".");
	}

}







/*
 * 
 * 
static boolean readImage(String imagePath, JImageConfig jimg){
	
	BufferedImage oImage = null;
	File of = new File(imagePath);
	try{
		oImage = ImageIO.read(of);
		WritableRaster oRaster = oImage.getRaster();
		jimg.width = oRaster.getWidth();
		jimg.height = oRaster.getHeight();
		jimg.size = 3 * jimg.width * jimg.height;
		
		int pix []= new int[4];
		jimg.bits = new int[jimg.size];
		oRaster.getPixel(0,0, pix);
		oRaster.getPixels(0, 0, jimg.width, jimg.height, jimg.bits);

	} catch(IOException e){
		e.printStackTrace();
	}
	
	return true;
}

public static void main(String[] args) {
	LPR l = new LPR();
	JImageConfig jimg = new JImageConfig();
	if(readImage("D:/WALL/carlpr/2.jpg", jimg)){
		System.out.println("read done! width="+jimg.width+" height="+jimg.height);
	}
//	String str = l.DetectLPR(jimg.bits, jimg.width, jimg.height);
	//System.out.println( str );
}
*/
/*
class JImageConfig
{
	int width=0;
	int height=0;
	int size = 0;
	int bits[]=null;
}
*/