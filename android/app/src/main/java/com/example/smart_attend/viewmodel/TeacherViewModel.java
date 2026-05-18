package com.example.smart_attend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smart_attend.model.Subject;
import com.example.smart_attend.network.RetrofitClient;
import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherViewModel extends ViewModel {

    private final MutableLiveData<List<Subject>> subjectsList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> subjectCreated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> subjectDeleted = new MutableLiveData<>();

    private String authToken;

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public LiveData<List<Subject>> getSubjectsList() { return subjectsList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getSubjectCreated() { return subjectCreated; }
    public LiveData<Boolean> getSubjectDeleted() { return subjectDeleted; }

    public void fetchSubjects() {
        if (authToken == null) return;
        isLoading.setValue(true);
        RetrofitClient.getApiService().getTeacherSubjects("Bearer " + authToken)
                .enqueue(new Callback<List<Subject>>() {
                    @Override
                    public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            subjectsList.setValue(response.body());
                        } else {
                            errorMessage.setValue("Failed to load subjects");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Subject>> call, Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Network error: " + t.getMessage());
                    }
                });
    }

    public void createSubject(String name, String code, int creditHours) {
        if (authToken == null) return;
        isLoading.setValue(true);

        JsonObject payload = new JsonObject();
        payload.addProperty("name", name);
        payload.addProperty("code", code);
        payload.addProperty("credit_hours", creditHours);

        RetrofitClient.getApiService().createSubject("Bearer " + authToken, payload)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful()) {
                            subjectCreated.setValue(true);
                            fetchSubjects(); // Refresh list
                        } else {
                            String msg = "Failed to add subject";
                            if (response.code() == 400) msg = "Subject code already exists";
                            errorMessage.setValue(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Network error: " + t.getMessage());
                    }
                });
    }

    public void deleteSubject(int subjectId) {
        if (authToken == null) return;
        RetrofitClient.getApiService().deleteSubject("Bearer " + authToken, subjectId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            subjectDeleted.setValue(true);
                            fetchSubjects(); // Refresh list
                        } else {
                            errorMessage.setValue("Failed to delete subject");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        errorMessage.setValue("Network error: " + t.getMessage());
                    }
                });
    }
}
