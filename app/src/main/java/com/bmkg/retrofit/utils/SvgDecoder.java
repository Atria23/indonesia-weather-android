package com.bmkg.retrofit.utils;

import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.caverock.androidsvg.SVG;

import java.io.IOException;
import java.io.InputStream;


import java.io.InputStream;
public class SvgDecoder implements ResourceDecoder<InputStream, PictureDrawable> {

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) {
        return true;
    }

    @Nullable
    @Override
    public Resource<PictureDrawable> decode(
            @NonNull InputStream source,
            int width,
            int height,
            @NonNull Options options
    ) throws IOException {

        try {
            SVG svg = SVG.getFromInputStream(source);
            Picture picture = svg.renderToPicture();
            return new SimpleResource<>(new PictureDrawable(picture));
        } catch (Exception e) {
            throw new IOException("Cannot decode SVG", e);
        }
    }
}
