package com.julun.jpushlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;


import com.julun.huanque.common.utils.ToastUtils;
import com.julun.huanque.common.utils.ULog;

import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;

/**
 * 处理tagalias相关的逻辑
 */
public class TagAliasOperatorHelper {
    private static final String TAG = "JPushHelper";
    private Logger logger= ULog.Companion.getLogger(TAG);
    public static int sequence = 1;
    /**
     * 增加
     */
    public static final int ACTION_ADD = 1;
    /**
     * 覆盖
     */
    public static final int ACTION_SET = 2;
    /**
     * 删除部分
     */
    public static final int ACTION_DELETE = 3;
    /**
     * 删除所有
     */
    public static final int ACTION_CLEAN = 4;
    /**
     * 查询
     */
    public static final int ACTION_GET = 5;

    public static final int ACTION_CHECK = 6;

    public static final int DELAY_SEND_ACTION = 1;

    public static final int DELAY_SET_MOBILE_NUMBER_ACTION = 2;

    private Context context;

    private static TagAliasOperatorHelper mInstance;

    private TagAliasOperatorHelper() {
    }

    public static TagAliasOperatorHelper getInstance() {
        if (mInstance == null) {
            synchronized (TagAliasOperatorHelper.class) {
                if (mInstance == null) {
                    mInstance = new TagAliasOperatorHelper();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        if (context != null) {
            this.context = context.getApplicationContext();
        }
    }

    private SparseArray<Object> setActionCache = new SparseArray<Object>();

    public Object get(int sequence) {
        return setActionCache.get(sequence);
    }

    public Object remove(int sequence) {
        return setActionCache.get(sequence);
    }

    public void put(int sequence, Object tagAliasBean) {
        setActionCache.put(sequence, tagAliasBean);
    }

    @SuppressLint("HandlerLeak")
    private Handler delaySendHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DELAY_SEND_ACTION:
                    if (msg.obj != null && msg.obj instanceof TagAliasBean) {
                        logger.info( "on delay time");
//                        sequence++;
                        TagAliasBean tagAliasBean = (TagAliasBean) msg.obj;
                        setActionCache.put(sequence, tagAliasBean);
                        if (context != null) {
                            handleAction(tagAliasBean);
                        } else {
                            logger.info("#unexcepted - context was null");
                        }
                    } else {
                        logger.info("#unexcepted - msg obj was incorrect");
                    }
                    break;
                case DELAY_SET_MOBILE_NUMBER_ACTION:
                    if (msg.obj != null && msg.obj instanceof String) {
                        logger.info( "retry set mobile number");
//                        sequence++;
                        String mobileNumber = (String) msg.obj;
                        setActionCache.put(sequence, mobileNumber);
                        if (context != null) {
                            handleAction(mobileNumber);
                        } else {
                            logger.info("#unexcepted - context was null");
                        }
                    } else {
                        logger.info("#unexcepted - msg obj was incorrect");
                    }
                    break;
            }
        }
    };

    public void handleAction(String mobileNumber) {
        sequence++;
        put(sequence, mobileNumber);
        logger.info( "sequence:" + sequence + ",mobileNumber:" + mobileNumber);
        JPushInterface.setMobileNumber(context, sequence, mobileNumber);
    }

    /**
     * 处理设置tag
     */
    public void handleAction(TagAliasBean tagAliasBean) {
        if (tagAliasBean == null) {
            return;
        }
        sequence++;
        put(sequence, tagAliasBean);
        if (tagAliasBean.isAliasAction) {
            switch (tagAliasBean.action) {
                case ACTION_GET:
                    JPushInterface.getAlias(context, sequence);
                    break;
                case ACTION_DELETE:
                    JPushInterface.deleteAlias(context, sequence);
                    break;
                case ACTION_SET:
                    JPushInterface.setAlias(context, sequence, tagAliasBean.alias);
                    break;
                default:
                    logger.info("unsupport alias action type");
                    return;
            }
        } else {
            switch (tagAliasBean.action) {
                case ACTION_ADD:
                    JPushInterface.addTags(context, sequence, tagAliasBean.tags);
                    break;
                case ACTION_SET:
                    JPushInterface.setTags(context, sequence, tagAliasBean.tags);
                    break;
                case ACTION_DELETE:
                    JPushInterface.deleteTags(context, sequence, tagAliasBean.tags);
                    break;
                case ACTION_CHECK:
                    //一次只能check一个tag
                    String tag = (String) tagAliasBean.tags.toArray()[0];
                    JPushInterface.checkTagBindState(context, sequence, tag);
                    break;
                case ACTION_GET:
                    JPushInterface.getAllTags(context, sequence);
                    break;
                case ACTION_CLEAN:
                    JPushInterface.cleanTags(context, sequence);
                    break;
                default:
                    logger.info( "unsupport tag action type");
                    return;
            }
        }
    }

    private boolean RetryActionIfNeeded(int errorCode, TagAliasBean tagAliasBean) {
        if (!JPushUtil.INSTANCE.isConnected(context)) {
            logger.info( "no network");
            return false;
        }
        //返回的错误码为6002 超时,6014 服务器繁忙,都建议延迟重试
        if (errorCode == 6002 || errorCode == 6014) {
            if (tagAliasBean != null) {
                Message message = new Message();
                message.what = DELAY_SEND_ACTION;
                message.obj = tagAliasBean;
                delaySendHandler.sendMessageDelayed(message, 1000 * 60);
                String logs = getRetryStr(tagAliasBean.isAliasAction, tagAliasBean.action, errorCode);
//                ExampleUtil.showToast(logs, context);
                return true;
            }
        }
        return false;
    }

