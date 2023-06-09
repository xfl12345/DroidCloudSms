package cc.xfl12345.android.droidcloudsms.model;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MySqliteLockManager {

    private static final ReentrantReadWriteLockHelper rwLock = new ReentrantReadWriteLockHelper();

    public static ReentrantReadWriteLock getLock() {
        return rwLock.getLock();
    }

    public static ReentrantReadWriteLock.WriteLock getWriteLock() {
        return rwLock.getWriteLock();
    }

    public static ReentrantReadWriteLock.ReadLock getReadLock() {
        return rwLock.getReadLock();
    }

    public static void lockWrite() {
        rwLock.lockWrite();
    }

    public static void unlockWrite() {
        rwLock.unlockWrite();
    }

    public static void lockRead() {
        rwLock.lockRead();
    }

    public static void unlockRead() {
        rwLock.unlockRead();
    }


}
