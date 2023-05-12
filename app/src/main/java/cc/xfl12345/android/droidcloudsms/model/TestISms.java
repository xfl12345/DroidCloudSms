package cc.xfl12345.android.droidcloudsms.model;

public interface TestISms {
    boolean isSmsSimPickActivityNeeded(int subId);

    int getPreferredSmsSubscription();
}
