package cc.xfl12345.android.droidcloudsms.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import java.util.List;

import at.blogc.android.views.ExpandableTextView;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.MySqliteLockManager;
import cc.xfl12345.android.droidcloudsms.model.TimeUtils;
import cc.xfl12345.android.droidcloudsms.model.database.SmsLog;

public class SmsLogRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private SmsLog emptyLogItem = new SmsLog();

    private SmsLog defaultLogItem;

    private List<SmsLog> items;

    private int itemCount = 0;

    public SmsLogRecyclerAdapter(Context context) {
        mContext = context;
        // cacheMap = new ConcurrentHashMap<>(32);

        ZonedDateTime utcStart = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC);
        defaultLogItem = new SmsLog();
        defaultLogItem.setId(-1L);
        defaultLogItem.setTime(utcStart);
        defaultLogItem.setValidationCode("无");
        defaultLogItem.setPhoneNumber("0");
        defaultLogItem.setContent("无");

        ZonedDateTime now = TimeUtils.getNowTime();
        ZonedDateTime tomorrowStartInUTC = TimeUtils.getTomorrowStartTimeInUTC(now);
        ZonedDateTime yesterdayStartInUTC = TimeUtils.getYesterdayStartTimeInUTC(now);

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
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.sms_log_item, parent, false);
        return new NormalHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NormalHolder normalHolder = (NormalHolder) holder;
        normalHolder.itemView.post(() -> normalHolder.setData(items.get(position)));
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public class NormalHolder extends RecyclerView.ViewHolder {
        public View headerBox;

        public ExpandableTextView contentBox;

        public TextView phoneNumberBox;

        public TextView timeBox;

        public TextView validationCodeBox;

        public MaterialButton contentExpandButton;

        public NormalHolder(View itemView) {
            super(itemView);
            headerBox = itemView.findViewById(R.id.header_box);
            contentBox = itemView.findViewById(R.id.content_box);
            phoneNumberBox = itemView.findViewById(R.id.phone_number_box);
            timeBox = itemView.findViewById(R.id.time_box);
            validationCodeBox = itemView.findViewById(R.id.validation_code_box);
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

        public void setData(SmsLog smsLog) {
            int headerBoxColor = 0;
            if (smsLog.getSmsResultCode() == null) {
                headerBoxColor = Color.YELLOW;
            } else {
                if (smsLog.getSmsResultCode() == Activity.RESULT_OK) {
                    headerBoxColor = Color.rgb(0, 64, 0);
                } else {
                    headerBoxColor = Color.RED;
                }
            }
            headerBox.setBackgroundColor(headerBoxColor);
            phoneNumberBox.setText(smsLog.getPhoneNumber());
            validationCodeBox.setText(smsLog.getValidationCode());
            timeBox.setText(TimeUtils.parseISO8601Time2InChineseCustom(smsLog.getTime()));
            contentBox.setText(smsLog.getContent());
        }
    }

}

