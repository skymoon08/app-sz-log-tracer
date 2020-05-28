package disruptor.event;

public interface EventReleaseAware {
    void setEventReleaser(EventReleaser eventReleaser);
}
