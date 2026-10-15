# Iquique Somos

Aplicación Android para que la gente de Iquique apoye las zonas de su ciudad.
El mapa muestra las zonas con marcadores y cada una tiene una lista de comentarios
donde la comunidad cuenta por qué le importa ese lugar.

## Qué hace

- **Mapa de Iquique** con las 12 zonas más conocidas de la ciudad, cada una con su
  marcador. Se puede filtrar por categoría (Playa, Plaza, Histórico, Naturaleza, Comercio).
- **Comentarios de apoyo** por zona: crear, editar y borrar, con motivo y fecha.
- **búsqueda y filtros** por zona, motivo y texto libre del mensaje o del autor.
- **mi ubicación** en el mapa a partir del GPS del dispositivo.
- **perfil** con la cantidad de zonas cargadas y de apoyos registrados.

## Técnicas usadas

| Área | Implementación |
|---|---|
| Persistencia | `SQLiteOpenHelper` con clase `ContratoIquique` que extiende `BaseColumns` |
| Consultas | `ContentValues` para escribir, `Cursor` y `rawQuery` para leer, con `selectionArgs` |
| Geolocalización | `FusedLocationProviderClient` de Google Play Services |
| Permisos | `ACCESS_FINE_LOCATION` solicitado en runtime con `ActivityResultContracts` |
| Interfaz | `ConstraintLayout`, `RecyclerView` con Adapter, `CardView`, `Spinner` |
| Navegación | Activities con Intents explícitos, `putExtra` para pasar el id de la zona e Intent implícito `geo:` |
| Multihilo | `ExecutorService` en `Tareas` para no bloquear el hilo principal |
| Sensores | `SensorEventListener` del acelerómetro, registrado en `onResume` y dado de baja en `onPause` |

El mapa usa **OpenStreetMap** con `osmdroid`. Los marcadores y los comentarios no
dependen de ninguna clave de API.

## Estructura

```
app/src/main/java/com/example/android1/
├── data/
│   ├── ContratoIquique.kt     esquema de las tablas
│   ├── BaseDeDatosHelper.kt   SQLiteOpenHelper, onCreate y onUpgrade
│   ├── ZonaDao.kt             CRUD de zonas
│   ├── ApoyoDao.kt            CRUD de apoyos con filtros
│   ├── IquiqueCatalogo.kt     zonas que se cargan al crear la base
│   ├── Zona.kt                modelo de zona
│   └── Apoyo.kt               modelo de apoyo
├── MainActivity.kt            pantalla de inicio
├── MapsActivity.kt            mapa con marcadores
├── ApoyosActivity.kt          lista de apoyos con filtros y CRUD
├── ZonaActivity.kt            detalle de una zona, con sensor
├── PerfilActivity.kt          resumen de la base
├── Ubicacion.kt               FusedLocationProviderClient
└── Tareas.kt                  ExecutorService para el trabajo en segundo plano
```

## Base de datos

Dos tablas definidas en `ContratoIquique`:

- **zona**: `_id`, `nombre`, `categoria`, `latitud`, `longitud`, `descripcion`
- **apoyo**: `_id`, `zona_id`, `autor`, `categoria`, `mensaje`, `fecha`

`apoyo.zona_id` es clave foránea hacia `zona._id`, con un índice para las consultas
por zona.

## Cómo ejecutarlo

```bash
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. Se instala en un
dispositivo o emulador con Android 7.0 (API 24) o superior.

## Permisos

La app pide `ACCESS_FINE_LOCATION` solo cuando se presiona el botón de ubicación,
no al abrirla.