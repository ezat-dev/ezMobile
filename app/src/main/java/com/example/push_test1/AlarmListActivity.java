package com.example.push_test1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Adapter.AlarmAdapter;
import Adapter.QualityAdapter;
import Interface.ApiService;
import Interface.MachineInterface;
import Interface.QualityInterfece;
import domain.Alarm;
import domain.Machine;
import domain.Quality;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlarmListActivity extends AppCompatActivity {
    private static final String TAG = "AlarmListActivity";
    private static final String BASE_URL = "http://192.168.1.115:9090/";
    private MachineInterface machineInterface;
    private ApiService apiService;
    private RecyclerView recyclerView;
    //private MachineAdapter adapter;
    private AlarmAdapter adapter;
    private Spinner spinnerMachineId2;
    private Button selectAlarmListButton;
    private EditText startTime;
    private EditText endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alarm_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 공통 버튼 레이아웃을 찾습니다.
        // 포함된 레이아웃의 ID는 activity_main.xml의 <include> 태그에서 지정한 ID와 동일해야 합니다.
        View commonButtonsLayout = findViewById(R.id.common_buttons_layout);

        // 공통 레이아웃 내에서 버튼1(현재 발생 알람)을 찾습니다.
        Button button1 = commonButtonsLayout.findViewById(R.id.button1);

        // 버튼1에 클릭 리스너를 설정하여 AlarmLogActivity로 이동합니다.
        button1.setOnClickListener(v -> {
            Intent intent = new Intent(AlarmListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // 공통 레이아웃 내에서 버튼2(알람 이력)를 찾습니다.
        Button button2 = commonButtonsLayout.findViewById(R.id.button2);

        // 버튼2에 클릭 리스너를 설정하여 AlarmHistoryActivity로 이동합니다.
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(AlarmListActivity.this, AlarmListActivity.class);
            startActivity(intent);
        });

        // Retrofit 인스턴스 생성
        // "http://서버_IP:포트" 부분을 실제 서버 주소로 변경하세요.
        // 에뮬레이터에서 로컬 PC 서버를 테스트할 경우 "http://10.0.2.2:8080"을 사용합니다.
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://ezat.co.kr/")
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // API 서비스 인터페이스 구현체 생성
        //machineInterface = retrofit.create(MachineInterface.class);
        apiService = retrofit.create(ApiService.class);

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.alarm_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //뷰 초기화(select 버튼, 조회 버튼)
        spinnerMachineId2 = findViewById(R.id.spinner_machine_id2);
        selectAlarmListButton = findViewById(R.id.select_alarm_list_button);

        //조회 버튼 클릭 시 데이터 조회
        selectAlarmListButton.setOnClickListener(v -> selectCreateAlarmList());

        // 1. 뷰 초기화 (findViewById)
        startTime = findViewById(R.id.et_start_time2);
        endTime = findViewById(R.id.et_end_time2);

        //기본 날짜 설정
        setDefaultDates();

        // 2. 💡 날짜 선택 리스너 연결
        // EditText 클릭 시 showDatePickerDialog 함수 호출
        startTime.setOnClickListener(v -> showDatePickerDialog(startTime));
        endTime.setOnClickListener(v -> showDatePickerDialog(endTime));

        //페이지 로딩 시 데이터 조회
        selectCreateAlarmList();
    }

    private void selectCreateAlarmList() {
        // 서버로 보낼 객체 생성 및 데이터 설정
        Alarm alarm = new Alarm();
        String selectedMachine = spinnerMachineId2.getSelectedItem().toString();

        String hogiValue;
        if (selectedMachine.equals("전체")) {
            hogiValue = "";
        }else if(selectedMachine.equals("cm")) {
         hogiValue = "cm.";
        } else {
            hogiValue = selectedMachine;
        }
        alarm.setHogi(hogiValue);

        String finalStartTime;
        String finalEndTime;

        String selectedStartTimeText = startTime.getText().toString();
        String selectedEndTimeText = endTime.getText().toString();

        if (selectedStartTimeText.isEmpty() || selectedEndTimeText.isEmpty()) {
            // 이 블록은 기본 날짜 설정 로직에 의해 거의 실행되지 않음
            Log.e(TAG, "날짜 필드가 비어있습니다. 기본값 재설정 필요.");
            setDefaultDates();

            selectedStartTimeText = startTime.getText().toString();
            selectedEndTimeText = endTime.getText().toString();
        }

        finalStartTime = selectedStartTimeText + " 00:00:00";
        finalEndTime = selectedEndTimeText + " 23:59:59";

        Log.d(TAG, "알람 조회 날짜: " + finalStartTime + " ~ " + finalEndTime);

        alarm.setStartTime(finalStartTime);
        alarm.setEndTime(finalEndTime);
        // API 호출
        Call<List<Alarm>> call = apiService.selectCreateAlarmList(alarm);

        // 비동기 요청
        call.enqueue(new Callback<List<Alarm>>() {
            @Override
            public void onResponse(Call<List<Alarm>> call, Response<List<Alarm>> response) {
                if (response.isSuccessful()) {
                    List<Alarm> alarmList = response.body();
                    if (alarmList != null) {
                        // 서버로부터 성공적으로 리스트를 받음
                        Log.d(TAG, "Successfully received " + alarmList.size() + " items.");
                        for (Alarm w : alarmList) {
                            Log.d(TAG, "알람 발생 시간: " + w.getRegtime());
                        }
                        // RecyclerView 어댑터 연결
                        adapter = new AlarmAdapter(alarmList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.w(TAG, "Response body is null.");
                    }
                } else {
                    // 서버 응답이 실패 (예: 404, 500 에러)
                    Log.e(TAG, "Server response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Alarm>> call, Throwable t) {
                // 네트워크 오류로 요청 실패
                Log.e(TAG, "Network request failed: " + t.getMessage(), t);
            }
        });
    }
    //날짜 선택할 때 캘린더
    private void showDatePickerDialog(final EditText targetEditText) {
        // 현재 날짜를 기준으로 캘린더 초기화
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog 생성 및 표시
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                // 날짜 선택 완료 시 호출될 리스너
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 선택된 날짜로 Calendar 설정
                    calendar.set(selectedYear, selectedMonth, selectedDay);

                    // 날짜 포맷 (YYYY-MM-DD)
                    // 만약 시간도 00:00:00 으로 포함하고 싶다면 "yyyy-MM-dd 00:00:00" 형태로 처리
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    // EditText에 결과 표시
                    targetEditText.setText(format.format(calendar.getTime()));
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    //기본 날짜 설정(어제~오늘)
    private void setDefaultDates() {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1); // 어제로 설정

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // EditText에 날짜 텍스트를 설정
        if (startTime != null && endTime != null) {
            startTime.setText(dateFormat.format(yesterday.getTime()));
            endTime.setText(dateFormat.format(today.getTime()));
        }
    }
}