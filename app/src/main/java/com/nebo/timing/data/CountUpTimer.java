package com.nebo.timing.data;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;

/**
 * @class CountUpTimer
 * @brief An object that contains the data members of a CountDownTimer class as a private member
 *        class.  This class will manage the intervals of which callbacks will be given to the
 *        controlling class via the `onFinish` and `onTick` methods via a public interface.
 * @member OnTimerEvents - interface that of which the owner of the `CountUpTimer` class must define
 * @member CountUpTimer - constructor
 * @member stop - will cancel the class controlled count down timer, and clear the remaining time.
 * @member play - will setup the countdown timer with the constructor provided future end time and
 *                event tick time.  If a remaining time is not -1 then it will be used instead.
 * @member puase - cancel the class controlled count down timer.
 */
public class CountUpTimer  {
    //**********************************************************************************************
    // PRIVATE CLASS MEMBERS
    //**********************************************************************************************
    private final OnTimerEvents mCallback;
    private final long mMillisInFuture;
    private final long mCountDownInterval;
    private Timer mCountDownTimer;
    private long mTimeRemaining = -1;

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

    //**********************************************************************************************
    // PUBLIC CLASS MEMBERS
    //**********************************************************************************************
    /**
     * @interface OnTimerEvents
     * @brief public interface in which the controlling object is responsible for defining.  If no
     *        interface is defined at time of using the public API methods on the class object then
     *        invalid behavior will occur.
     * @member onTick - callback function that will be called when an `onTick` event occurs at the
     *                  defined interval by the constructor.
     * @member onFinish - callback function that will be called with the count down timer elapses
     *                    locally, prior to the restart of the next count down timer at the control
     *                    class defined future milli-second amount.
     */
    public interface OnTimerEvents {
        void onTick(long millisUntilFinished);
        void onFinish();
    }

    /**
     * @function CountUpTimer
     * @brief Constructor to setup the count up timer class object.
     * @param futureMillis number of milliseconds that of which the count down time will run from.
     * @param interval number of milliseconds in which a callback will be registered from the
     *                 countdown timer to the count up timer class object and the controlling class.
     * @param callback interface class definition that allows for the callback handling for interval
     *                 and completion of the timer w.r.t the controlling class.
     * @note Negative parameters will result in an error being thrown and Null callback will result
     *       in a run-time exception.
     */
    public CountUpTimer(long futureMillis, long interval, @NonNull OnTimerEvents callback) {
        if (futureMillis < 0 || interval < 0) {
            throw new java.lang.IllegalArgumentException("Invalid timer values");
        }
        mMillisInFuture = futureMillis;
        mCountDownInterval = interval;
        mCallback = callback;
    }

    /**
     * @function stop
     * @brief informs the count up timer to cancel the current count down timer and to set its
     *        reference to null in addition to clearing out the time remaining.  If the countdown
     *        timer is null already then just the time remaining is cleared.
     */
    public void stop() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mTimeRemaining = -1;
    }

    /**
     * @function play
     * @brief starts the countdown timer instance with a new class instance.  If time remaining from
     *        a previous `onTick` is not -1 then it will be used and then immediately cleared.
     *        Otherwise the controlling class provided milli-second target and interval amount will
     *        be used.
     */
    public void play() {
        long time = 0;

        if (mTimeRemaining != -1) {
            time = mTimeRemaining;
            mTimeRemaining = -1;
        }
        else {
            time = mMillisInFuture;
        }

        mCountDownTimer = new Timer(time, mCountDownInterval);
        mCountDownTimer.start();
    }

    /**
     * @function pause
     * @brief cancel the current count down time, effectively stopping it.
     */
    public void pause() {
        // Can cancel the remaining time but leave the time remaining alone such
        // that if the timer is restarted it will use the `time remaining`.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }
}
