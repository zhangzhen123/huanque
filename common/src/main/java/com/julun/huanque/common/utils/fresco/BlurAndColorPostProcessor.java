/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.julun.huanque.common.utils.fresco;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.internal.Preconditions;
import com.facebook.imagepipeline.nativecode.NativeBlurFilter;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.julun.huanque.common.utils.bitmap.BitmapUtil;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * A fast and memory-efficient post processor performing an iterative box blur. For details see
 * {@link NativeBlurFilter#iterativeBoxBlur(Bitmap, int, int)}.
 */
public class BlurAndColorPostProcessor extends BasePostprocessor {

    private static final int DEFAULT_ITERATIONS = 3;
    private static final int COLORS[] = {Color.BLACK};
    private final int mIterations;
    private final int mBlurRadius;
    private final int[] mColors;
    private CacheKey mCacheKey;

    public BlurAndColorPostProcessor(int blurRadius) {
        this(DEFAULT_ITERATIONS, blurRadius, COLORS);
    }

    public BlurAndColorPostProcessor(int iterations, int blurRadius, @Nullable int[] colors) {
        Preconditions.checkArgument(iterations > 0);
        Preconditions.checkArgument(blurRadius > 0);
        mIterations = iterations;
        mBlurRadius = blurRadius;
        mColors = colors;
    }

    @Override
    public void process(Bitmap bitmap) {
        if (mColors != null && mColors.length > 0) {
            Bitmap bp = BitmapUtil.INSTANCE.addGradient2(bitmap, mColors);
            NativeBlurFilter.iterativeBoxBlur(bp, mIterations, mBlurRadius);
        } else {
            NativeBlurFilter.iterativeBoxBlur(bitmap, mIterations, mBlurRadius);
        }

    }

    @Nullable
    @Override
    public CacheKey getPostprocessorCacheKey() {
        if (mCacheKey == null) {
            final String key = String.format((Locale) null, "i%dr%d", mIterations, mBlurRadius);
            mCacheKey = new SimpleCacheKey(key);
        }
        return mCacheKey;
    }
}
