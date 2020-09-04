package com.julun.huanque.common.widgets.emotion;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.julun.huanque.common.helper.DensityHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yummyLau on 18-7-11
 * Email: yummyl.lau@gmail.com
 * blog: yummylau.com
 */
public class EmojiSpanBuilder {

    public static Pattern sPatternEmotion = Pattern.compile(
            "\\[([\u4e00-\u9fa5\\w])+\\]|[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]");

    /**
     * 是否显示大表情
     * @return
     */
    public static boolean allEmoji(Context context, String text) {
        Matcher matcherEmotion = sPatternEmotion.matcher(text);
        int keyLengh = 0;

        int count = 0;
        while (matcherEmotion.find()) {
            count++;
            String key = matcherEmotion.group();
            keyLengh += key.length();
        }
        if (count <= 3 && keyLengh == text.length()) {
            return true;
        } else {
            return false;
        }
    }

    public static Spannable buildEmotionSpannable(Context context, String text) {
        return buildEmotionSpannable(context, text, false);
    }

    /**
     *
     * @param context
     * @param big 显示大表情
     * @return
     */
    public static Spannable buildEmotionSpannable(Context context, String text, Boolean big) {
        int border = DensityHelper.dp2px(20f);
        if (big) {
            border = DensityHelper.dp2px(44f);
        }
        String showContent = text;
        if (big) {
            // 都是表情，在表情之间增加一个空格符(增加间距)
            StringBuilder sBuilder = new StringBuilder();
            Matcher matcherEmotion = sPatternEmotion.matcher(text);
            while (matcherEmotion.find()) {
                sBuilder.append(matcherEmotion.group()).append(" ");
            }
            showContent = sBuilder.deleteCharAt(sBuilder.length() - 1).toString();
        }
        Matcher matcherEmotion = sPatternEmotion.matcher(showContent);
        SpannableString spannableString = new SpannableString(showContent);
        while (matcherEmotion.find()) {
            String key = matcherEmotion.group();
            int imgRes = Emotions.getDrawableResByName(key);
            if (imgRes != -1) {
                int start = matcherEmotion.start();
                Drawable drawable = ContextCompat.getDrawable(context, imgRes);
                drawable.setBounds(0, 0, border, border);
                CenterImageSpan span = new CenterImageSpan(drawable);
                spannableString.setSpan(span, start, start + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spannableString;
    }

    /**
     * 获取特权表情的地址
     * @param context
     * @param text
     * @return
     */
    public static int getPrivilegeResource(Context context, String text) {
        Matcher matcherEmotion = sPatternEmotion.matcher(text);
        if (matcherEmotion.find()) {
            String key = matcherEmotion.group();
            int imgRes = Emotions.getDrawableResByName(key);
            return imgRes;
        }
        return 0;
    }
}
