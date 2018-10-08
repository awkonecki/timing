package com.nebo.timing.data;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * @class StopWatch
 * @brief An object that manages the CountDownTimer class as a private member.  The stop watch
 *        provides a similar API but removes the knowledge of of the current state of the timer
 *        from the controlling class.  Expected behavior is that the controlling class will use this
 *        instead of the `CountUpTimer` directly to manage stop watch behaviors.
 * @member StopWatchState - enum that defines the supported states for the stop watch.
 * @member StopWatch - constructor
 * @member stop - forces a cancel on teh underlying CountDownTimer and clears out the total elapsed
 *                time.
 * @member play - will start the CountDownTimer object underneath.
 * @member pause - keeps the current time that has elapsed.
 * @member getState - obtain the current state of the stopwatch object.
 * @member StopWatchTickEvents - interface that allows for controlling object to receive callback
 *                               events from the stopwatch.
 */
public class StopWatch {
    //**********************************************************************************************
    // PRIVATE CLASS MEMBERS
    //**********************************************************************************************
    private CountUpTimer mCountUpTimer = null;
    private final long mFutureTime, mIntervalTime;
    private long mMilliSeconds = 0L, mBaseMilliSeconds = 0L;
    private StopWatchState mState = StopWatchState.STOPPED;
    private StopWatchTickEvents mCallback = null;

    private class CountUpTimerInterface implements CountUpTimer.OnTimerEvents {
        @Override
        public void onTick(long millisUntilFinished) {
            mCallback.tickEvent((mMilliSeconds - millisUntilFinished) + mBaseMilliSeconds);
            mMilliSeconds = millisUntilFinished;
        }

        @Override
        public void onFinish() {
            mBaseMilliSeconds += mFutureTime;
            mMilliSeconds = mFutureTime;
        }
    }

    //**********************************************************************************************
    // PUBLIC CLASS MEMBERS
    //**********************************************************************************************

    /**
     * @enum StopWatchState
     * @brief Enumerate the supported states, and description string via the toString method.
     */
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

    /**
     * @func StopWatch
     * @brief Constructor for the stopwatch class object.
     * @param futureTime - represents the interval in which the underlying countuptimer cycles on.
     * @param futureIntervals - represents how frequent w.r.t the interval that the time will be
     *                          updated.
     * @param callback - controlling class defined interface that informs the object where to send
     *                   the current time elapsed on tick events.
     */
    public StopWatch(long futureTime, long futureIntervals, @NonNull StopWatchTickEvents callback) {
        mFutureTime = futureTime;
        mIntervalTime = futureIntervals;
        mCallback = callback;
        mCountUpTimer = new CountUpTimer(futureTime, futureIntervals, new CountUpTimerInterface());
    }

    /**
     * @func StopWatch
     * @brief Constructor for the stopwatch class object.
     * @param futureTime - represents the interval in which the underlying countuptimer cycles on.
     * @param futureIntervals - represents how frequent w.r.t the interval that the time will be
     *                          updated.
     * @param callback - controlling class defined interface that informs the object where to send
     *                   the current time elapsed on tick events.
     * @param baseTime - indicates the starting base time for the stopwatch.
     */
    public StopWatch(
            long futureTime,
            long futureIntervals,
            @NonNull StopWatchTickEvents callback,
            long baseTime)
    {
        this(futureTime, futureIntervals, callback);
        mBaseMilliSeconds = baseTime;
    }

    /**
     * @func play
     * @brief transition to the play state for the StopWatch object.
     */
    public void play() {
        switch (mState) {
            case PLAYING:
                mCountUpTimer.stop();
                mMilliSeconds = mFutureTime;
                mBaseMilliSeconds = 0L;
                break;
            case PAUSED:
                break;
            case STOPPED:
                mMilliSeconds = mFutureTime;
                mBaseMilliSeconds = 0L;
                break;
            default:
                throw new java.lang.IllegalArgumentException(
                        "Invalid StopWatch object state in play with state of " + mState.toString()
                );

        }

        mCountUpTimer.play();
        mState = StopWatchState.PLAYING;
    }

    /**
     * @func stop
     * @brief transition to the stop state for the StopWatch object.
     */
    public void stop() {
        switch (mState) {
            case PLAYING:
            case PAUSED:
            case STOPPED:
                mMilliSeconds = mFutureTime;
                mBaseMilliSeconds = 0L;
                break;
            default:
                throw new java.lang.IllegalArgumentException(
                        "Invalid StopWatch object state in stop with state of " + mState.toString()
                );
        }

        mCountUpTimer.stop();
        mState = StopWatchState.STOPPED;
    }

    /**
     * @func pause
     * @brief transition to the pause state for the StopWatchObject
     */
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

    /**
     * @func getState
     * @brief Obtain the current state of which the StopWatch objec is in.
     * @return State of the StopWatch
     */
    public StopWatchState getState() {
        return mState;
    }

    /**
     * @func getLastUpdatedTime
     * @brief allows a way to obtain the last time that elapsed with the StopWatch object.  Does not
     *        represent the entire amount of time elapsed since the StopWatch object only sees up
     *        to the class defined interval amount.
     * @return the elapsed witnessed time by the `StopWatch` object.
     */
    public long getLastUpdatedTime() {
        return mBaseMilliSeconds + mMilliSeconds;
    }

    /**
     * @interface StopWatchTickEvents
     * @brief provides the definition in which the StopWatch object can notify the controlling class
     *        when events occur.
     */
    public interface StopWatchTickEvents {
        void tickEvent(long milliSecondsElapsed);
    }
}
