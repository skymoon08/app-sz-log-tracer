package disruptor.exception;

/**
 * Used to alert {@link disruptor.event.EventProcessor}s waiting at a {@link disruptor.sequence.SequenceBarrier} of status changes.
 * <p>
 * It does not fill in a stack trace for performance reasons.
 */
public final class AlertException extends Exception {
    /**
     * Pre-allocated exception to avoid garbage generation
     */
    public static final AlertException INSTANCE = new AlertException();

    /**
     * Private constructor so only a single instance exists.
     */
    private AlertException() {
    }

    /**
     * Overridden so the stack trace is not filled in for this exception for performance reasons.
     *
     * @return this instance.
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
