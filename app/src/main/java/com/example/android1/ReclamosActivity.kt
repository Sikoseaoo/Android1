package com.example.android1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.android1.data.Reporte
import com.example.android1.data.ReporteDao
import com.example.android1.data.Zona
import com.example.android1.data.ZonaDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.Date

class ReclamosActivity : AppCompatActivity() {

    private lateinit var lista: RecyclerView
    private lateinit var contador: TextView
    private lateinit var vacio: TextView
    private lateinit var filtroZona: Spinner
    private lateinit var filtroCategoria: Spinner
    private lateinit var busqueda: EditText

    private val reporteDao by lazy { ReporteDao(this) }
    private val zonaDao by lazy { ZonaDao(this) }

    private var zonas: List<Zona> = emptyList()
    private val reclamos = mutableListOf<Reporte>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reclamos)

        findViewById<View>(R.id.botonVolverReclamos).setOnClickListener { finish() }

        contador = findViewById(R.id.contadorReclamos)
        vacio = findViewById(R.id.vacioReclamos)
        lista = findViewById(R.id.listaReclamos)
        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = Adaptador()

        filtroZona = findViewById(R.id.spinnerZona)
        filtroCategoria = findViewById(R.id.spinnerCategoria)

        filtroZona.onItemSelectedListener = alCambiarFiltro()
        filtroCategoria.onItemSelectedListener = alCambiarFiltro()

        filtroCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.filtro_todos_motivos)) + Reporte.CATEGORIAS,
        )

        busqueda = findViewById(R.id.campoBusqueda)
        busqueda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (zonas.isNotEmpty()) cargar()
            }
        })

        findViewById<FloatingActionButton>(R.id.botonNuevoReclamo).setOnClickListener {
            abrirFormulario(null)
        }

        cargarZonas()
    }

    private fun alCambiarFiltro() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(padre: AdapterView<*>?, vista: View?, posicion: Int, id: Long) {
            if (zonas.isNotEmpty()) cargar()
        }

        override fun onNothingSelected(padre: AdapterView<*>?) = Unit
    }

    private fun cargarZonas() {
        Tareas.consultar({ zonaDao.listar() }) { resultado ->
            zonas = resultado
            filtroZona.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(getString(R.string.filtro_todas)) + zonas.map { it.nombre },
            )
            cargar()
        }
    }

    private fun cargar() {
        val zonaSeleccionada = filtroZona.selectedItemPosition - 1
        val categoriaSeleccionada = filtroCategoria.selectedItemPosition - 1
        val texto = busqueda.text.toString().trim()

        val zonaId = if (zonaSeleccionada in zonas.indices) zonas[zonaSeleccionada].id else null
        val categoria = if (categoriaSeleccionada >= 0) {
            Reporte.CATEGORIAS[categoriaSeleccionada]
        } else {
            null
        }

        Tareas.consultar({ reporteDao.filtrar(zonaId, categoria, texto) }) { resultado ->
            reclamos.clear()
            reclamos.addAll(resultado)
            lista.adapter?.notifyDataSetChanged()
            contador.text = getString(R.string.reclamos_contador, reclamos.size)
            vacio.visibility = if (reclamos.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun abrirFormulario(reclamo: Reporte?) {
        if (zonas.isEmpty()) return

        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_reclamo, null)
        val spinnerZona = vista.findViewById<Spinner>(R.id.spinnerFormZona)
        val spinnerCategoria = vista.findViewById<Spinner>(R.id.spinnerFormCategoria)
        val campoAutor = vista.findViewById<EditText>(R.id.campoAutor)
        val campoMensaje = vista.findViewById<EditText>(R.id.campoMensaje)

        spinnerZona.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, zonas.map { it.nombre }
        )
        spinnerCategoria.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, Reporte.CATEGORIAS
        )

        if (reclamo != null) {
            campoAutor.setText(reclamo.autor)
            campoMensaje.setText(reclamo.mensaje)
            spinnerZona.setSelection(zonas.indexOfFirst { it.id == reclamo.zonaId }.coerceAtLeast(0))
            spinnerCategoria.setSelection(Reporte.CATEGORIAS.indexOf(reclamo.categoria).coerceAtLeast(0))
        }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(if (reclamo == null) R.string.form_titulo_nuevo else R.string.form_titulo_editar)
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

            val registro = Reporte(
                id = reclamo?.id ?: 0,
                zonaId = zonas[spinnerZona.selectedItemPosition].id,
                autor = autor,
                categoria = Reporte.CATEGORIAS[spinnerCategoria.selectedItemPosition],
                mensaje = mensaje,
                fecha = reclamo?.fecha ?: Date(),
            )

            Tareas.ejecutar {
                if (reclamo == null) reporteDao.insertar(registro) else reporteDao.actualizar(registro)
            }
            avisar(getString(R.string.reclamo_guardado))
            dialogo.dismiss()
            cargar()
        }
    }

    private fun confirmarBorrado(reclamo: Reporte) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirmar_eliminar)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                Tareas.ejecutar { reporteDao.eliminar(reclamo.id) }
                avisar(getString(R.string.reclamo_eliminado))
                cargar()
            }
            .show()
    }

    private fun avisar(mensaje: String) {
        Snackbar.make(lista, mensaje, Snackbar.LENGTH_SHORT).show()
    }

    private inner class Adaptador : RecyclerView.Adapter<Adaptador.Fila>() {

        inner class Fila(inflada: View) : RecyclerView.ViewHolder(inflada) {
            val categoria: TextView = inflada.findViewById(R.id.itemCategoria)
            val fecha: TextView = inflada.findViewById(R.id.itemFecha)
            val zona: TextView = inflada.findViewById(R.id.itemZona)
            val mensaje: TextView = inflada.findViewById(R.id.itemMensaje)
            val autor: TextView = inflada.findViewById(R.id.itemAutor)
            val editar: ImageButton = inflada.findViewById(R.id.botonEditar)
            val borrar: ImageButton = inflada.findViewById(R.id.botonBorrar)
        }

        override fun onCreateViewHolder(padre: ViewGroup, tipo: Int): Fila {
            val inflada = LayoutInflater.from(padre.context)
                .inflate(R.layout.item_reclamo, padre, false)
            return Fila(inflada)
        }

        override fun onBindViewHolder(fila: Fila, posicion: Int) {
            val reclamo = reclamos[posicion]
            fila.categoria.text = reclamo.categoria
            fila.fecha.text = reclamo.fechaTexto
            fila.zona.text = reclamo.zonaNombre
            fila.mensaje.text = reclamo.mensaje
            fila.autor.text = reclamo.autor
            fila.editar.setOnClickListener { abrirFormulario(reclamo) }
            fila.borrar.setOnClickListener { confirmarBorrado(reclamo) }
        }

        override fun getItemCount(): Int = reclamos.size
    }
}