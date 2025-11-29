package com.bmkg.retrofit.utils;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

@GlideModule
public class SvgModule extends AppGlideModule {

    @Override
    public void registerComponents(
            @NonNull Context context,
            @NonNull Glide glide,
            @NonNull Registry registry
    ) {

        registry
                .register(PictureDrawable.class, PictureDrawable.class, new SvgDrawableTranscoder())
                .append(InputStream.class, PictureDrawable.class, new SvgDecoder());
    }
}
