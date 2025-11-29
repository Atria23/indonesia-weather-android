package com.bmkg.retrofit.utils;

import android.graphics.drawable.PictureDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class SvgDrawableTranscoder implements ResourceTranscoder<PictureDrawable, PictureDrawable> {

    @Nullable
    @Override
    public Resource<PictureDrawable> transcode(
            @NonNull Resource<PictureDrawable> toTranscode,
            @NonNull Options options
    ) {
        return new SimpleResource<>(toTranscode.get());
    }
}
