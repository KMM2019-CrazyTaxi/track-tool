package helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Data wrapper class representing a listenable value.
 *
 * @param <T> Type of stored value
 */
public class UpdateWrapper<T> implements UpdateListener<T> {
    private T data = null;
    private List<UpdateListener<T>> listeners;

    /**
     * UpdateWrapper constructor
     * @param data Initial data value
     */
    public UpdateWrapper(T data) {
        this.data = data;
        listeners = new ArrayList<>();
    }

    /**
     * UpdateWrapper constructor
     */
    public UpdateWrapper() {
        listeners = new ArrayList<>();
    }

    /**
     * Get data value
     * @return Value
     */
    synchronized public T get() {
        return data;
    }

    /**
     * Update the data value. This notifies all listeners and wakes waiters
     * @param data New data value
     */
    synchronized public void update(T data){
        this.data = data;
        notifyListers(data);
    }

    /**
     * Subscribe to value updates of this remote data
     * @param o Object to be notified
     */
    synchronized public void subscribe(UpdateListener<T> o) {
        listeners.add(o);
    }

    /**
     * Unsubscribe to value updates of this remote data
     * @param o Object to be no longer be notified
     */
    synchronized public void unsubscribe(UpdateListener<T> o) {
        listeners.remove(o);
    }

    synchronized private void notifyListers(T data) {
        for (UpdateListener<T> o : listeners) {
            o.update(data);
        }
    }
}
