package disruptor.event;

/**
 * <p>Implementations translate (write) data representations into events claimed from the {@link RingBuffer}.</p>
 *
 * <p>When publishing to the RingBuffer, provide an EventTranslator. The RingBuffer will select the next available
 * event by sequence and provide it to the EventTranslator (which should update the event), before publishing
 * the sequence update.</p>
 *
 * @param <T> event implementation storing the data for sharing during exchange or parallel coordination of an event.
 */
public interface EventTranslator<T> {
    /**
     * Translate a data representation into fields set in given event
     *
     * @param event    into which the data should be translated.
     * @param sequence that is assigned to event.
     */
    void translateTo(final T event, long sequence);
}
