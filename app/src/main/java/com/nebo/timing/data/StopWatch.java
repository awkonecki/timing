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
    private final static long sFUTURE_TIME = (-1L >>> 1);
    private long mIntervalTime = sDEFAULT_TIME_INTERVAL;
    private long mMilliSeconds = 0L, mBaseMilliSeconds = 0L, prevTime = -1L;
    private StopWatchState mState = StopWatchState.STOPPED;
    private StopWatchTickEvents mCallback = null;

    private class CountUpTimerInterface implements CountUpTimer.OnTimerEvents {
        @Override
        public void onTick(long millisUntilFinished) {
            long time = System.currentTimeMillis();
            if (prevTime == -1L) {
                mMilliSeconds = mBaseMilliSeconds;
            }
            else {
                mMilliSeconds += (time - prevTime);
            }

            prevTime = time;
            if (mCallback != null) {
                mCallback.tickEvent(mMilliSeconds);
            }
        }

        @Override
        public void onFinish() {}
    }

    //**********************************************************************************************
    // PUBLIC CLASS MEMBERS
    //**********************************************************************************************

    public final static long sDEFAULT_TIME_INTERVAL = 100L;
    public final static int sPLAY_STATE_VALUE = 0;
    public final static int sPAUSE_STATE_VALUE = 1;
    public final static int sSTOP_STATE_VALUE = 2;

    /**
     * @enum StopWatchState
     * @brief Enumerate the supported states, and description string via the toString method.
     */
    public enum StopWatchState {
         PLAYING (sPLAY_STATE_VALUE) {
            @Override
            public String toString() {
                return "Playing";
            }
            },
         PAUSED (sPAUSE_STATE_VALUE) {
            @Override
            public String toString() {
                return "Paused";
            }
         },
         STOPPED (sSTOP_STATE_VALUE) {
            @Override
            public String toString() {
                return "Stopped";
            }
         };

         private final int number;
         StopWatchState(int number) {
             this.number = number;
         }

         public int getStateValue() {
            return number;
         }
    }

    /**
     * @func unRegisterCallback
     * @brief when the controlling element exists from its life-cycle want to make sure that before
     *        it does that the callback is set to null such that a delivery is not attempted on a
     *        null process definition.
     */
    public void unRegisterCallback() {
        mCallback = null;
    }

    /**
     * @funct registerCallback
     * @brief allows for the owner to re-establish the callback that is responsible for handling
     *        tick events.
     * @param callback - interface defined class that is responsible for defining where onTick
     *                   callbacks are sent.
     */
    public void registerCallback(StopWatchTickEvents callback) {
        mCallback = callback;
    }

    /**
     * @func buildTimeStamp
     * @brief Method responsible for building the time stamp w.r.t the defined number of passed
     *        milli-seconds.
     * @param milliSecondsElapsed - total number of milli-seconds that has elapased since the start
     *                              of a stopwatch instance.
     * @return string that represents the total break down of seconds minutes and hours.
     */
    public static String buildTimeStamp(long milliSecondsElapsed) {
        long totalMilli = milliSecondsElapsed % 1000;
        long totalSeconds = milliSecondsElapsed / 1000L;

        long totalHours = totalSeconds / 3600L;
        long totalMinutes = (totalSeconds - (totalHours * 3600L)) / 60L;
        totalSeconds = (totalSeconds - (totalHours * 3600L) - (totalMinutes * 60L));

        StringBuilder sb = new StringBuilder();

        if (totalHours == 0) {
            sb.append("00:");
        }
        else if (totalHours < 10) {
            sb.append("0").append(Long.toString(totalHours)).append(':');
        }
        else {
            sb.append(Long.toString(totalHours)).append(':');
        }

        if (totalMinutes > 59) {
            throw new java.lang.IllegalArgumentException(
                    "Total Number of minutes must be less than 60 " + Long.toString(totalMinutes)
            );
        }
        else if (totalMinutes < 10) {
            sb.append('0').append(Long.toString(totalMinutes)).append(':');
        }
        else {
            sb.append(Long.toString(totalMinutes)).append(':');
        }

        if (totalSeconds > 59) {
            throw new java.lang.IllegalArgumentException(
                    "Total Number of seconds must be less than 60 " + Long.toString(totalSeconds)
            );
        }
        else if (totalSeconds < 10) {
            sb.append('0').append(Long.toString(totalSeconds));
        }
        else {
            sb.append(Long.toString(totalSeconds));
        }

        sb.append('.').append(Long.toString(totalMilli));

        return sb.toString();
    }

    /**
     * @func StopWatch
     * @brief Constructor for the stopwatch class object.
     * @param callback - controlling class defined interface that informs the object where to send
     *                   the current time elapsed on tick events.
     */
    public StopWatch(@NonNull StopWatchTickEvents callback) {
        mCallback = callback;
        mCountUpTimer = new CountUpTimer(sFUTURE_TIME, mIntervalTime, new CountUpTimerInterface());
    }

    /**
     * @func StopWatch
     * @brief Constructor for the stopwatch class object.
     * @param callback - controlling class defined interface that informs the object where to send
     *                   the current time elapsed on tick events.
     * @param futureIntervals - represents how frequent w.r.t the interval that the time will be
     *                          updated.
     * @param baseTime - indicates the starting base time for the stopwatch.
     */
    public StopWatch(
            @NonNull StopWatchTickEvents callback,
            long futureIntervals,
            long baseTime)
    {
        this(callback);
        mIntervalTime = futureIntervals;
        mBaseMilliSeconds = baseTime;
        mMilliSeconds = baseTime;
    }

    /**
     * @func play
     * @brief transition to the play state for the StopWatch object.
     */
    public void play() {
        switch (mState) {
            case PLAYING:
                mCountUpTimer.stop();
                mMilliSeconds = 0L;
                prevTime = -1;
                break;
            case PAUSED:
                prevTime = System.currentTimeMillis();
                break;
            case STOPPED:
                mMilliSeconds = 0L;
                prevTime = -1;
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
                mMilliSeconds = 0L;
                prevTime = -1;
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
        return mMilliSeconds;
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
