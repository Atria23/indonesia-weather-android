package com.bmkg.retrofit.utils;

import android.graphics.drawable.PictureDrawable;
import android.view.View;

import com.bumptech.glide.request.target.ImageViewTarget;

public class SvgSoftwareLayerSetter extends ImageViewTarget<PictureDrawable> {

    public SvgSoftwareLayerSetter(View view) {
        super((android.widget.ImageView) view);
    }

    @Override
    protected void setResource(PictureDrawable resource) {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ((android.widget.ImageView) view).setImageDrawable(resource);
    }
}
