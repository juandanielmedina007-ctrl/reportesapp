package com.medinagarces.reportesapp.ui
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.medinagarces.reportesapp.data.*
data class UiState(val screen:String="list",val rows:List<Reporte> = emptyList(),val total:Long=0,val last:Boolean=true,val query:String="",val busy:Boolean=false,val message:String="",val detail:Reporte?=null,val detailId:Int?=null)
class ReportesViewModel:ViewModel(){
 private val repository=ReporteRepository()
 private val mutable=MutableLiveData(UiState())
 val state:LiveData<UiState> = mutable
 private var s:UiState get()=mutable.value!!;set(v){mutable.value=v}
 init { listar() }
 private fun run(work:suspend ()->Unit){
  if(s.busy)return
  s=s.copy(busy=true,message="")
  viewModelScope.launch {try{work()}catch(e:CancellationException){throw e}catch(e:Exception){s=s.copy(message=error(e))}finally{s=s.copy(busy=false)}}
 }
 fun listar(more:Boolean=false){run{
  val offset=if(more)s.rows.size else 0
  val p=repository.listar(offset)
  s=s.copy(screen="list",rows=if(more)(s.rows+p.content).distinctBy{it.id}else p.content,total=p.totalElements,last=p.last,query="",detail=null)
 }}
 fun buscar(dni:String){if(!dni.matches(Regex("[0-9]{8}"))){s=s.copy(message="Ingresa un DNI de 8 dígitos.");return};run{
  val rows=repository.buscar(dni);s=s.copy(rows=rows,total=rows.size.toLong(),query=dni,last=true)
 }}
 fun nuevo(){if(!s.busy)s=s.copy(screen="form",message="")}
 fun volver(){if(!s.busy){s=s.copy(screen="list",message="");if(s.query.isEmpty())listar()else buscar(s.query)}}
 fun detalle(id:Int){if(s.busy)return;s=s.copy(screen="detail",detail=null,detailId=id);cargarDetalle()}
 fun cargarDetalle(){val id=s.detailId?:return;run{s=s.copy(detail=repository.obtener(id))}}
 fun registrar(r:CrearReporte){run{
  val created=repository.registrar(r);s=s.copy(screen="detail",detail=created,detailId=created.id,message="Reporte registrado correctamente.")
 }}
 fun atender(){val id=s.detailId?:return;run{s=s.copy(detail=repository.atender(id),message="Reporte atendido correctamente.")}}
 private fun error(e:Exception):String=when(e){
  is retrofit2.HttpException -> when(e.code()){404->"El reporte ya no existe.";400->"Verifica los datos ingresados.";else->"El servidor respondió con error ${e.code()}. Vuelve a intentar."}
  is java.io.IOException -> "No se pudo conectar. Verifica tu conexión y que la API esté encendida. En Render el primer inicio puede tardar; vuelve a intentar."
  else -> "No se pudo completar la operación. Vuelve a intentar."
 }
}
