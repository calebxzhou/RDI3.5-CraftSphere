package calebzhou.rdi.craftsphere.model;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ApiResponse<T extends Serializable> implements Serializable {
    private String type;
    private String message;
    private Serializable data;

    public ApiResponse() {
    }

    public ApiResponse(String type, String message, @Nullable Serializable data) {
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Nullable
    public T getData() {
        return (T)data;
    }
    public void setData(Serializable data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
