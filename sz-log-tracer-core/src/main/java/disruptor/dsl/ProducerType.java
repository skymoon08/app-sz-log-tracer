package disruptor.dsl;

/**
 * Defines producer types to support creation of RingBuffer with correct sequencer and publisher.
 */
public enum ProducerType {
    /**
     * Create a RingBuffer with a single event publisher to the RingBuffer
     */
    SINGLE,

    /**
     * Create a RingBuffer supporting multiple event publishers to the one RingBuffer
     */
    MULTI
}
