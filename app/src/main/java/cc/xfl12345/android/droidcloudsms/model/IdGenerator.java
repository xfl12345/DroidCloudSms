package cc.xfl12345.android.droidcloudsms.model;

public class IdGenerator {
    private final Object lock = new Object();

    private int id = 0;

    public IdGenerator() {}

    public IdGenerator(int id) {
        this.id = id;
    }


    public int getPreparedId() {
        return id;
    }

    public int generate() {
        synchronized (lock) {
            int result = id;
            id += 1;
            return result;
        }
    }

}
