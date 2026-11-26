package com.test.tickersdemo.network

import com.test.tickersdemo.data.TopSecurities
import com.test.tickersdemo.data.TopSecuritiesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TradernetApi {
    @POST("api/getTopSecurities")
    suspend fun getTopSecurities(@Body request: TopSecuritiesRequest): Response<TopSecurities>
}
