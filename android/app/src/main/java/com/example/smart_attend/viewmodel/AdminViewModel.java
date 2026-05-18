package com.example.smart_attend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smart_attend.network.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminViewModel extends ViewModel {

    private final MutableLiveData<JsonObject> stats = new MutableLiveData<>();
    private final MutableLiveData<java.util.List<com.example.smart_attend.model.User>> usersList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<JsonObject> getStats() { return stats; }
    public LiveData<java.util.List<com.example.smart_attend.model.User>> getUsersList() { return usersList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void fetchStats() {
        isLoading.setValue(true);
        RetrofitClient.getApiService().getAdminStats()
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            stats.setValue(response.body());
                        } else {
                            errorMessage.setValue("Failed to load dashboard stats");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Network error: " + t.getMessage());
                    }
                });
    }

    public void fetchUsers() {
        isLoading.setValue(true);
        RetrofitClient.getApiService().getUsers().enqueue(new Callback<java.util.List<com.example.smart_attend.model.User>>() {
            @Override
            public void onResponse(Call<java.util.List<com.example.smart_attend.model.User>> call, Response<java.util.List<com.example.smart_attend.model.User>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    usersList.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch users");
                }
            }

            @Override
            public void onFailure(Call<java.util.List<com.example.smart_attend.model.User>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void updateUserRole(int userId, String role) {
        isLoading.setValue(true);
        JsonObject body = new JsonObject();
        body.addProperty("role", role);
        body.addProperty("status", "active");
        
        RetrofitClient.getApiService().updateUserRole(userId, body).enqueue(new Callback<com.example.smart_attend.model.User>() {
            @Override
            public void onResponse(Call<com.example.smart_attend.model.User> call, Response<com.example.smart_attend.model.User> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    fetchUsers(); // refresh the list
                } else {
                    errorMessage.setValue("Failed to update user role");
                }
            }

            @Override
            public void onFailure(Call<com.example.smart_attend.model.User> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }
}
