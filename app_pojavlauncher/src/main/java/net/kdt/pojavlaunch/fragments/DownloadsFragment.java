package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.utils.JellyAnimations;

public class DownloadsFragment extends Fragment {
    public static final String TAG = "DownloadsFragment";

    private TextView mTaskCountText;
    private View mEmptyView;

    private final TaskCountListener mTaskListener = count -> {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> updateTaskState(count));
        }
        return false;
    };

    public DownloadsFragment() {
        super(R.layout.fragment_downloads);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTaskCountText = view.findViewById(R.id.downloads_task_count);
        mEmptyView = view.findViewById(R.id.downloads_empty_view);

        ProgressKeeper.addTaskCountListener(mTaskListener);
        updateTaskState(ProgressKeeper.getTaskCount());

        JellyAnimations.animateScreen(view);
        JellyAnimations.attachTouchFeedback(view);
    }

    private void updateTaskState(int count) {
        if (mTaskCountText != null) {
            mTaskCountText.setText(count + " Tasks");
        }
        if (mEmptyView != null) {
            mEmptyView.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ProgressKeeper.removeTaskCountListener(mTaskListener);
    }
}
