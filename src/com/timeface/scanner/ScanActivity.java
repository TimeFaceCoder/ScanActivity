package com.timeface.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.timeface.scanner.constant.OpenCVConstant;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ScanActivity extends Activity {
    private static final int CODE_SCAN_RESULT = 1;
    private static final String SDCARD_PATH = File.separator + "sdcard" + File.separator;
    private static final String SCAN_IMAGE_PATH = "timeface_img";
    private static final String TAG = "ScanActivity";
    private static final String IMG_PATH = SDCARD_PATH + SCAN_IMAGE_PATH;
        
    private String mImgName = System.currentTimeMillis()+".jpg";
    private Toast mToast;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        Button btn = (Button) findViewById(R.id.scan_btn);
        btn.setOnClickListener(new View.OnClickListener() {

			@Override
            public void onClick(View v) {
				File file = new File(IMG_PATH);
                if (!file.exists()) {
                	file.mkdir();
                }
                String imgPath = IMG_PATH + File.separator + mImgName;
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imgPath)));
                startActivityForResult(intent, CODE_SCAN_RESULT);
            }
        });
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mImgName = System.currentTimeMillis()+".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            showToast();
        } else if (requestCode == CODE_SCAN_RESULT) {
        	String imgPath = IMG_PATH + File.separator + mImgName;
    		Intent intent = new Intent();
            intent.putExtra(OpenCVConstant.KEY_SCAN_SRC_IMG, imgPath);
            intent.putExtra(OpenCVConstant.KEY_HANDLED_IMAGE_SIDE, new int[] {100, 200});
            intent.putExtra(OpenCVConstant.KEY_SCAN_RESULT_IMG, OpenCVConstant.RESULT_IMG_PATH);
            intent.setClass(this, ScanHandleActivity.class);
            startActivity(intent);
        }
    }

	private void showToast() {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
