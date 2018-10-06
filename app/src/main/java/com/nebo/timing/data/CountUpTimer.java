package com.nebo.timing.data;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;

public class CountUpTimer  {
    private final OnTimerEvents mCallback;
    private final long mMillisInFuture;
    private final long mCountDownInterval;
    private Timer mCountDownTimer;
    private long mTimeRemaining = -1;

    // Provide a way to define an interface that of which the CountUpTimer will
    // execute in the `onFinish` method.
    public interface OnTimerEvents {
        void onTick(long millisUntilFinished);
        void onFinish();
    }

    public CountUpTimer(long futureMillis, long interval, @NonNull OnTimerEvents callback) {
        if (futureMillis < 0 || interval < 0) {
            throw new java.lang.IllegalArgumentException("Invalid timer values");
        }
        mMillisInFuture = futureMillis;
        mCountDownInterval = interval;
        mCallback = callback;
    }

    // Support if the timer needs to be stopped.
    public void stop() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mTimeRemaining = -1;
    }

    public void play() {
        long time = 0;

        if (mTimeRemaining != -1) {
            time = mTimeRemaining;
        }
        else {
            time = mMillisInFuture;
        }

        mCountDownTimer = new Timer(time, mCountDownInterval);
        mCountDownTimer.start();
    }

    public void pause() {
        // Can cancel the remaining time but leave the time remaining alone such
        // that if the timer is restarted it will use the `time remaining`.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private class Timer extends CountDownTimer {
        private Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            CountUpTimer.this.onTick(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            CountUpTimer.this.onFinish();
        }
    }

    private void onTick(long millisUntilFinished) {
        // 1. need to save state to support pause & resume functionality.
        mTimeRemaining = millisUntilFinished;

        // 2. Operate on the caller defined onTick method.
        mCallback.onTick(millisUntilFinished);
    }

    private void onFinish() {
        // 1. use the instance provided call back.
        mCallback.onFinish();

        // 2. clear the time remaining.
        mTimeRemaining = -1;

        // 3. Reset the timer to keep going.
        play();
    }
}
