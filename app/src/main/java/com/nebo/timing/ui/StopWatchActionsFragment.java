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
    private STATE stopWatchActionState = STATE.STOPED;

    private enum STATE {
        PLAYING,
        STOPED
    }

    public enum ACTIONS {
        Start {
            @Override
            public String toString() {
                return "Start";
            }
        },
        Stop {
            @Override
            public String toString() {
                return "Stop";
            }
        },
        Lap {
            @Override
            public String toString() {
                return "Lap";
            }
        },
        Reset {
            @Override
            public String toString() {
                return "Reset";
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
        return mBinding.getRoot();
    }
}
