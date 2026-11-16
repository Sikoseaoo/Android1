# Diagrama de flujo de actividades (UI Flow / Intents)

Navegación entre pantallas de la app, con los intents que se usan en cada salto.
Los intents son explícitos (`Intent(this, Destino::class.java)`) salvo el mapa
externo, que es un intent implícito `geo:`.

```mermaid
flowchart TD
    Inicio([Inicio]) --> Main[MainActivity]

    Main -->|botonMapa<br/>Intent explícito| Maps[MapsActivity]
    Main -->|botonReclamos<br/>Intent explícito| Reclamos[ReclamosActivity]
    Main -->|botonPerfil<br/>Intent explícito| Perfil[PerfilActivity]

    subgraph MapsActivity
        Maps --> Motor{¿Hay clave de Google<br/>y Play Services?}
        Motor -->|Sí| GMap[Google Maps<br/>SupportMapFragment]
        Motor -->|No| OSM[osmdroid<br/>teselas de Esri]
        GMap --> MapaListo[Dibuja marcadores<br/>de zonas]
        OSM --> MapaListo
        Maps -->|spinnerCategoriaMapa| FiltroMapa[ZonaDao.listar / porCategoria]
        MapaListo -->|clic en marcador| AbrirZona[abrirZona]
        Maps -->|botonMiUbicacion| Permiso{¿Permiso de<br/>ubicación?}
        Permiso -->|No| PedirPermiso[Solicita ACCESS_FINE_LOCATION]
        PedirPermiso --> Permiso
        Permiso -->|Sí| Ubicacion[FusedLocationProviderClient<br/>Ubicacion.ultima]
        Ubicacion --> MiUbicacion[Centra el mapa<br/>y marca la posición]
    end

    AbrirZona -->|Intent explícito<br/>putExtra EXTRA_ZONA_ID| Zona[ZonaActivity]

    subgraph ZonaActivity
        Zona --> CargarZona[ZonaDao.porId<br/>ReporteDao.porZona]
        CargarZona --> Detalle[Detalle de la zona<br/>y lista de reclamos]
        Detalle -->|botonVerMapa| MapaExterno[Intent implícito geo:<br/>ACTION_VIEW]
        Detalle -->|botonEditarZona| DialogoZona[Diálogo editar zona]
        DialogoZona --> GuardarZona[ZonaDao.actualizar]
        Detalle -->|botonBorrarZona| ConfirmarZona[Confirmar borrado]
        ConfirmarZona --> BorrarZona[ReporteDao.eliminarPorZona<br/>ZonaDao.eliminar]
        Detalle -->|botonNuevoReclamoZona| DialogoNuevo[Diálogo nuevo reclamo]
        DialogoNuevo --> InsertarReclamo[ReporteDao.insertar]
        Detalle -->|botonBorrar del ítem| EliminarReclamo[ReporteDao.eliminar]
        Detalle -->|onResume / onPause| Sensor[SensorEventListener<br/>acelerómetro]
        Detalle -->|botonVolverZona| FinZona([finish])
    end

    subgraph ReclamosActivity
        Reclamos --> CargarZonas[ZonaDao.listar<br/>llena el spinner de zonas]
        CargarZonas --> Filtrar[ReporteDao.filtrar<br/>zona + categoría + texto]
        Reclamos -->|botonNuevoReclamo| DialogoReclamo[Diálogo crear / editar]
        DialogoReclamo --> GuardarReclamo[ReporteDao.insertar / actualizar]
        Reclamos -->|botonEditar del ítem| DialogoReclamo
        Reclamos -->|botonBorrar del ítem| ConfirmarReclamo[Confirmar borrado]
        ConfirmarReclamo --> EliminarReclamo2[ReporteDao.eliminar]
        Reclamos -->|botonVolverReclamos| FinReclamos([finish])
    end

    subgraph PerfilActivity
        Perfil --> Contar[ZonaDao.contar<br/>ReporteDao.contar]
        Contar --> Resumen[Muestra zonas<br/>y reclamos]
        Perfil -->|botonVolver| FinPerfil([finish])
    end

    FinZona --> Maps
    FinReclamos --> Main
    FinPerfil --> Main
```

## Intents usados

| Origen | Destino | Tipo | Datos |
|---|---|---|---|
| `MainActivity` | `MapsActivity` | explícito | — |
| `MainActivity` | `ReclamosActivity` | explícito | — |
| `MainActivity` | `PerfilActivity` | explícito | — |
| `MapsActivity` | `ZonaActivity` | explícito | `putExtra(EXTRA_ZONA_ID, zona.id)` |
| `ZonaActivity` | app de mapas del sistema | implícito | `geo:lat,lon?q=...` con `ACTION_VIEW` |
