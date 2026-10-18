package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.game.renderer.RendererCache;
import net.kdt.pojavlaunch.instances.Instance;
import net.kdt.pojavlaunch.instances.InstanceInstaller;
import net.kdt.pojavlaunch.instances.Instances;
import net.kdt.pojavlaunch.modloaders.FabricVersion;
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils;
import net.kdt.pojavlaunch.modloaders.ForgelikeUtils;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.RTSpinnerAdapter;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.profiles.VersionSelectorDialog;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.CropperUtils;
import net.kdt.pojavlaunch.utils.JellyAnimations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileTypeSelectFragment extends Fragment implements CropperUtils.CropperReceiver {
    public static final String TAG = "ProfileTypeSelectFragment";

    private final ActivityResultLauncher<?> mCropperLauncher = CropperUtils.registerCropper(this, this);

    private String mSelectedGameVersion = "1.20.1";
    private String mSelectedLoader = "vanilla"; // vanilla, fabric, forge, neoforge, quilt, optifine, legacy_fabric, modpack, bta
    private String mSelectedLoaderVersion = null;
    private String mSelectedControlLayout = null;
    private Bitmap mCustomIconBitmap = null;
    private int mRecommendedIconSize = 128;
    private boolean mAdvancedExpanded = false;

    private List<String> mRenderNames;

    // View References
    private EditText mInstanceNameInput, mJvmArgsInput;
    private TextView mVersionText, mCtrlText, mPathInfoText;
    private TextView mPreviewName, mPreviewVersion, mPreviewLoader, mPreviewRuntime, mPreviewRenderer, mPreviewPath, mPreviewStatus;
    private ImageView mInstanceIcon, mPreviewIcon, mAdvancedArrow;
    private Spinner mLoaderSpinner, mRuntimeSpinner, mRendererSpinner;
    private View mLoaderVersionCard, mAdvancedContent, mAdvancedHeader;
    private ProgressBar mLoaderProgress;
    private CheckBox mSharedDataCheckBox;
    private Button mVersionSelectBtn, mCtrlSelectBtn, mSubmitBtn, mCancelBtn;

    // Loader Tiles
    private View mTileVanilla, mTileFabric, mTileForge, mTileNeoforge, mTileQuilt, mTileOptifine, mTileLegacyFabric, mTileModpack, mTileBta;

    public ProfileTypeSelectFragment() {
        super(R.layout.fragment_profile_type);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String customControl = (String) ExtraCore.consumeValue(ExtraConstants.FILE_SELECTOR);
        if (customControl != null) {
            mSelectedControlLayout = customControl;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupSpinners(view.getContext());
        setupListeners();
        selectLoaderTile("vanilla");
        updatePreview();

        JellyAnimations.animateScreen(view);
        JellyAnimations.attachTouchFeedback(view);
        JellyAnimations.pressFeedback(mVersionSelectBtn, mCtrlSelectBtn, mSubmitBtn, mCancelBtn);
        JellyAnimations.pressFeedback(mTileVanilla, mTileFabric, mTileForge, mTileNeoforge, mTileQuilt, mTileOptifine, mTileLegacyFabric, mTileModpack, mTileBta);
    }

    private void bindViews(@NonNull View view) {
        View backBtn = view.findViewById(R.id.create_instance_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> Tools.backToMainMenu(requireActivity()));
        }

        View iconContainer = view.findViewById(R.id.create_instance_icon_container);
        mInstanceIcon = view.findViewById(R.id.create_instance_icon);
        if (iconContainer != null) {
            iconContainer.setOnClickListener(v -> {
                mRecommendedIconSize = Math.max(mInstanceIcon.getWidth(), mInstanceIcon.getHeight());
                if (mRecommendedIconSize <= 0) mRecommendedIconSize = 128;
                CropperUtils.startCropper(mCropperLauncher);
            });
        }

        mInstanceNameInput = view.findViewById(R.id.create_instance_name_input);
        mPathInfoText = view.findViewById(R.id.create_instance_path_info);

        mVersionText = view.findViewById(R.id.create_instance_version_text);
        mVersionSelectBtn = view.findViewById(R.id.create_instance_version_btn);

        mTileVanilla = view.findViewById(R.id.loader_tile_vanilla);
        mTileFabric = view.findViewById(R.id.loader_tile_fabric);
        mTileForge = view.findViewById(R.id.loader_tile_forge);
        mTileNeoforge = view.findViewById(R.id.loader_tile_neoforge);
        mTileQuilt = view.findViewById(R.id.loader_tile_quilt);
        mTileOptifine = view.findViewById(R.id.loader_tile_optifine);
        mTileLegacyFabric = view.findViewById(R.id.loader_tile_legacy_fabric);
        mTileModpack = view.findViewById(R.id.loader_tile_modpack);
        mTileBta = view.findViewById(R.id.loader_tile_bta);

        mLoaderVersionCard = view.findViewById(R.id.create_instance_loader_version_card);
        mLoaderSpinner = view.findViewById(R.id.create_instance_loader_spinner);
        mLoaderProgress = view.findViewById(R.id.create_instance_loader_progress);

        mAdvancedHeader = view.findViewById(R.id.create_instance_advanced_header);
        mAdvancedContent = view.findViewById(R.id.create_instance_advanced_content);
        mAdvancedArrow = view.findViewById(R.id.create_instance_advanced_arrow);

        mCtrlText = view.findViewById(R.id.create_instance_ctrl_text);
        mCtrlSelectBtn = view.findViewById(R.id.create_instance_ctrl_btn);
        mRuntimeSpinner = view.findViewById(R.id.create_instance_runtime_spinner);
        mRendererSpinner = view.findViewById(R.id.create_instance_renderer_spinner);
        mJvmArgsInput = view.findViewById(R.id.create_instance_jvm_args);
        mSharedDataCheckBox = view.findViewById(R.id.create_instance_shared_data);

        mPreviewIcon = view.findViewById(R.id.preview_instance_icon);
        mPreviewName = view.findViewById(R.id.preview_instance_name);
        mPreviewVersion = view.findViewById(R.id.preview_instance_version);
        mPreviewLoader = view.findViewById(R.id.preview_instance_loader);
        mPreviewRuntime = view.findViewById(R.id.preview_instance_runtime);
        mPreviewRenderer = view.findViewById(R.id.preview_instance_renderer);
        mPreviewPath = view.findViewById(R.id.preview_instance_path);
        mPreviewStatus = view.findViewById(R.id.preview_instance_status);

        mSubmitBtn = view.findViewById(R.id.create_instance_submit_btn);
        mCancelBtn = view.findViewById(R.id.create_instance_cancel_btn);

        if (mSelectedControlLayout != null) {
            mCtrlText.setText(mSelectedControlLayout);
        }
    }

    private void setupSpinners(@NonNull Context context) {
        // Runtime Spinner
        List<Runtime> runtimes = MultiRTUtils.getRuntimes();
        mRuntimeSpinner.setAdapter(new RTSpinnerAdapter(context, runtimes));
        mRuntimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Renderer Spinner
        RendererCache list = RendererCache.getCompatibleRenderers(context);
        mRenderNames = list.rendererIds;
        List<String> renderList = new ArrayList<>(list.rendererDisplayNames.length + 1);
        renderList.addAll(Arrays.asList(list.rendererDisplayNames));
        renderList.add(context.getString(R.string.global_default));
        mRendererSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_simple_list_1, renderList));
        mRendererSpinner.setSelection(renderList.size() - 1);
        mRendererSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        mInstanceNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        View.OnClickListener versionSelectListener = v ->
                VersionSelectorDialog.open(requireContext(), false, (id, snapshot) -> {
                    mSelectedGameVersion = id;
                    mVersionText.setText(id);
                    updateLoaderVersions();
                    updatePreview();
                });
        mVersionSelectBtn.setOnClickListener(versionSelectListener);
        mVersionText.setOnClickListener(versionSelectListener);

        mCtrlSelectBtn.setOnClickListener(v -> {
            Bundle bundle = new Bundle(3);
            bundle.putBoolean(FileSelectorFragment.BUNDLE_SELECT_FOLDER, false);
            bundle.putString(FileSelectorFragment.BUNDLE_ROOT_PATH, Tools.CTRLMAP_PATH);
            Tools.swapFragment(requireActivity(), FileSelectorFragment.class, FileSelectorFragment.TAG, bundle);
        });

        mAdvancedHeader.setOnClickListener(v -> {
            mAdvancedExpanded = !mAdvancedExpanded;
            mAdvancedContent.setVisibility(mAdvancedExpanded ? View.VISIBLE : View.GONE);
            mAdvancedArrow.setRotation(mAdvancedExpanded ? 90f : 0f);
        });

        // Loader Tile Click Handlers
        if (mTileVanilla != null) mTileVanilla.setOnClickListener(v -> selectLoaderTile("vanilla"));
        if (mTileFabric != null) mTileFabric.setOnClickListener(v -> selectLoaderTile("fabric"));
        if (mTileForge != null) mTileForge.setOnClickListener(v -> selectLoaderTile("forge"));
        if (mTileNeoforge != null) mTileNeoforge.setOnClickListener(v -> selectLoaderTile("neoforge"));
        if (mTileQuilt != null) mTileQuilt.setOnClickListener(v -> selectLoaderTile("quilt"));
        if (mTileOptifine != null) mTileOptifine.setOnClickListener(v -> selectLoaderTile("optifine"));
        if (mTileLegacyFabric != null) mTileLegacyFabric.setOnClickListener(v -> selectLoaderTile("legacy_fabric"));
        if (mTileModpack != null) mTileModpack.setOnClickListener(v -> selectLoaderTile("modpack"));
        if (mTileBta != null) mTileBta.setOnClickListener(v -> selectLoaderTile("bta"));

        mCancelBtn.setOnClickListener(v -> Tools.backToMainMenu(requireActivity()));
        mSubmitBtn.setOnClickListener(v -> performCreation());
    }

    private void selectLoaderTile(String loaderId) {
        mSelectedLoader = loaderId;
        mSelectedLoaderVersion = null;

        updateTileVisuals();

        boolean showLoaderVersion = "fabric".equals(loaderId) || "forge".equals(loaderId)
                || "neoforge".equals(loaderId) || "quilt".equals(loaderId)
                || "legacy_fabric".equals(loaderId);

        if (mLoaderVersionCard != null) {
            mLoaderVersionCard.setVisibility(showLoaderVersion ? View.VISIBLE : View.GONE);
        }

        if (showLoaderVersion) {
            updateLoaderVersions();
        }

        updatePreview();
    }

    private void updateTileVisuals() {
        setTileSelected(mTileVanilla, "vanilla".equals(mSelectedLoader));
        setTileSelected(mTileFabric, "fabric".equals(mSelectedLoader));
        setTileSelected(mTileForge, "forge".equals(mSelectedLoader));
        setTileSelected(mTileNeoforge, "neoforge".equals(mSelectedLoader));
        setTileSelected(mTileQuilt, "quilt".equals(mSelectedLoader));
        setTileSelected(mTileOptifine, "optifine".equals(mSelectedLoader));
        setTileSelected(mTileLegacyFabric, "legacy_fabric".equals(mSelectedLoader));
        setTileSelected(mTileModpack, "modpack".equals(mSelectedLoader));
        setTileSelected(mTileBta, "bta".equals(mSelectedLoader));
    }

    private void setTileSelected(View tile, boolean isSelected) {
        if (tile == null) return;
        tile.setBackgroundResource(isSelected ? R.drawable.bg_card_selected : R.drawable.bg_card_elevated);
        if (isSelected) {
            JellyAnimations.springScale(tile, 1.02f);
        } else {
            tile.setScaleX(1.0f);
            tile.setScaleY(1.0f);
        }
    }

    private void updateLoaderVersions() {
        if ("vanilla".equals(mSelectedLoader) || "optifine".equals(mSelectedLoader)
                || "modpack".equals(mSelectedLoader) || "bta".equals(mSelectedLoader)) {
            return;
        }

        if (mLoaderProgress != null) mLoaderProgress.setVisibility(View.VISIBLE);
        if (mLoaderSpinner != null) mLoaderSpinner.setAdapter(null);

        if ("fabric".equals(mSelectedLoader) || "quilt".equals(mSelectedLoader) || "legacy_fabric".equals(mSelectedLoader)) {
            FabriclikeUtils utils = FabriclikeUtils.FABRIC_UTILS;
            if ("quilt".equals(mSelectedLoader)) utils = FabriclikeUtils.QUILT_UTILS;
            else if ("legacy_fabric".equals(mSelectedLoader)) utils = FabriclikeUtils.LEGACY_FABRIC_UTILS;

            final FabriclikeUtils finalUtils = utils;
            PojavApplication.sExecutorService.execute(() -> {
                try {
                    FabricVersion[] versions = finalUtils.downloadLoaderVersions(mSelectedGameVersion);
                    Tools.runOnUiThread(() -> {
                        if (mLoaderProgress != null) mLoaderProgress.setVisibility(View.GONE);
                        if (versions != null && versions.length > 0) {
                            ArrayAdapter<FabricVersion> adapter = new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_spinner_dropdown_item, versions);
                            mLoaderSpinner.setAdapter(adapter);
                            mSelectedLoaderVersion = versions[0].version;
                            mLoaderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    mSelectedLoaderVersion = ((FabricVersion) parent.getItemAtPosition(position)).version;
                                    updatePreview();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                        }
                        updatePreview();
                    });
                } catch (Exception e) {
                    Tools.runOnUiThread(() -> {
                        if (mLoaderProgress != null) mLoaderProgress.setVisibility(View.GONE);
                    });
                }
            });
        } else if ("forge".equals(mSelectedLoader) || "neoforge".equals(mSelectedLoader)) {
            ForgelikeUtils utils = ForgelikeUtils.FORGE_UTILS;
            if ("neoforge".equals(mSelectedLoader)) utils = ForgelikeUtils.NEOFORGE_UTILS;

            final ForgelikeUtils finalUtils = utils;
            PojavApplication.sExecutorService.execute(() -> {
                try {
                    List<String> versions = finalUtils.downloadVersions();
                    Tools.runOnUiThread(() -> {
                        if (mLoaderProgress != null) mLoaderProgress.setVisibility(View.GONE);
                        if (versions != null && !versions.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_spinner_dropdown_item, versions);
                            mLoaderSpinner.setAdapter(adapter);
                            mSelectedLoaderVersion = versions.get(0);
                            mLoaderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    mSelectedLoaderVersion = (String) parent.getItemAtPosition(position);
                                    updatePreview();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                        }
                        updatePreview();
                    });
                } catch (Exception e) {
                    Tools.runOnUiThread(() -> {
                        if (mLoaderProgress != null) mLoaderProgress.setVisibility(View.GONE);
                    });
                }
            });
        }
    }

    private String getComputedDefaultName() {
        String capitalizedLoader = Character.toUpperCase(mSelectedLoader.charAt(0)) + mSelectedLoader.substring(1);
        if ("vanilla".equals(mSelectedLoader)) capitalizedLoader = "Minecraft";
        else if ("neoforge".equals(mSelectedLoader)) capitalizedLoader = "NeoForge";

        return capitalizedLoader + " " + mSelectedGameVersion;
    }

    private void updatePreview() {
        if (getView() == null) return;

        String typedName = mInstanceNameInput.getText().toString().trim();
        String finalName = typedName.isEmpty() ? getComputedDefaultName() : typedName;

        mPreviewName.setText(finalName);
        mPathInfoText.setText(".minecraft/instances/" + finalName);
        mPreviewPath.setText(".../instances/" + finalName);
        mPreviewVersion.setText(mSelectedGameVersion);

        String capitalizedLoader = Character.toUpperCase(mSelectedLoader.charAt(0)) + mSelectedLoader.substring(1);
        if ("vanilla".equals(mSelectedLoader)) capitalizedLoader = "Vanilla";
        else if ("neoforge".equals(mSelectedLoader)) capitalizedLoader = "NeoForge";
        else if ("legacy_fabric".equals(mSelectedLoader)) capitalizedLoader = "Legacy Fabric";

        if (mSelectedLoaderVersion != null && !mSelectedLoaderVersion.isEmpty()) {
            mPreviewLoader.setText(capitalizedLoader + " v" + mSelectedLoaderVersion);
        } else {
            mPreviewLoader.setText(capitalizedLoader);
        }

        // Preview Icon
        if (mCustomIconBitmap != null) {
            mInstanceIcon.setImageBitmap(mCustomIconBitmap);
            mPreviewIcon.setImageBitmap(mCustomIconBitmap);
        } else {
            int iconRes = R.drawable.cryonixlauncher;
            if ("fabric".equals(mSelectedLoader) || "legacy_fabric".equals(mSelectedLoader)) iconRes = R.drawable.ic_fabric;
            else if ("forge".equals(mSelectedLoader)) iconRes = R.drawable.ic_forge;
            else if ("neoforge".equals(mSelectedLoader)) iconRes = R.drawable.ic_neoforge;
            else if ("quilt".equals(mSelectedLoader)) iconRes = R.drawable.ic_quilt;

            mInstanceIcon.setImageResource(iconRes);
            mPreviewIcon.setImageResource(iconRes);
        }

        // Runtime Preview
        Runtime runtime = (Runtime) mRuntimeSpinner.getSelectedItem();
        if (runtime != null && runtime.name != null && !runtime.name.equals("<Default>")) {
            mPreviewRuntime.setText(runtime.name);
        } else {
            mPreviewRuntime.setText("Default Java");
        }

        // Renderer Preview
        if (mRendererSpinner.getSelectedItem() != null) {
            mPreviewRenderer.setText(mRendererSpinner.getSelectedItem().toString());
        } else {
            mPreviewRenderer.setText("Default Renderer");
        }
    }

    private String getSelectedRuntime() {
        Runtime selectedRuntime = (Runtime) mRuntimeSpinner.getSelectedItem();
        if (selectedRuntime == null || selectedRuntime.name == null
                || selectedRuntime.name.equals("<Default>") || selectedRuntime.versionString == null) {
            return null;
        }
        return selectedRuntime.name;
    }

    private String getSelectedRenderer() {
        if (mRenderNames == null || mRendererSpinner.getSelectedItemPosition() == mRenderNames.size()) {
            return null;
        }
        return mRenderNames.get(mRendererSpinner.getSelectedItemPosition());
    }

    private void performCreation() {
        if (ProgressKeeper.hasOngoingTasks()) {
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_SHORT).show();
            return;
        }

        String typedName = mInstanceNameInput.getText().toString().trim();
        final String finalName = typedName.isEmpty() ? getComputedDefaultName() : typedName;

        if ("optifine".equals(mSelectedLoader)) {
            Tools.swapFragment(requireActivity(), OptiFineInstallFragment.class, OptiFineInstallFragment.TAG, null);
            return;
        } else if ("modpack".equals(mSelectedLoader)) {
            Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, null);
            return;
        } else if ("bta".equals(mSelectedLoader)) {
            Tools.swapFragment(requireActivity(), BTAInstallFragment.class, BTAInstallFragment.TAG, null);
            return;
        }

        if ("vanilla".equals(mSelectedLoader)) {
            try {
                Instance instance = Instances.createInstance(i -> {
                    i.name = finalName;
                    i.versionId = mSelectedGameVersion;
                    i.selectedRuntime = getSelectedRuntime();
                    i.renderer = getSelectedRenderer();
                    i.controlLayout = mSelectedControlLayout;
                    i.jvmArgs = mJvmArgsInput.getText().toString().trim();
                    i.sharedData = mSharedDataCheckBox.isChecked();
                    if (mCustomIconBitmap != null) {
                        try {
                            i.encodeNewIcon(mCustomIconBitmap);
                        } catch (Exception ignored) {}
                    }
                }, mSelectedGameVersion);
                Instances.setSelectedInstance(instance);
                Toast.makeText(requireContext(), R.string.create_instance_success, Toast.LENGTH_SHORT).show();
                Tools.backToMainMenu(requireActivity());
            } catch (IOException e) {
                Tools.showError(requireContext(), e);
            }
        } else if ("fabric".equals(mSelectedLoader) || "quilt".equals(mSelectedLoader) || "legacy_fabric".equals(mSelectedLoader)) {
            FabriclikeUtils utils = FabriclikeUtils.FABRIC_UTILS;
            if ("quilt".equals(mSelectedLoader)) utils = FabriclikeUtils.QUILT_UTILS;
            else if ("legacy_fabric".equals(mSelectedLoader)) utils = FabriclikeUtils.LEGACY_FABRIC_UTILS;

            final FabriclikeUtils finalUtils = utils;
            mSubmitBtn.setEnabled(false);
            PojavApplication.sExecutorService.execute(() -> {
                try {
                    String versionId = finalUtils.install(mSelectedGameVersion, mSelectedLoaderVersion);
                    if (versionId != null) {
                        Instance instance = Instances.createInstance(i -> {
                            i.name = finalName;
                            i.icon = finalUtils.getIconName();
                            i.versionId = versionId;
                            i.selectedRuntime = getSelectedRuntime();
                            i.renderer = getSelectedRenderer();
                            i.controlLayout = mSelectedControlLayout;
                            i.jvmArgs = mJvmArgsInput.getText().toString().trim();
                            i.sharedData = mSharedDataCheckBox.isChecked();
                            if (mCustomIconBitmap != null) {
                                try {
                                    i.encodeNewIcon(mCustomIconBitmap);
                                } catch (Exception ignored) {}
                            }
                        }, versionId);
                        Instances.setSelectedInstance(instance);
                        Tools.runOnUiThread(() -> {
                            mSubmitBtn.setEnabled(true);
                            Toast.makeText(requireContext(), R.string.create_instance_success, Toast.LENGTH_SHORT).show();
                            Tools.backToMainMenu(requireActivity());
                        });
                    } else {
                        Tools.runOnUiThread(() -> {
                            mSubmitBtn.setEnabled(true);
                            Toast.makeText(requireContext(), R.string.global_error, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Tools.runOnUiThread(() -> {
                        mSubmitBtn.setEnabled(true);
                        Tools.showError(requireContext(), e);
                    });
                }
            });
        } else if ("forge".equals(mSelectedLoader) || "neoforge".equals(mSelectedLoader)) {
            final ForgelikeUtils utils = "neoforge".equals(mSelectedLoader) ? ForgelikeUtils.NEOFORGE_UTILS : ForgelikeUtils.FORGE_UTILS;

            try {
                InstanceInstaller instanceInstaller = utils.createInstaller(mSelectedLoaderVersion);
                Instance instance = Instances.createInstance(i -> {
                    i.name = finalName;
                    i.icon = utils.getIconName();
                    i.installer = instanceInstaller;
                    i.selectedRuntime = getSelectedRuntime();
                    i.renderer = getSelectedRenderer();
                    i.controlLayout = mSelectedControlLayout;
                    i.jvmArgs = mJvmArgsInput.getText().toString().trim();
                    i.sharedData = mSharedDataCheckBox.isChecked();
                    if (mCustomIconBitmap != null) {
                        try {
                            i.encodeNewIcon(mCustomIconBitmap);
                        } catch (Exception ignored) {}
                    }
                }, mSelectedLoaderVersion != null ? mSelectedLoaderVersion : mSelectedGameVersion);
                Instances.setSelectedInstance(instance);
                instanceInstaller.start();
                Toast.makeText(requireContext(), R.string.create_instance_success, Toast.LENGTH_SHORT).show();
                Tools.backToMainMenu(requireActivity());
            } catch (Exception e) {
                Tools.showError(requireContext(), e);
            }
        }
    }

    @Override
    public float getAspectRatio() {
        return 1f;
    }

    @Override
    public int getTargetMaxSide() {
        return mRecommendedIconSize;
    }

    @Override
    public void onCropped(Bitmap contentBitmap) {
        mCustomIconBitmap = contentBitmap;
        updatePreview();
    }

    @Override
    public void onFailed(Exception exception) {
        Tools.showErrorRemote(exception);
    }
}
