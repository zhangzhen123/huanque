package com.luck.picture.lib;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.EventEntity;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.rxbus2.RxBus;
import com.luck.picture.lib.tools.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

public class PictureVideoPlayActivity extends PictureBaseActivity implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, View.OnClickListener {

    public static String VIDEO_MEDIA = "video_media";
    public static String IS_CHOOSE="is_choose";//是不是正在选择的界面 不是的话就没必要传数据
    private boolean is_choose=false;
    private String video_path = "";
    private LocalMedia cur_video;
    private ImageView picture_left_back;
    private MediaController mMediaController;
    private VideoView mVideoView;
    private ImageView iv_play;
    private TextView video_tv_ok;
    private int mPositionWhenPaused = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_activity_video_play);

        //全屏化处理
        StatusBarUtil.setTransparent(this);
        RelativeLayout rl = findViewById(R.id.rl_title);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) rl.getLayoutParams();
        lp.setMargins(0, StatusBarUtil.getStatusBarHeight(this), 0, 0);

        cur_video = getIntent().getParcelableExtra(VIDEO_MEDIA);
        is_choose=getIntent().getBooleanExtra(IS_CHOOSE,false);
        video_path = cur_video.getPath();
        picture_left_back = (ImageView) findViewById(R.id.left_back);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setBackgroundColor(Color.BLACK);
        iv_play = (ImageView) findViewById(R.id.iv_play);
        video_tv_ok = (TextView) findViewById(R.id.video_tv_ok);
        mMediaController = new MediaController(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setMediaController(mMediaController);
        picture_left_back.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        video_tv_ok.setOnClickListener(this);
    }


    @Override
    public void onStart() {
        // Play Video
        mVideoView.setVideoPath(video_path);
        mVideoView.start();
        super.onStart();
    }

    @Override
    public void onPause() {
        // Stop video when the activity is pause.
        mPositionWhenPaused = mVideoView.getCurrentPosition();
        mVideoView.stopPlayback();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMediaController = null;
        mVideoView = null;
        iv_play = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        // Resume video player
        if (mPositionWhenPaused >= 0) {
            mVideoView.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }

        super.onResume();
    }

    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (null != iv_play) {
            iv_play.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.left_back) {
            finish();
        } else if (id == R.id.iv_play) {
            mVideoView.start();
            iv_play.setVisibility(View.INVISIBLE);
        } else if (id == R.id.video_tv_ok) {
            if(is_choose){
                ArrayList<LocalMedia> list = new ArrayList<LocalMedia>();
                list.add(cur_video);
                onResult(list);
            }else{
                onBackPressed();
            }
        }
    }

    @Override
    public void onResult(List<LocalMedia> images) {
        RxBus.getDefault().post(new EventEntity(PictureConfig.PREVIEW_DATA_FLAG, images));
        // 如果开启了压缩，先不关闭此页面，PictureImageGridActivity压缩完在通知关闭
        onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {
            @Override
            public Object getSystemService(String name) {
                if (Context.AUDIO_SERVICE.equals(name)) {
                    return getApplicationContext().getSystemService(name);
                }
                return super.getSystemService(name);
            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // video started
                    mVideoView.setBackgroundColor(Color.TRANSPARENT);
                    return true;
                }
                return false;
            }
        });
    }
}
