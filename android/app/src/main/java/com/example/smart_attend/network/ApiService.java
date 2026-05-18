package com.example.smart_attend.network;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/auth/google")
    Call<JsonObject> authenticateWithGoogle(@Body JsonObject body);

    @GET("api/admin/stats")
    Call<JsonObject> getAdminStats();

    @GET("api/admin/users")
    Call<java.util.List<com.example.smart_attend.model.User>> getUsers();

    @PATCH("api/admin/users/{id}")
    Call<com.example.smart_attend.model.User> updateUserRole(@Path("id") int id, @Body JsonObject body);

    @POST("api/teacher/subjects")
    Call<JsonObject> createSubject(@Header("Authorization") String token, @Body JsonObject body);

    @GET("api/teacher/subjects")
    Call<java.util.List<com.example.smart_attend.model.Subject>> getTeacherSubjects(@Header("Authorization") String token);

    @POST("api/teacher/subjects/{id}/claim")
    Call<JsonObject> claimSubject(@Header("Authorization") String token, @Path("id") int id);

    @DELETE("api/teacher/subjects/{id}")
    Call<JsonObject> deleteSubject(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/teacher/students")
    Call<java.util.List<com.example.smart_attend.model.EnrolledStudent>> getTeacherStudents(@Header("Authorization") String token);

    @GET("api/student/subjects")
    Call<java.util.List<com.example.smart_attend.model.Subject>> getAllSubjects(@Header("Authorization") String token);

    @GET("api/student/enrolled-subjects")
    Call<java.util.List<com.example.smart_attend.model.Subject>> getEnrolledSubjects(@Header("Authorization") String token);

    @POST("api/student/enroll")
    Call<JsonObject> enrollSubject(@Header("Authorization") String token, @Body JsonObject body);

    @POST("api/teacher/sessions/start")
    Call<JsonObject> startSession(@Header("Authorization") String token, @Body JsonObject body);

    @POST("api/student/sessions/join")
    Call<JsonObject> joinSession(@Header("Authorization") String token, @Body JsonObject body);
}
