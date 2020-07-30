package com.julun.huanque.common.widgets.blurview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.julun.huanque.common.helper.StringHelper;

public class FrescoGaussianBlurDraweeView extends SimpleDraweeView {

    public FrescoGaussianBlurDraweeView (Context context) {
        super (context);
    }

    public FrescoGaussianBlurDraweeView (Context context, AttributeSet attrs) {
        super (context, attrs);
    }

    public FrescoGaussianBlurDraweeView (Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
    }

    //加载图片带有毛玻璃效果
    public void loadImageUrlWithBlur (String url) {
        if (StringHelper.INSTANCE.isEmpty (url)) {
            return;
        }
        /*
        DraweeController controller = Fresco.newDraweeControllerBuilder ()
                .setImageRequest (ImageRequestBuilder.newBuilderWithSource (Uri.parse (url))
//                    .setPostprocessor (FrescoProcessor.getInstance ().blurPostprocessor).build ())
                    .setPostprocessor (blurPostprocessor) )
                .build ();
        */
        DraweeController controller = Fresco.newDraweeControllerBuilder ()
                .setImageRequest (ImageRequestBuilder.newBuilderWithSource (Uri.parse (url))
                        .setPostprocessor (blurPostprocessor)
                        .build ())
                .build ();
        this.setController (controller);
    }
    /**
     * 使用Postprocessor实现毛玻璃图片效果
     */
    public static Postprocessor blurPostprocessor = new GaussianBlurProcessor();
}