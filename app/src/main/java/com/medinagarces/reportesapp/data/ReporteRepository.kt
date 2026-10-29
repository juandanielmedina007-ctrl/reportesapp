package com.medinagarces.reportesapp.data
import com.medinagarces.reportesapp.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
class ReporteRepository {
 private val api=Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
  .client(OkHttpClient.Builder().connectTimeout(30,TimeUnit.SECONDS).readTimeout(120,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS).build())
  .addConverterFactory(GsonConverterFactory.create()).build().create(ReportesApi::class.java)
 suspend fun listar(offset:Int)=api.listar(offset)
 suspend fun registrar(r:CrearReporte)=api.registrar(r)
 suspend fun obtener(id:Int)=api.obtener(id)
 suspend fun buscar(dni:String)=api.buscar(dni)
 suspend fun atender(id:Int)=api.atender(id)
}
