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

public class LoadingServicesFragment extends Fragment {
    public static final String TAG = "LoadingServicesFragment";

    private ProgressBar mProgressBar;
    private TextView mStepText;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mStepIndex = 0;

    private static final String[] STEPS = new String[]{
            "Initializing Java runtimes & native libraries...",
            "Configuring audio, input & renderer pipelines...",
            "Connecting to Modpack & Version repositories...",
            "Pre-caching shaders & asset catalogs...",
            "Preparing Cryonix V4 user profile...",
            "Backend and Frontend Services Ready!"
    };

    public LoadingServicesFragment() {
        super(R.layout.fragment_loading_services);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mProgressBar = view.findViewById(R.id.services_progress_bar);
        mStepText = view.findViewById(R.id.loading_step_text);

        startLoadingSequence();
    }

    private void startLoadingSequence() {
        mStepIndex = 0;
        runNextStep();
    }

    private void runNextStep() {
        if (!isAdded()) return;

        if (mStepIndex < STEPS.length) {
            String currentStep = STEPS[mStepIndex];
            if (mStepText != null) {
                mStepText.setText(currentStep);
            }
            int progress = (int) (((float) (mStepIndex + 1) / STEPS.length) * 100);
            if (mProgressBar != null) {
                mProgressBar.setProgress(progress);
            }

            mStepIndex++;
            mHandler.postDelayed(this::runNextStep, 800);
        } else {
            // Sequence completed -> Navigate to Play Splash Screen
            mHandler.postDelayed(() -> {
                if (isAdded()) {
                    Tools.swapFragment(requireActivity(), PlaySplashFragment.class, PlaySplashFragment.TAG, null);
                }
            }, 500);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }
}
