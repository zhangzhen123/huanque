package com.luck.picture.lib;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.dialog.CustomDialog;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.ToastManage;
import com.luck.picture.lib.widget.PreviewViewPager;
import com.luck.picture.lib.widget.bigimageview.BigImageView;
import com.luck.picture.lib.widget.bigimageview.BigImageViewer;
import com.luck.picture.lib.widget.bigimageview.FrescoImageViewFactory;
import com.luck.picture.lib.widget.bigimageview.frescoloader.FrescoImageLoader;
import com.luck.picture.lib.widget.longimage.ImageSource;
import com.luck.picture.lib.widget.longimage.ImageViewState;
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangzhen
 * @data 2019/2/19
 * 图片浏览器 引入BigImageView 供外部调用
 **/

public class ImagePreviewActivity extends PictureBaseActivity implements View.OnClickListener {
    private ImageButton left_back;
    private TextView tv_title;
    private PreviewViewPager viewPager;
    private List<LocalMedia> images = new ArrayList<>();
    private int position = 0;
    private String directory_path;
    private SimpleFragmentAdapter adapter;
    private LayoutInflater inflater;
    private RxPermissions rxPermissions;
    private loadDataThread loadDataThread;

    private LocalMedia currentMedia;

    public LocalMedia getCurrentMedia() {
        return currentMedia;
    }
    public List<LocalMedia> getImages(){
        return images;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity_external_preview);

        RelativeLayout rl = findViewById(R.id.rl_title);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //全屏化处理
//        StatusBarUtil.setTransparent(this);
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) rl.getLayoutParams();
//        lp.setMargins(0, StatusBarUtil.getStatusBarHeight(this), 0, 0);
//        StatusBarUtil.setColor(this,Color.BLACK);

        inflater = LayoutInflater.from(this);
        tv_title = (TextView) findViewById(R.id.picture_title);
        left_back = (ImageButton) findViewById(R.id.left_back);
        viewPager = (PreviewViewPager) findViewById(R.id.preview_pager);
        position = getIntent().getIntExtra(PictureConfig.EXTRA_POSITION, 0);
        directory_path = getIntent().getStringExtra(PictureConfig.DIRECTORY_PATH);
        images = (List<LocalMedia>) getIntent().getSerializableExtra(PictureConfig.EXTRA_PREVIEW_SELECT_LIST);
        left_back.setOnClickListener(this);
        initViewPageAdapterData();
        if(position<images.size()){
            currentMedia = images.get(position);//
        }
        BigImageViewer.initialize(FrescoImageLoader.with(getApplicationContext()));
    }

    private void initViewPageAdapterData() {
        tv_title.setText(position + 1 + "/" + images.size());
        adapter = new SimpleFragmentAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tv_title.setText(position + 1 + "/" + images.size());
                currentMedia = images.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        finish();
        overridePendingTransition(0, R.anim.a3);
    }

    public class SimpleFragmentAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return images.size();
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
            View contentView = inflater.inflate(R.layout.big_image_preview, container, false);
            // 常规图控件
            final BigImageView imageView = (BigImageView) contentView.findViewById(R.id.itemImage);
            imageView.setImageViewFactory(new FrescoImageViewFactory());
            LocalMedia media = images.get(position);
            if (media != null) {
                final String path;
                final String pictureType = media.getPictureType();
                boolean isGif = PictureMimeType.isGif(pictureType);
                if(isGif){
                    path=media.getPath();
                }else if (media.isCut() && !media.isCompressed()) {
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
                           /* Uri.parse(media.getCompressPath()),*/
                            Uri.parse(path)
                    );
                } else {
                    imageView.showImage(Uri.parse(path));
                }

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        overridePendingTransition(0, R.anim.a3);
                    }
                });
