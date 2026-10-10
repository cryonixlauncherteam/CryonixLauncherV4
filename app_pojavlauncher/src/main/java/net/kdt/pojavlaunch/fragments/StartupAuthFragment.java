package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.accounts.Account;
import net.kdt.pojavlaunch.authenticator.accounts.Accounts;

import java.io.IOException;

public class StartupAuthFragment extends Fragment {
    public static final String TAG = "StartupAuthFragment";

    private EditText mUsernameInput;
    private RadioButton mRbMicrosoft;

    public StartupAuthFragment() {
        super(R.layout.fragment_startup_auth);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mUsernameInput = view.findViewById(R.id.username_input);
        mRbMicrosoft = view.findViewById(R.id.rb_microsoft);
        View signInButton = view.findViewById(R.id.signin_button);

        if (signInButton != null) {
            signInButton.setOnClickListener(v -> performLogin());
        }
    }

    private void performLogin() {
        if (mRbMicrosoft != null && mRbMicrosoft.isChecked()) {
            Tools.swapFragment(requireActivity(), MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG, null);
            return;
        }

        String username = mUsernameInput != null ? mUsernameInput.getText().toString().trim() : "";
        if (username.isEmpty()) {
            username = "CryonixUser";
        }

        try {
            final String finalUsername = username;
            Account account = Accounts.create(acc -> acc.username = finalUsername);
            Accounts.setCurrent(account);
            Toast.makeText(requireContext(), "Signed in as " + account.username, Toast.LENGTH_SHORT).show();

            // Transition to Backend/Frontend Services Loading Screen
            Tools.swapFragment(requireActivity(), LoadingServicesFragment.class, LoadingServicesFragment.TAG, null);
        } catch (IOException e) {
            Tools.showError(requireContext(), e);
        }
    }
}
