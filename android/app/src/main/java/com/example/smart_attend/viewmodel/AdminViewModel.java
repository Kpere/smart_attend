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
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<JsonObject> getStats() { return stats; }
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
}
