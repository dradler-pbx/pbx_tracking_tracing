package com.example.pbxtrackingtracingapp.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("exec") // relative path
    suspend fun linkComponents(
        @Field("action") action: String,
        @Field("frame_pn") framePn: String,
        @Field("frame_sn") frameSn: String,
        @Field("cmp_sn") cmpSn: String,
        @Field("cmp_pn") cmpPn: String
    ): Response<String>
}
