package com.example.android1

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android1.data.Apoyo
import com.example.android1.data.ApoyoDao
import com.example.android1.data.IquiqueCatalogo
import com.example.android1.data.Zona
import com.example.android1.data.ZonaDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Date

class ZonaActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null

    private val apoyoDao by lazy { ApoyoDao(this) }
    private val zonaDao by lazy { ZonaDao(this) }

    private var zonaId: Long = 0
    private var zonaActual: Zona? = null
    private val apoyos = mutableListOf<Apoyo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zona)

        zonaId = intent.getLongExtra(EXTRA_ZONA_ID, 0)
        if (zonaId == 0L) {
            finish()
            return
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        findViewById<View>(R.id.botonVolverZona).setOnClickListener { finish() }

        val lista = findViewById<RecyclerView>(R.id.listaApoyosZona)
        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = Adaptador()

        findViewById<FloatingActionButton>(R.id.botonApoyarZona).setOnClickListener {
            abrirFormulario()
        }

        findViewById<View>(R.id.botonVerMapa).setOnClickListener { abrirEnMapaExterno() }
        findViewById<View>(R.id.botonEditarZona).setOnClickListener { abrirEdicion() }
        findViewById<View>(R.id.botonBorrarZona).setOnClickListener { confirmarBorrado() }

        cargarZona()
    }

    private fun abrirEdicion() {
        val zona = zonaActual ?: return

        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_zona, null)
        val campoNombre = vista.findViewById<EditText>(R.id.campoZonaNombre)
        val campoDescripcion = vista.findViewById<EditText>(R.id.campoZonaDescripcion)
        val campoLatitud = vista.findViewById<EditText>(R.id.campoZonaLatitud)
        val campoLongitud = vista.findViewById<EditText>(R.id.campoZonaLongitud)
        val spinnerCategoria = vista.findViewById<Spinner>(R.id.spinnerZonaCategoria)

        campoNombre.setText(zona.nombre)
        campoDescripcion.setText(zona.descripcion)
        campoLatitud.setText(zona.latitud.toString())
        campoLongitud.setText(zona.longitud.toString())

        spinnerCategoria.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, IquiqueCatalogo.CATEGORIAS
        )
        spinnerCategoria.setSelection(
            IquiqueCatalogo.CATEGORIAS.indexOf(zona.categoria).coerceAtLeast(0)
        )

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.form_titulo_zona)
            .setView(vista)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.guardar, null)
            .create()

        dialogo.show()
        dialogo.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = campoNombre.text.toString().trim()
            val descripcion = campoDescripcion.text.toString().trim()
            val latitud = campoLatitud.text.toString().trim().toDoubleOrNull()
            val longitud = campoLongitud.text.toString().trim().toDoubleOrNull()

            if (nombre.isEmpty()) {
                campoNombre.error = getString(R.string.form_error_nombre)
                return@setOnClickListener
            }
            if (latitud == null || longitud == null) {
                campoLatitud.error = getString(R.string.form_error_coordenadas)
                return@setOnClickListener
            }

            val actualizada = zona.copy(
                nombre = nombre,
                categoria = IquiqueCatalogo.CATEGORIAS[spinnerCategoria.selectedItemPosition],
                descripcion = descripcion,
                latitud = latitud,
                longitud = longitud,
            )

            Tareas.ejecutar { zonaDao.actualizar(actualizada) }
            Toast.makeText(this, R.string.zona_guardada, Toast.LENGTH_SHORT).show()
            dialogo.dismiss()
            cargarZona()
        }
    }

    private fun confirmarBorrado() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirmar_eliminar_zona)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                Tareas.ejecutar {
                    apoyoDao.eliminarPorZona(zonaId)
                    zonaDao.eliminar(zonaId)
                }
                Toast.makeText(this, R.string.zona_eliminada, Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }

    private fun abrirEnMapaExterno() {
        val zona = zonaActual ?: return
        val uri = Uri.parse(
            "geo:${zona.latitud},${zona.longitud}" +
                "?q=${zona.latitud},${zona.longitud}(${Uri.encode(zona.nombre)})"
        )

        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            Toast.makeText(
                this, getString(R.string.error_abrir_mapa, zona.nombre), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cargarZona() {
        Tareas.consultar({ zonaDao.porId(zonaId) }) { zona ->
            if (zona == null) {
                finish()
                return@consultar
            }

            zonaActual = zona

            findViewById<TextView>(R.id.zonaNombre).text = zona.nombre
            findViewById<TextView>(R.id.zonaCategoria).text = zona.categoria
            findViewById<TextView>(R.id.zonaDescripcion).text = zona.descripcion
            findViewById<TextView>(R.id.zonaCoordenadas).text =
                getString(R.string.coordenadas, zona.latitud, zona.longitud)
        }

        Tareas.consultar({ apoyoDao.porZona(zonaId) }) { resultado ->
            apoyos.clear()
            apoyos.addAll(resultado)
            findViewById<RecyclerView>(R.id.listaApoyosZona).adapter?.notifyDataSetChanged()
            findViewById<TextView>(R.id.zonaApoyos).text =
                getString(R.string.zona_apoyos, resultado.size)
        }
    }

    private fun abrirFormulario() {
        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_apoyo, null)
        val spinnerCategoria = vista.findViewById<Spinner>(R.id.spinnerFormCategoria)
        val campoAutor = vista.findViewById<EditText>(R.id.campoAutor)
        val campoMensaje = vista.findViewById<EditText>(R.id.campoMensaje)

        vista.findViewById<Spinner>(R.id.spinnerFormZona).isEnabled = false
        spinnerCategoria.adapter = android.widget.ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, Apoyo.CATEGORIAS
        )

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.form_titulo_nuevo)
            .setView(vista)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.guardar, null)
            .create()

        dialogo.show()
        dialogo.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val autor = campoAutor.text.toString().trim()
            val mensaje = campoMensaje.text.toString().trim()

            if (autor.isEmpty()) {
                campoAutor.error = getString(R.string.form_error_autor)
                return@setOnClickListener
            }
            if (mensaje.isEmpty()) {
                campoMensaje.error = getString(R.string.form_error_mensaje)
                return@setOnClickListener
            }

            val apoyo = Apoyo(
                id = 0,
                zonaId = zonaId,
                autor = autor,
                categoria = Apoyo.CATEGORIAS[spinnerCategoria.selectedItemPosition],
                mensaje = mensaje,
                fecha = Date(),
            )

            Tareas.ejecutar { apoyoDao.insertar(apoyo) }
            dialogo.dismiss()
            cargarZona()
        }
    }

    override fun onSensorChanged(evento: SensorEvent) {
        if (evento.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = evento.values[0] / SensorManager.GRAVITY_EARTH
        val y = evento.values[1] / SensorManager.GRAVITY_EARTH
        findViewById<TextView>(R.id.zonaSensor).text =
            getString(R.string.sensor_acelerometro, x, y)
    }

    override fun onAccuracyChanged(sensor: Sensor?, precision: Int) = Unit

    override fun onResume() {
        super.onResume()
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private inner class Adaptador : RecyclerView.Adapter<Adaptador.Fila>() {

        inner class Fila(inflada: View) : RecyclerView.ViewHolder(inflada) {
            val autor: TextView = inflada.findViewById(R.id.itemAutor)
            val fecha: TextView = inflada.findViewById(R.id.itemFecha)
            val categoria: TextView = inflada.findViewById(R.id.itemCategoria)
            val mensaje: TextView = inflada.findViewById(R.id.itemMensaje)
            val borrar: ImageButton = inflada.findViewById(R.id.botonBorrar)
        }

        override fun onCreateViewHolder(padre: ViewGroup, tipo: Int): Fila {
            val inflada = LayoutInflater.from(padre.context)
                .inflate(R.layout.item_apoyo, padre, false)
            return Fila(inflada)
        }

        override fun onBindViewHolder(fila: Fila, posicion: Int) {
            val apoyo = apoyos[posicion]
            fila.autor.text = apoyo.autor
            fila.fecha.text = apoyo.fechaTexto
            fila.categoria.text = apoyo.categoria
            fila.mensaje.text = apoyo.mensaje
            fila.borrar.setOnClickListener {
                Tareas.ejecutar { apoyoDao.eliminar(apoyo.id) }
                cargarZona()
            }
        }

        override fun getItemCount(): Int = apoyos.size
    }

    companion object {
        const val EXTRA_ZONA_ID = "com.example.android1.ZONA_ID"
    }
}