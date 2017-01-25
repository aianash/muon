package com.shoplane.muon.common.utils.userinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * Created by ravmon on 30/8/15.
 */
public class BlurEffectUtil {
    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 7.5f;

    public static Bitmap blur(View view) {
        return blur(view.getContext(), getScreenshot(view));
    }

    public static Bitmap blur(Context ctx, Bitmap bitmap) {
        int bitmapBlurWidth = Math.round(bitmap.getWidth() * BITMAP_SCALE);
        int bitmapBlurHeight = Math.round(bitmap.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, bitmapBlurWidth,
                bitmapBlurHeight, false);
        Bitmap outputBlurBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript renderScript = RenderScript.create(ctx);
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        Allocation allocationInput = Allocation.createFromBitmap(renderScript, inputBitmap);
        Allocation allocationOutput = Allocation.createFromBitmap(renderScript,
                outputBlurBitmap);
        scriptIntrinsicBlur.setRadius(BLUR_RADIUS);
        scriptIntrinsicBlur.setInput(allocationInput);
        scriptIntrinsicBlur.forEach(allocationOutput);
        allocationOutput.copyTo(outputBlurBitmap);

        return outputBlurBitmap;
    }

    private static Bitmap getScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
