package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.push_test1.R;

import java.util.List;

import domain.Alarm;
import domain.Machine;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>{

private List<Alarm> alarmList;

public AlarmAdapter(List<Alarm> alarmList) {
    this.alarmList = alarmList;
}

@NonNull
@Override
public AlarmAdapter.AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // machine_item.xml을 인플레이트하여 뷰를 생성
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.machine_item, parent, false);
    return new AlarmAdapter.AlarmViewHolder(view);
}

@Override
public void onBindViewHolder(@NonNull AlarmAdapter.AlarmViewHolder holder, int position) {
    // 뷰 홀더에 데이터 바인딩
    Alarm currentItem = alarmList.get(position);
    holder.regtime.setText("날짜: " + currentItem.getRegtime());
    holder.hogi.setText("설비명: " + currentItem.getHogi() + " 설비");
    holder.comment.setText("내용: " + currentItem.getComment());
}

@Override
public int getItemCount() {
    return alarmList.size();
}

// 뷰의 개별 항목을 보유하는 내부 클래스
public static class AlarmViewHolder extends RecyclerView.ViewHolder {
    public TextView regtime;
    public TextView hogi;
    private TextView comment;

    public AlarmViewHolder(@NonNull View itemView) {
        super(itemView);
        regtime = itemView.findViewById(R.id.item_regtime_textview);
        hogi = itemView.findViewById(R.id.item_hogi_textview);
        comment = itemView.findViewById(R.id.item_comment_textview);
    }
}
}
