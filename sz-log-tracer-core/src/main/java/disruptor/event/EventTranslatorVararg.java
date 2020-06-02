package disruptor.event;

/**
 * Implementations translate another data representations into events claimed from the {@link disruptor.buffer.RingBuffer}
 *
 * @param <T> event implementation storing the data for sharing during exchange or parallel coordination of an event.
 * @see EventTranslator
 */
public interface EventTranslatorVararg<T> {
    /**
     * Translate a data representation into fields set in given event
     *
     * @param event    into which the data should be translated.
     * @param sequence that is assigned to event.
     * @param args     The array of user arguments.
     */
    void translateTo(final T event, long sequence, final Object... args);
}
