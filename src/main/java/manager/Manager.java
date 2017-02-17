package manager;

import lifecycle.LifeCycle;

/**
 * 对一组线程进行管理.
 *
 * @author skywalker
 */
public interface Manager<T> extends LifeCycle {

    /**
     * 从管理的线程中选取一个.
     */
    T chooseOne(Object param);

}
