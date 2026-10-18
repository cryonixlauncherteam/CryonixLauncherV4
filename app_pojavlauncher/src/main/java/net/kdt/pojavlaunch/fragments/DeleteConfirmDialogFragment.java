package net.kdt.pojavlaunch.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.instances.Instance;
import net.kdt.pojavlaunch.instances.Instances;
import net.kdt.pojavlaunch.instances.InstanceIconProvider;
import net.kdt.pojavlaunch.utils.JellyAnimations;

import java.io.IOException;

public class DeleteConfirmDialogFragment extends DialogFragment {
    public static String TAG = "delete_dialog_confirm";
    private final Instance mInstance = Instances.loadSelectedInstance();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (mInstance == null) {
            dismiss();
        }

        String instanceName = mInstance != null && mInstance.name != null ? mInstance.name : "Selected Instance";
        String message = getString(R.string.instance_delete_confirmation) + "\n\nInstance: " + instanceName;

        return new AlertDialog.Builder(requireContext(), R.style.CryonixDialogTheme)
                .setTitle(R.string.instance_delete)
                .setMessage(message)
                .setPositiveButton(R.string.global_delete, (dialog, which) -> {
                    if (mInstance == null) return;
                    InstanceIconProvider.dropIcon(mInstance);
                    try {
                        Instances.removeInstance(mInstance);
                    } catch (IOException e) {
                        Tools.showErrorRemote(e);
                    }
                    androidx.fragment.app.Fragment parentFrag = getParentFragment();
                    if (parentFrag instanceof MainMenuFragment) {
                        ((MainMenuFragment) parentFrag).onResume();
                    } else {
                        Tools.removeCurrentFragment(requireActivity());
                    }
                })
                .setNegativeButton(R.string.global_no, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            JellyAnimations.animateDialog(getDialog());
        }
    }
}
