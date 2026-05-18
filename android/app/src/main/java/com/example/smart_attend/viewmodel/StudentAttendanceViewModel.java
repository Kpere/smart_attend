package com.example.smart_attend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smart_attend.network.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAttendanceViewModel extends ViewModel {

    private final MutableLiveData<Boolean> joinSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private String authToken;

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public LiveData<Boolean> getJoinSuccess() { return joinSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void joinSession(int subjectId, String code) {
        if (authToken == null) return;
        isLoading.setValue(true);

        JsonObject payload = new JsonObject();
        payload.addProperty("subject_id", subjectId);
        payload.addProperty("code", code);

        RetrofitClient.getApiService().joinSession("Bearer " + authToken, payload)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful()) {
                            joinSuccess.setValue(true);
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                if (errorBody.contains("expired")) {
                                    errorMessage.setValue("The session has expired.");
                                } else if (errorBody.contains("Invalid attendance code")) {
                                    errorMessage.setValue("Invalid attendance code.");
                                } else if (errorBody.contains("already been marked present")) {
                                    errorMessage.setValue("You have already been marked present!");
                                } else {
                                    errorMessage.setValue("Failed to join session.");
                                }
                            } catch (Exception e) {
                                errorMessage.setValue("Failed to join session.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Network error: " + t.getMessage());
                    }
                });
    }

    public void resetJoinSuccess() {
        joinSuccess.setValue(false);
    }
}
