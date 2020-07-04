package com.luck.picture.lib.tools;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * @author zhangzhen
 * @data 2019/2/19
 **/
public class FrescoImageUtils {

    public static void loadNativeFilePath(@NonNull SimpleDraweeView imgView, @NonNull String filePath, float width, float height, Context ctx) {
        loadNativeFilePathWithPx(imgView, filePath, ScreenUtils.dip2px(ctx,width), ScreenUtils.dip2px(ctx,height));
    }

    public static void loadNativeFilePathWithPx(@NonNull SimpleDraweeView imgView, @NonNull String filePath, int width, int height) {
        if (!TextUtils.isEmpty(filePath)) {
            Uri uri = Uri.parse("file://" + filePath);
            ImageRequestBuilder build = ImageRequestBuilder.newBuilderWithSource(uri);

            if (width > 0 && height > 0) {
                build.setResizeOptions(new ResizeOptions(width, height));
            }
            ImageRequest request = build.build();
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder().
                    setImageRequest(request).setAutoPlayAnimations(true).
                    setOldController(imgView.getController()).build();
            imgView.setController(controller);
        }
    }

    public static void loadImageLocal(@NonNull SimpleDraweeView imgView, int placeHolderResId, Context ctx) {
        imgView.setImageURI("res://" + ctx.getPackageName() + '/' + placeHolderResId);
    }

    public static void loadRemoteImageInpx(@NonNull SimpleDraweeView imgView, @NonNull Uri url, int width, int height, Context ctx) {
//        Uri uri = Uri.parse(url);
        ImageRequestBuilder build = ImageRequestBuilder.newBuilderWithSource(url);

        if (width > 0 && height > 0) {
            build.setResizeOptions(new ResizeOptions(ScreenUtils.dip2px(ctx,width), ScreenUtils.dip2px(ctx,height)));
        }
        ImageRequest request = build.build();
        AbstractDraweeController controller = Fresco.newDraweeControllerBuilder().setImageRequest(request).
                setAutoPlayAnimations(true).
                setOldController(imgView.getController()).build();
        imgView.setController(controller);
    }
}
