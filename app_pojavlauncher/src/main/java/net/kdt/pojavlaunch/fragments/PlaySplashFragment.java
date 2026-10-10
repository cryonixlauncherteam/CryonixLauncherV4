package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.accounts.Account;
import net.kdt.pojavlaunch.authenticator.accounts.Accounts;
import net.kdt.pojavlaunch.instances.Instance;
import net.kdt.pojavlaunch.instances.Instances;

public class PlaySplashFragment extends Fragment {
    public static final String TAG = "PlaySplashFragment";

    public PlaySplashFragment() {
        super(R.layout.fragment_play_splash);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View playButton = view.findViewById(R.id.splash_play_button);
        TextView accountNameText = view.findViewById(R.id.account_name_text);
        TextView accountTypeText = view.findViewById(R.id.account_type_text);
        TextView versionText = view.findViewById(R.id.splash_version_text);

        Account currentAccount = Accounts.getCurrent();
        if (currentAccount != null) {
            if (accountNameText != null) {
                accountNameText.setText(currentAccount.username);
            }
            if (accountTypeText != null) {
                accountTypeText.setText(currentAccount.authType.name());
            }
        }

        try {
            Instance selectedInstance = Instances.loadSelectedInstance();
            if (selectedInstance != null && versionText != null) {
                String name = Tools.validOrNullString(selectedInstance.name);
                versionText.setText(name != null ? name : selectedInstance.versionId);
            }
        } catch (Exception ignored) {
        }

        if (playButton != null) {
            playButton.setOnClickListener(v -> {
                // Tapping PLAY opens the Main Home Screen (Screen 3)
                Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
            });
        }
    }
}
