package net.kdt.pojavlaunch.prefs.screens;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import net.kdt.pojavlaunch.LauncherActivity;
import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

/**
 * Preference for the main screen, any sub-screen inherits this class for consistent layout and behavior
 */
public class LauncherPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected Runnable mVisibilityUpdater = () -> {};

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_preference_settings, container, false);
        FrameLayout contentCard = root.findViewById(R.id.preference_fragment_container);

        View preferenceView = super.onCreateView(inflater, contentCard, savedInstanceState);
        if (contentCard != null && preferenceView != null) {
            contentCard.addView(preferenceView);
        }

        setupCategoryNavigation(root);
        return root;
    }

    private void setupCategoryNavigation(View view) {
        View backBtn = view.findViewById(R.id.settings_back_button);
        View homeBtn = view.findViewById(R.id.settings_home_button);

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> Tools.backToMainMenu(requireActivity()));
        }
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> Tools.backToMainMenu(requireActivity()));
        }

        View catVideo = view.findViewById(R.id.cat_video_button);
        View catControls = view.findViewById(R.id.cat_controls_button);
        View catJava = view.findViewById(R.id.cat_java_button);
        View catMisc = view.findViewById(R.id.cat_misc_button);
        View catExperimental = view.findViewById(R.id.cat_experimental_button);

        if (catVideo != null) {
            catVideo.setOnClickListener(v -> Tools.swapFragment(requireActivity(), LauncherPreferenceVideoFragment.class, LauncherActivity.SETTING_FRAGMENT_TAG, null));
        }
        if (catControls != null) {
            catControls.setOnClickListener(v -> Tools.swapFragment(requireActivity(), LauncherPreferenceControlFragment.class, LauncherActivity.SETTING_FRAGMENT_TAG, null));
        }
        if (catJava != null) {
            catJava.setOnClickListener(v -> Tools.swapFragment(requireActivity(), LauncherPreferenceJavaFragment.class, LauncherActivity.SETTING_FRAGMENT_TAG, null));
        }
        if (catMisc != null) {
            catMisc.setOnClickListener(v -> Tools.swapFragment(requireActivity(), LauncherPreferenceMiscellaneousFragment.class, LauncherActivity.SETTING_FRAGMENT_TAG, null));
        }
        if (catExperimental != null) {
            catExperimental.setOnClickListener(v -> Tools.swapFragment(requireActivity(), LauncherPreferenceExperimentalFragment.class, LauncherActivity.SETTING_FRAGMENT_TAG, null));
        }

        // Highlight active category
        if (this instanceof LauncherPreferenceVideoFragment && catVideo != null) {
            catVideo.setBackgroundResource(R.drawable.bg_sidebar_selected);
        } else if (this instanceof LauncherPreferenceControlFragment && catControls != null) {
            catControls.setBackgroundResource(R.drawable.bg_sidebar_selected);
        } else if (this instanceof LauncherPreferenceJavaFragment && catJava != null) {
            catJava.setBackgroundResource(R.drawable.bg_sidebar_selected);
        } else if (this instanceof LauncherPreferenceMiscellaneousFragment && catMisc != null) {
            catMisc.setBackgroundResource(R.drawable.bg_sidebar_selected);
        } else if (this instanceof LauncherPreferenceExperimentalFragment && catExperimental != null) {
            catExperimental.setBackgroundResource(R.drawable.bg_sidebar_selected);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.setBackgroundColor(getResources().getColor(R.color.background_app));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        mVisibilityUpdater = this::updateVisibility;
        addPreferencesFromResource(R.xml.pref_main);
        setupNotificationRequestPreference();
    }

    private void updateVisibility(){
        Preference pref = findPreference("notification_permission_request");
        if (pref != null) {
            pref.setVisible(!getLauncherActivity().checkForPermission(33, Manifest.permission.POST_NOTIFICATIONS));
        }
    }

    private void setupNotificationRequestPreference() {
        Preference mRequestNotificationPermissionPreference = findPreference("notification_permission_request");
        if (mRequestNotificationPermissionPreference != null) {
            Activity activity = getActivity();
            if(activity instanceof LauncherActivity) {
                mRequestNotificationPermissionPreference.setOnPreferenceClickListener(preference -> {
                    ((LauncherActivity) activity).askForPermission(33, Manifest.permission.POST_NOTIFICATIONS);
                    return true;
                });
            }else{
                mRequestNotificationPermissionPreference.setVisible(false);
            }
        }
        updateVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if(sharedPreferences != null) sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mVisibilityUpdater.run();
    }

    @Override
    public void onPause() {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if(sharedPreferences != null) sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        LauncherPreferences.loadPreferences(getContext());
    }

    protected Preference requirePreference(CharSequence key) {
        Preference preference = findPreference(key);
        if(preference != null) return preference;
        throw new IllegalStateException("Preference "+key+" is null");
    }
    @SuppressWarnings("unchecked")
    protected <T extends Preference> T requirePreference(CharSequence key, Class<T> preferenceClass) {
        Preference preference = requirePreference(key);
        if(preferenceClass.isInstance(preference)) return (T)preference;
        throw new IllegalStateException("Preference "+key+" is not an instance of "+preferenceClass.getSimpleName());
    }
    protected LauncherActivity getLauncherActivity(){
        return ((LauncherActivity) getActivity());
    }
}
