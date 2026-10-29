package com.medinagarces.reportesapp

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo

/** Prueba de extremo a extremo sobre la app real y la API en ejecución. */
class FlowInstrumentation : Instrumentation() {
 override fun onCreate(arguments:Bundle?) { super.onCreate(arguments); start() }
 private val evidence=mutableListOf<String>()
 override fun onStart() {
  try {
   startActivitySync(Intent().setClassName(targetContext,"com.medinagarces.reportesapp.ui.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
   await("lista inicial") { find { it.viewIdResourceName?.endsWith(":id/count")==true && it.text?.startsWith("5 de ")==true }!=null }
   capture("01-lista")
   clickText("Cargar 5 siguientes")
   await("segunda página") { find { it.text?.startsWith("10 de ")==true }!=null }
   capture("02-paginacion")
   clickText("+ Nuevo")
   setField("dni","11223344");setField("address","Calle Prueba Android 456");setField("reference","Reporte ficticio de verificación")
   clickText("Registrar reporte")
   await("registro y detalle") { find { it.text?.contains("DNI 11223344")==true }!=null }
   capture("03-detalle")
   clickText("Marcar como atendido");clickText("Confirmar")
   await("atención") { find { it.text?.toString()?.equals("✓ Reporte atendido",ignoreCase=true)==true }!=null }
   capture("04-atendido")
   clickText("← Volver")
   await("regreso a lista") { find { it.viewIdResourceName?.endsWith(":id/dniSearch")==true }!=null }
   setField("dniSearch","11223344");clickText("Buscar")
   await("búsqueda por DNI") { find { it.text?.contains("reportes · DNI 11223344")==true }!=null }
   await("estado persistido") { find { it.text?.startsWith("✓ ATENDIDO")==true }!=null }
   capture("05-busqueda")
   val result=Bundle();result.putString("stream","PASS: ${evidence.joinToString("; ")}\n");finish(Activity.RESULT_OK,result)
  } catch(e:Throwable) {
   capture("error")
   val result=Bundle();result.putString("stream","FAIL: ${e.message}\n${e.stackTraceToString()}\n");finish(Activity.RESULT_CANCELED,result)
  }
 }
 private fun capture(name:String) {
  val bitmap=uiAutomation.takeScreenshot()?:return
  val file=java.io.File(targetContext.getExternalFilesDir(null),"$name.png")
  file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
 }
 private fun find(predicate:(AccessibilityNodeInfo)->Boolean):AccessibilityNodeInfo? {
  fun scan(node:AccessibilityNodeInfo?):AccessibilityNodeInfo? {
   if(node==null)return null
   if(predicate(node))return node
   for(i in 0 until node.childCount) { val result=scan(node.getChild(i));if(result!=null)return result }
   return null
  }
  uiAutomation.serviceInfo=uiAutomation.serviceInfo.apply { flags=flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS }
  return scan(uiAutomation.rootInActiveWindow)
 }
 private fun await(label:String,condition:()->Boolean) {
  val end=SystemClock.uptimeMillis()+20000
  while(SystemClock.uptimeMillis()<end) { if(condition()){evidence.add(label);return};SystemClock.sleep(150) }
  error("Tiempo agotado: $label")
 }
 private fun clickText(text:String) {
  var node:AccessibilityNodeInfo?=null
  await("control $text") { node=find{it.text?.toString()?.equals(text,ignoreCase=true)==true && it.isEnabled};node!=null }
  var clickable=node!!
  while(!clickable.isClickable && clickable.parent!=null)clickable=clickable.parent
  check(clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)){"No se pudo pulsar $text"}
  SystemClock.sleep(200)
 }
 private fun setField(id:String,value:String) {
  var node:AccessibilityNodeInfo?=null
  await("campo $id") {node=find{it.viewIdResourceName=="com.medinagarces.reportesapp:id/$id"};node!=null}
  check(node!!.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,Bundle().apply{putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value)}))
 }
}
