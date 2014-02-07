package com.lpr;

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.util.Log;

import com.base.S;

public class LPR {

	private static final String TAG = "com.lpr.LPR" ;  
	private static LPR lpr =null;
	
	private short[] pixs = null;
	private int[] pix = null;
	private int imageWidth=0;
	private int imageHeight=0;
	private int imageSize =0;

	public native static byte[]  DetectLPR(short[] pixs, int width, int height, int maxsize);//

	static {
		System.loadLibrary("TelLPRecognition");//
		//	System.out.println(System.getProperty("java.library.path"));
		//	System.setProperty("java.library.path", ".");
	}
	
	private LPR(){
//		if(pix == null) pix = new int[S.MIN_IMAGE_SIZE];
	}
	
	public static LPR getInstance(){
		if(lpr==null)
			lpr = new LPR();
		return lpr;
	}
	

	public String StartDetectLPRFromBmp(Bitmap rawBitmap){

		if(rawBitmap==null){
			return "";
		}
		int rawHeight = rawBitmap.getHeight(); 
		int rawWidth = rawBitmap.getWidth(); 
		int size = rawHeight * rawWidth;

		if(pix == null || size > this.imageSize){
			Log.d(TAG, "Image's size("+size+") > pre image size"+this.imageSize);
			pix = null;
			pixs = null;
			pix = new int[size];
			pixs = new short[size*3];
			this.imageHeight = rawHeight;
			this.imageWidth = rawWidth;
			this.imageSize = size;
		}

		Log.d(TAG, "get pixes !");
		rawBitmap.getPixels( pix, 0, rawWidth, 0, 0, rawWidth, rawHeight); 
		if(rawBitmap.isRecycled()==false)
			rawBitmap.recycle();
		
		for(int i=0,j=0; i<size; i++)
		{
			pixs[j++] = (short) ((pix[i] & 0x00FF0000) >> 16);
			pixs[j++] = (short) ((pix[i] & 0x0000FF00) >> 8);
			pixs[j++] = (short) ((pix[i] & 0x000000FF) >> 0);
		}

		String str = null;
		try{
			Log.d(TAG, "call  DetectLPR ");
			byte test[] = DetectLPR(pixs, rawWidth, rawHeight, S.MAX_BYTE);
			//	byte test[]={(byte)0xd4,(byte) 0xa5,(byte)0x41,(byte)0x42};
			Log.d(TAG, "callback of  DetectLPR ");
			str = new String(test,"GB2312");
			str = str.trim();
			test = null;
		}catch(Exception e){
			Log.e(TAG, "Detect LPR error!");
			e.printStackTrace();
		}
		return str;
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