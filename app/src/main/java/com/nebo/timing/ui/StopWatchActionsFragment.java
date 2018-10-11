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

    private static final int sSTART_STATE = 0;
    private static final int sSTOPPED_STATE = 1;

    private enum STATE {
        PLAYING (sSTART_STATE),
        STOPPED (sSTOPPED_STATE);

        private int mStateValue = sSTOPPED_STATE;
        STATE(int value) {
            mStateValue = value;
        }

        public int getStateValue() {
            return mStateValue;
        }
    }

    public enum ACTIONS {
        Start () {
            public String toStringFromContext(Context context) {
                return  context.getString(R.string.fab_action_start);
            }
        },
        Stop () {
            public String toStringFromContext(Context context) {
                return  context.getString(R.string.fab_action_stop);
            }
        },
        Lap ( ){
            public String toStringFromContext(Context context) {
                return  context.getString(R.string.fab_action_lap);
            }
        },
        Reset () {
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

        if (savedInstanceState != null)
        {
             switch (savedInstanceState.getInt(
                    getString(R.string.key_stopwatch_actions_state), sSTOPPED_STATE)) {
                 case sSTART_STATE:
                     mStopWatchActionState = STATE.PLAYING;
                     break;
                 case sSTOPPED_STATE:
                     mStopWatchActionState = STATE.STOPPED;
                     break;
             }
        }

        // Set the current selection of the FABs.
        setFABText();

        // Setup callbacks for the FABs.
        mBinding.fabResetLap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(mStopWatchActionState) {
                    case PLAYING:
                        // Lap is available - no state transition needs to occur.
                        mInterface.handleStopWatchAction(ACTIONS.Lap);
                        break;
                    case STOPPED:
                        // Reset is available - remain in the stopped state.
                        mInterface.handleStopWatchAction(ACTIONS.Reset);
                        break;
                }
            }
        });

        mBinding.fabStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(mStopWatchActionState) {
                    case PLAYING:
                        // Stop is available - transition to stopped.
                        mInterface.handleStopWatchAction(ACTIONS.Stop);
                        // Update state
                        mStopWatchActionState = STATE.STOPPED;
                        // Update UI
                        setFABText();
                        break;
                    case STOPPED:
                        // Start is available - transition to playing.
                        mInterface.handleStopWatchAction(ACTIONS.Start);
                        // Update state
                        mStopWatchActionState = STATE.PLAYING;
                        // Update UI
                        setFABText();
                        break;
                }
            }
        });

        return mBinding.getRoot();
    }

    private void setFABText() {
        // Set the FABs to their correct context based off of the current UI state.
        switch(mStopWatchActionState) {
            case PLAYING:
                mBinding.tvResetLap.setText(getString(R.string.lap));
                mBinding.tvStartStop.setText(getString(R.string.stop));
                break;
            case STOPPED:
                mBinding.tvResetLap.setText(getString(R.string.reset));
                mBinding.tvStartStop.setText(getString(R.string.start));
                break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.key_stopwatch_actions_state),
                mStopWatchActionState.getStateValue());
    }
}
