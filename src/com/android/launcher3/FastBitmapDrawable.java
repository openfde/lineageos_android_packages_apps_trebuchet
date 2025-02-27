/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import static com.android.launcher3.anim.Interpolators.ACCEL;
import static com.android.launcher3.anim.Interpolators.DEACCEL;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Property;

import com.android.launcher3.graphics.PlaceHolderIconDrawable;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.model.data.ItemInfoWithIcon;
import com.android.launcher3.util.Themes;
import com.android.launcher3.R;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.android.launcher3.util.FileUtils;
import com.android.launcher3.LauncherSettings;
import java.io.File;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.BitmapDrawable;

public class FastBitmapDrawable extends Drawable {

    private static final float PRESSED_SCALE = 1.1f;

    private static final float DISABLED_DESATURATION = 1f;
    private static final float DISABLED_BRIGHTNESS = 0.5f;

    public static final int CLICK_FEEDBACK_DURATION = 200;

    private static ColorFilter sDisabledFColorFilter;

    protected final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    protected Bitmap mBitmap;
    protected final int mIconColor;

    private boolean mIsPressed;
    private boolean mIsDisabled;
    private float mDisabledAlpha = 1f;

    // Animator and properties for the fast bitmap drawable's scale
    private static final Property<FastBitmapDrawable, Float> SCALE
            = new Property<FastBitmapDrawable, Float>(Float.TYPE, "scale") {
        @Override
        public Float get(FastBitmapDrawable fastBitmapDrawable) {
            return fastBitmapDrawable.mScale;
        }

        @Override
        public void set(FastBitmapDrawable fastBitmapDrawable, Float value) {
            fastBitmapDrawable.mScale = value;
            fastBitmapDrawable.invalidateSelf();
        }
    };
    private ObjectAnimator mScaleAnimation;
    private float mScale = 1;

    private int mAlpha = 255;

    public FastBitmapDrawable(Bitmap b) {
        this(b, Color.TRANSPARENT);
    }

    public FastBitmapDrawable(BitmapInfo info) {
        this(info.icon, info.color);
    }

    protected FastBitmapDrawable(Bitmap b, int iconColor) {
        this(b, iconColor, false);
    }

    protected FastBitmapDrawable(Bitmap b, int iconColor, boolean isDisabled) {
        mBitmap = b;
        mIconColor = iconColor;
        setFilterBitmap(true);
        setIsDisabled(isDisabled);
    }

    @Override
    public final void draw(Canvas canvas) {
        if (mScale != 1f) {
            int count = canvas.save();
            Rect bounds = getBounds();
            canvas.scale(mScale, mScale, bounds.exactCenterX(), bounds.exactCenterY());
            drawInternal(canvas, bounds);
            canvas.restoreToCount(count);
        } else {
            drawInternal(canvas, getBounds());
        }
    }

