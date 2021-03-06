/*
 * Copyright 2015 Cesar Diez Sanchez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timeface.scanner.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;


/**
 * @author cesards
 */
public class CropImageView extends ImageView {
	private static String TAG = "CropImageView";
	
	public enum PointLocation {
		/**
		 * 左边上的点
		 */
		LT,
//		LC,
		
		/**
		 * 上边上的点
		 */
		TR,
//		TC,
		
		/**
		 * 右边上的点
		 */
		RB,
//		RC,
		
		/**
		 * 下边上的点
		 */
		BL;
//		BC;		
	}
	
	private HashMap<PointLocation, Point> mPointMap;
	
	private float mRatio = -1;

	private Paint mPaint;

	private int mViewWidth;

	private int mViewHight;

	private HashMap<PointLocation, Rect> mAreaMap;

	private PointLocation mUsingKey;

	private float mOffset;

	private float mLastonTouchMoveEventX = -1;

	private float mLastonTouchMoveEventY = -1;

	public CropImageView(Context context) {
		super(context);
		mPaint = new Paint();
		
		mPaint.setStyle(Style.STROKE);//设置非填充
		mPaint.setStrokeWidth(2);//笔宽2像素
		mPaint.setColor(Color.RED);//设置为红笔
		mPaint.setAntiAlias(true);//锯齿不显示
		
		mAreaMap = new HashMap<PointLocation, Rect>();
	}
	
	public void setCropPiontMap(HashMap<PointLocation, Point> map) {
		mPointMap = map;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Drawable d = getDrawable();
		mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
		mViewHight = MeasureSpec.getSize(heightMeasureSpec);
		mRatio = mViewWidth / ((float) d.getIntrinsicWidth());
		
		mOffset = 0;
		if (d != null) {
			float aspectRatio = d.getIntrinsicHeight() / ((float) d.getIntrinsicWidth());
			float imageHight = mViewWidth * aspectRatio;
			mOffset = (mViewHight - imageHight) / 2;
		}
		
		calculatePoints();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		canvas.drawLine(0, 0, 1000, 1000, mPaint);
		drawLine(canvas, getPointXFromMap(PointLocation.LT), getPointYFromMap(PointLocation.LT) + mOffset, getPointXFromMap(PointLocation.TR), getPointYFromMap(PointLocation.TR) + mOffset);
		drawLine(canvas, getPointXFromMap(PointLocation.TR), getPointYFromMap(PointLocation.TR) + mOffset, getPointXFromMap(PointLocation.RB), getPointYFromMap(PointLocation.RB) + mOffset);
		drawLine(canvas, getPointXFromMap(PointLocation.RB), getPointYFromMap(PointLocation.RB) + mOffset, getPointXFromMap(PointLocation.BL), getPointYFromMap(PointLocation.BL) + mOffset);
		drawLine(canvas, getPointXFromMap(PointLocation.BL), getPointYFromMap(PointLocation.BL) + mOffset, getPointXFromMap(PointLocation.LT), getPointYFromMap(PointLocation.LT) + mOffset);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		Log.d("12345", "getAction = " + event.getAction());
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mUsingKey = null;
			Set<Entry<PointLocation, Rect>> rectSet = mAreaMap.entrySet();
			Iterator<Entry<PointLocation, Rect>> it=rectSet.iterator();
			while(it.hasNext()) {
				Map.Entry entry=(Map.Entry)it.next();
				Rect rect = (Rect) entry.getValue();
				if (rect.contains((int) event.getX(), (int) event.getY())) {
					mUsingKey = (PointLocation) entry.getKey();
					break;
				}
			}
			mLastonTouchMoveEventX = event.getX();
			mLastonTouchMoveEventY = event.getY();
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mUsingKey = null;
			mLastonTouchMoveEventX = -1;
			mLastonTouchMoveEventY = -1;
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (mLastonTouchMoveEventX != -1 && mLastonTouchMoveEventY != -1) {
				double moveLen = Math.sqrt(Math.pow(event.getX() - mLastonTouchMoveEventX, 2) + Math.pow(event.getY() - mLastonTouchMoveEventY, 2));
				if (moveLen > 3) {
					updataPoint(event.getX(), event.getY());
					calculateRect();
					invalidate();
				}
			}
			break;

		default:
			break;
		}
		
		
		return true;
	}
	
	public void release () {
		mPaint.reset();
		mAreaMap.clear();
		mLastonTouchMoveEventX = -1;
		mLastonTouchMoveEventY = -1;
		mOffset = 0;
		mPointMap.clear();
		mUsingKey = null;
		mRatio = 0;
	}
	
	private void updataPoint(float x, float y) {
		Point point = mPointMap.get(mUsingKey);
		if (point == null) {
			return;
		}
		point.x = (int) (point.x + (x - mLastonTouchMoveEventX) / mRatio + 0.5);
		point.y = (int) (point.y + (y - mLastonTouchMoveEventY) / mRatio + 0.5);
		mLastonTouchMoveEventX = x;
		mLastonTouchMoveEventY = y;
		Log.d(TAG, "onTouchEvent change " + point);
	}

	private void drawLine(Canvas canvas, float startX, float startY, float stopX, float stopY) {
		if (canvas == null) {
			return;
		}
		canvas.drawLine(startX, startY, stopX, stopY, mPaint);
	}

	private float getPointXFromMap(PointLocation pl) {
		if (mPointMap == null || mPointMap.isEmpty() || mRatio == 0) {
			return 0;
		}
		Point point = mPointMap.get(pl);
		return point.x * mRatio;
	}
	
	private float getPointYFromMap(PointLocation pl) {
		if (mPointMap == null || mPointMap.isEmpty() || mRatio == 0) {
			return 0;
		}
		Point piont = mPointMap.get(pl);
		
		return piont.y * mRatio;
	}
	
	private float getXFromPoint(Point point) {
		return point.x * mRatio;
	}
	
	private float getYFromPoint(Point point) {
		return point.y * mRatio + mOffset;
	}
	
	private void calculatePoints() {
		if (mPointMap == null || mPointMap.isEmpty()) {
			return;
		}
		
		calculateRect();
	}
	
	private void calculateRect() {
		Point ltPoint = mPointMap.get(PointLocation.LT);
		Point trPoint = mPointMap.get(PointLocation.TR);
		Point rbPoint = mPointMap.get(PointLocation.RB);
		Point blPoint = mPointMap.get(PointLocation.BL);
		
		mAreaMap.put(PointLocation.LT, getRectFromPoint(ltPoint));
		
		mAreaMap.put(PointLocation.TR, getRectFromPoint(trPoint));
		
		mAreaMap.put(PointLocation.RB, getRectFromPoint(rbPoint));
		
		mAreaMap.put(PointLocation.BL, getRectFromPoint(blPoint));
		
	}

	private Rect getRectFromPoint(Point point) {
		float minLen = dip2px(getContext(), 40f);
		int left = (int) (getXFromPoint(point) - minLen + 0.5);
		if (left < 0) {
			left = 0;
		}
		int right = (int) (getXFromPoint(point) + minLen + 0.5);
		if (right > mViewWidth) {
			right = mViewWidth;
		}
		int top = (int) (getYFromPoint(point) - minLen + 0.5);
		if (top < 0) {
			top = 0;
		}
		int bottom = (int) (getYFromPoint(point) + minLen + 0.5);
		if (bottom > mViewHight) {
			bottom = mViewHight;
		}
		
		Rect rect = new Rect(left, top, right, bottom);
		return rect;
	}
	
	private static float dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return dipValue * scale;
	}

}
