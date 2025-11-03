//package Adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.push_test1.R;
//
//import java.util.List;
//
//import domain.Machine;
//import domain.Quality;
//
//public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.MachineViewHolder> {
//
//    private List<Machine> machineList;
//
//    public MachineAdapter(List<Machine> machineList) {
//        this.machineList = machineList;
//    }
//
//    @NonNull
//    @Override
//    public MachineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // machine_item.xml을 인플레이트하여 뷰를 생성
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.machine_item, parent, false);
//        return new MachineViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MachineViewHolder holder, int position) {
//        // 뷰 홀더에 데이터 바인딩
//        Machine currentItem = machineList.get(position);
//        holder.dateTextView.setText("날짜: " + currentItem.getGong_date());
//        holder.mch_nameTextView.setText("설비명: " + currentItem.getMch_name());
//        holder.resultTextView.setText("결과: " + currentItem.getResult());
//    }
//
//    @Override
//    public int getItemCount() {
//        return machineList.size();
//    }
//
//    // 뷰의 개별 항목을 보유하는 내부 클래스
//    public static class MachineViewHolder extends RecyclerView.ViewHolder {
//        public TextView dateTextView;
//        public TextView mch_nameTextView;
//        private TextView resultTextView;
//
//        public MachineViewHolder(@NonNull View itemView) {
//            super(itemView);
//            dateTextView = itemView.findViewById(R.id.item_gong_date_textview);
//            mch_nameTextView = itemView.findViewById(R.id.item_mch_name_textview);
//            resultTextView = itemView.findViewById(R.id.item_result_textview);
//        }
//    }
//}