    private boolean RetrySetMObileNumberActionIfNeeded(int errorCode, String mobileNumber) {
        if (!JPushUtil.INSTANCE.isConnected(context)) {
            logger.info( "no network");
            return false;
        }
        //返回的错误码为6002 超时,6024 服务器内部错误,建议稍后重试
        if (errorCode == 6002 || errorCode == 6024) {
            Message message = new Message();
            message.what = DELAY_SET_MOBILE_NUMBER_ACTION;
            message.obj = mobileNumber;
            delaySendHandler.sendMessageDelayed(message, 1000 * 60);
            String str = "Failed to set mobile number due to %s. Try again after 60s.";
            str = String.format(Locale.ENGLISH, str, (errorCode == 6002 ? "timeout" : "server internal error”"));
//            ExampleUtil.showToast(str, context);
            return true;
        }
        return false;

    }

    private String getRetryStr(boolean isAliasAction, int actionType, int errorCode) {
        String str = "Failed to %s %s due to %s. Try again after 60s.";
        str = String.format(Locale.ENGLISH, str, getActionStr(actionType), (isAliasAction ? "alias" : " tags"), (errorCode == 6002 ? "timeout" : "server too busy"));
        return str;
    }

    private String getActionStr(int actionType) {
        switch (actionType) {
            case ACTION_ADD:
                return "add";
            case ACTION_SET:
                return "set";
            case ACTION_DELETE:
                return "delete";
            case ACTION_GET:
                return "get";
            case ACTION_CLEAN:
                return "clean";
            case ACTION_CHECK:
                return "check";
        }
        return "unkonw operation";
    }

    void onTagOperatorResult(JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        logger.info( "action - onTagOperatorResult, sequence:" + sequence + ",tags:" + jPushMessage.getTags());
        //根据sequence从之前操作缓存中获取缓存记录
        TagAliasBean tagAliasBean = (TagAliasBean) setActionCache.get(sequence);
        if (tagAliasBean == null) {
            return;
        }
        if (jPushMessage.getErrorCode() == 0) {
            setActionCache.remove(sequence);
            String logs = "tag Success,sequence:" + sequence+getActionStr(tagAliasBean.action) + " tags success";
            logger.info( logs);
        } else {
            String logs = "Failed to " + getActionStr(tagAliasBean.action) + " tags";
            if (jPushMessage.getErrorCode() == 6018) {
                //tag数量超过限制,需要先清除一部分再add
                logs += ", tags is exceed limit need to clean";
            }
            logs += ", errorCode:" + jPushMessage.getErrorCode();
            logger.info(logs);
            if (!RetryActionIfNeeded(jPushMessage.getErrorCode(), tagAliasBean)) {
            }
        }
    }

    void onCheckTagOperatorResult(JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        logger.info("action - onCheckTagOperatorResult, sequence:" + sequence + ",checktag:" + jPushMessage.getCheckTag());
        //根据sequence从之前操作缓存中获取缓存记录
        TagAliasBean tagAliasBean = (TagAliasBean) setActionCache.get(sequence);
        if (tagAliasBean == null) {
            ToastUtils.INSTANCE.show("获取缓存记录失败");
            return;
        }
        if (jPushMessage.getErrorCode() == 0) {
            setActionCache.remove(sequence);
        } else {
            String logs = "Failed to " + getActionStr(tagAliasBean.action) + " tags, errorCode:" + jPushMessage.getErrorCode();
            logger.info( logs);
            if (!RetryActionIfNeeded(jPushMessage.getErrorCode(), tagAliasBean)) {
            }
        }
    }

    void onAliasOperatorResult(JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        logger.info("action - onAliasOperatorResult, sequence:" + sequence + ",alias:" + jPushMessage.getAlias());
        //根据sequence从之前操作缓存中获取缓存记录
        TagAliasBean tagAliasBean = (TagAliasBean) setActionCache.get(sequence);
        if (tagAliasBean == null) {
            ToastUtils.INSTANCE.show("获取缓存记录失败");
            return;
        }
        if (jPushMessage.getErrorCode() == 0) {
            setActionCache.remove(sequence);
            String logs = "modify alias Success,sequence:" + sequence+getActionStr(tagAliasBean.action);

            logger.info(logs);
        } else {
            String logs = "Failed to " + getActionStr(tagAliasBean.action) + " alias, errorCode:" + jPushMessage.getErrorCode();
            logger.info(logs);
            if (!RetryActionIfNeeded(jPushMessage.getErrorCode(), tagAliasBean)) {
            }
        }
    }

    //设置手机号码回调
    void onMobileNumberOperatorResult(JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        logger.info("action - onMobileNumberOperatorResult, sequence:" + sequence + ",mobileNumber:" + jPushMessage.getMobileNumber());
        if (jPushMessage.getErrorCode() == 0) {
            logger.info("action - set mobile number Success,sequence:" + sequence);
            setActionCache.remove(sequence);
        } else {
            String logs = "Failed to set mobile number, errorCode:" + jPushMessage.getErrorCode();
            logger.info(logs);
            if (!RetrySetMObileNumberActionIfNeeded(jPushMessage.getErrorCode(), jPushMessage.getMobileNumber())) {
                ToastUtils.INSTANCE.show(logs);
            }
        }
    }

    public static class TagAliasBean {
        public int action;
        public Set<String> tags;
        public String alias;
        public boolean isAliasAction;

        @Override
        public String toString() {
            return "TagAliasBean{" +
                    "action=" + action +
                    ", tags=" + tags +
                    ", alias='" + alias + '\'' +
                    ", isAliasAction=" + isAliasAction +
                    '}';
        }
    }


}
