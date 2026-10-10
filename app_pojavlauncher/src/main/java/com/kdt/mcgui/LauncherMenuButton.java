package com.kdt.mcgui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import git.artdeell.mojo.R;
import fr.spse.extended_view.ExtendedButton;

public class LauncherMenuButton extends ExtendedButton {

    public LauncherMenuButton(@NonNull Context context) {
        super(context);
        setSettings();
    }
    public LauncherMenuButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSettings();
    }

    /** Set style stuff */
    private void setSettings(){
        Resources resources = getContext().getResources();

        int padding = resources.getDimensionPixelSize(R.dimen._16sdp);
        setCompoundDrawablePadding(padding);
        setPaddingRelative(padding, 0, padding, 0);
        setGravity(Gravity.CENTER_VERTICAL);
        setTextColor(ResourcesCompat.getColor(resources, R.color.primary_text, null));
        setTypeface(ResourcesCompat.getFont(getContext(), R.font.noto_sans_bold));

        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen._12ssp));

        // Set drawable size
        int[] sizes = getExtendedViewData().getSizeCompounds();
        sizes[0] = resources.getDimensionPixelSize(R.dimen._24sdp);
        getExtendedViewData().setSizeCompounds(sizes);
        postProcessDrawables();
    }
}
