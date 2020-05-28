package disruptor.event;

import disruptor.base.DataProvider;
import disruptor.sequence.Sequenced;

public interface EventSequencer<T> extends DataProvider<T>, Sequenced {

}
