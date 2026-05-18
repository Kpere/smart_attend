package com.example.smart_attend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smart_attend.model.User;
import com.example.smart_attend.network.RetrofitClient;
import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminViewModel extends ViewModel {

    private final MutableLiveData<List<User>> usersList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<List<User>> getUsersList() {
        return usersList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void fetchUsers() {
        isLoading.setValue(true);
        RetrofitClient.getApiService().getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    usersList.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch users");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void updateUserRole(int userId, String newRole) {
        isLoading.setValue(true);
        JsonObject payload = new JsonObject();
        payload.addProperty("role", newRole);

        RetrofitClient.getApiService().updateUserRole(userId, payload).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Refetch users to get updated list
                    fetchUsers();
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to update role");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }
}
