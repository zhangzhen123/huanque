package com.julun.huanque.common.agora.handler;

import java.util.ArrayList;

import io.agora.rtc.IRtcEngineEventHandler;

public class AgoraEventHandler extends IRtcEngineEventHandler {
    private ArrayList<EventHandler> mHandler = new ArrayList<>();

    public void addHandler(EventHandler handler) {
        mHandler.add(handler);
    }

    public void removeHandler(EventHandler handler) {
        mHandler.remove(handler);
    }

    /**
     * 移除所有的handler,SDK release的时候使用
     */
    public void removeAllHandlers() {
        mHandler.clear();
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onJoinChannelSuccess(channel, uid, elapsed);
        }
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onLeaveChannel(stats);
        }
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        }
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onUserJoined(uid, elapsed);
        }
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        for (EventHandler handler : mHandler) {
            handler.onUserOffline(uid, reason);
        }
    }

    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onLocalVideoStats(stats);
        }
    }

    @Override
    public void onRtcStats(RtcStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRtcStats(stats);
        }
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        for (EventHandler handler : mHandler) {
            handler.onNetworkQuality(uid, txQuality, rxQuality);
        }
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteVideoStats(stats);
        }
    }

    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteAudioStats(stats);
        }
    }

    /**
     * token即将过期回调
     *
     * @param token
     */
    @Override
    public void onTokenPrivilegeWillExpire(String token) {
        for (EventHandler handler : mHandler) {
            handler.onTokenPrivilegeWillExpire(token);
        }
    }

    @Override
    public void onError(int err) {
        for (EventHandler handler : mHandler) {
            handler.onError(err);
        }
    }

    /**
     * token过期回调
     */
    @Override
    public void onRequestToken() {
        for (EventHandler handler : mHandler) {
            handler.onRequestToken();
        }

    }

    /**
     * 本地首帧已发送回调
     *
     * @param width   本地渲染视频的宽（px）
     * @param height  本地渲染视频的高（px）
     * @param elapsed 从本地用户调用 joinChannel 方法直至该回调被触发的延迟（毫秒） 如果在 joinChannel 之前调用了 startPreview，则返回的是从调用 startPreview 直至该回调被触发的延迟（毫秒）
     */
    @Override
    public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onFirstLocalVideoFrame(width, height, elapsed);
        }
    }

    @Override
    public void onLastmileQuality(int quality) {
        for (EventHandler handler : mHandler) {
            handler.onLastmileQuality(quality);
        }
    }

    @Override
    public void onLastmileProbeResult(LastmileProbeResult result) {
        for (EventHandler handler : mHandler) {
            handler.onLastmileProbeResult(result);
        }
    }

    /**
     * 网络异常回调
     * @param state
     * @param reason
     */
    @Override
    public void onConnectionStateChanged(int state, int reason) {
        for (EventHandler handler : mHandler) {
            handler.onConnectionStateChanged(state, reason);
        }
    }

    /**
     * 网络连接中断，且 SDK 无法在 10 秒内连接服务器回调。
     */
    @Override
    public void onConnectionLost(){
        for (EventHandler handler : mHandler) {
            handler.onConnectionLost();
        }
    }

    @Override
    public void onAudioRouteChanged(int routing) {
        for (EventHandler handler : mHandler) {
            handler.onAudioRouteChanged(routing);
        }
    }


}
