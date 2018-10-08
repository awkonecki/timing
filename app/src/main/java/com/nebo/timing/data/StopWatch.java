package com.nebo.timing.data;

import android.support.annotation.NonNull;
import android.util.Log;

public class StopWatch {
    private CountUpTimer mCountUpTimer = null;
    private final long mFutureTime, mIntervalTime;
    private long mMilliSeconds = 0L, mBaseMilliSeconds = 0L;
    private StopWatchState mState = StopWatchState.STOPPED;
    private StopWatchTickEvents mCallback = null;

    private class CountUpTimerInterface implements CountUpTimer.OnTimerEvents {
        @Override
        public void onTick(long millisUntilFinished) {
            mMilliSeconds += (mFutureTime - millisUntilFinished);
            mCallback.tickEvent(mMilliSeconds + mBaseMilliSeconds);
        }

        @Override
        public void onFinish() {
            mBaseMilliSeconds += mFutureTime;
            mMilliSeconds = 0L;
        }
    }

    public enum StopWatchState {
        PLAYING {
            @Override
            public String toString() {
                return "Playing";
            }
        },
        PAUSED {
            @Override
            public String toString() {
                return "Paused";
            }
        },
        STOPPED {
            @Override
            public String toString() {
                return "Stopped";
            }
        }
    }

    public StopWatch(long futureTime, long futureIntervals, @NonNull StopWatchTickEvents callback) {
        mFutureTime = futureTime;
        mIntervalTime = futureIntervals;
        mCallback = callback;
        mCountUpTimer = new CountUpTimer(futureTime, futureIntervals, new CountUpTimerInterface());
    }

    public void play() {
        switch (mState) {
            case PLAYING:
                mCountUpTimer.stop();
                mMilliSeconds = 0L;
                break;
            case PAUSED:
                break;
            case STOPPED:
                mMilliSeconds = 0L;
                break;
            default:
                throw new java.lang.IllegalArgumentException(
                        "Invalid StopWatch object state in play with state of " + mState.toString()
                );

        }

        mCountUpTimer.play();
        mState = StopWatchState.PLAYING;
    }

    public void stop() {
        switch (mState) {
            case PLAYING:
            case PAUSED:
            case STOPPED:
                mMilliSeconds = 0L;
                break;
            default:
                throw new java.lang.IllegalArgumentException(
                        "Invalid StopWatch object state in stop with state of " + mState.toString()
                );
        }

        mCountUpTimer.stop();
        mState = StopWatchState.STOPPED;
    }

    public void pause() {
        switch (mState) {
            case PLAYING:
            case PAUSED:
            case STOPPED:
                break;
            default:
                throw new java.lang.IllegalArgumentException(
                        "Invalid StopWatch object state in pause with state of " + mState.toString()
                );
        }

        mCountUpTimer.pause();
        mState = StopWatchState.PAUSED;
    }

    public StopWatchState getState() {
        return mState;
    }

    public interface StopWatchTickEvents {
        void tickEvent(long milliSecondsElapsed);
    }
}
