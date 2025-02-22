/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR condITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.protonaosp.launcher3;

import android.annotation.ColorInt;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.Settings;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.RemoteViews;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.launcher3.views.ActivityContext;
import com.android.launcher3.widget.LocalColorExtractor;

import dev.kdrag0n.colorkt.Color;
import dev.kdrag0n.colorkt.cam.Zcam;
import dev.kdrag0n.colorkt.data.Illuminants;
import dev.kdrag0n.colorkt.rgb.Srgb;
import dev.kdrag0n.colorkt.tristimulus.CieXyzAbs;
import dev.kdrag0n.colorkt.ucs.lab.CieLab;
import dev.kdrag0n.monet.theme.ColorScheme;
import dev.kdrag0n.monet.theme.DynamicColorScheme;
import dev.kdrag0n.monet.theme.MaterialYouTargets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThemedLocalColorExtractor extends LocalColorExtractor implements
        WallpaperManager.LocalWallpaperColorConsumer {
    // Shade number -> color resource ID maps
    private static final SparseIntArray ACCENT1_RES = new SparseIntArray(13);
    private static final SparseIntArray ACCENT2_RES = new SparseIntArray(13);
    private static final SparseIntArray ACCENT3_RES = new SparseIntArray(13);
    private static final SparseIntArray NEUTRAL1_RES = new SparseIntArray(13);
    private static final SparseIntArray NEUTRAL2_RES = new SparseIntArray(13);

    private Context mContext;
    private final WallpaperManager wallpaperManager;
    private Listener listener;

    // For calculating and returning bounds
    private final RectF tempRectF = new RectF();

    static {
        ACCENT1_RES.put(   0, android.R.color.system_accent1_0);
        ACCENT1_RES.put(  10, android.R.color.system_accent1_10);
        ACCENT1_RES.put(  50, android.R.color.system_accent1_50);
        ACCENT1_RES.put( 100, android.R.color.system_accent1_100);
        ACCENT1_RES.put( 200, android.R.color.system_accent1_200);
        ACCENT1_RES.put( 300, android.R.color.system_accent1_300);
        ACCENT1_RES.put( 400, android.R.color.system_accent1_400);
        ACCENT1_RES.put( 500, android.R.color.system_accent1_500);
        ACCENT1_RES.put( 600, android.R.color.system_accent1_600);
        ACCENT1_RES.put( 700, android.R.color.system_accent1_700);
        ACCENT1_RES.put( 800, android.R.color.system_accent1_800);
        ACCENT1_RES.put( 900, android.R.color.system_accent1_900);
        ACCENT1_RES.put(1000, android.R.color.system_accent1_1000);

        ACCENT2_RES.put(   0, android.R.color.system_accent2_0);
        ACCENT2_RES.put(  10, android.R.color.system_accent2_10);
        ACCENT2_RES.put(  50, android.R.color.system_accent2_50);
        ACCENT2_RES.put( 100, android.R.color.system_accent2_100);
        ACCENT2_RES.put( 200, android.R.color.system_accent2_200);
        ACCENT2_RES.put( 300, android.R.color.system_accent2_300);
        ACCENT2_RES.put( 400, android.R.color.system_accent2_400);
        ACCENT2_RES.put( 500, android.R.color.system_accent2_500);
        ACCENT2_RES.put( 600, android.R.color.system_accent2_600);
        ACCENT2_RES.put( 700, android.R.color.system_accent2_700);
        ACCENT2_RES.put( 800, android.R.color.system_accent2_800);
        ACCENT2_RES.put( 900, android.R.color.system_accent2_900);
        ACCENT2_RES.put(1000, android.R.color.system_accent2_1000);

        ACCENT3_RES.put(   0, android.R.color.system_accent3_0);
        ACCENT3_RES.put(  10, android.R.color.system_accent3_10);
        ACCENT3_RES.put(  50, android.R.color.system_accent3_50);
        ACCENT3_RES.put( 100, android.R.color.system_accent3_100);
        ACCENT3_RES.put( 200, android.R.color.system_accent3_200);
        ACCENT3_RES.put( 300, android.R.color.system_accent3_300);
        ACCENT3_RES.put( 400, android.R.color.system_accent3_400);
        ACCENT3_RES.put( 500, android.R.color.system_accent3_500);
        ACCENT3_RES.put( 600, android.R.color.system_accent3_600);
        ACCENT3_RES.put( 700, android.R.color.system_accent3_700);
        ACCENT3_RES.put( 800, android.R.color.system_accent3_800);
        ACCENT3_RES.put( 900, android.R.color.system_accent3_900);
        ACCENT3_RES.put(1000, android.R.color.system_accent3_1000);

        NEUTRAL1_RES.put(   0, android.R.color.system_neutral1_0);
        NEUTRAL1_RES.put(  10, android.R.color.system_neutral1_10);
        NEUTRAL1_RES.put(  50, android.R.color.system_neutral1_50);
        NEUTRAL1_RES.put( 100, android.R.color.system_neutral1_100);
        NEUTRAL1_RES.put( 200, android.R.color.system_neutral1_200);
        NEUTRAL1_RES.put( 300, android.R.color.system_neutral1_300);
        NEUTRAL1_RES.put( 400, android.R.color.system_neutral1_400);
        NEUTRAL1_RES.put( 500, android.R.color.system_neutral1_500);
        NEUTRAL1_RES.put( 600, android.R.color.system_neutral1_600);
        NEUTRAL1_RES.put( 700, android.R.color.system_neutral1_700);
        NEUTRAL1_RES.put( 800, android.R.color.system_neutral1_800);
        NEUTRAL1_RES.put( 900, android.R.color.system_neutral1_900);
        NEUTRAL1_RES.put(1000, android.R.color.system_neutral1_1000);

        NEUTRAL2_RES.put(   0, android.R.color.system_neutral2_0);
        NEUTRAL2_RES.put(  10, android.R.color.system_neutral2_10);
        NEUTRAL2_RES.put(  50, android.R.color.system_neutral2_50);
        NEUTRAL2_RES.put( 100, android.R.color.system_neutral2_100);
        NEUTRAL2_RES.put( 200, android.R.color.system_neutral2_200);
        NEUTRAL2_RES.put( 300, android.R.color.system_neutral2_300);
        NEUTRAL2_RES.put( 400, android.R.color.system_neutral2_400);
        NEUTRAL2_RES.put( 500, android.R.color.system_neutral2_500);
        NEUTRAL2_RES.put( 600, android.R.color.system_neutral2_600);
        NEUTRAL2_RES.put( 700, android.R.color.system_neutral2_700);
        NEUTRAL2_RES.put( 800, android.R.color.system_neutral2_800);
        NEUTRAL2_RES.put( 900, android.R.color.system_neutral2_900);
        NEUTRAL2_RES.put(1000, android.R.color.system_neutral2_1000);
    }

    public ThemedLocalColorExtractor(Context context) {
        mContext = context;
        wallpaperManager = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
    }

    private static void addColorsToArray(Map<Integer, Color> swatch,
            SparseIntArray resMap, SparseIntArray array) {
        for (Map.Entry<Integer, Color> entry : swatch.entrySet()) {
            int shade = entry.getKey();
            int resId = resMap.get(shade, -1);
            if (resId != -1) {
                Srgb color = (Srgb) entry.getValue();
                array.put(resId, 0xff000000 | color.toRgb8());
            }
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public SparseIntArray generateColorsOverride(WallpaperColors colors) {
        SparseIntArray colorRes = new SparseIntArray(5 * 13);
        double luminance = (double) Settings.Secure.getLong(mContext.getContentResolver(), "monet_engine_white_luminance_user", (long) CieXyzAbs.DEFAULT_SDR_WHITE_LUMINANCE);
        Zcam.ViewingConditions cond = new Zcam.ViewingConditions(
            /* surroundFactor */ Zcam.ViewingConditions.SURROUND_AVERAGE,
            /* adaptingLuminance */ 0.4 * luminance,
            /* backgroundLuminance */ new CieLab(50.0, 0.0, 0.0, Illuminants.D65)
                    .toXyz().getY() * luminance,
            /* referenceWhite */ CieXyzAbs.fromRel(Illuminants.D65, luminance)
        );
        ColorScheme targets = new MaterialYouTargets(getChroma(), false, cond);
        @ColorInt int customColor = Settings.Secure.getInt(mContext.getContentResolver(), "monet_engine_custom_color", 0);
        @ColorInt int colorOverride = Settings.Secure.getInt(mContext.getContentResolver(), "monet_engine_color_override", -1);
        Color color = new Srgb((colorOverride != -1 && customColor != 0) ? colorOverride : colors.getPrimaryColor().toArgb());
        ColorScheme colorScheme = new DynamicColorScheme(targets, color, getChroma(), cond, true);

        addColorsToArray(colorScheme.getAccent1(), ACCENT1_RES, colorRes);
        addColorsToArray(colorScheme.getAccent2(), ACCENT2_RES, colorRes);
        addColorsToArray(colorScheme.getAccent3(), ACCENT3_RES, colorRes);
        addColorsToArray(colorScheme.getNeutral1(), NEUTRAL1_RES, colorRes);
        addColorsToArray(colorScheme.getNeutral2(), NEUTRAL2_RES, colorRes);

        return colorRes;
    }

    @Override
    public void setWorkspaceLocation(Rect pos, View child, int screenId) {
        ActivityContext activityContext = (ActivityContext) ActivityContext.lookupContext(child.getContext());
        if (!(activityContext instanceof Launcher)) {
            tempRectF.setEmpty();
            return;
        }
        Launcher launcher = (Launcher) activityContext;
        Resources res = launcher.getResources();
        DeviceProfile dp = launcher.getDeviceProfile().inv.getDeviceProfile(launcher);
        float screenWidth = dp.widthPx;
        float screenHeight = dp.heightPx;
        int numScreens = launcher.getWorkspace().getNumPagesForWallpaperParallax();
        float relativeScreenWidth = 1f / numScreens;

        int[] dragLayerBounds = new int[2];
        launcher.getDragLayer().getLocationOnScreen(dragLayerBounds);
        // Translate from drag layer coordinates to screen coordinates.
        int screenLeft = pos.left + dragLayerBounds[0];
        int screenTop = pos.top + dragLayerBounds[1];
        int screenRight = pos.right + dragLayerBounds[0];
        int screenBottom = pos.bottom + dragLayerBounds[1];
        tempRectF.left = (screenLeft / screenWidth + screenId) * relativeScreenWidth;
        tempRectF.right = (screenRight / screenWidth + screenId) * relativeScreenWidth;
        tempRectF.top = screenTop / screenHeight;
        tempRectF.bottom = screenBottom / screenHeight;

        if (tempRectF.left < 0
                || tempRectF.right > 1
                || tempRectF.top < 0
                || tempRectF.bottom > 1) {
            tempRectF.setEmpty();
        }

        if (wallpaperManager != null && !tempRectF.isEmpty()) {
            wallpaperManager.removeOnColorsChangedListener(this);
            wallpaperManager.addOnColorsChangedListener(this, new ArrayList<RectF>(List.of(tempRectF)));
        }
    }

    @Override
    public void applyColorsOverride(Context base, WallpaperColors colors) {
        RemoteViews.ColorResources res =
                RemoteViews.ColorResources.create(base, generateColorsOverride(colors));
        if (res != null) {
            res.apply(base);
        }
    }

    @Override
    public void onColorsChanged(RectF area, WallpaperColors colors) {
        if (listener != null) {
            listener.onColorsChanged(generateColorsOverride(colors));
        }
    }

    private float getChroma() {
        return Settings.Secure.getFloat(mContext.getContentResolver(), "monet_engine_chroma_factor", 1.0f);
    }
}
