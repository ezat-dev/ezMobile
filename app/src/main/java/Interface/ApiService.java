package Interface;

import java.util.List;

import domain.Alarm;
import domain.Machine;
import domain.Temp_data;
import domain.Users;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("/geomet/machine/tempMonitoring/list")
    Call<List<Temp_data>> getTempMonitoringList1(
            @Field("startDate") String startDate,
            @Field("endDate") String endDate,
            @Field("mch_code") String mch_code
    );

    //로그인 요청
    @POST("ezPublic/user/android/login")
    Call<Boolean> androidLogin(@Body Users user);

    //로그인 성공 시 device token 업데이트
    @POST("ezPublic/user/android/deviceTokenUpdate")
    Call<Boolean> deviceTokenUpdate(@Body Users user);

    //발생 알람 조회
    @POST("ezPublic/alarm/android/selectCreateAlarmList")
    Call<List<Alarm>> selectCreateAlarmList(@Body Alarm alarm);

    //차트 온도 데이터 조회
    @POST("ezPublic/alarm/android/getTempDatas")
    Call<List<Alarm>> getTempDatas(@Body Alarm alarm);
}
