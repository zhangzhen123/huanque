package com.julun.huanque.common.utils.fresco;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

import androidx.annotation.ColorInt;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * @author zhangzhen
 * @data 2017/3/21
 **/
public class DraweeHolderBuilder {
    /**
     * 根据上下文 (Uri)uri 返回一个DraweeHolder
     * @param context
     * @param uri
     * @return
     */
    public static DraweeHolder createHolder(Context context, Uri uri){
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(context.getResources())
        /*.set...*/
        .build();
        DraweeHolder<GenericDraweeHierarchy> mDraweeHolder = DraweeHolder.create(hierarchy, context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .setOldController(mDraweeHolder.getController())
                .build();
        mDraweeHolder.setController(controller);
        return mDraweeHolder;
    }
    /**
     * 根据上下文 （String）uri 返回一个DraweeHolder
     * @param context
     * @param uri
     * @return
     */
    public static DraweeHolder createHolder(Context context, String uri){
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(context.getResources())
        /*.set...*/
                .setPlaceholderImage(new ColorDrawable(Color.GRAY))
                .build();
        DraweeHolder<GenericDraweeHierarchy> mDraweeHolder = DraweeHolder.create(hierarchy, context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .setOldController(mDraweeHolder.getController())
                .build();
        mDraweeHolder.setController(controller);
        return mDraweeHolder;
    }
    /**
     * 根据上下文 资源Id 返回一个DraweeHolder
     * @param context
     * @param imageResId
     * @return
     */
    public static DraweeHolder createHolder(Context context, int imageResId){
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(context.getResources())
        /*.set...*/
                .build();
        ImageRequest request = ImageRequestBuilder.newBuilderWithResourceId(imageResId)
                .setImageDecodeOptions(ImageDecodeOptions.newBuilder().setDecodePreviewFrame(true).build())
                .build();
        DraweeHolder<GenericDraweeHierarchy> mDraweeHolder = DraweeHolder.create(hierarchy, context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .setOldController(mDraweeHolder.getController())
                .build();
        mDraweeHolder.setController(controller);
        return mDraweeHolder;
    }

    /**
     * 返回一个圆形图片 如果不需要边框传0
     *
     * @param borderRedId 边框的颜色
     * @param borderWidth 边框的大小
     */
    public static DraweeHolder createCircleHolder(Context context, String uri, @ColorInt int borderRedId, float borderWidth) {
        RoundingParams roundingParams = RoundingParams.asCircle();
        if (borderRedId != 0 && borderWidth > 0f) {
            roundingParams.setBorder(borderRedId, borderWidth);
        }
        return createCircleHolder(context, uri, roundingParams);
    }

    public static DraweeHolder createCircleHolder(Context context, String uri, RoundingParams params) {

        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(context.getResources())
                .setPlaceholderImage(new ColorDrawable(Color.GRAY))
                .setRoundingParams(params)
                .build();
        DraweeHolder<GenericDraweeHierarchy> mDraweeHolder = DraweeHolder.create(hierarchy, context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .setOldController(mDraweeHolder.getController())
                .build();
        mDraweeHolder.setController(controller);
        return mDraweeHolder;
    }
}
