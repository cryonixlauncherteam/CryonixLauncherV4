package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.openPath;
import static net.kdt.pojavlaunch.Tools.shareLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.CustomControlsActivity;
import git.artdeell.mojo.R;

import net.kdt.pojavlaunch.LauncherActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.accounts.Account;
import net.kdt.pojavlaunch.authenticator.accounts.Accounts;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.instances.Instance;
import net.kdt.pojavlaunch.instances.Instances;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.File;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;
    private TextView mSelectedInstanceTitle;
    private TextView mSelectedInstanceStatus;
    private TextView mFeaturedInstanceTitle;
    private TextView mUserProfileName;
    private TextView mInstanceCountText;

    private final ActivityResultLauncher<Object> mModInstallerLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("jar"), (data)->{
                if(data != null) Tools.launchModInstaller(requireContext(), data);
            });

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View mNewsButton = view.findViewById(R.id.news_button);
        View mDiscordButton = view.findViewById(R.id.discord_button);
        View mSocialMediaButton = view.findViewById(R.id.social_media_button);
        View mYoutubeButton = view.findViewById(R.id.youtube_button);

        View mCustomControlButton = view.findViewById(R.id.custom_control_button);
        View mInstallJarButton = view.findViewById(R.id.install_jar_button);
        View mShareLogsButton = view.findViewById(R.id.share_logs_button);
        View mOpenDirectoryButton = view.findViewById(R.id.open_files_button);

        View mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        View mDuplicateButton = view.findViewById(R.id.duplicate_instance_button);
        View mDeleteButton = view.findViewById(R.id.delete_instance_button);
        View mCreateInstanceButton = view.findViewById(R.id.create_instance_button);
        View mSidebarSettingsButton = view.findViewById(R.id.sidebar_settings_button);
        View mSidebarAccountsButton = view.findViewById(R.id.sidebar_accounts_button);
        View mManageAccountButton = view.findViewById(R.id.manage_account_button);

        View mFloatingAssistantButton = view.findViewById(R.id.floating_assistant_button);
        View mAiAssistantCard = view.findViewById(R.id.ai_assistant_card);

        View mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mSelectedInstanceTitle = view.findViewById(R.id.selected_instance_title);
        mSelectedInstanceStatus = view.findViewById(R.id.selected_instance_status);
        mFeaturedInstanceTitle = view.findViewById(R.id.featured_instance_title);
        mUserProfileName = view.findViewById(R.id.user_profile_name);
        mInstanceCountText = view.findViewById(R.id.instance_count_text);

        if (mNewsButton != null) {
            mNewsButton.setOnClickListener(v -> Tools.openURL(requireActivity(), Tools.URL_HOME));
            mNewsButton.setOnLongClickListener((v)->{
                Tools.swapFragment(requireActivity(), GamepadMapperFragment.class, GamepadMapperFragment.TAG, null);
                return true;
            });
        }

        if (mDiscordButton != null) {
            mDiscordButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://discord.gg/pGDg857dSj"));
        } else if (mSocialMediaButton != null) {
            mSocialMediaButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://discord.gg/pGDg857dSj"));
        }

        if (mYoutubeButton != null) {
            mYoutubeButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://youtube.com/@cryonixlauncherofficial?si=bCrRNZ1h1sfVBv3q"));
        }

        if (mCustomControlButton != null) {
            mCustomControlButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), CustomControlsActivity.class)));
        }

        if (mInstallJarButton != null) {
            mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation());
        }

        if (mEditProfileButton != null) {
            mEditProfileButton.setOnClickListener(v -> {
                if (mVersionSpinner != null) {
                    mVersionSpinner.openProfileEditor(requireActivity());
                } else {
                    Tools.swapFragment(requireActivity(), InstanceEditorFragment.class, InstanceEditorFragment.TAG, null);
                }
            });
        }

        if (mDuplicateButton != null) {
            mDuplicateButton.setOnClickListener(v -> duplicateSelectedInstance());
        }

        if (mDeleteButton != null) {
            mDeleteButton.setOnClickListener(v -> {
                DeleteConfirmDialogFragment dialogFragment = new DeleteConfirmDialogFragment();
                dialogFragment.show(getChildFragmentManager(), DeleteConfirmDialogFragment.TAG);
            });
        }

        if (mCreateInstanceButton != null) {
            mCreateInstanceButton.setOnClickListener(v ->
                    Tools.swapFragment(requireActivity(), ProfileTypeSelectFragment.class, ProfileTypeSelectFragment.TAG, null));
        }

        if (mSidebarSettingsButton != null) {
            mSidebarSettingsButton.setOnClickListener(v ->
                    Tools.swapFragment(requireActivity(), LauncherPreferenceFragment.class, LauncherActivity.SETTING_FRAGMENT_TAG, null));
        }

        if (mSidebarAccountsButton != null) {
            mSidebarAccountsButton.setOnClickListener(v ->
                    Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null));
        }

        if (mManageAccountButton != null) {
            mManageAccountButton.setOnClickListener(v ->
                    Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null));
        }

        if (mFloatingAssistantButton != null) {
            mFloatingAssistantButton.setOnClickListener(v -> showAssistantDialog());
        }

        if (mAiAssistantCard != null) {
            mAiAssistantCard.setOnClickListener(v -> showAssistantDialog());
        }

        if (mPlayButton != null) {
            mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));
        }

        if (mShareLogsButton != null) {
            mShareLogsButton.setOnClickListener((v) -> shareLog(requireContext()));
        }

        if (mOpenDirectoryButton != null) {
            mOpenDirectoryButton.setOnClickListener((v)-> openGameDirectory(v.getContext()));
        }

        updateSelectedInstanceDetails();
    }

    private void showAssistantDialog() {
        Tools.dialog(requireContext(), "Cryonix Assistant",
                "Welcome to CryonixLauncher V4!\n\n• Manage your Minecraft instances from the main panel.\n• Use 'Execute a .jar' to install Forge/Fabric/Modpack installers.\n• Adjust memory, renderer, and controls in Settings.");
    }

    private void duplicateSelectedInstance() {
        try {
            Instance selected = Instances.loadSelectedInstance();
            if (selected == null) {
                Toast.makeText(requireContext(), R.string.no_instance, Toast.LENGTH_LONG).show();
                return;
            }
            Instance copy = Instances.createInstance(inst -> {
                inst.versionId = selected.versionId;
                inst.name = selected.name != null ? selected.name + " (Copy)" : "Copy";
                inst.controlLayout = selected.controlLayout;
                inst.jvmArgs = selected.jvmArgs;
                inst.renderer = selected.renderer;
                inst.selectedRuntime = selected.selectedRuntime;
                inst.sharedData = selected.sharedData;
            }, "copy");
            Instances.setSelectedInstance(copy);
            if (mVersionSpinner != null) {
                mVersionSpinner.reloadProfiles();
            }
            updateSelectedInstanceDetails();
            Toast.makeText(requireContext(), "Instance duplicated", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Tools.showError(requireContext(), e);
        }
    }

    private void updateSelectedInstanceDetails() {
        try {
            Instances instances = Instances.loadDisplay();
            Instance selected = Instances.loadSelectedInstance();
            if (selected != null) {
                String title = Tools.validOrNullString(selected.name);
                if (title == null) title = selected.versionId;
                if (mSelectedInstanceTitle != null && title != null) {
                    mSelectedInstanceTitle.setText(title);
                }
                if (mFeaturedInstanceTitle != null && title != null) {
                    mFeaturedInstanceTitle.setText("Minecraft " + title);
                }
                if (mSelectedInstanceStatus != null) {
                    mSelectedInstanceStatus.setText("● Ready");
                }
            }
            if (instances != null && mInstanceCountText != null) {
                int total = instances.list.size();
                int current = total > 0 ? (instances.selectedIndex + 1) : 0;
                mInstanceCountText.setText(current + "/" + total);
            }
            Account account = Accounts.getCurrent();
            if (account != null && mUserProfileName != null) {
                mUserProfileName.setText(account.username);
            }
        } catch (Exception ignored) {
        }
    }

    private void openGameDirectory(Context context) {
        Instance instance = Instances.loadSelectedInstance();
        if(instance == null) {
            Toast.makeText(context, R.string.no_instance, Toast.LENGTH_LONG).show();
            return;
        }
        File gameDirectory = instance.getGameDirectory();
        if(FileUtils.ensureDirectorySilently(gameDirectory)) {
            openPath(context, gameDirectory, false);
        }else {
            Toast.makeText(context, R.string.gamedir_open_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true);
        updateSelectedInstanceDetails();
    }

    private void runInstallerWithConfirmation() {
        if (ProgressKeeper.getTaskCount() == 0) {
            mModInstallerLauncher.launch(null);
        } else Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }
}
