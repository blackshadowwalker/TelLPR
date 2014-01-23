package com.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class ImageDispose {
	private static String TAG = "ImageDispose";
	private static WindowManager mWinManager = null;
	private static Context  context = null;
	static Bitmap mBitmap=null;

	public ImageDispose(){}

	public static void init(Context c, WindowManager win){
		context = c;
		mWinManager = win;
	}
	/*
	 * 旋转图片
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
		//旋转图片 动作
		if(angle==0)
			return bitmap;
		if(bitmap==null)
			return null;
		Matrix matrix = new Matrix();;
		matrix.postRotate(angle);
		System.out.println("angle2=" + angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree  = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/*
	 * 1. 处理大图片，缩小到屏幕尺寸
	 * 2. 选择图片，正常显示
	 */
	public static Bitmap ImageAdjusted(Uri uri){
		return ImageAdjusted(getFilePathFromContentUri(uri, context.getContentResolver()));
	}
	/*
	 * 1. 处理大图片，缩小到屏幕尺寸
	 * 2. 选择图片，正常显示
	 */
	public static Bitmap ImageAdjusted(String path){

		Display currentDisplay = mWinManager.getDefaultDisplay();
		int dw = currentDisplay.getWidth();  
		int dh = currentDisplay.getHeight();  
		Log.i(TAG, "Display: "+dw + " x " + dh);
		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();  
		bmpFactoryOptions.inJustDecodeBounds = true;  
		//	mBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null,  bmpFactoryOptions); 
		mBitmap = BitmapFactory.decodeFile(path, bmpFactoryOptions);
		int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)dh);  
		int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)dw);  

		Log.v("HEIGHRATIO", ""+heightRatio);  
		Log.v("WIDTHRATIO", ""+widthRatio);  

		bmpFactoryOptions.inJustDecodeBounds = false;  
		if(bmpFactoryOptions==null )
			Log.e(TAG, "bmpFactoryOptions==NULL");
		if (heightRatio > 1 && widthRatio > 1)  
		{  
			bmpFactoryOptions.inSampleSize =  heightRatio > widthRatio ? heightRatio:widthRatio;  
		}  
		Log.d(TAG, "bmpFactoryOptions.inSampleSize  = " + bmpFactoryOptions.inSampleSize);
	//	if(mBitmap.isRecycled())  mBitmap.recycle();
	//	mBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null,  bmpFactoryOptions); 
		mBitmap = BitmapFactory.decodeFile(path, bmpFactoryOptions);
		int degree = readPictureDegree(path);
		Log.d(TAG, "degree = " + degree);
		Bitmap b =  rotaingImageView(degree ,mBitmap);
		if(mBitmap.isRecycled()) mBitmap.recycle();
		return b;  
	}


	public static String savePicToSdcard(Bitmap bitmap, String path,  String fileName) 
	{  
		String filePath = "";  
		if (bitmap == null) {  
			return filePath;  
		} else {  

			filePath=path+ fileName;  
			File destFile = new File(filePath);  
			OutputStream os = null;  
			try {  
				os = new FileOutputStream(destFile);  
				bitmap.compress(CompressFormat.JPEG, 100, os);  
				os.flush();  
				os.close();  
			} catch (IOException e) {  
				filePath = "";  
			}  
		}  
		return filePath;  
	}

	public static String getFilePathFromContentUri(Uri uri,
			ContentResolver contentResolver) {
		String filePath;
		String[] filePathColumn = {MediaColumns.DATA};

		Cursor cursor = contentResolver.query(uri, filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		filePath = cursor.getString(columnIndex);
		cursor.close();
		return filePath;
	}

}
