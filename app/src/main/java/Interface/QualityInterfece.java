package Interface;

import java.util.List;

import domain.Quality;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface QualityInterfece {
    @POST("/geomet/quality/tusTest/list")
    Call<List<Quality>> getqualityList(@Body Quality work);
}
