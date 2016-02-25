package com.fei435;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import com.fei435.FileUtils;
import com.fei435.FileUtils.NoSdcardException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//�ļ���
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel;
import java.io.RandomAccessFile;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder.Exception;
import com.googlecode.javacv.cpp.opencv_core;
/**
 * convertFromBitmaptoVideo
 * @author yanjiaqi  qq:985202568
 * modified by feifei435
 */

public class ScreenCapture {
	private static int switcher = 0;//¼���
	private static boolean isPaused = false;//��ͣ��
	private static double RECORD_FPS = 10f;
	
	private static String video_path_name = null;
	
	
	//��ȡ��Ƶһ֡������  ע�� bitNameΪ·��+�ļ���
    public static int saveBitmapToFile(Bitmap mBitmap, String bitName){
    	FileOutputStream fOut = null;
    	Log.i("ScreenCapture", "saveBitmapToFile enter");
    	if (null == bitName || bitName.length() <= 4) {
    		return Constant.CAM_RES_FAIL_FILE_NAME_ERROR;
    	}
    	
    	File f = new File(bitName);
    	Log.i("ScreenCapture", "saveBitmapToFile, fname =" + f);
    	try {
	    	f.createNewFile();
	    	Log.i("ScreenCapture", "saveBitmapToFile, createNewFile success, f=" + f);
	    	fOut = new FileOutputStream(f);
	    	Log.i("ScreenCapture", "saveBitmapToFile, FileOutputStream success, fOut=" + fOut);
    	} catch (IOException e) {
    		Log.i("ScreenCapture", "exception, err=" + e.getMessage());
    		return Constant.CAM_RES_FAIL_FILE_WRITE_ERROR;
    	}
    	
    	mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
    	
    	try {
    		fOut.flush();
    		fOut.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    		return Constant.CAM_RES_FAIL_BITMAP_ERROR;
    	}
    	
    	return Constant.CAM_RES_OK;
    }
	

	public static void start(){
		
		video_path_name = FileUtils.generateFileName("VID_");
		switcher = 1;
		
		new Thread(){
			public void run(){
				Log.i("ScreenCapture", "ScreenCapture�߳�������");
				try {
					new FileUtils().creatSDDir(FileUtils.FILE_PATH);
				
					//TODO:��ѡ�ķ������������640 480��Ϊ�ȶ�ȡassets�е�ʾ��ͼƬ�Ŀ�ߣ�Ҳ����ȷ��¼�����
					FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
							video_path_name, 640, 480);
					Log.i("ScreenCapture", "recorder�Ѵ�����"+"width:"+recorder.getImageHeight()+"height:"+recorder.getImageHeight());
			
					recorder.setFormat("mp4");
					recorder.setFrameRate(RECORD_FPS);//¼��֡��
					recorder.start();
				
					while(switcher!=0){
						if(!isPaused){
							
							//TODO:�ж��Ƿ����һ֡�ظ�  ��֤֡��
							//��Ȼ������cvLoadImageֱ�Ӹ���·����ȡͼ�񣬵���Ϊ��ʹ��java���ļ������ֻ��⣬����Ҫ����һ��File����
							
						    if(!FileUtils.frameFileLocked) {
						        if(new FileUtils().isFileExist(FileUtils.TMP_FRAME_NAME, FileUtils.FILE_PATH)){
						        	FileUtils.frameFileLocked = true;//����
							        Log.i("filelock", "recorder:�ѽ�"+FileUtils.TMP_FRAME_NAME+"����");
							        
						        	FileUtils.frameFileLocked = true;//����
							        Log.i("filelock", "recorder:�ѽ�"+FileUtils.TMP_FRAME_NAME+"����");
						        	
							        opencv_core.IplImage image = cvLoadImage(new FileUtils().getSDCardRoot()+ FileUtils.FILE_PATH + File.separator+FileUtils.TMP_FRAME_NAME);
									Log.i("ScreenCapture", "recorder���ڽ�֡"+System.currentTimeMillis()+"���浽MP4�ļ�");
									recorder.record(image);
									
									//�����ļ�
							        FileUtils.frameFileLocked = false;
							        Log.i("filelock", "recorder:�ѽ�"+FileUtils.TMP_FRAME_NAME+"����");
							        
							        try {//¼��һ֡��Ϣһ��
										sleep(200);
										Log.i("filelock", "recorder:sleep some time");
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							        
						        } else {
						        	Log.i("ScreenCapture", "�ȴ�tmpframe.jpg");
						        	try {
										sleep(200);
										Log.i("filelock", "recorder:sleep some time");
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
						    } else {
						        //MjpegView�߳�����дjpg��������
						    	Log.i("ScreenCapture", "MjpegView�߳�����д"+FileUtils.TMP_FRAME_NAME+",��������ͼ��");
						    	try {
									sleep(200);
									Log.i("filelock", "recorder:sleep some time");
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
						    }
						}
					}
					recorder.stop();
					
					Log.i("ScreenCapture", "recorder��ֹͣ");
				}catch(FileUtils.NoSdcardException e){
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	//����ֵΪ��Ƶ·��
	public static String stop(){
		switcher = 0;
		isPaused = false;
		return video_path_name;
	}
	public static void pause(){
		if(switcher==1){
			isPaused = true;
		}
	}
	public static void restart(){
		if(switcher==1){
			isPaused = false;
		}
	}
	public static boolean isStarted(){
		if(switcher==1){
			return true;
		}else{
			return false;
		}
	}
	public static boolean isPaused(){
		return isPaused;
	}
		
	private static Bitmap getImageFromFile(String filename){
		Bitmap image = null;
		try{
			image = BitmapFactory.decodeFile(
					new FileUtils().getSDCardRoot() + 
					FileUtils.FILE_PATH + File.separator + filename
					);
		}catch (NoSdcardException e) {
			e.printStackTrace();
		}
		return image;
	}
}

