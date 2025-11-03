package com.example.push_test1;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Interface.ApiResponse;
import Interface.ApiService;
import domain.Alarm;
import domain.Machine;
import domain.Temp_data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "my_channel_id1";
    private static final int NOTIFICATION_ID = 32;

    // 알림 재시도를 위해 보관할 필드
    private String pendingTitle = null;
    private String pendingMessage = null;

    private LineChart lineChart;
    public static final String BASE_URL = "http://192.168.1.115:9090/";
    private EditText startTime;
    private EditText endTime;
    private Button btnSelectChart;
    private Spinner spinnerMachineId;


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "POST_NOTIFICATIONS permission granted");
                    Toast.makeText(this, "알림 권한 허용됨", Toast.LENGTH_SHORT).show();
                    // 권한 허용 후 대기 중인 알림이 있으면 발행
                    if (pendingTitle != null && pendingMessage != null) {
                        actuallyNotify(pendingTitle, pendingMessage);
                        pendingTitle = null;
                        pendingMessage = null;
                    }
                } else {
                    Log.d(TAG, "POST_NOTIFICATIONS permission denied");
                    Toast.makeText(this, "알림 권한이 거부되어 알림을 보낼 수 없습니다.", Toast.LENGTH_LONG).show();
                    // 필요하면 사용자에게 권한 설정 방법 안내
                }
            });

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    //푸시알림 전송
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("알림");
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this); // Firebase 초기화
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        //setContentView(R.layout.activity_main);

