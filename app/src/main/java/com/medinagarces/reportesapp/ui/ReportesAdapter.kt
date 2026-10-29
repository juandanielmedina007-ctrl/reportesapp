package com.medinagarces.reportesapp.ui
import android.view.*
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.medinagarces.reportesapp.data.Reporte
import com.medinagarces.reportesapp.databinding.ItemReportBinding
class ReportesAdapter(private val open:(Int)->Unit):RecyclerView.Adapter<ReportesAdapter.Holder>(){
 var rows:List<Reporte> = emptyList();set(v){field=v;notifyDataSetChanged()}
 class Holder(val b:ItemReportBinding):RecyclerView.ViewHolder(b.root)
 override fun onCreateViewHolder(parent:ViewGroup,type:Int)=Holder(ItemReportBinding.inflate(LayoutInflater.from(parent.context),parent,false))
 override fun getItemCount()=rows.size
 override fun onBindViewHolder(h:Holder,p:Int){val r=rows[p];h.b.apply{
  status.text=if(r.estado==1)"● PENDIENTE  ·  #${r.id}" else "✓ ATENDIDO  ·  #${r.id}"
  status.setTextColor(Color.parseColor(if(r.estado==1)"#A66417" else "#157A61"))
  title.text=r.tipo;address.text=r.direccion;meta.text="DNI ${r.dni}  ·  ${formatFecha(r.fecha)}"
  root.contentDescription="Reporte ${r.id}, ${r.tipo}, ${if(r.estado==1)"pendiente" else "atendido"}, ${r.direccion}. Ver detalle"
  root.setOnClickListener{open(r.id)}
 }}
}
fun formatFecha(value:String):String=try{java.time.LocalDateTime.parse(value).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}catch(e:Exception){value}
