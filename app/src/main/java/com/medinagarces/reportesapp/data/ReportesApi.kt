package com.medinagarces.reportesapp.data
import retrofit2.http.*
data class Reporte(val id:Int,val dni:String,val fecha:String,val tipo:String,val direccion:String,val referencia:String?,val estado:Int)
data class CrearReporte(val dni:String,val tipo:String,val direccion:String,val referencia:String?)
data class Pagina(val content:List<Reporte>,val totalElements:Long,val offset:Int,val limit:Int,val last:Boolean)
interface ReportesApi {
 @GET("reportes/") suspend fun listar(@Query("offset") offset:Int,@Query("limit") limit:Int=5):Pagina
 @POST("reportes/") suspend fun registrar(@Body request:CrearReporte):Reporte
 @GET("reportes/{id}") suspend fun obtener(@Path("id") id:Int):Reporte
 @GET("reportes/ciudadano/{dni}") suspend fun buscar(@Path("dni") dni:String):List<Reporte>
 @PUT("reportes/{id}") suspend fun atender(@Path("id") id:Int):Reporte
}
