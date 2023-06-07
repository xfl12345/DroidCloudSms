package cc.xfl12345.android.droidcloudsms.model;

import org.teasoft.bee.android.CreateAndUpgrade;
import org.teasoft.honey.osql.autogen.Ddl;

import cc.xfl12345.android.droidcloudsms.model.database.NotificationLog;
import cc.xfl12345.android.droidcloudsms.model.database.SmsLog;

public class BeeCreateAndUpgrade implements CreateAndUpgrade {
    @Override
    public void onCreate() {
        Ddl.createTable(SmsLog.class, false);
        Ddl.createTable(NotificationLog.class, false);
    }

    @Override
    public void onUpgrade(int oldVersion, int newVersion) {

    }
}
