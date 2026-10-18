package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.openPath;
import static net.kdt.pojavlaunch.Tools.shareLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.CustomControlsActivity;
import git.neko.cryonixlauncher.R;

import net.kdt.pojavlaunch.LauncherActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.accounts.Account;
import net.kdt.pojavlaunch.authenticator.accounts.Accounts;
import net.kdt.pojavlaunch.authenticator.accounts.SkinHeadRenderer;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.utils.JellyAnimations;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.instances.DisplayInstance;
import net.kdt.pojavlaunch.instances.Instance;
import net.kdt.pojavlaunch.instances.Instances;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
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
    private TextView mTaskStatusText;
    private TextView mTaskCountBadge;
    private ImageView mSelectedInstancePreview;

    private final ActivityResultLauncher<Object> mModInstallerLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("jar"), (data)->{
                if(data != null) Tools.launchModInstaller(requireContext(), data);
            });

    private final TaskCountListener mTaskCountListener = count -> {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> updateTasksUI(count));
        }
        return false;
    };

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View mSidebarHomeButton = view.findViewById(R.id.sidebar_home);
        View mSidebarInstancesButton = view.findViewById(R.id.sidebar_instances);
        View mSidebarDownloadsButton = view.findViewById(R.id.sidebar_downloads);
        View mSidebarModsButton = view.findViewById(R.id.sidebar_mods);
        View mSidebarSettingsButton = view.findViewById(R.id.sidebar_settings_button);
        if (mSidebarSettingsButton == null) mSidebarSettingsButton = view.findViewById(R.id.setting_button);

        View mSidebarAccountsButton = view.findViewById(R.id.sidebar_accounts_button);
        if (mSidebarAccountsButton == null) mSidebarAccountsButton = view.findViewById(R.id.manage_account_button);

        View mCustomControlButton = view.findViewById(R.id.custom_control_button);
        View mInstallJarButton = view.findViewById(R.id.install_jar_button);

        View mPlayButton = view.findViewById(R.id.play_button);
        if (mPlayButton == null) mPlayButton = view.findViewById(R.id.right_launch_button);

        View mManageInstanceButton = view.findViewById(R.id.btn_manage_instance);
        View mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        View mCreateInstanceButton = view.findViewById(R.id.create_instance_button);
        View mCreateFirstInstanceButton = view.findViewById(R.id.btn_create_first_instance);
        View mViewAllInstancesButton = view.findViewById(R.id.btn_view_all_instances);
        View mViewAllTasksButton = view.findViewById(R.id.btn_view_all_tasks);

        View mDiscordButton = view.findViewById(R.id.discord_button);
        View mYoutubeButton = view.findViewById(R.id.youtube_button);

        View mCardInstancesSelector = view.findViewById(R.id.card_instances_selector);
        if (mCardInstancesSelector == null) mCardInstancesSelector = view.findViewById(R.id.instances_panel);

        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mSelectedInstanceTitle = view.findViewById(R.id.selected_instance_title);
        if (mSelectedInstanceTitle == null) mSelectedInstanceTitle = view.findViewById(R.id.current_instance_label);

        mSelectedInstanceStatus = view.findViewById(R.id.selected_instance_status);
        mFeaturedInstanceTitle = view.findViewById(R.id.featured_instance_title);

        mUserProfileName = view.findViewById(R.id.user_profile_name);
        if (mUserProfileName == null) mUserProfileName = view.findViewById(R.id.profile_name);

        mInstanceCountText = view.findViewById(R.id.instance_count_text);
        if (mInstanceCountText == null) mInstanceCountText = view.findViewById(R.id.instance_counter);

        mTaskStatusText = view.findViewById(R.id.task_status_text);
        mTaskCountBadge = view.findViewById(R.id.task_count_badge);
        mSelectedInstancePreview = view.findViewById(R.id.selected_instance_preview);

        // Navigation sidebar handlers
        if (mSidebarHomeButton != null) {
            attachTouchFeedback(mSidebarHomeButton, v -> updateSelectedInstanceDetails());
        }

        View.OnClickListener openInstancesListener = v ->
                Tools.swapFragment(requireActivity(), ProfileTypeSelectFragment.class, ProfileTypeSelectFragment.TAG, null);

        if (mSidebarInstancesButton != null) attachTouchFeedback(mSidebarInstancesButton, openInstancesListener);
        if (mCardInstancesSelector != null) attachTouchFeedback(mCardInstancesSelector, openInstancesListener);
        if (mViewAllInstancesButton != null) attachTouchFeedback(mViewAllInstancesButton, openInstancesListener);
        if (mCreateInstanceButton != null) attachTouchFeedback(mCreateInstanceButton, openInstancesListener);
        if (mCreateFirstInstanceButton != null) attachTouchFeedback(mCreateFirstInstanceButton, openInstancesListener);

        if (mSidebarDownloadsButton != null) {
            attachTouchFeedback(mSidebarDownloadsButton, v ->
                    Tools.swapFragment(requireActivity(), DownloadsFragment.class, DownloadsFragment.TAG, null));
        }

        if (mViewAllTasksButton != null) {
            attachTouchFeedback(mViewAllTasksButton, v ->
                    Tools.swapFragment(requireActivity(), DownloadsFragment.class, DownloadsFragment.TAG, null));
        }

        if (mSidebarModsButton != null) {
            attachTouchFeedback(mSidebarModsButton, v ->
                    Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, null));
        }

        if (mSidebarSettingsButton != null) {
            attachTouchFeedback(mSidebarSettingsButton, v ->
                    Tools.swapFragment(requireActivity(), LauncherPreferenceFragment.class, LauncherActivity.SETTING_FRAGMENT_TAG, null));
        }

        if (mSidebarAccountsButton != null) {
            attachTouchFeedback(mSidebarAccountsButton, this::showAccountSelectionMenu);
        }

        if (mCustomControlButton != null) {
            attachTouchFeedback(mCustomControlButton, v -> startActivity(new Intent(requireContext(), CustomControlsActivity.class)));
        }

        if (mInstallJarButton != null) {
            attachTouchFeedback(mInstallJarButton, v -> runInstallerWithConfirmation());
        }

        View.OnClickListener openEditorListener = v -> {
            if (mVersionSpinner != null) {
                mVersionSpinner.openProfileEditor(requireActivity());
            } else {
                Tools.swapFragment(requireActivity(), InstanceEditorFragment.class, InstanceEditorFragment.TAG, null);
            }
        };

        if (mManageInstanceButton != null) attachTouchFeedback(mManageInstanceButton, openEditorListener);
        if (mEditProfileButton != null) attachTouchFeedback(mEditProfileButton, openEditorListener);

        if (mPlayButton != null) {
            attachTouchFeedback(mPlayButton, v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));
        }

        if (mDiscordButton != null) {
            attachTouchFeedback(mDiscordButton, v -> Tools.openURL(requireActivity(), "https://discord.gg/pGDg857dSj"));
        }

        if (mYoutubeButton != null) {
            attachTouchFeedback(mYoutubeButton, v -> Tools.openURL(requireActivity(), "https://www.youtube.com/channel/UCIUOBDJhIcR0CMghlhp0G0Q"));
        }

        View mDuplicateButton = view.findViewById(R.id.duplicate_instance_button);
        View mDeleteButton = view.findViewById(R.id.delete_instance_button);
        View mOpenFilesButton = view.findViewById(R.id.open_files_button);

        if (mDuplicateButton != null) {
            attachTouchFeedback(mDuplicateButton, v -> {
                try {
                    Instance current = Instances.loadSelectedInstance();
                    if (current != null) {
                        String baseName = Tools.validOrNullString(current.name) != null ? current.name : current.versionId;
                        Instance dup = Instances.createInstance(i -> {
                            i.name = baseName + " (Copy)";
                            i.versionId = current.versionId;
                            i.sharedData = current.sharedData;
                            i.jvmArgs = current.jvmArgs;
                            i.argsMode = current.argsMode;
                            i.renderer = current.renderer;
                            i.selectedRuntime = current.selectedRuntime;
                            i.controlLayout = current.controlLayout;
                        }, "copy");
                        Instances.setSelectedInstance(dup);
                        updateSelectedInstanceDetails();
                        Toast.makeText(requireContext(), "Instance duplicated!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Failed to duplicate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (mDeleteButton != null) {
            attachTouchFeedback(mDeleteButton, v -> {
                DeleteConfirmDialogFragment dialogFragment = new DeleteConfirmDialogFragment();
                dialogFragment.show(getChildFragmentManager(), DeleteConfirmDialogFragment.TAG);
            });
        }

        if (mOpenFilesButton != null) {
            attachTouchFeedback(mOpenFilesButton, v -> {
                try {
                    Instance current = Instances.loadSelectedInstance();
                    File dir = current != null ? current.getGameDirectory() : new File(Tools.DIR_GAME_NEW);
                    Tools.openPath(requireContext(), dir, false);
                } catch (Exception e) {
                    Tools.swapFragment(requireActivity(), FileSelectorFragment.class, FileSelectorFragment.TAG, null);
                }
            });
        }

        View mBtnPrev = view.findViewById(R.id.btn_prev_instance);
        View mBtnNext = view.findViewById(R.id.btn_next_instance);

        if (mBtnPrev != null) {
            mBtnPrev.setOnClickListener(v -> navigateInstance(-1));
        }
        if (mBtnNext != null) {
            mBtnNext.setOnClickListener(v -> navigateInstance(1));
        }

        ProgressKeeper.addTaskCountListener(mTaskCountListener);
        updateSelectedInstanceDetails();
        updateTasksUI(ProgressKeeper.getTaskCount());

        JellyAnimations.animateScreen(view);
        JellyAnimations.attachTouchFeedback(view);
    }

    private void updateTasksUI(int count) {
        if (mTaskCountBadge != null) {
            mTaskCountBadge.setText(String.valueOf(count));
        }
        if (mTaskStatusText != null) {
            if (count > 0) {
                mTaskStatusText.setText(count + " active operation" + (count > 1 ? "s" : "") + " in progress...");
                mTaskStatusText.setTextColor(getResources().getColor(R.color.primary_accent));
            } else {
                mTaskStatusText.setText("No active tasks.");
                mTaskStatusText.setTextColor(getResources().getColor(R.color.secondary_text));
            }
        }
    }

    private void navigateInstance(int direction) {
        try {
            Instances instances = Instances.loadDisplay();
            if (instances != null && !instances.list.isEmpty()) {
                int nextIndex = instances.selectedIndex + direction;
                if (nextIndex < 0) nextIndex = instances.list.size() - 1;
                if (nextIndex >= instances.list.size()) nextIndex = 0;
                Instances.setSelectedInstance(instances.list.get(nextIndex));
                updateSelectedInstanceDetails();
            }
        } catch (Exception ignored) {}
    }

    private void attachTouchFeedback(View view, View.OnClickListener listener) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            if (!net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATIONS_ENABLED) {
                return false;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().cancel();
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80L)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().cancel();
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(220L)
                            .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
                    break;
            }
            return false;
        });
        if (listener != null) {
            view.setOnClickListener(listener);
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
                View root = getView();
                if (root != null) {
                    TextView versionSub = root.findViewById(R.id.selected_instance_version);
                    if (versionSub == null) versionSub = root.findViewById(R.id.current_instance_version);
                    if (versionSub != null) {
                        versionSub.setText("Minecraft " + (selected.versionId != null ? selected.versionId : title));
                    }
                }
            }
            if (instances != null && mInstanceCountText != null) {
                int total = instances.list.size();
                int current = total > 0 ? (instances.selectedIndex + 1) : 0;
                mInstanceCountText.setText(current + "/" + total);
            }

            refreshAccountUI();

            setupInstancesRecyclerView(instances);
        } catch (Exception ignored) {
        }
    }

    private void refreshAccountUI() {
        try {
            Account account = Accounts.getCurrent();
            if (account == null) {
                Accounts accounts = Accounts.load();
                if (accounts != null && !accounts.accounts.isEmpty()) {
                    int idx = Math.min(accounts.selectionIndex, accounts.accounts.size() - 1);
                    account = accounts.accounts.get(Math.max(0, idx));
                    Accounts.setCurrent(account);
                }
            }

            String name = (account != null && account.username != null && !account.username.isEmpty())
                    ? account.username : "Guest / Sign In";

            if (mUserProfileName != null) {
                mUserProfileName.setText(name);
            }

            View root = getView();
            if (root != null) {
                TextView profileNameAlt = root.findViewById(R.id.profile_name);
                if (profileNameAlt != null && profileNameAlt != mUserProfileName) {
                    profileNameAlt.setText(name);
                }

                Bitmap appliedSkinHead = getAppliedSkinHead();
                Bitmap accountSkinHead = account != null ? account.getSkinFace() : null;
                ImageView avatar = root.findViewById(R.id.profile_avatar);
                if (avatar != null && (appliedSkinHead != null || accountSkinHead != null)) {
                    avatar.setImageBitmap(appliedSkinHead != null ? appliedSkinHead : accountSkinHead);
                }
                if (mSelectedInstancePreview != null) {
                    if (appliedSkinHead != null || accountSkinHead != null) {
                        mSelectedInstancePreview.setImageBitmap(appliedSkinHead != null ? appliedSkinHead : accountSkinHead);
                    }
                }
            }
        } catch (Exception e) {
            if (mUserProfileName != null) {
                mUserProfileName.setText("Guest / Sign In");
            }
        }
    }

    @Nullable
    private Bitmap getAppliedSkinHead() {
        File skinFile = new File(Tools.DIR_GAME_NEW, "skins/current_skin.png");
        if (!skinFile.isFile()) return null;

        Bitmap skin = BitmapFactory.decodeFile(skinFile.getAbsolutePath());
        if (skin == null) return null;
        try {
            return new SkinHeadRenderer().render(160, skin);
        } finally {
            skin.recycle();
        }
    }

    private void showAccountSelectionMenu(View anchorView) {
        if (anchorView == null || getContext() == null) return;
        try {
            Accounts accounts = Accounts.load();
            PopupMenu popup = new PopupMenu(requireContext(), anchorView);
            Account currentAcc = Accounts.getCurrent();

            if (accounts != null && !accounts.accounts.isEmpty()) {
                for (int i = 0; i < accounts.accounts.size(); i++) {
                    Account acc = accounts.accounts.get(i);
                    boolean isSelected = currentAcc != null && acc.mSaveLocation != null && currentAcc.mSaveLocation != null
                            && acc.mSaveLocation.getName().equals(currentAcc.mSaveLocation.getName());
                    String label = (isSelected ? "✓  " : "    ") + acc.username;
                    popup.getMenu().add(0, i, i, label);
                }
            }

            int addAccountItemId = 1000;
            int skinsItemId = 2000;
            popup.getMenu().add(1, skinsItemId, 2000, "🖌️ Skins & Capes");
            popup.getMenu().add(1, addAccountItemId, 1000, "+ Add / Manage Account");

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == addAccountItemId) {
                    Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null);
                    return true;
                } else if (itemId == skinsItemId) {
                    Tools.swapFragment(requireActivity(), SkinCapeFragment.class, SkinCapeFragment.TAG, null);
                    return true;
                } else if (accounts != null && itemId >= 0 && itemId < accounts.accounts.size()) {
                    Account selectedAcc = accounts.accounts.get(itemId);
                    Accounts.setCurrent(selectedAcc);
                    refreshAccountUI();
                    Toast.makeText(requireContext(), "Active account: " + selectedAcc.username, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popup.show();
        } catch (Exception e) {
            Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null);
        }
    }

    private void setupInstancesRecyclerView(Instances instances) {
        View view = getView();
        if (view == null || instances == null) return;
        RecyclerView instancesList = view.findViewById(R.id.instances_list);
        View emptyView = view.findViewById(R.id.empty_instances_view);

        if (instancesList == null) return;

        if (instances.list == null || instances.list.isEmpty()) {
            instancesList.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);
        instancesList.setVisibility(View.VISIBLE);

        instancesList.setLayoutManager(new LinearLayoutManager(getContext()));
        instancesList.setAdapter(new RecyclerView.Adapter<HomeInstanceViewHolder>() {
            @NonNull
            @Override
            public HomeInstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instance, parent, false);
                return new HomeInstanceViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull HomeInstanceViewHolder holder, int position) {
                DisplayInstance displayInst = instances.list.get(position);
                boolean isSelected = position == instances.selectedIndex;

                String displayTitle = Tools.validOrNullString(displayInst.name) != null ? displayInst.name : displayInst.versionId;
                holder.name.setText(displayTitle);
                holder.version.setText(displayInst.versionId);
                holder.status.setText("● Ready");

                if (isSelected) {
                    holder.root.setBackgroundResource(R.drawable.bg_settings_category_selected);
                } else {
                    holder.root.setBackgroundResource(R.drawable.cryonix_settings_item);
                }

                attachTouchFeedback(holder.root, v -> {
                    Instances.setSelectedInstance(displayInst);
                    updateSelectedInstanceDetails();
                });

                if (holder.playBtn != null) {
                    attachTouchFeedback(holder.playBtn, v -> {
                        Instances.setSelectedInstance(displayInst);
                        ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
                    });
                }

                if (holder.menuBtn != null) {
                    attachTouchFeedback(holder.menuBtn, v -> {
                        if (mVersionSpinner != null) {
                            mVersionSpinner.openProfileEditor(requireActivity());
                        } else {
                            Tools.swapFragment(requireActivity(), InstanceEditorFragment.class, InstanceEditorFragment.TAG, null);
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return instances.list.size();
            }
        });
    }

    private static class HomeInstanceViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView name;
        TextView version;
        TextView status;
        ImageButton menuBtn;
        ImageButton playBtn;

        HomeInstanceViewHolder(View v) {
            super(v);
            root = v.findViewById(R.id.instance_card_root);
            name = v.findViewById(R.id.instance_name);
            version = v.findViewById(R.id.instance_version);
            status = v.findViewById(R.id.instance_status);
            menuBtn = v.findViewById(R.id.instance_menu);
            playBtn = v.findViewById(R.id.instance_play_small);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true);
        updateSelectedInstanceDetails();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ProgressKeeper.removeTaskCountListener(mTaskCountListener);
    }

    private void runInstallerWithConfirmation() {
        if (ProgressKeeper.getTaskCount() == 0) {
            mModInstallerLauncher.launch(null);
        } else Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }
}
