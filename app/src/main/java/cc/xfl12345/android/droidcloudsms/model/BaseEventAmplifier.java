package cc.xfl12345.android.droidcloudsms.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class BaseEventAmplifier<T> {
    protected ConcurrentHashMap<T, Object> listenerSet = new ConcurrentHashMap<>();

    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public void addListener(T listener) {
        try {
            lock.writeLock().lock();
            listenerSet.put(listener, new Object());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeListener(T listener) {
        try {
            lock.writeLock().lock();
            return listenerSet.remove(listener) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clearListener() {
        try {
            lock.writeLock().lock();
            listenerSet.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

}