    protected void drawInternal(Canvas canvas, Rect bounds) {
        if(mBitmap != null){
            canvas.drawBitmap(mBitmap, null, bounds, mPaint);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // No op
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            mPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setFilterBitmap(boolean filterBitmap) {
        mPaint.setFilterBitmap(filterBitmap);
        mPaint.setAntiAlias(filterBitmap);
    }

    public int getAlpha() {
        return mAlpha;
    }

    public void setScale(float scale) {
        if (mScaleAnimation != null) {
            mScaleAnimation.cancel();
            mScaleAnimation = null;
        }
        mScale = scale;
        invalidateSelf();
    }

    public float getAnimatedScale() {
        return mScaleAnimation == null ? 1 : mScale;
    }

    public float getScale() {
        return mScale;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmap.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmap.getHeight();
    }

    @Override
    public int getMinimumWidth() {
        return getBounds().width();
    }

    @Override
    public int getMinimumHeight() {
        return getBounds().height();
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public ColorFilter getColorFilter() {
        return mPaint.getColorFilter();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean isPressed = false;
        for (int s : state) {
            if (s == android.R.attr.state_pressed) {
                isPressed = true;
                break;
            }
        }
        if (mIsPressed != isPressed) {
            mIsPressed = isPressed;

            if (mScaleAnimation != null) {
                mScaleAnimation.cancel();
                mScaleAnimation = null;
            }

            if (mIsPressed) {
                // Animate when going to pressed state
                mScaleAnimation = ObjectAnimator.ofFloat(this, SCALE, PRESSED_SCALE);
                mScaleAnimation.setDuration(CLICK_FEEDBACK_DURATION);
                mScaleAnimation.setInterpolator(ACCEL);
                mScaleAnimation.start();
            } else {
                if (isVisible()) {
                    mScaleAnimation = ObjectAnimator.ofFloat(this, SCALE, 1f);
                    mScaleAnimation.setDuration(CLICK_FEEDBACK_DURATION);
                    mScaleAnimation.setInterpolator(DEACCEL);
                    mScaleAnimation.start();
                } else {
                    mScale = 1f;
                    invalidateSelf();
                }
            }
            return true;
        }
        return false;
    }

    public void setIsDisabled(boolean isDisabled) {
        if (mIsDisabled != isDisabled) {
            mIsDisabled = isDisabled;
            updateFilter();
        }
    }

    protected boolean isDisabled() {
        return mIsDisabled;
    }

    private ColorFilter getDisabledColorFilter() {
        if (sDisabledFColorFilter == null) {
            ColorMatrix tempBrightnessMatrix = new ColorMatrix();
            ColorMatrix tempFilterMatrix = new ColorMatrix();

            tempFilterMatrix.setSaturation(1f - DISABLED_DESATURATION);
            float scale = 1 - DISABLED_BRIGHTNESS;
            int brightnessI =   (int) (255 * DISABLED_BRIGHTNESS);
            float[] mat = tempBrightnessMatrix.getArray();
            mat[0] = scale;
            mat[6] = scale;
            mat[12] = scale;
            mat[4] = brightnessI;
            mat[9] = brightnessI;
            mat[14] = brightnessI;
            mat[18] = mDisabledAlpha;
            tempFilterMatrix.preConcat(tempBrightnessMatrix);
            sDisabledFColorFilter = new ColorMatrixColorFilter(tempFilterMatrix);
        }
        return sDisabledFColorFilter;
    }

    /**
     * Updates the paint to reflect the current brightness and saturation.
     */
    protected void updateFilter() {
        mPaint.setColorFilter(mIsDisabled ? getDisabledColorFilter() : null);
        invalidateSelf();
    }

    @Override
    public ConstantState getConstantState() {
        return new MyConstantState(mBitmap, mIconColor, mIsDisabled);
    }

    protected static class MyConstantState extends ConstantState {
        protected final Bitmap mBitmap;
        protected final int mIconColor;
        protected final boolean mIsDisabled;

        public MyConstantState(Bitmap bitmap, int color, boolean isDisabled) {
            mBitmap = bitmap;
            mIconColor = color;
            mIsDisabled = isDisabled;
        }

        @Override
        public FastBitmapDrawable newDrawable() {
            return new FastBitmapDrawable(mBitmap, mIconColor, mIsDisabled);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }

    /**
     * Interface to be implemented by custom {@link BitmapInfo} to handle drawable construction
     */
    public interface Factory {

        /**
         * Called to create a new drawable
         */
        FastBitmapDrawable newDrawable();
    }

    /**
     * Returns a FastBitmapDrawable with the icon.
     */
    public static FastBitmapDrawable newIcon(Context context, ItemInfoWithIcon info) {
        FastBitmapDrawable drawable = newIcon(context, info.bitmap);
       
        if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DIRECTORY){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_dir);
            BitmapInfo bitmapInfo = new BitmapInfo(bitmap,0);
            drawable = newIcon(context, bitmapInfo);
        }else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DOCUMENT ){
            String fileName = info.title.toString() ;
            int resId =  R.mipmap.icon_doc;
            String fileType = FileUtils.getFileTyle(fileName);
            if(fileType !=null){
                 if(fileType.contains("png") || fileType.contains("jpg")){
                    resId =  R.mipmap.icon_pic;
                 }else if(fileType.contains("txt") || fileType.contains("md") || fileType.contains("xml")  || fileType.contains("java")  || fileType.contains("htm") || fileType.contains("json")  ){
                    resId =  R.mipmap.icon_doc;
                 } else{
                    resId =  R.mipmap.icon_unkown;
                 } 
            }else{
                resId =  R.mipmap.icon_unkown;
            }
            // Log.i("bella"," newIcon  fileType : "+fileType  + " , info "+info );
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resId);
            BitmapInfo bitmapInfo = new BitmapInfo(bitmap,R.color.default_shadow_color_no_alpha);
            drawable = newIcon(context, bitmapInfo);
        }else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_LINUX_APP){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_linux);
            String title = info.title.toString();
            Map<String,Object> map = FileUtils.getLinuxDesktopFileContent(info.title.toString());

