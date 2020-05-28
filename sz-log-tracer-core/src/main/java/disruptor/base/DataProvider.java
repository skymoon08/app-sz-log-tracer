package disruptor.base;

public interface DataProvider<T> {
    T get(long sequence);
}
