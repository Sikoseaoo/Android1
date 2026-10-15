# Reclamos Iquique

Aplicación Android para reportar reclamos y quejas sobre las zonas de Iquique.
El mapa muestra las zonas con marcadores y cada una tiene su propia lista de
reclamos, con motivo y fecha, que la gente puede crear, editar y borrar.

## Qué hace

- **Mapa de Iquique** con 12 zonas y sus marcadores, filtrables por categoría
  (Playa, Plaza, Histórico, Naturaleza, Comercio).
- **Reclamos por zona**: crear, editar y eliminar, con motivo, autor y fecha.
- **Búsqueda y filtros** por zona, motivo y texto libre del mensaje o del autor.
- **Mi ubicación** en el mapa a partir del GPS del dispositivo.
- **Perfil** con la cantidad de zonas cargadas y de reclamos registrados.
- **Lectura del acelerómetro** en la pantalla de cada zona.

## Mapa: Google Maps con respaldo

La app intenta usar **Google Maps** y cae a **OpenStreetMap** si no hay clave o
si el dispositivo no tiene Google Play Services. El cartel arriba del mapa indica
cuál de los dos se está usando.

La clave se lee de `local.properties`, que está en el `.gitignore`:

```properties
MAPS_API_KEY=tu_clave_aqui
```

Sin esa línea, o con la línea vacía, la app funciona igual usando OpenStreetMap.

## Técnicas usadas

| Área | Implementación |
|---|---|
| Persistencia | `SQLiteOpenHelper` con clase `ContratoIquique` que extiende `BaseColumns` |
| Consultas | `ContentValues` para escribir, `Cursor` y `rawQuery` para leer, con `selectionArgs` |
| Migración | `onUpgrade()` recrea el esquema al subir de la versión 1 a la 2 |
| Geolocalización | `FusedLocationProviderClient` de Google Play Services |
| Permisos | `ACCESS_FINE_LOCATION` solicitado en runtime con `ActivityResultContracts` |
| Mapas | `SupportMapFragment` con `OnMapReadyCallback`, `MarkerOptions` y `CameraUpdateFactory` |
| Interfaz | `ConstraintLayout`, `RecyclerView` con Adapter, `CardView`, `Spinner` |
| Navegación | Activities con Intents explícitos, `putExtra` para el id de zona e Intent implícito `geo:` |
| Multihilo | `ExecutorService` en `Tareas` para no bloquear el hilo principal |
| Sensores | `SensorEventListener` del acelerómetro, registrado en `onResume` y dado de baja en `onPause` |

## Estructura

```
app/src/main/java/com/example/android1/
├── data/
│   ├── ContratoIquique.kt     esquema de las tablas
│   ├── BaseDeDatosHelper.kt   SQLiteOpenHelper, onCreate y onUpgrade
│   ├── ZonaDao.kt             CRUD de zonas
│   ├── ReporteDao.kt          CRUD de reclamos, con filtros
│   ├── IquiqueCatalogo.kt     zonas que se cargan al crear la base
│   ├── Zona.kt                modelo de zona
│   └── Reporte.kt             modelo de reclamo
├── MainActivity.kt            pantalla de inicio
├── MapsActivity.kt            Google Maps con respaldo a OpenStreetMap
├── ReclamosActivity.kt        lista de reclamos con filtros y CRUD
├── ZonaActivity.kt            detalle de una zona, con sensor
├── PerfilActivity.kt          resumen de la base
├── Ubicacion.kt               FusedLocationProviderClient
└── Tareas.kt                  ExecutorService para el trabajo en segundo plano
```

## Base de datos

Dos tablas definidas en `ContratoIquique`:

- **zona**: `_id`, `nombre`, `categoria`, `latitud`, `longitud`, `descripcion`
- **reporte**: `_id`, `zona_id`, `autor`, `categoria`, `mensaje`, `fecha`

`reporte.zona_id` es clave foránea hacia `zona._id`, con un índice para las
consultas por zona.

## Cómo ejecutarlo

```bash
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. Se instala en un
dispositivo o emulador con Android 7.0 (API 24) o superior.

## Permisos

La app pide `ACCESS_FINE_LOCATION` solo cuando se presiona el botón de ubicación,
no al abrirla.