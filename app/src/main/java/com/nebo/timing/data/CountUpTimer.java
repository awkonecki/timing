package com.nebo.timing.data;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;

public class CountUpTimer  {
    private final OnTimerEvents mCallback;
    private final long mMillisInFuture;
    private final long mCountDownInterval;
    private Timer mCountDownTimer;

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
        restart();
    }

    // Support if the timer needs to be canceled.
    public void cancel() {
        mCountDownTimer.cancel();
        mCountDownTimer = null;
    }

    public void restart() {
        mCountDownTimer = new Timer(mMillisInFuture, mCountDownInterval);
        mCountDownTimer.start();
    }

    public void pause() {

    }

    private class Timer extends CountDownTimer {
        private Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mCallback.onTick(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            CountUpTimer.this.onFinish();
        }
    }

    private void onFinish() {
        // 1. use the instance provided call back.
        mCallback.onFinish();

        // 2. Reset the timer to keep going.
        restart();
    }
}
