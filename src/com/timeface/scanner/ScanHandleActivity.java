package com.timeface.scanner;

import java.io.File;
import java.util.HashMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.timeface.scanner.constant.OpenCVConstant;
import com.timeface.scanner.cv.ImageHandleListener;
import com.timeface.scanner.cv.OpenCVManager;
import com.timeface.scanner.view.CropImageView;
import com.timeface.scanner.view.CropImageView.PointLocation;

/**
 * 用于显示和处理扫描后的图片
 * Created by wudi on 2015/4/20.
 */
public class ScanHandleActivity extends Activity implements ImageHandleListener {

    private static final String TAG = "ScanHandleActivity";
	private static final int MSG_SCAN_FINISH = 0;
	private static final int MSG_CROP_FINISH = 1;
	
    private OpenCVManager mOpenCVManager;
    private String mContentImagePath;
    private int[] mResultImageSide;
    
	private Bitmap mScrBitmap;
	
	private HashMap<PointLocation, Point> mPointMap;
	private LinearLayout mContentLayout;
	
	private ProgressDialog mProgressDialog;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SCAN_FINISH:
				mScrBitmap = null;
		        mScrBitmap =BitmapFactory.decodeFile(mContentImagePath);
		        if (mScrBitmap != null) {
		        	CropImageView view = new CropImageView(ScanHandleActivity.this);
		        	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		        	view.setLayoutParams(params);
		        	view.setImageBitmap(mScrBitmap);
		        	view.setCropPiontMap(getPiontMap());
//		        	view.setBackgroundColor(Color.BLUE);
		        	mContentLayout.addView(view);
		        }
		        
		        dismissProgressDialog();
				break;
				
			case MSG_CROP_FINISH:
		        dismissProgressDialog();
				break;

			default:
				break;
			}
		};
	};

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                	File file = new File(mContentImagePath);
                	
                	if (file.exists()) {
                		//若该文件存在
	                    mOpenCVManager.findBrim(mContentImagePath);
	                    
	                    showProgressDialog(getString(R.string.scan_dialog_title), getString(R.string.scan_dialog_msg));
                	}
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.scan_handle, null);
        setContentView(mContentLayout);
        Intent i = getIntent();
        mContentImagePath = i.getStringExtra(OpenCVConstant.KEY_SCAN_SRC_IMG);
        mResultImageSide = i.getIntArrayExtra(OpenCVConstant.KEY_HANDLED_IMAGE_SIDE);
        final String resultImagePath = i.getStringExtra(OpenCVConstant.KEY_SCAN_RESULT_IMG);
        
        Button mCropBtn = (Button) findViewById(R.id.crop_btn);
        mCropBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File file = new File(resultImagePath);
				if (file.exists()) {
					file.delete();
				}
				mOpenCVManager.cropImage(mContentImagePath, mPointMap, mResultImageSide);
				showProgressDialog(getString(R.string.crop_dialog_title), getString(R.string.crop_dialog_msg));
			}
		});
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	mOpenCVManager = new OpenCVManager(mLoaderCallback);
        mOpenCVManager.init();
        mOpenCVManager.setImageHandleListener(this);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mScrBitmap.recycle();
    }
    
    private void showProgressDialog(CharSequence title, CharSequence message) {
    	if (mProgressDialog != null && mProgressDialog.isShowing()) {
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
    	}
    	mProgressDialog = new ProgressDialog(this);
    	mProgressDialog.setTitle(title);
    	mProgressDialog.setMessage(message);
    	mProgressDialog.show();
	}
    
    private void dismissProgressDialog() {
    	if (mProgressDialog != null && mProgressDialog.isShowing()) {
    		mProgressDialog.dismiss();
    	}
	}
    
    private HashMap<PointLocation, Point> getPiontMap() {
		if (mPointMap != null) {
			return mPointMap;
		}
		mPointMap = new HashMap<CropImageView.PointLocation, Point>();
		Point ltPoint = mOpenCVManager.getLTPoint();
		mPointMap.put(PointLocation.LT, ltPoint);
		
		Point blPoint = mOpenCVManager.getBLPoint();
		mPointMap.put(PointLocation.BL, blPoint);
		
		Point rbPoint = mOpenCVManager.getRBPiont();
		mPointMap.put(PointLocation.RB, rbPoint);
		
		Point trPoint = mOpenCVManager.getTRPoint();
		mPointMap.put(PointLocation.TR, trPoint);
		return mPointMap;
	}

	@Override
	public void onScanFinish() {
		mHandler.sendEmptyMessage(MSG_SCAN_FINISH);
	}

	@Override
	public void onCropFinish() {
		mHandler.sendEmptyMessage(MSG_CROP_FINISH);
	}

}
