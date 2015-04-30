package com.timeface.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
    public static final String KEY_SCAN_RESULT_IMG = "KEY_SCAN_RESULT_IMG";
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
//        	if (saveImage(imgPath, data)) {
//        	saveImage(imgPath, data);
        		Intent intent = new Intent();
                intent.putExtra(KEY_SCAN_RESULT_IMG, imgPath);
                intent.setClass(this, ScanHandleActivity.class);
                startActivity(intent);
//        	} else {
//        		showToast();
//        	}
        }
    }

	private boolean saveImage(String path, Intent data) {
		File file = new File(path);// 创建文件
		Uri imgUri = data.getData();
		if (!file.exists() && imgUri != null) {
			ContentResolver resolver = getContentResolver();
			FileOutputStream fo = null;
			try {
				// 使用ContentProvider通过URI获取原始图片
				Bitmap photo = MediaStore.Images.Media.getBitmap(resolver,
						imgUri);

				if (photo != null) {
					// 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
					// Bitmap smallBitmap = ImageTools.zoomBitmap(photo,
					// photo.getWidth() / SCALE, photo.getHeight() / SCALE);
					// 释放原始图片占用的内存，防止out of memory异常发生

					file.createNewFile();
					fo = new FileOutputStream(file);
					photo.compress(Bitmap.CompressFormat.JPEG, 100, fo);
					fo.flush();
					// while (read != -1) {
					// fo.write(read);
					// read = in.read();
					// }

					photo.recycle();
					return true;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fo != null) {
						fo.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;

	}

	private void showToast() {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, R.string.scan, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
