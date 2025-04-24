package com.gmpire.guruklub.data.network

import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.library.YoutubeMain
import com.gmpire.guruklub.data.model.question.Question
import io.reactivex.Maybe
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.util.ArrayList


interface IApiService {

    @GET("{url}")
    abstract fun getRequest(
        @Path(value = "url", encoded = true) path: String,
        @QueryMap hashMap: Map<String, String>
    ): Maybe<Response<ResponseBody>>

    @FormUrlEncoded
    @POST("{url}")
    abstract fun postRequest(
        @Path(value = "url", encoded = true) path: String,
        @FieldMap hashMap: Map<String, String>
    ): Maybe<Response<ResponseBody>>

    @FormUrlEncoded
    @POST("{url}")
    abstract fun postRequestSync(
        @Path(value = "url", encoded = true) path: String,
        @FieldMap hashMap: Map<String, String>
    ): Call<Response<ResponseBody>>

    @Multipart
    //   @Headers("Content-Type: multipart/form-data")
    @POST("{url}")
    abstract fun sendDocuments(
        @Path(value = "url", encoded = true) path: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>
    ): Maybe<Response<ResponseBody>>

    @Multipart
    //  @Headers("Content-Type: multipart/form-data")
    @POST("{url}")
    abstract fun sendDocuments(
        @Path(value = "url", encoded = true) path: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part
    ): Maybe<Response<ResponseBody>>


    @Headers("Content-Type: application/json")
    @POST("{url}")
    abstract fun postRequestForRaw(
        @Path(value = "url", encoded = true) path: String,
        @Body requestBody: RequestBody
    ): Maybe<Response<ResponseBody>>

    @FormUrlEncoded
    @POST("/get-questions-by-keyword-search")
    fun getQuestionByKeyword(@Field("keyword") keyword : String): Call<BaseModel<PaginationModel<ArrayList<Question>>>>?

    @GET("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video")
    fun getYoutubeSuggestions(@Query(value = "relatedToVideoId") videoId : String, @Query(value = "key") apiKey : String): Call<YoutubeMain>?

}