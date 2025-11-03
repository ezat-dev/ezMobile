package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.push_test1.R;

import java.util.List;
import domain.Quality;

public class QualityAdapter extends RecyclerView.Adapter<QualityAdapter.QualityViewHolder> {

    private List<Quality> qualityList;

    public QualityAdapter(List<Quality> qualityList) {
        this.qualityList = qualityList;
    }

    @NonNull
    @Override
    public QualityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // quality_item.xml을 인플레이트하여 뷰를 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quality_item, parent, false);
        return new QualityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QualityViewHolder holder, int position) {
        // 뷰 홀더에 데이터 바인딩
        Quality currentItem = qualityList.get(position);
        holder.idTextView.setText("ID: " + currentItem.getId());
        holder.mch_nameTextView.setText("mch_name: " + currentItem.getMch_name());
    }

    @Override
    public int getItemCount() {
        return qualityList.size();
    }

    // 뷰의 개별 항목을 보유하는 내부 클래스
    public static class QualityViewHolder extends RecyclerView.ViewHolder {
        public TextView idTextView;
        public TextView mch_nameTextView;

        public QualityViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.item_id_textview);
            mch_nameTextView = itemView.findViewById(R.id.item_mch_name_textview);
        }
    }
}
