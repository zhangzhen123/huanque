package com.julun.huanque.common.widgets.emotion;


import androidx.annotation.DrawableRes;

import java.io.Serializable;

/**
 * Created by yummyLau on 18-7-11
 * Email: yummyl.lau@gmail.com
 * blog: yummylau.com
 */
public class Emotion implements Serializable {
    
    public String text;

    @DrawableRes
    public int drawableRes;

    public Emotion(String text, @DrawableRes int drawableRes) {
        this.text = text;
        this.drawableRes = drawableRes;
    }
}
