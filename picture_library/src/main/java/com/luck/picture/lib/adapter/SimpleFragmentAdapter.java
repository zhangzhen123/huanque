package com.luck.picture.lib.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.luck.picture.lib.PictureVideoPlayActivity;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.widget.bigimageview.BigImageView;
import com.luck.picture.lib.widget.bigimageview.FrescoImageViewFactory;
import com.luck.picture.lib.widget.longimage.ImageSource;
import com.luck.picture.lib.widget.longimage.ImageViewState;
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView;

import java.io.File;
import java.util.List;

/**
 * @author：luck
 * @data：2018/1/27 下午7:50
 * @描述:图片预览
 */

public class SimpleFragmentAdapter extends PagerAdapter {
    private List<LocalMedia> images;
    private Context mContext;
    private OnCallBackActivity onBackPressed;

    public interface OnCallBackActivity {
        /**
         * 关闭预览Activity
         */
        void onActivityBackPressed();
    }

    public SimpleFragmentAdapter(List<LocalMedia> images, Context context,
                                 OnCallBackActivity onBackPressed) {
        super();
        this.images = images;
        this.mContext = context;
        this.onBackPressed = onBackPressed;
    }

    @Override
    public int getCount() {
        if (images != null) {
            return images.size();
        }
        return 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        (container).removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View contentView = LayoutInflater.from(container.getContext())
                .inflate(R.layout.big_image_inner_preview, container, false);
        final BigImageView imageView = (BigImageView) contentView.findViewById(R.id.itemImage);
        imageView.setImageViewFactory(new FrescoImageViewFactory());
        ImageView iv_play = (ImageView) contentView.findViewById(R.id.iv_play);
        LocalMedia media = images.get(position);
        if (media != null) {
            final String pictureType = media.getPictureType();
            boolean eqVideo = pictureType.startsWith(PictureConfig.VIDEO);
            iv_play.setVisibility(eqVideo ? View.VISIBLE : View.GONE);
            final String path;
            boolean isGif = PictureMimeType.isGif(pictureType);
            //gif图展示使用原图
            if (isGif) {
                path = media.getPath();
            } else if (media.isCut() && !media.isCompressed()) {
                // 裁剪过
                path = media.getCutPath();
            } else if (media.isCompressed() || (media.isCut() && media.isCompressed())) {
                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                path = media.getCompressPath();
            } else {
                path = media.getPath();
            }

            boolean isHttp = PictureMimeType.isHttp(path);
            if (isHttp) {
                imageView.showImage(
                        Uri.parse(media.getCompressPath()),
                        Uri.parse(path)
                );
            } else {
                imageView.showImage(Uri.parse(path));
            }

//----------------------------------------这里是使用fresco去加载大图可自控制浏览内存占用 但是不支持缩放----------------------------------
//        final View contentView = LayoutInflater.from(container.getContext())
//                .inflate(R.layout.picture_image_inner_preview, container, false);
//        // 常规图控件
//        final SimpleDraweeView imageView = (SimpleDraweeView) contentView.findViewById(R.id.preview_image);
//        // 长图控件
//        final SubsamplingScaleImageView longImg = (SubsamplingScaleImageView) contentView.findViewById(R.id.longImg);
//
//        ImageView iv_play = (ImageView) contentView.findViewById(R.id.iv_play);
//        LocalMedia media = images.get(position);
//        if (media != null) {
//            final String pictureType = media.getPictureType();
//            boolean eqVideo = pictureType.startsWith(PictureConfig.VIDEO);
//            iv_play.setVisibility(eqVideo ? View.VISIBLE : View.GONE);
//            final String path;
//            if (media.isCut() && !media.isCompressed()) {
//                // 裁剪过
//                path = media.getCutPath();
//            } else if (media.isCompressed() || (media.isCut() && media.isCompressed())) {
//                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
//                path = media.getCompressPath();
//            } else {
//                path = media.getPath();
//            }
//            boolean isGif = PictureMimeType.isGif(pictureType);
//            final boolean eqLongImg = PictureMimeType.isLongImg(media);
//            imageView.setVisibility(eqLongImg && !isGif ? View.GONE : View.VISIBLE);
//            longImg.setVisibility(eqLongImg && !isGif ? View.VISIBLE : View.GONE);
//            if (eqLongImg) {
//                File file = new File(path);
//                displayLongPic(file, longImg);
//            } else {
//                boolean isHttp = PictureMimeType.isHttp(path);
//                if (isHttp) {
//                    FrescoImageUtils.loadRemoteImage(imageView,path,160,200,mContext);
//                } else {
//                    FrescoImageUtils.loadNativeFilePathWithPx(imageView,path,480,800);
//                }
//
//            }
 //----------------------------------------这里是使用fresco去加载大图可自控制浏览内存占用 但是不支持缩放----------------------------------
            // 压缩过的gif就不是gif了
//            if (isGif && !media.isCompressed()) {
//                RequestOptions gifOptions = new RequestOptions()
//                        .override(480, 800)
//                        .priority(Priority.HIGH)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE);
//                Glide.with(contentView.getContext())
//                        .asGif()
//                        .load(path)
//                        .apply(gifOptions)
//                        .into(imageView);
//            } else {
//                RequestOptions options = new RequestOptions()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                if(eqLongImg){
//                    Glide.with(contentView.getContext())
//                            .load(path)
//                            .downloadOnly(new SimpleTarget<File>() {
//                                @Override
//                                public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
//                                    displayLongPic(resource,longImg);
//                                }
//                            });
//
////                    longImg.setImage(ImageSource.uri("file://"+path));
//                }else{
//                    Glide.with(contentView.getContext())
//                            .asBitmap()
//                            .load(path)
//                            .apply(options)
//                            .into(new SimpleTarget<Bitmap>(480, 800) {
//                                @Override
//                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//                                        imageView.setImageBitmap(resource);
//                                }
//                            });
//                }
//
//            }
//            imageView.setOnViewTapListener(new OnViewTapListener() {
//                @Override
//                public void onViewTap(View view, float x, float y) {
//                    if (onBackPressed != null) {
//                        onBackPressed.onActivityBackPressed();
//                    }
//                }
//            });
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onBackPressed != null) {
                        onBackPressed.onActivityBackPressed();
                    }
                }
            });
            iv_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("video_path", path);
                    intent.putExtras(bundle);
                    intent.setClass(mContext, PictureVideoPlayActivity.class);
                    mContext.startActivity(intent);
                }
            });
        }
        (container).addView(contentView, 0);
        return contentView;
    }

    /**
     * 加载长图
     *
     * @param file
     * @param longImg
     */
    private void displayLongPic(File file, SubsamplingScaleImageView longImg) {
        longImg.setQuickScaleEnabled(true);
        longImg.setZoomEnabled(true);
        longImg.setPanEnabled(true);
        longImg.setDoubleTapZoomDuration(100);
        longImg.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        longImg.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        longImg.setImage(ImageSource.uri(Uri.fromFile(file)), new ImageViewState(0, new PointF(0, 0), 0));
    }
}
