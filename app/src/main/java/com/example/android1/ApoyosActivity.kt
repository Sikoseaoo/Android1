package com.example.android1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android1.data.Apoyo
import com.example.android1.data.ApoyoDao
import com.example.android1.data.Zona
import com.example.android1.data.ZonaDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.Date

class ApoyosActivity : AppCompatActivity() {

    private lateinit var lista: RecyclerView
    private lateinit var contador: TextView
    private lateinit var vacio: TextView
    private lateinit var filtroZona: Spinner

    private val apoyoDao by lazy { ApoyoDao(this) }
    private val zonaDao by lazy { ZonaDao(this) }

    private var zonas: List<Zona> = emptyList()
    private val apoyos = mutableListOf<Apoyo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apoyos)

        findViewById<View>(R.id.botonVolverApoyos).setOnClickListener { finish() }

        contador = findViewById(R.id.contadorApoyos)
        vacio = findViewById(R.id.vacioApoyos)
        lista = findViewById(R.id.listaApoyos)
        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = Adaptador()

        filtroZona = findViewById(R.id.spinnerZona)
        filtroZona.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(padre: AdapterView<*>?, vista: View?, posicion: Int, id: Long) {
                if (zonas.isNotEmpty()) cargar()
            }

            override fun onNothingSelected(padre: AdapterView<*>?) = Unit
        }

        findViewById<FloatingActionButton>(R.id.botonNuevoApoyo).setOnClickListener {
            abrirFormulario(null)
        }

        cargarZonas()
    }

    private fun cargarZonas() {
        Tareas.consultar({ zonaDao.listar() }) { lista ->
            zonas = lista
            filtroZona.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(getString(R.string.filtro_todas)) + zonas.map { it.nombre },
            )
            cargar()
        }
    }

    private fun cargar() {
        val seleccion = filtroZona.selectedItemPosition - 1
        Tareas.consultar({
            if (seleccion >= 0 && seleccion < zonas.size) {
                apoyoDao.porZona(zonas[seleccion].id)
            } else {
                apoyoDao.listar()
            }
        }) { resultado ->
            apoyos.clear()
            apoyos.addAll(resultado)
            lista.adapter?.notifyDataSetChanged()
            contador.text = getString(R.string.apoyos_contador, apoyos.size)
            vacio.visibility = if (apoyos.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun abrirFormulario(apoyo: Apoyo?) {
        if (zonas.isEmpty()) return

        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_apoyo, null)
        val spinnerZona = vista.findViewById<Spinner>(R.id.spinnerFormZona)
        val spinnerCategoria = vista.findViewById<Spinner>(R.id.spinnerFormCategoria)
        val campoAutor = vista.findViewById<EditText>(R.id.campoAutor)
        val campoMensaje = vista.findViewById<EditText>(R.id.campoMensaje)

        spinnerZona.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, zonas.map { it.nombre }
        )
        spinnerCategoria.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, Apoyo.CATEGORIAS
        )

        if (apoyo != null) {
            campoAutor.setText(apoyo.autor)
            campoMensaje.setText(apoyo.mensaje)
            spinnerZona.setSelection(zonas.indexOfFirst { it.id == apoyo.zonaId }.coerceAtLeast(0))
            spinnerCategoria.setSelection(Apoyo.CATEGORIAS.indexOf(apoyo.categoria).coerceAtLeast(0))
        }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(
                if (apoyo == null) R.string.form_titulo_nuevo else R.string.form_titulo_editar
            )
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

            val registro = Apoyo(
                id = apoyo?.id ?: 0,
                zonaId = zonas[spinnerZona.selectedItemPosition].id,
                autor = autor,
                categoria = Apoyo.CATEGORIAS[spinnerCategoria.selectedItemPosition],
                mensaje = mensaje,
                fecha = apoyo?.fecha ?: Date(),
            )

            Tareas.ejecutar {
                if (apoyo == null) apoyoDao.insertar(registro) else apoyoDao.actualizar(registro)
            }
            avisar(getString(R.string.apoyo_guardado))
            dialogo.dismiss()
            cargar()
        }
    }

    private fun confirmarBorrado(apoyo: Apoyo) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirmar_eliminar)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                Tareas.ejecutar { apoyoDao.eliminar(apoyo.id) }
                avisar(getString(R.string.apoyo_eliminado))
                cargar()
            }
            .show()
    }

    private fun avisar(mensaje: String) {
        Snackbar.make(lista, mensaje, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Tareas.cerrar()
    }

    private inner class Adaptador : RecyclerView.Adapter<Adaptador.Fila>() {

        inner class Fila(inflada: View) : RecyclerView.ViewHolder(inflada) {
            val autor: TextView = inflada.findViewById(R.id.itemAutor)
            val fecha: TextView = inflada.findViewById(R.id.itemFecha)
            val zona: TextView = inflada.findViewById(R.id.itemZona)
            val categoria: TextView = inflada.findViewById(R.id.itemCategoria)
            val mensaje: TextView = inflada.findViewById(R.id.itemMensaje)
            val editar: ImageButton = inflada.findViewById(R.id.botonEditar)
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
            fila.zona.text = apoyo.zonaNombre
            fila.categoria.text = apoyo.categoria
            fila.mensaje.text = apoyo.mensaje
            fila.editar.setOnClickListener { abrirFormulario(apoyo) }
            fila.borrar.setOnClickListener { confirmarBorrado(apoyo) }
        }

        override fun getItemCount(): Int = apoyos.size
    }
}