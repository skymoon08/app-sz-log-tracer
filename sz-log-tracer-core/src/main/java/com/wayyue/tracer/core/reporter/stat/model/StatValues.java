package com.wayyue.tracer.core.reporter.stat.model;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Constantly updated statistics
 *
 * <p>
 * Note that the CAS mechanism is used here.
 * This mechanism replaces the lock with a failed retry
 *
 * It allows multiple threads to modify an atom at the same time,
 * but when a new value is written and it is found that the new value has been modified,
 * the assignment process needs to be re-executed until it succeeds.
 *
 *
 * It should be noted that the assignment process may be repeatedly executed.
 * The only change allowed in the assignment algorithm is the value of the attribute inside the atom.
 * Other factors cannot be changed.This can improve performance in the absence of most concurrent conflicts.
 *
 * All changes to the value in the AtomicReference must be written to a completely new array,
 * and the values ​​in the existing array cannot be directly changed (so that the atom is modified)
 * </p>
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class StatValues {
    /**
     *  the real value
     */
    private final AtomicReference<long[]> values = new AtomicReference<long[]>();

    public StatValues(long[] values) {
        this.values.set(values);
    }

    /**
     * write new value
     * (All operations need to meet the CAS mechanism)
     * @param update new value need to update
     */
    public void update(long[] update) {
        long[] current;
        long[] tmp = new long[update.length];
        do {
            current = values.get();
            for (int k = 0; k < update.length && k < current.length; k++) {
                tmp[k] = current[k] + update[k];
            }
        } while (!values.compareAndSet(current, tmp));

    }

    /**
     * Empty the slot after print
     *
     * The value may have been updated during printing
     * So pass in the array of values ​​that need to be cleared, minus the value that has been printed.
     *
     * (All operations need to meet the CAS mechanism)
     *
     *  @param toBeClear toBeClear
     */
    public void clear(long[] toBeClear) {
        long[] current;
        long[] tmp = new long[toBeClear.length];
        do {
            current = values.get();
            for (int k = 0; k < current.length && k < toBeClear.length; k++) {
                tmp[k] = current[k] - toBeClear[k];
            }
        } while (!values.compareAndSet(current, tmp));
    }

    /**
     * Return current value
     *
     * Here you can get and return directly, the returned value will definitely not change
     * Because any change to the atoms of values ​​is directly over the reference address of the array.
     * and does not update the values ​​in the array.
     * @return
     */
    public long[] getCurrentValue() {
        return values.get();
    }
}
