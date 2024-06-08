package com.aldi.codestories.data.remote

import com.aldi.codestories.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getStories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 25
    ): StoryResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 25,
        @Query("location") location : Int = 1,
    ): StoryResponse

    @Multipart
    @POST("stories")
    suspend fun uploadNewStory(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Double?,
        @Part("lon") lon: Double?
    ): AddStoryResponse
}
