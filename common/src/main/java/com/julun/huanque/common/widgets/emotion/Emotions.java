package com.julun.huanque.common.widgets.emotion;

import android.text.TextUtils;

import com.julun.huanque.common.R;
import com.julun.huanque.common.constant.EmojiType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 编码参考 http://www.oicqzone.com/tool/emoji/
 * Created by yummyLau on 18-7-11
 * Email: yummyl.lau@gmail.com
 * blog: yummylau.com
 */
public class Emotions {

    // 普通表情
    public static Map<String, Integer> NORMAL_EMOTIONS = new LinkedHashMap<>();
    // 特权表情
    public static Map<String, Integer> PRIVILEGE_EMOTIONS = new LinkedHashMap<>();
    // 动画表情
    public static Map<String, Integer> ANIMATION_EMOTIONS = new LinkedHashMap<>();

    static {
        // 加载普通表情
        NORMAL_EMOTIONS.put("[爱你]", R.drawable.aini);
        NORMAL_EMOTIONS.put("[鄙视]", R.drawable.bishi);
        NORMAL_EMOTIONS.put("[不屑]", R.drawable.buxie);
        NORMAL_EMOTIONS.put("[白眼]", R.drawable.baiyan);
        NORMAL_EMOTIONS.put("[大哭]", R.drawable.daku);
        NORMAL_EMOTIONS.put("[尴尬]", R.drawable.ganga);
        NORMAL_EMOTIONS.put("[无语]", R.drawable.wuyu);
        NORMAL_EMOTIONS.put("[调皮]", R.drawable.tiaopi);
        NORMAL_EMOTIONS.put("[搞怪]", R.drawable.gaoguai);
        NORMAL_EMOTIONS.put("[害怕]", R.drawable.haipa);
        NORMAL_EMOTIONS.put("[酷]", R.drawable.ku);
        NORMAL_EMOTIONS.put("[灵感]", R.drawable.linggan);
        NORMAL_EMOTIONS.put("[滑稽]", R.drawable.huaji);
        NORMAL_EMOTIONS.put("[可爱]", R.drawable.keai);
        NORMAL_EMOTIONS.put("[哭了]", R.drawable.kule);
        NORMAL_EMOTIONS.put("[鼻血]", R.drawable.bixue);
        NORMAL_EMOTIONS.put("[骂人]", R.drawable.maren);
        NORMAL_EMOTIONS.put("[蔑视]", R.drawable.mieshi);
        NORMAL_EMOTIONS.put("[偷笑]", R.drawable.touxiao);
        NORMAL_EMOTIONS.put("[倒了]", R.drawable.daole);
        NORMAL_EMOTIONS.put("[投降]", R.drawable.touxiang);
        NORMAL_EMOTIONS.put("[委屈]", R.drawable.weiqu);
        NORMAL_EMOTIONS.put("[随便]", R.drawable.suibian);
        NORMAL_EMOTIONS.put("[笑哭]", R.drawable.xiaoku);
        NORMAL_EMOTIONS.put("[笑喷]", R.drawable.xiaopen);
        NORMAL_EMOTIONS.put("[眨眼]", R.drawable.zhayan);
        NORMAL_EMOTIONS.put("[真棒]", R.drawable.zhenbang);

        NORMAL_EMOTIONS.put("[我不看]", R.drawable.wobukan);
        NORMAL_EMOTIONS.put("[我不听]", R.drawable.wobuting);
        NORMAL_EMOTIONS.put("[色迷迷]", R.drawable.semimi);
        NORMAL_EMOTIONS.put("[气呼呼]", R.drawable.qihuhu);
        NORMAL_EMOTIONS.put("[惊讶]", R.drawable.jingya);
        NORMAL_EMOTIONS.put("[困了]", R.drawable.kunle);

        // 加载特权表情
        PRIVILEGE_EMOTIONS.put("[约吗]", R.drawable.expression_privilege_yuema);
        PRIVILEGE_EMOTIONS.put("[等撩]", R.drawable.expression_privilege_dengliao);
        PRIVILEGE_EMOTIONS.put("[送花]", R.drawable.expression_privilege_songhua);
        PRIVILEGE_EMOTIONS.put("[么么哒]", R.drawable.expression_privilege_memeda);
        PRIVILEGE_EMOTIONS.put("[色眯眯]", R.drawable.expression_privilege_semimi);
        PRIVILEGE_EMOTIONS.put("[晚安]", R.drawable.expression_privilege_wanan);
        PRIVILEGE_EMOTIONS.put("[乞讨]", R.drawable.expression_privilege_qitao);
        PRIVILEGE_EMOTIONS.put("[耍酷]", R.drawable.expression_privilege_shuaku);
        PRIVILEGE_EMOTIONS.put("[在吗]", R.drawable.expression_privilege_zaima);
        PRIVILEGE_EMOTIONS.put("[太难了]", R.drawable.expression_privilege_tainanle);
        PRIVILEGE_EMOTIONS.put("[生气]", R.drawable.expression_privilege_shengqi);
        PRIVILEGE_EMOTIONS.put("[委屈]", R.drawable.expression_privilege_weiqv);
        PRIVILEGE_EMOTIONS.put("[哈哈哈]", R.drawable.expression_privilege_hahaha);
        PRIVILEGE_EMOTIONS.put("[约架]", R.drawable.expression_privilege_yuejia);
        PRIVILEGE_EMOTIONS.put("[咬你]", R.drawable.expression_privilege_yaoni);
        PRIVILEGE_EMOTIONS.put("[挑衅]", R.drawable.expression_privilege_tiaoxin);

        // 加载动画表情
        ANIMATION_EMOTIONS.put("[猜拳]", R.drawable.icon_caiquan);
        ANIMATION_EMOTIONS.put("[骰子]", R.drawable.icon_shaizi);
    }

    private static String emotionCode2String(int code) {
        return new String(Character.toChars(code));
    }

    public static int getNormalResByName(String emotionName) {
        if (!TextUtils.isEmpty(emotionName) && NORMAL_EMOTIONS.containsKey(emotionName)) {
            return NORMAL_EMOTIONS.get(emotionName);
        }
        return -1;
    }

    public static int getDrawableResByName(String emotionName) {
        if (!TextUtils.isEmpty(emotionName) && NORMAL_EMOTIONS.containsKey(emotionName)) {
            return NORMAL_EMOTIONS.get(emotionName);
        }

        if (!TextUtils.isEmpty(emotionName) && PRIVILEGE_EMOTIONS.containsKey(emotionName)) {
            return PRIVILEGE_EMOTIONS.get(emotionName);
        }

        if (!TextUtils.isEmpty(emotionName) && ANIMATION_EMOTIONS.containsKey(emotionName)) {
            return ANIMATION_EMOTIONS.get(emotionName);
        }
        return -1;
    }

    public static List<Emotion> getEmotions(String type) {
        List<Emotion> emotions = new ArrayList<>();
        Iterator<Map.Entry<String, Integer>> entries = null;
        switch (type) {
        case EmojiType.NORMAL:
            entries = NORMAL_EMOTIONS.entrySet().iterator();
            break;
        case EmojiType.PREROGATIVE:
            entries = PRIVILEGE_EMOTIONS.entrySet().iterator();
            break;
        case EmojiType.ANIMATION:
            entries = ANIMATION_EMOTIONS.entrySet().iterator();
            break;
        default:
            break;
        }

        if (entries != null) {
            while (entries.hasNext()) {
                Map.Entry<String, Integer> entry = entries.next();
                emotions.add(new Emotion(entry.getKey(), entry.getValue()));
            }
        }

        return emotions;
    }
}
