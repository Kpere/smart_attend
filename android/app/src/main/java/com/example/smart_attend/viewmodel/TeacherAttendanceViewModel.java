package com.example.smart_attend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smart_attend.network.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAttendanceViewModel extends ViewModel {

    private final MutableLiveData<String> sessionCode = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private String authToken;

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public LiveData<String> getSessionCode() { return sessionCode; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void startSession(int subjectId) {
        if (authToken == null) return;
        isLoading.setValue(true);

        JsonObject payload = new JsonObject();
        payload.addProperty("subject_id", subjectId);

        RetrofitClient.getApiService().startSession("Bearer " + authToken, payload)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            sessionCode.setValue(response.body().get("code").getAsString());
                        } else {
                            errorMessage.setValue("Failed to start session");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Network error: " + t.getMessage());
                    }
                });
    }

    public void clearSessionCode() {
        sessionCode.setValue(null);
    }
}
