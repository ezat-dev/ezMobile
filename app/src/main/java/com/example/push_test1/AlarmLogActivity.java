package com.example.push_test1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Adapter.QualityAdapter;
import Interface.QualityInterfece;
import domain.Quality;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlarmLogActivity extends AppCompatActivity {

    private static final String TAG = "AlarmLogActivity";
    private QualityInterfece qualityInterface;
    private RecyclerView recyclerView;
    private QualityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alarm_log);
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
            Intent intent = new Intent(AlarmLogActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // 공통 레이아웃 내에서 버튼2(알람 이력)를 찾습니다.
        Button button2 = commonButtonsLayout.findViewById(R.id.button2);

        // 버튼2에 클릭 리스너를 설정하여 AlarmHistoryActivity로 이동합니다.
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(AlarmLogActivity.this, AlarmListActivity.class);
            startActivity(intent);
        });

        // Retrofit 인스턴스 생성
        // "http://서버_IP:포트" 부분을 실제 서버 주소로 변경하세요.
        // 에뮬레이터에서 로컬 PC 서버를 테스트할 경우 "http://10.0.2.2:8080"을 사용합니다.
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://ezat.co.kr/")
                .baseUrl("http://192.168.1.115:9090/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // API 서비스 인터페이스 구현체 생성
        qualityInterface = retrofit.create(QualityInterfece.class);

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.quality_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //페이지 로딩 시 데이터 조회
        getqualityList();

        // 예시: 버튼 클릭 시 요청 보내기
//        findViewById(R.id.button4).setOnClickListener(v -> {
//            getqualityList();
//        });
    }

    private void getqualityList() {
        // 서버로 보낼 Work 객체 생성 및 데이터 설정
        Quality quality = new Quality();
        // ... 필요한 데이터 설정

        // API 호출
        Call<List<Quality>> call = qualityInterface.getqualityList(quality);

        // 비동기 요청
        call.enqueue(new Callback<List<Quality>>() {
            @Override
            public void onResponse(Call<List<Quality>> call, Response<List<Quality>> response) {
                if (response.isSuccessful()) {
                    List<Quality> qualityList = response.body();
                    if (qualityList != null) {
                        // 서버로부터 성공적으로 리스트를 받음
                        Log.d(TAG, "Successfully received " + qualityList.size() + " items.");
                        for (Quality w : qualityList) {
                            Log.d(TAG, "Work ID: " + w.getId() + ", Name: " + w.getDate());
                        }
                        // RecyclerView 어댑터 연결
                        adapter = new QualityAdapter(qualityList);
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
            public void onFailure(Call<List<Quality>> call, Throwable t) {
                // 네트워크 오류로 요청 실패
                Log.e(TAG, "Network request failed: " + t.getMessage(), t);
            }
        });
    }
}