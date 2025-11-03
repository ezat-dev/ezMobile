package com.example.push_test1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

import Interface.ApiService;
import domain.Users;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity { // <-- 여기서 AppCompatActivity를 상속받아야 합니다.

    private EditText usernameEditText; // 💡 XML ID와 통일성을 위해 이 이름 사용을 권장
    private EditText passwordEditText;
    private static final String BASE_URL = "http://192.168.1.115:9090/"; //요청 ip
    //private static final String BASE_URL = "http://ezat.co.kr/"; //요청 ip
    //토큰 저장할 변수
    public static String CURRENT_FCM_TOKEN = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 로그인 페이지 레이아웃 (activity_login.xml) 설정

        // 💡 XML ID(R.id.edit_username)를 사용하여 변수를 초기화합니다.
        usernameEditText = findViewById(R.id.edit_username);
        passwordEditText = findViewById(R.id.edit_password);

        // 로그인 버튼 클릭 이벤트 설정
        Button loginButton = findViewById(R.id.login_button); // activity_login.xml에 이 ID로 버튼이 있다고 가정
        loginButton.setOnClickListener(v -> attemptLogin());

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        // 💡 토큰을 static 변수에 저장
                        CURRENT_FCM_TOKEN = token;
                        System.out.println("FCM, Device token: " + token);
                        Log.d("FCM", "Device token: " + token);
                    } else {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                    }
                });
    }

    private void attemptLogin() {
        String userId = usernameEditText.getText().toString().trim();
        String userPw = passwordEditText.getText().toString().trim();

        if (userId.isEmpty() || userPw.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrofit 인스턴스 생성 (기존과 동일)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 💡 LoginRequest 대신 Users 객체를 생성하여 전송
        //Users loginUser = new Users(userId, userPw);
        Users loginUser = new Users();
        loginUser.setUser_id(userId);
        loginUser.setUser_pw(userPw);

        // 💡 응답 타입은 Call<Boolean>
        apiService.androidLogin(loginUser).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isSuccess = response.body();

                    if (isSuccess) {
                        // 🚀 로그인 성공!
                        Log.d("LOGIN", "로그인 성공.");
                        Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        updateDeviceToken(loginUser);

                        // MainActivity로 이동
                        Intent intent = new Intent(LoginActivity.this, AlarmListActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 로그인 실패 (서버에서 false를 반환)
                        Toast.makeText(LoginActivity.this, "로그인 실패: 아이디 또는 비밀번호를 확인하세요.", Toast.LENGTH_LONG).show();
                        Log.e("LOGIN", "로그인 실패: 서버에서 false 반환");
                    }
                } else {
                    // 서버 응답 오류 (4xx, 5xx)
                    Toast.makeText(LoginActivity.this, "로그인 요청 실패: 서버 응답 오류", Toast.LENGTH_SHORT).show();
                    Log.e("LOGIN", "응답 코드: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                // 네트워크 오류
                Toast.makeText(LoginActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                Log.e("LOGIN", "네트워크 오류: " + t.getMessage());
            }
        });
    }

    private void updateDeviceToken(Users loginUser) {
        String fcmToken = LoginActivity.CURRENT_FCM_TOKEN;

        if (fcmToken == null) {
            Log.w("TOKEN", "FCM 토큰이 아직 준비되지 않았습니다. 업데이트를 건너뜁니다.");
            return;
        }
        loginUser.setDevice_token(fcmToken);

        // 2. Retrofit 인스턴스 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        // 3. Users 객체 생성 및 ID, Token 설정
        //Users userForUpdate = new Users(userId, null); // user_pw는 필요 없으므로 null
        //userForUpdate.setDevice_token(fcmToken);

        // 4. API 요청
        apiService.deviceTokenUpdate(loginUser).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null && response.body()) {
                    Log.d("TOKEN", "Device Token 업데이트 성공.");
                } else {
                    Log.e("TOKEN", "Device Token 업데이트 실패. 응답 코드: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("TOKEN", "Device Token 업데이트 네트워크 오류: " + t.getMessage());
            }
        });
    }
}