//        Button button = findViewById(R.id.button);
//        button.setOnClickListener(v -> sendNotification("알림 도착", "새로운 메시지가 도착했습니다."));

        // 공통 버튼 레이아웃을 찾습니다.
        // 포함된 레이아웃의 ID는 activity_main.xml의 <include> 태그에서 지정한 ID와 동일해야 합니다.
        View commonButtonsLayout = findViewById(R.id.common_buttons_layout);

        // 공통 레이아웃 내에서 버튼1(현재 발생 알람)을 찾습니다.
        Button button1 = commonButtonsLayout.findViewById(R.id.button1);

        // 버튼1에 클릭 리스너를 설정하여 AlarmLogActivity로 이동합니다.
        button1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // 공통 레이아웃 내에서 버튼2(알람 이력)를 찾습니다.
        Button button2 = commonButtonsLayout.findViewById(R.id.button2);

        // 버튼2에 클릭 리스너를 설정하여 AlarmHistoryActivity로 이동합니다.
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlarmListActivity.class);
            startActivity(intent);
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    System.out.println("FCM, Device token: " + token);
                    Log.d("FCM", "Device token: " + token);
                });

        lineChart = findViewById(R.id.lineChart);

        // 1. 뷰 초기화 (findViewById)
        startTime = findViewById(R.id.et_start_time);
        endTime = findViewById(R.id.et_end_time);
        btnSelectChart = findViewById(R.id.btn_select_chart);
        spinnerMachineId = findViewById(R.id.spinner_machine_id);

        // 2. 💡 날짜 선택 리스너 연결
        // EditText 클릭 시 showDatePickerDialog 함수 호출
        startTime.setOnClickListener(v -> showDatePickerDialog(startTime));
        endTime.setOnClickListener(v -> showDatePickerDialog(endTime));

        //기본 날짜 세팅
        setDefaultDates();
        //데이터 조회
        //fetchDataAndDrawChart();

        //조회 버튼 클릭 시 데이터 조회
        btnSelectChart.setOnClickListener(v -> fetchDataAndDrawChart());
    }

    //차트
    private void fetchDataAndDrawChart() {
        //호기, 시작날짜, 종료날짜 가져오기
        Alarm alarm = new Alarm();
        String selectedMachine = spinnerMachineId.getSelectedItem().toString();
        Log.d(TAG, "차트 조회 설비: " + selectedMachine);
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
            Log.e(TAG, "날짜 필드가 비어있습니다. 기본값 재설정 필요.");
            setDefaultDates();

            selectedStartTimeText = startTime.getText().toString();
            selectedEndTimeText = endTime.getText().toString();
        }

        finalStartTime = selectedStartTimeText;
        finalEndTime = selectedEndTimeText;

        Log.d(TAG, "차트 조회 날짜: " + finalStartTime + " ~ " + finalEndTime);

        alarm.setStartTime(finalStartTime);
        alarm.setEndTime(finalEndTime);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // API 요청에 필요한 파라미터 값 설정
        Call<List<Temp_data>> call = apiService.getTempMonitoringList1(
                "2025-08-13 09:00:00",
                "2025-09-25 09:00:00",
                "T_600"
        );

        call.enqueue(new Callback<List<Temp_data>>() {
            @Override
            public void onResponse(Call<List<Temp_data>> call, Response<List<Temp_data>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Temp_data> tempList = response.body();
                    if (!tempList.isEmpty()) {
                        Log.d(TAG, "tempList.size: " + tempList.size());
                        setupChartData(tempList);
                    } else {
                        Log.d(TAG, "데이터가 비어있습니다.");
                    }
                } else {
                    Log.e("Retrofit", "Response was not successful or body was null");
                }
            }

            @Override
            public void onFailure(Call<List<Temp_data>> call, Throwable t) {
                Log.e("Retrofit", "API request failed: " + t.getMessage());
            }
        });
    }

    private void setupChartData(List<Temp_data> tempList) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < tempList.size(); i++) {
            Temp_data tempData = tempList.get(i);
            // T_600_d12000 값이 null인지 확인하고 기본값(0f)으로 대체
            Integer tempValue = 0;
            if (tempData.getT_600_d12000() != null) {
                tempValue = tempData.getT_600_d12000();
            }
            entries.add(new Entry(i, tempValue));
            labels.add(tempData.getTemp_time());
        }

        LineDataSet dataSet = new LineDataSet(entries, "온도");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // 차트 새로고침

        // X축 라벨 설정 및 커스텀
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setDrawLabels(true);
        lineChart.getDescription().setEnabled(false); // 차트 설명 비활성화
    }

    private void sendToActivity(Context context, String from, String contents, String hogi){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("from",from);
        intent.putExtra("contents",contents);
        intent.putExtra("hogi",hogi);


        //Flag는 액티비티를 띄울 방법이나 액티비티를 관리하는 방법등에 대한 옵션 정보
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_SINGLE_TOP|
                Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "알람 생성?");
                Log.d(TAG, "Notification channel created");
            } else {
                Log.w(TAG, "NotificationManager is null, channel not created");
            }
        }
    }

    // 권한 확인 포함한 진입점
    private void sendNotification(String title, String message) {
        // Android 13 이상이면 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                actuallyNotify(title, message);
            } else {
                // 권한 요청 후 허용되면 실제 알림을 발행하도록 대기
                pendingTitle = title;
                pendingMessage = message;
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            actuallyNotify(title, message);
        }
    }

    // 실제 알림 생성/발행
    private void actuallyNotify(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // 임시로 테스트용 아이콘(android.R.drawable.*) 사용 — 배포 시 알맞은 알림 전용 아이콘 사용 권장
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification posted");
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException posting notification", se);
            Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Exception posting notification", e);
            Toast.makeText(this, "알림 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //날짜 선택할 때 캘린더
    private void showDatePickerDialog(final EditText targetEditText) {
        // 현재 날짜를 기준으로 캘린더 초기화
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

// 1단계: 날짜 선택 다이얼로그
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                // 날짜 선택 완료 시 호출될 리스너
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 선택된 날짜로 Calendar 설정
                    calendar.set(selectedYear, selectedMonth, selectedDay);

                    // 💡 2단계: 날짜 선택 후 바로 시간 선택 다이얼로그 호출
                    showTimePickerDialog(targetEditText, calendar);
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }
    //날짜 선택 후 시간 선택
    private void showTimePickerDialog(final EditText targetEditText, final Calendar selectedDateCalendar) {
        int hour = selectedDateCalendar.get(Calendar.HOUR_OF_DAY); // 24시간 형식
        int minute = selectedDateCalendar.get(Calendar.MINUTE);

        // TimePickerDialog 생성 및 표시
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, selectedHour, selectedMinute) -> {
                    // 선택된 시간과 분을 Calendar에 설정
                    selectedDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedDateCalendar.set(Calendar.MINUTE, selectedMinute);
                    selectedDateCalendar.set(Calendar.SECOND, 0); // 초는 00으로 고정

                    // 최종 포맷: YYYY-MM-DD HH:mm:ss
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    // EditText에 최종 결과 표시
                    targetEditText.setText(format.format(selectedDateCalendar.getTime()));

                    // 참고: 초(second)는 00으로 고정했으므로, 초까지 표시하려면 SimpleDateFormat에 :ss를 추가해야 합니다.
                    // 현재는 yyyy-MM-dd HH:mm:ss 포맷입니다.
                    Log.d("DateTimePicker", "최종 선택 시간: " + targetEditText.getText().toString());
                },
                hour, // 초기 시간
                minute, // 초기 분
                true // 24시간 형식 사용 (true)
        );
        timePickerDialog.show();
    }

    //기본 날짜 설정
    private void setDefaultDates() {
        // 1. Calendar 인스턴스 준비
        final Calendar now = Calendar.getInstance(); // 현재 시각

        // 2. 종료 시간 (endTime) 설정
        // 현재 시각을 YYYY-MM-DD HH:mm:ss 포맷으로 설정
        // 💡 참고: HH는 24시간 표기입니다. hh는 12시간 표기입니다.
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // 현재 시각은 그대로 endTime으로 사용
        String formattedEndTime = dateTimeFormat.format(now.getTime());

        // 3. 시작 시간 (startTime) 설정
        final Calendar twoHoursAgo = (Calendar) now.clone();
        // 현재 시각에서 2시간(2 * 60분)을 뺌
        twoHoursAgo.add(Calendar.HOUR_OF_DAY, -2);

        // 2시간 전 시각을 YYYY-MM-DD HH:mm:ss 포맷으로 설정
        String formattedStartTime = dateTimeFormat.format(twoHoursAgo.getTime());

        // 4. EditText에 결과 설정
        if (startTime != null && endTime != null) {
            // ★ 중요: 이제 EditText에 '날짜'만 표시하는 것이 아니라 '시간'도 함께 표시해야 합니다.
            // 이는 사용자에게 포맷을 명확하게 보여주기 위함입니다.
            startTime.setText(formattedStartTime);
            endTime.setText(formattedEndTime);

            Log.d("DEFAULT_DATE", "기본 시작 시간: " + formattedStartTime);
            Log.d("DEFAULT_DATE", "기본 종료 시간: " + formattedEndTime);
        }
    }
}
