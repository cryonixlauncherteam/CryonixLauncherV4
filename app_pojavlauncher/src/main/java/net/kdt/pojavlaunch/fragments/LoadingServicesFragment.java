package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.JellyAnimations;

public class LoadingServicesFragment extends Fragment {
    public static final String TAG = "LoadingServicesFragment";

    private ProgressBar mProgressBar;
    private TextView mStepText;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public LoadingServicesFragment() {
        super(R.layout.fragment_loading_services);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mProgressBar = view.findViewById(R.id.services_progress_bar);
        mStepText = view.findViewById(R.id.loading_step_text);

        continueToLauncher();

        JellyAnimations.animateScreen(view);
        JellyAnimations.attachTouchFeedback(view);
    }

    private void continueToLauncher() {
        if (mStepText != null) mStepText.setText("Starting launcher...");
        // There is no observable initialization operation on this path. Avoid
        // displaying a fabricated task sequence or a fictitious percentage.
        mHandler.postDelayed(() -> {
            if (isAdded()) {
                Tools.swapFragment(requireActivity(), PlaySplashFragment.class, PlaySplashFragment.TAG, null);
            }
        }, 250);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }
}
