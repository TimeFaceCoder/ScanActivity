package com.timeface.scanner;

import java.io.File;
import java.util.HashMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.timeface.scanner.cv.OpenCVManager;
import com.timeface.scanner.view.CropImageView;
import com.timeface.scanner.view.CropImageView.PointLocation;

/**
 * 用于显示和处理扫描后的图片
 * Created by wudi on 2015/4/20.
 */
public class ScanHandleActivity extends Activity {

    private static final String TAG = "ScanHandleActivity";
    private OpenCVManager mOpenCVManager;
//    private Bitmap mResultBitmap;
    private String mContentImagePath;
	private Bitmap mScrBitmap;
	
	private HashMap<PointLocation, Point> mPointMap;
	private LinearLayout mContentLayout;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                	File file = new File(mContentImagePath);
                	
                	if (file.exists()) {
	                    mOpenCVManager.findBrim(mContentImagePath);
	                    
	                    mScrBitmap = null;
	        			//若该文件存在
	                    
	                    mScrBitmap =BitmapFactory.decodeFile(mContentImagePath);
	                    if (mScrBitmap != null) {
//	                    	ImageView imgView = (ImageView) findViewById(R.id.content);
//	                    	imgView.setImageDrawable(getResources().getDrawable(R.drawable.shape_grad_black_transp_70));
//	                    	imgView.setX(mOpenCVManager.getL());
//	                    	imgView.setY(mOpenCVManager.getT());
//	                    	LayoutParams params = new LayoutParams(mOpenCVManager.getR() - mOpenCVManager.getL(), mOpenCVManager.getB() - mOpenCVManager.getT());
//							imgView.setLayoutParams(params);
//							imgView.setBackgroundColor(Color.RED);
	                    	
//	                    	ImageView rImageView = (ImageView) findViewById(R.id.content_result);
//	                    	rImageView.setImageBitmap(mScrBitmap);
	                    	CropImageView view = new CropImageView(ScanHandleActivity.this);
	                    	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	                    	view.setLayoutParams(params);
	                    	view.setImageBitmap(mScrBitmap);
	                    	view.setCropPiontMap(getPiontMap());
	                    	view.setBackgroundColor(Color.BLUE);
	                    	mContentLayout.addView(view);
	                    }
                	}
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

		private HashMap<PointLocation, Point> getPiontMap() {
			if (mPointMap != null) {
				return mPointMap;
			}
			mPointMap = new HashMap<CropImageView.PointLocation, Point>();
			Point ltPoint = mOpenCVManager.getLTPoint();
//			if (ltPoint == null) {
//				Point ltPoint = new Point(0, 0);
//			}
			mPointMap.put(PointLocation.LT, ltPoint);
			
			Point blPoint = mOpenCVManager.getBLPoint();
//			if (blPoint == null) {
//				Point blPoint = new Point(0, 1000);
//			}
			mPointMap.put(PointLocation.BL, blPoint);
			
			Point rbPoint = mOpenCVManager.getRBPiont();
//			if (rbPoint == null) {
//				Point rbPoint = new Point(1000, 1000);
//			}
			mPointMap.put(PointLocation.RB, rbPoint);
			
			Point trPoint = mOpenCVManager.getTRPoint();
//			if (trPoint == null) {
//				Point trPoint = new Point(1000, 0);
//			}
			mPointMap.put(PointLocation.TR, trPoint);
			return mPointMap;
		}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.scan_handle, null);
        setContentView(mContentLayout);
        Intent i = getIntent();
        mContentImagePath = i.getStringExtra(ScanActivity.KEY_SCAN_RESULT_IMG);
        
        Button mCropBtn = (Button) findViewById(R.id.crop_btn);
        mCropBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File file = new File("/sdcard/doc3.jpg");
				if (file.exists()) {
					file.delete();
				}
				mOpenCVManager.cropImage(mContentImagePath, mPointMap);
			}
		});
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	mOpenCVManager = new OpenCVManager(mLoaderCallback);
        mOpenCVManager.init();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mScrBitmap.recycle();
    }

}
