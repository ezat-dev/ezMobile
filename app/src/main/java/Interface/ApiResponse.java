package Interface;

import java.util.List;

import domain.Temp_data;

public class ApiResponse {
    private String status;
    private List<Temp_data> data;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Temp_data> getData() {
        return data;
    }

    public void setData(List<Temp_data> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


