package Interface;

import java.util.List;

import domain.Machine;
import domain.Quality;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MachineInterface {
    @POST("/geomet/machine/logStatus/list")
    Call<List<Machine>> getlogStatusList(@Body Machine Machine);
}
