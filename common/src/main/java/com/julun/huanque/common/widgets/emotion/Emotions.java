package com.julun.huanque.common.widgets.emotion;

import android.text.TextUtils;

import com.julun.huanque.common.R;

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

    public static Map<String, Integer> EMOTIONS = new LinkedHashMap<>();

    static {
        EMOTIONS.put("[爱你]", R.drawable.aini);
        EMOTIONS.put("[鄙视]", R.drawable.bishi);
        EMOTIONS.put("[不屑]", R.drawable.buxie);
        EMOTIONS.put("[白眼]", R.drawable.baiyan);
        EMOTIONS.put("[大哭]", R.drawable.daku);
        EMOTIONS.put("[尴尬]", R.drawable.ganga);
        EMOTIONS.put("[无语]", R.drawable.wuyu);
        EMOTIONS.put("[调皮]", R.drawable.tiaopi);
        EMOTIONS.put("[搞怪]", R.drawable.gaoguai);
        EMOTIONS.put("[害怕]", R.drawable.haipa);
        EMOTIONS.put("[酷]", R.drawable.ku);
        EMOTIONS.put("[灵感]", R.drawable.linggan);
        EMOTIONS.put("[滑稽]", R.drawable.huaji);
        EMOTIONS.put("[可爱]", R.drawable.keai);
        EMOTIONS.put("[哭了]", R.drawable.kule);
        EMOTIONS.put("[鼻血]", R.drawable.bixue);
        EMOTIONS.put("[骂人]", R.drawable.maren);
        EMOTIONS.put("[蔑视]", R.drawable.mieshi);
        EMOTIONS.put("[偷笑]", R.drawable.touxiao);
        EMOTIONS.put("[倒了]", R.drawable.daole);
        EMOTIONS.put("[投降]", R.drawable.touxiang);
        EMOTIONS.put("[委屈]", R.drawable.weiqu);
        EMOTIONS.put("[随便]", R.drawable.suibian);
        EMOTIONS.put("[笑哭]", R.drawable.xiaoku);
        EMOTIONS.put("[笑喷]", R.drawable.xiaopen);
        EMOTIONS.put("[眨眼]", R.drawable.zhayan);
        EMOTIONS.put("[真棒]", R.drawable.zhenbang);
    }

    private static String emotionCode2String(int code) {
        return new String(Character.toChars(code));
    }

    public static int getDrawableResByName(String emotionName) {
        if (!TextUtils.isEmpty(emotionName) && EMOTIONS.containsKey(emotionName)) {
            return EMOTIONS.get(emotionName);
        }
        return -1;
    }

    public static List<Emotion> getEmotions() {
        List<Emotion> emotions = new ArrayList<>();
        Iterator<Map.Entry<String, Integer>> entries = EMOTIONS.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Integer> entry = entries.next();
            emotions.add(new Emotion(entry.getKey(), entry.getValue()));
        }
        return emotions;
    }
}