            if(map !=null && !map.isEmpty()){
                // String icon = map.get("icon").toString();
               try{
                String name = map.get("name").toString().replaceAll(" ", "_");
                String exec = map.get("exec").toString().replaceAll(" %F", "").replaceAll(" %u", "").replaceAll(" %U", "").replaceAll(" ", "");
                int lastIndex = exec.lastIndexOf('/');
                String key = name ;
                if(FileUtils.containsChinese(name)){
                    if(lastIndex > 0){
                        key = exec.substring(lastIndex+1);
                     }
                }
                String IconPath = FileUtils.getSystemProperty(key ,"-1");

                Log.i("bella","FastBitmapDrawable name : "+name  + " , IconPath "+IconPath + ",key "+key );
        
                if("-1".equals(IconPath) ){

                }else{
                    String icon = "/volumes"+"/"+FileUtils.getLinuxUUID() + IconPath;
                    File f = new File(icon);
                    Log.i("bella","FastBitmapDrawable exists : "+f.exists());
                    if(IconPath.contains(".svg") ){
                        bitmap = FileUtils.svgToBitmap(FileUtils.loadSvgFromAssets(context,icon));
                    }else{
                        bitmap = BitmapFactory.decodeFile(icon); 
                    }    
                }
               }catch(Exception e){
                  e.printStackTrace();
               }
            }
            Bitmap b2 = FileUtils.vectorToBitmap(context, R.mipmap.bg_linux);
            b2  = FileUtils.scaleBitmap(b2,80,80);
            if(bitmap != null ){
                bitmap  = FileUtils.scaleBitmap(bitmap,48,48);
                Bitmap b = FileUtils.overlayBitmaps(b2,bitmap);
                BitmapInfo bi = new BitmapInfo(b,0);
                drawable = newIcon(context, bi);
            }else{
                bitmap  = b2;
                BitmapInfo bi = new BitmapInfo(bitmap,0);
                drawable = newIcon(context, bi);
            }
        }

        // FastBitmapDrawable drawable = newIcon(context, info.bitmap);
        drawable.setIsDisabled(info.isDisabled());
        return drawable;
    }

    /**
     * Creates a drawable for the provided BitmapInfo
     */
    public static FastBitmapDrawable newIcon(Context context, BitmapInfo info) {
        final FastBitmapDrawable drawable;
        if (info instanceof Factory) {
            drawable = ((Factory) info).newDrawable();
        } else if (info.isLowRes()) {
            drawable = new PlaceHolderIconDrawable(info, context);
        } else {
            drawable = new FastBitmapDrawable(info);
        }
        drawable.mDisabledAlpha = Themes.getFloat(context, R.attr.disabledIconAlpha, 1f);
        return drawable;
    }
}
