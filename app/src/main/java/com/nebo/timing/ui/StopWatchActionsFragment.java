package com.nebo.timing.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.R;
import com.nebo.timing.databinding.FragmentStopwatchActionsBinding;

public class StopWatchActionsFragment extends Fragment {
    private FragmentStopwatchActionsBinding mBinding = null;
    private StopWatchActions mInterface = null;
    private STATE mStopWatchActionState = STATE.STOPPED;

    private enum STATE {
        PLAYING,
        STOPPED
    }

    public enum ACTIONS {
        Start {
            public String toStringFromContext(Context context) {
                return  context.getString(R.string.fab_action_start);
            }
        },
        Stop {
            public String toStringFromContext(Context context) {
                return  context.getString(R.string.fab_action_stop);
            }
        },
        Lap {
            public String toStringFromContext(Context context) {
                return  context.getString(R.string.fab_action_lap);
            }
        },
        Reset {
            public String toStringFromContext(Context context) {
                return  context.getString(R.string.fab_action_reset);
            }
        }
    }

    public interface StopWatchActions {
        public void handleStopWatchAction(ACTIONS action);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mInterface = (StopWatchActions) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(
                    context.toString() + " must implement StopWatchActions interface."
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_stopwatch_actions,
                container,
                false);

        getString(R.string.fab_action_start);

        // Set the FABs to their correct context based off of the current UI state.
        switch(mStopWatchActionState) {
            case PLAYING:
                mBinding.tvResetLap.setText(getString(R.string.reset));
                mBinding.tvStartStop.setText(getString(R.string.start));
                break;
            case STOPPED:
                mBinding.tvResetLap.setText(getString(R.string.lap));
                mBinding.tvStartStop.setText(getString(R.string.stop));
                break;
        }

        // Setup callbacks for the FABs.
        mBinding.fabResetLap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mBinding.fabStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return mBinding.getRoot();
    }
}
