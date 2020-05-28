package disruptor.event;


public interface EventFactory<T> {
    /*
     * Implementations should instantiate an event object, with all memory already allocated where possible.
     */
    T newInstance();
}