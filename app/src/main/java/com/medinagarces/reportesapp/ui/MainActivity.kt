package com.medinagarces.reportesapp.ui
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.medinagarces.reportesapp.BuildConfig
import com.medinagarces.reportesapp.data.CrearReporte
import com.medinagarces.reportesapp.databinding.*
class MainActivity:ComponentActivity(){
 private lateinit var vm:ReportesViewModel
 private lateinit var b:ActivityMainBinding
 private var screen=""
 private var list:ScreenListBinding?=null
 private var form:ScreenFormBinding?=null
 private var detail:ScreenDetailBinding?=null
 private lateinit var adapter:ReportesAdapter
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  vm=ViewModelProvider(this)[ReportesViewModel::class.java]
  b=ActivityMainBinding.inflate(layoutInflater);setContentView(b.root)
  val padding=(20*resources.displayMetrics.density).toInt()
  ViewCompat.setOnApplyWindowInsetsListener(b.root){v,insets->val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime());v.setPadding(padding,bars.top+padding,padding,bars.bottom+padding);insets}
  b.connection.text=BuildConfig.API_BASE_URL
  b.listTab.setOnClickListener{vm.volver()};b.newTab.setOnClickListener{vm.nuevo()}
  adapter=ReportesAdapter{vm.detalle(it)}
  vm.state.observe(this){render(it)}
  onBackPressedDispatcher.addCallback(this,object:OnBackPressedCallback(true){override fun handleOnBackPressed(){if(vm.state.value!!.screen!="list")vm.volver()else finish()}})
 }
 private fun render(s:UiState){
  if(screen!=s.screen){screen=s.screen;b.content.removeAllViews();list=null;form=null;detail=null
   when(screen){
    "list"->{val v=ScreenListBinding.inflate(layoutInflater,b.content,true);list=v
     v.reports.layoutManager=LinearLayoutManager(this);v.reports.adapter=adapter
     v.search.setOnClickListener{vm.buscar(v.dniSearch.text.toString().trim())}
     v.refresh.setOnClickListener{v.dniSearch.setText("");vm.listar()};v.more.setOnClickListener{vm.listar(true)}
     v.dniSearch.setText(s.query)
    }
    "form"->{val v=ScreenFormBinding.inflate(layoutInflater,b.content,true);form=v
     v.type.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("DOMICILIARIA","DESMONTE","MALEZA","OTROS"))
     v.cancel.setOnClickListener{vm.volver()};v.save.setOnClickListener{
      val dni=v.dni.text.toString().trim();val address=v.address.text.toString().trim()
      when{!dni.matches(Regex("[0-9]{8}"))->{v.dni.error="Ingresa 8 dígitos";v.dni.requestFocus()}
       address.isBlank()->{v.address.error="Ingresa la dirección";v.address.requestFocus()}
       else->vm.registrar(CrearReporte(dni,v.type.selectedItem.toString(),address,v.reference.text.toString().trim().ifEmpty{null}))}
     }
    }
    "detail"->{val v=ScreenDetailBinding.inflate(layoutInflater,b.content,true);detail=v
     v.back.setOnClickListener{vm.volver()};v.retry.setOnClickListener{vm.cargarDetalle()}
     v.attend.setOnClickListener{AlertDialog.Builder(this).setTitle("Atender reporte").setMessage("¿Confirmas que Limpieza Pública atendió este punto de acumulación?").setNegativeButton("Cancelar",null).setPositiveButton("Confirmar"){_,_->vm.atender()}.show()}
    }
   }
  }
  b.listTab.isEnabled=!s.busy;b.newTab.isEnabled=!s.busy
  list?.let{v->adapter.rows=s.rows
   v.count.text=if(s.query.isEmpty())"${s.rows.size} de ${s.total} reportes" else "${s.total} reportes · DNI ${s.query}"
   v.progress.visibility=if(s.busy)View.VISIBLE else View.GONE
   val msg=s.message.ifEmpty{if(!s.busy&&s.rows.isEmpty())"No hay reportes para mostrar. Puedes registrar uno nuevo." else ""}
   v.message.text=msg;v.message.visibility=if(msg.isEmpty())View.GONE else View.VISIBLE
   v.more.visibility=if(s.query.isEmpty()&&!s.last)View.VISIBLE else View.GONE
   v.more.isEnabled=!s.busy;v.search.isEnabled=!s.busy;v.refresh.isEnabled=!s.busy
  }
  form?.let{v->v.progress.visibility=if(s.busy)View.VISIBLE else View.GONE;v.message.text=s.message;v.save.isEnabled=!s.busy;v.cancel.isEnabled=!s.busy}
  detail?.let{v->v.progress.visibility=if(s.busy)View.VISIBLE else View.GONE;v.message.text=s.message
   v.back.isEnabled=!s.busy;v.retry.isEnabled=!s.busy
   val r=s.detail;v.title.text=if(r==null)"Detalle del reporte" else "Reporte #${r.id}"
   v.body.text=if(r==null)"Cargando información…" else "${if(r.estado==1)"● PENDIENTE" else "✓ ATENDIDO"}\n\n${r.tipo}\n\nDIRECCIÓN\n${r.direccion}\n\nREFERENCIA\n${r.referencia?.takeIf{it.isNotBlank()}?:"Sin referencia"}\n\nCIUDADANO\nDNI ${r.dni}\n\nFECHA\n${formatFecha(r.fecha)}"
   v.attend.isEnabled=!s.busy&&r?.estado==1;v.attend.text=if(r?.estado==0)"✓ Reporte atendido" else "Marcar como atendido"
  }
 }
}
