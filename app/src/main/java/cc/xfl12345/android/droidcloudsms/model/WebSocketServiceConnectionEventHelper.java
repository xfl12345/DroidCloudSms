package cc.xfl12345.android.droidcloudsms.model;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import cc.xfl12345.android.droidcloudsms.WebsocketService;

public class WebSocketServiceConnectionEventHelper extends BaseEventAmplifier<WebSocketServiceConnectionListener> implements WebSocketServiceConnectionListener {

    protected WebsocketService service = null;

    protected ReentrantReadWriteLock serviceLock = new ReentrantReadWriteLock(true);

    public WebsocketService getService() {
        try {
            serviceLock.readLock().lock();
            return service;
        } finally {
            serviceLock.readLock().unlock();
        }
    }

    public boolean isConnected() {
        return getService() != null;
    }

    @Override
    public void addListener(WebSocketServiceConnectionListener listener) {
        try {
            serviceLock.readLock().lock();
            try {
                lock.writeLock().lock();
                listenerSet.put(listener, new Object());
                if (service != null) {
                    listener.onServiceConnected(service);
                }
            } finally {
                lock.writeLock().unlock();
            }
        } finally {
            serviceLock.readLock().unlock();
        }
    }

    @Override
    public void onServiceConnected(WebsocketService service) {
        try {
            serviceLock.writeLock().lock();
            this.service = service;
            lock.readLock().lock();
            listenerSet.keySet().parallelStream().forEach(item -> item.onServiceConnected(this.service));
        } finally {
            lock.readLock().unlock();
            serviceLock.writeLock().unlock();
        }
    }

    @Override
    public void onServiceDisconnected() {
        try {
            serviceLock.writeLock().lock();
            this.service = null;
            lock.readLock().lock();
            listenerSet.keySet().parallelStream().forEach(WebSocketServiceConnectionListener::onServiceDisconnected);
        } finally {
            lock.readLock().unlock();
            serviceLock.writeLock().unlock();
        }
    }
}
