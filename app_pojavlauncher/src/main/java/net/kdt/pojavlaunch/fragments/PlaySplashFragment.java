package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.neko.cryonixlauncher.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.JellyAnimations;

public class PlaySplashFragment extends Fragment {
    public static final String TAG = "PlaySplashFragment";

    public PlaySplashFragment() {
        super(R.layout.fragment_play_splash);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View playButton = view.findViewById(R.id.splash_play_button);

        if (playButton != null) {
            playButton.setOnClickListener(v -> {
                // Tapping PLAY opens the Main Home Screen
                Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
            });
        }

        JellyAnimations.animateScreen(view);
        JellyAnimations.attachTouchFeedback(view);
    }
}
