package manager;

import java.util.List;

/**
 * 线程选取策略.
 *
 * @author skywalker
 */
public interface ChooseStrategy<T> {

    T choose(Object param);

    void setCandidates(List<T> candidates);

}
