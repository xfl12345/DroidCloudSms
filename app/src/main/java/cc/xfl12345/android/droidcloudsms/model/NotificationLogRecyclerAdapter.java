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

import org.teasoft.bee.osql.OrderType;
import org.teasoft.honey.osql.core.ConditionImpl;
import org.teasoft.honey.osql.shortcut.BF;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import at.blogc.android.views.ExpandableTextView;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.database.NotificationLog;

public class NotificationLogRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private NotificationLog emptyLogItem = new NotificationLog();

    private NotificationLog defaultLogItem;

    private List<NotificationLog> items;

    private int itemCount = 0;

    public NotificationLogRecyclerAdapter(Context context) {
        mContext = context;
        // cacheMap = new ConcurrentHashMap<>(32);

        ZonedDateTime utcStart = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC);
        defaultLogItem = new NotificationLog();
        defaultLogItem.setId(-1L);
        defaultLogItem.setTime(utcStart);
        defaultLogItem.setTag("无");
        defaultLogItem.setLogLevel(Log.VERBOSE);
        defaultLogItem.setContent("无");

        ZonedDateTime now = TimeUtils.getNowTimeInISO8601();
        ZonedDateTime tomorrowStartInUTC = now
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(1)
            .withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime yesterdayStartInUTC = tomorrowStartInUTC.minusDays(2);

        MySqliteLockManager.lockWrite();
        items = BF.getSuidRich().select(emptyLogItem, new ConditionImpl()
            .between(
                "utcTimeStamp",
                yesterdayStartInUTC.toInstant().toEpochMilli(),
                tomorrowStartInUTC.toInstant().toEpochMilli()
            )
            .orderBy("utcTimeStamp", OrderType.DESC)
        );
        itemCount = items.size();
        MySqliteLockManager.unlockWrite();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.notification_log_item, parent, false);
        return new NormalHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NormalHolder normalHolder = (NormalHolder) holder;
        NotificationLog finalNotificationLog = items.get(position);
        normalHolder.itemView.post(() -> normalHolder.setData(finalNotificationLog));
    }

    @Override
    public int getItemCount() {
        return itemCount;
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
            timeBox.setText(ZonedDateTime.parse(notificationLog.getTime())
                .withZoneSameInstant(ZoneOffset.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
            );
            contentBox.setText(notificationLog.getContent());

            if (TextViewUtils.getTextViewLines(contentBox, contentBox.getWidth()) <= 1) {
                contentExpandButton.setVisibility(View.INVISIBLE);
            } else {
                contentExpandButton.setVisibility(View.VISIBLE);
            }
        }
    }

}

