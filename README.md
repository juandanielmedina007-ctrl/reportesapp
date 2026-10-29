# Reporta Limpio — Android

Autor: Juan Daniel Medina Garces. Package: `com.medinagarces.reportesapp`.

Kotlin, Android Views/XML, RecyclerView, Retrofit, OkHttp, ViewModel y LiveData. Mínimo Android 8 (API 26), compile/target SDK 36, JDK 21.

## Abrir y ejecutar

Abra esta carpeta en Android Studio, configure JDK 21 y Android SDK 36; deje que Gradle sincronice. Inicie la API y ejecute la app en el emulador. `local.properties` es específico de cada PC y se excluye del repositorio.

Compilar desde terminal: `./gradlew.bat assembleDebug lintDebug`.

APK generado: `app/build/outputs/apk/debug/app-debug.apk`.

## Cambiar servidor

La URL predeterminada es `http://10.0.2.2:8080/`. Para Render:

```powershell
./gradlew.bat assembleDebug '-PAPI_BASE_URL=https://SU-SERVICIO.onrender.com/'
```

También puede definir `API_BASE_URL=https://SU-SERVICIO.onrender.com/` en `gradle.properties`. Mantenga la barra final. Para celular físico utilice la IP de su PC en la misma red, por ejemplo `http://192.168.1.20:8080/`, y permita ese puerto en su red local.

El manifiesto contiene INTERNET y permite HTTP para la demostración local. Las peticiones tienen 120 segundos de espera de lectura para el primer arranque del servicio en Render.

## Flujo y arquitectura

`MainActivity` presenta los layouts y escucha el estado. `ReportesViewModel` coordina carga, búsqueda, creación y atención. `ReporteRepository` encapsula Retrofit y `ReportesApi` declara los cinco endpoints.

La lista comienza con 5 reportes; el botón agrega 5 siguientes y desaparece al finalizar. Buscar DNI obtiene todos sus reportes y desactiva la paginación durante el filtro. Ver todos/Actualizar restaura la lista general. Al registrar se abre el detalle creado; al atender se confirma la acción y se actualiza el estado devuelto por la API. No existe base de datos simulada dentro de la app: los reportes provienen de la API.

Incluye validación del formulario, bloqueo de solicitudes simultáneas, mensajes sin resultados, errores de conexión, reintento y estados de carga. ViewModel conserva las consultas durante cambios de configuración. Los campos de formulario usan IDs para que Android restaure el texto.
