package com.podhoarder.object;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import com.squareup.picasso.Transformation;

import java.util.Map;
import java.util.WeakHashMap;

public final class PaletteTransformation implements Transformation {
    private static final PaletteTransformation INSTANCE = new PaletteTransformation();
    private static final Map<Bitmap, Palette> CACHE = new WeakHashMap<Bitmap, Palette>();

    public static PaletteTransformation getInstance() {
        return INSTANCE;
    }

    public static Palette getPalette(Bitmap bitmap) {
        return CACHE.get(bitmap);
    }

    private PaletteTransformation() {}

    @Override public Bitmap transform(Bitmap source) {
        Palette palette = Palette.generate(source);
        CACHE.put(source, palette);
        return source;
    }

    @Override
    public String key() {
        return ""; // Stable key for all requests. An unfortunate requirement.
    }

}