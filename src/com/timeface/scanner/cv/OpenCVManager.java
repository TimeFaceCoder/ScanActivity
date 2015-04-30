package com.timeface.scanner.cv;

import java.util.HashMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.timeface.scanner.view.CropImageView.PointLocation;

import android.graphics.Point;
import android.util.Log;

/**
 * Created by Administrator on 2015/4/21.
 */
public class OpenCVManager {
    
    static {
//    	System.loadLibrary("OpenCVEngine");
//    	System.loadLibrary("OpenCVEngine_jni");
//    	System.loadLibrary("opencv_info");
    	System.loadLibrary("opencv_java");
    	System.loadLibrary("timefacescanner_native");
//    	System.load("data/data/com.timeface.scanner/lib/libtimefacescanner_native.so");
//        System.load("data/data/com.timeface.scanner/lib/libopencv_info.so");
//        System.load("data/data/com.timeface.scanner/lib/libopencv_java.so");
    }

    private static final String TAG = "OpenCVManager";
    private BaseLoaderCallback mLoaderCallback;
	private boolean isInit = false;
	private int[][] mPointArray;

    public OpenCVManager (BaseLoaderCallback loaderCallback) {
        mLoaderCallback = loaderCallback;
    }

    public void init() {
        if(!isInit && OpenCVLoader.initDebug()){
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            isInit = true;
        }
    }

    public void findBrim(String path) {
    	Log.d("OpenCVManager", "path = " + path);
    	mPointArray = nScan(path);
    }
    
    public Point getLTPoint() {
    	if (mPointArray == null) {
    		return null;
    	}
    	int[] array = mPointArray[0];
    	if (array == null) {
    		return null;
    	}
    	return new Point(array[0], array[1]);
	}
    
    public Point getTRPoint() {
    	if (mPointArray == null) {
    		return null;
    	}
    	int[] array = mPointArray[1];
    	if (array == null) {
    		return null;
    	}
    	return new Point(array[0], array[1]);
	}
    
    public Point getRBPiont() {
    	if (mPointArray == null) {
    		return null;
    	}
    	int[] array = mPointArray[2];
    	if (array == null) {
    		return null;
    	}
    	return new Point(array[0], array[1]);
	}
    
    public Point getBLPoint() {
    	if (mPointArray == null) {
    		return null;
    	}
    	int[] array = mPointArray[3];
    	if (array == null) {
    		return null;
    	}
    	return new Point(array[0], array[1]);
	}
    
    public void cropImage(String path, HashMap<PointLocation,Point> pointMap){
    	if (pointMap == null || pointMap.size() < 4) {
    		return;
    	}
    	int[][] pointArray = new int[4][2];
    	pointArray[0][0] = pointMap.get(PointLocation.LT).x;
    	pointArray[0][1] = pointMap.get(PointLocation.LT).y;
    	pointArray[1][0] = pointMap.get(PointLocation.TR).x;
    	pointArray[1][1] = pointMap.get(PointLocation.TR).y;
    	pointArray[2][0] = pointMap.get(PointLocation.RB).x;
    	pointArray[2][1] = pointMap.get(PointLocation.RB).y;
    	pointArray[3][0] = pointMap.get(PointLocation.BL).x;
    	pointArray[3][1] = pointMap.get(PointLocation.BL).y;
		nCrop(path, pointArray);
    }
    
    private static native int[][] nScan(String path);
    private static native void nCrop(String path, int[][] pointArray);
}
