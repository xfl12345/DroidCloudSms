package cc.xfl12345.android.droidcloudsms.model;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockHelper {

    private ReentrantReadWriteLock rwLock;

    public ReentrantReadWriteLockHelper() {
        this(true);
    }

    public ReentrantReadWriteLockHelper(boolean fair) {
        rwLock = new ReentrantReadWriteLock(fair);
    }

    public ReentrantReadWriteLock getLock() {
        return rwLock;
    }

    public ReentrantReadWriteLock.WriteLock getWriteLock() {
        return rwLock.writeLock();
    }

    public ReentrantReadWriteLock.ReadLock getReadLock() {
        return rwLock.readLock();
    }

    public void lockWrite() {
        rwLock.writeLock().lock();
    }

    public void unlockWrite() {
        rwLock.writeLock().unlock();
    }

    public void lockRead() {
        rwLock.readLock().lock();
    }

    public void unlockRead() {
        rwLock.readLock().unlock();
    }

}