//                imageView.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        if (rxPermissions == null) {
//                            rxPermissions = new RxPermissions(ImagePreviewActivity.this);
//                        }
//                        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                                .subscribe(new Observer<Boolean>() {
//                                    @Override
//                                    public void onSubscribe(Disposable d) {
//                                    }
//
//                                    @Override
//                                    public void onNext(Boolean aBoolean) {
//                                        if (aBoolean) {
//                                            showDownLoadDialog(path);
//                                        } else {
//                                            ToastManage.s(mContext, getString(R.string.picture_jurisdiction));
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable e) {
//                                    }
//
//                                    @Override
//                                    public void onComplete() {
//                                    }
//                                });
//                        return true;
//                    }
//                });
            }
            (container).addView(contentView, 0);
            return contentView;
        }
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

    /**
     * 加载长图
     *
     * @param bitmap
     * @param longImg
     */
    private void displayLongPic(Bitmap bitmap, SubsamplingScaleImageView longImg) {
        longImg.setQuickScaleEnabled(true);
        longImg.setZoomEnabled(true);
        longImg.setPanEnabled(true);
        longImg.setDoubleTapZoomDuration(100);
        longImg.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        longImg.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        longImg.setImage(ImageSource.bitmap(bitmap), new ImageViewState(0, new PointF(0, 0), 0));
    }

    /**
     * 下载图片提示
     */
    private void showDownLoadDialog(final String path) {
        final CustomDialog dialog = new CustomDialog(ImagePreviewActivity.this,
                ScreenUtils.getScreenWidth(ImagePreviewActivity.this) * 3 / 4,
                ScreenUtils.getScreenHeight(ImagePreviewActivity.this) / 4,
                R.layout.picture_wind_base_dialog_xml, R.style.Theme_dialog);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        Button btn_commit = (Button) dialog.findViewById(R.id.btn_commit);
        TextView tv_title = (TextView) dialog.findViewById(R.id.tv_title);
        TextView tv_content = (TextView) dialog.findViewById(R.id.tv_content);
        tv_title.setText(getString(R.string.picture_prompt));
        tv_content.setText(getString(R.string.picture_prompt_content));
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPleaseDialog();
                saveFile(path);
//                boolean isHttp = PictureMimeType.isHttp(path);
//                if (isHttp) {
//                    loadDataThread = new loadDataThread(path);
//                    loadDataThread.start();
//                } else {
//                    // 有可能本地图片
//                    try {
//                        String dirPath = PictureFileUtils.createDir(ImagePreviewActivity.this,
//                                System.currentTimeMillis() + ".png", directory_path);
//                        PictureFileUtils.copyFile(path, dirPath);
//                        ToastManage.s(mContext, getString(R.string.picture_save_success) + "\n" + dirPath);
//                        dismissDialog();
//                    } catch (IOException e) {
//                        ToastManage.s(mContext, getString(R.string.picture_save_error) + "\n" + e.getMessage());
//                        dismissDialog();
//                        e.printStackTrace();
//                    }
//                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void saveFile(@NonNull String path) {
        boolean isHttp = PictureMimeType.isHttp(path);
        if (isHttp) {
            loadDataThread = new loadDataThread(path);
            loadDataThread.start();
        } else {
            // 有可能本地图片
            try {
                String dirPath = PictureFileUtils.createDir(ImagePreviewActivity.this,
                        System.currentTimeMillis() + ".png", directory_path);
                PictureFileUtils.copyFile(path, dirPath);
                ToastManage.s(mContext, getString(R.string.picture_save_success) + "\n" + dirPath);
                dismissDialog();
            } catch (IOException e) {
                ToastManage.s(mContext, getString(R.string.picture_save_error) + "\n" + e.getMessage());
                dismissDialog();
                e.printStackTrace();
            }
        }
    }

    // 进度条线程
    public class loadDataThread extends Thread {
        private String path;

        public loadDataThread(String path) {
            super();
            this.path = path;
        }

        @Override
        public void run() {
            try {
                showLoadingImage(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // 下载图片保存至手机
    public void showLoadingImage(String urlPath) {
        try {
            URL u = new URL(urlPath);
            String path = PictureFileUtils.createDir(ImagePreviewActivity.this,
                    System.currentTimeMillis() + ".png", directory_path);
            byte[] buffer = new byte[1024 * 8];
            int read;
            int ava = 0;
            long start = System.currentTimeMillis();
            BufferedInputStream bin;
            bin = new BufferedInputStream(u.openStream());
            BufferedOutputStream bout = new BufferedOutputStream(
                    new FileOutputStream(path));
            while ((read = bin.read(buffer)) > -1) {
                bout.write(buffer, 0, read);
                ava += read;
                long speed = ava / (System.currentTimeMillis() - start);
            }
            bout.flush();
            bout.close();
            Message message = handler.obtainMessage();
            message.what = 200;
            message.obj = path;
            handler.sendMessage(message);
        } catch (IOException e) {
            ToastManage.s(mContext, getString(R.string.picture_save_error) + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 200:
                    String path = (String) msg.obj;
                    ToastManage.s(mContext, getString(R.string.picture_save_success) + "\n" + path);
                    dismissDialog();
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, R.anim.a3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadDataThread != null) {
            handler.removeCallbacks(loadDataThread);
            loadDataThread = null;
        }
    }
}
