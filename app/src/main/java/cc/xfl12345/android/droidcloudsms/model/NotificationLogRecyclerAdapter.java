package cc.xfl12345.android.droidcloudsms.model;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.OrderType;
import org.teasoft.bee.osql.api.SuidRich;
import org.teasoft.honey.osql.core.ConditionImpl;
import org.teasoft.honey.osql.shortcut.BF;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import at.blogc.android.views.ExpandableTextView;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.database.NotificationLog;

public class NotificationLogRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private NotificationLog emptyLogItem = new NotificationLog();

    private NotificationLog defaultLogItem;

    private Map<Integer, NotificationLog> cacheMap;

    private int itemCount = 0;

    public NotificationLogRecyclerAdapter(Context context) {
        mContext = context;
        cacheMap = new ConcurrentHashMap<>(32);

        ZonedDateTime utcStart = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC);
        defaultLogItem = new NotificationLog();
        defaultLogItem.setId(-1L);
        defaultLogItem.setTime(utcStart);
        defaultLogItem.setTag("无");
        defaultLogItem.setLogLevel(Log.VERBOSE);
        defaultLogItem.setContent("无");

        updateItemCount();
        new Thread(() -> {
            getItemAndPreAddCache(0);
        }, NotificationLogRecyclerAdapter.class.getCanonicalName() + "_Constructor").start();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.notification_log_item, parent, false);
        return new NormalHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        new Thread(() -> {
            asyncUpdateItemCount();
            NotificationLog notificationLog = cacheMap.get(position);
            if (notificationLog == null) {
                notificationLog = getItemAndPreAddCache(position);
            }

            NotificationLog finalNotificationLog = notificationLog;
            NormalHolder normalHolder = (NormalHolder) holder;
            normalHolder.itemView.post(() -> normalHolder.setData(finalNotificationLog));
        }, NotificationLogRecyclerAdapter.class.getCanonicalName() + "_onBindViewHolder").start();
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    private void asyncUpdateItemCount() {
        new Thread(() -> {
            synchronized (NotificationLogRecyclerAdapter.class) {
                updateItemCount();
            }
        }, NotificationLogRecyclerAdapter.class.getCanonicalName() + "_asyncUpdateItemCount").start();
    }

    private void updateItemCount() {
        itemCount = BF.getSuidRich().count(emptyLogItem);
    }

    private NotificationLog getItemAndPreAddCache(int position) {
        NotificationLog notificationLog = emptyLogItem;

        synchronized (NotificationLogRecyclerAdapter.class) {
            List<NotificationLog> items = BF.getSuidRich().select(emptyLogItem, new ConditionImpl()
                .orderBy("utcTimeStamp", OrderType.ASC)
                .start(position)
                .size(20)
            );
            if (items.size() > 0) {
                notificationLog = items.get(0);
                for (int i = 0; i < items.size(); i++) {
                    cacheMap.putIfAbsent(position + i, items.get(i));
                }
            }
        }

        return notificationLog;
    }

    public class NormalHolder extends RecyclerView.ViewHolder {
        public View headerBox;

        public ExpandableTextView contentBox;

        public TextView tagBox;

        public TextView timeBox;

        public MaterialButton contentExpandButton;

        public NormalHolder(View itemView) {
            super(itemView);
            headerBox = itemView.findViewById(R.id.header_box);
            contentBox = itemView.findViewById(R.id.content_box);
            tagBox = itemView.findViewById(R.id.tag_box);
            timeBox = itemView.findViewById(R.id.time_box);
            contentExpandButton = itemView.findViewById(R.id.content_expand_button);

            contentBox.collapse();
            contentExpandButton.setText(R.string.click_me_to_expand);
            contentExpandButton.setOnClickListener(v -> {
                // 如果已经展开，点击之后会收起，所以要显示“点我展开”
                contentExpandButton.setText(contentBox.isExpanded()
                    ? R.string.click_me_to_expand
                    : R.string.click_me_to_collapse
                );
                contentBox.toggle();
            });
        }

        public void setData(NotificationLog notificationLog) {
            int headerBoxColor = 0;
            switch (notificationLog.getLogLevel()) {
                case Log.INFO:
                    headerBoxColor = Color.CYAN;
                    break;
                case Log.ERROR:
                    headerBoxColor = Color.RED;
                    break;
                default:
                    headerBoxColor = Color.GRAY;
                    break;
            }
            headerBox.setBackgroundColor(headerBoxColor);
            tagBox.setText(notificationLog.getTag());
            timeBox.setText(ZonedDateTime.parse(notificationLog.getTime()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            contentBox.setText(notificationLog.getContent());
        }
    }

}

