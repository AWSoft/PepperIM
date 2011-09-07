/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.util;

/**
 * Class providing automated control of pausing the execution of the calling
 * thread based on "sleep steps" and "increase times" that are specified in the
 * constructor.
 * @author Felix Wiemuth
 */
public class SleepControl {
    /**
     * Exception type thrown by pepperim.utils.SleepControl constructor if the
     * parameters given are not valid.
     */
    public static class SleepControlSettingsException extends Exception {
        private static final long serialVersionUID = 1L;
        public SleepControlSettingsException(String msg) {
            super(msg);
        }
    }

    //poll frequence in normal mode [0] / suspend modes [1]..[n]
    final private int[] sleepTime;
    //times of null-poll until going into next suspend mode
    final private int[] increaseCount;

    private int cnt;
    private int sleepMode;

    /**
     * Create a new sleep controller with own counter/state and settings
     * @param sleepTime Times (milliseconds) to sleep at different steps/modes:
     *        [0] normal mode, [1]..[n] suspend modes
     * @param increaseTime Times (milliseconds) after which the sleep time should be increased
     *        to the next specified in 'sleepTime':
     *        [0] not used, [1] sleepTime[0]->sleepTime[1], ...
     *        Note that this only includes the time in sleep(), time needed for
     *        further code execution between sleep() calls increases these
     *        times!
     */
    public SleepControl(int[] sleepTime, int[] increaseTime) throws SleepControlSettingsException {
        if (sleepTime.length != increaseTime.length)
            throw new SleepControlSettingsException("'sleepTime' and 'increaseTime' arrays have to be of the same length!");
        this.sleepTime = sleepTime;
        this.increaseCount = increaseTime;
        //transform: time to increase -> count to increase
        for (int i = 0; i < increaseCount.length; i++)
            increaseCount[i] /= sleepTime[i];
        reset();
    }

    /**
     * Count an operation as idle - increment the internal counter towards an
     * increasing of sleep time as specified in constructor
     */
    public void count() {
        cnt++;
        for (int i = sleepMode+1; i < sleepTime.length; i++) {
            if (cnt >= increaseCount[i])
                sleepMode = i;
            else
                break;
        }
    }

    /**
     * Tell SleepControl that an operation was not idle so the sleep time is
     * resetted to the first specified in constructor
     */
    public void reset() {
        cnt = 0;
        sleepMode = 0;
    }

    /**
     * Perform a sleep regarding the current status which depends on parameters
     * given in constructor
     * @throws InterruptedException
     */
    public void sleep() throws InterruptedException {
        Thread.sleep(sleepTime[sleepMode]);
    }

}
