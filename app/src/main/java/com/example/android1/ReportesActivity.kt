package com.example.android1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android1.data.PlayasTarapaca
import com.example.android1.data.Reporte
import com.example.android1.data.ReportesAlmacen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Date

class ReportesActivity : AppCompatActivity() {

    private lateinit var lista: RecyclerView
    private lateinit var contador: TextView

    private val datos = mutableListOf<Reporte>()
    private val todos = mutableListOf<Reporte>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        findViewById<View>(R.id.botonVolver).setOnClickListener { finish() }

        contador = findViewById(R.id.contadorReportes)
        lista = findViewById(R.id.listaReportes)
        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = Adaptador(datos)

        findViewById<FloatingActionButton>(R.id.botonNuevoReporte).setOnClickListener {
            abrirFormulario()
        }

        val filtros = findViewById<RadioGroup>(R.id.filtrosCategoria)
        filtros.addView(
            RadioButton(this).apply {
                text = getString(R.string.filtro_todas)
                isChecked = true
            }
        )
        for (categoria in ReportesAlmacen.CATEGORIAS) {
            filtros.addView(RadioButton(this).apply { text = categoria })
        }
        for (indice in 0 until filtros.childCount) {
            filtros.getChildAt(indice).setOnClickListener {
                val etiqueta = (filtros.getChildAt(indice) as RadioButton).text.toString()
                filtrar(etiqueta)
            }
        }

        ReportesAlmacen.conSemilla(this)
        cargar()
    }

    private fun cargar() {
        todos.clear()
        todos.addAll(ReportesAlmacen.leer(this).sortedByDescending { it.fecha })
        filtrar(getString(R.string.filtro_todas))
    }

    private fun filtrar(categoria: String) {
        datos.clear()

        if (categoria == getString(R.string.filtro_todas)) {
            datos.addAll(todos)
        } else {
            datos.addAll(ReportesAlmacen.porCategoria(todos, categoria))
        }

        lista.adapter?.notifyDataSetChanged()
        contador.text = getString(R.string.reportes_contador, datos.size)
    }

    private fun abrirFormulario() {
        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_reporte, null)

        val grupoPlayas = vista.findViewById<RadioGroup>(R.id.grupoPlayas)
        val grupoCategorias = vista.findViewById<RadioGroup>(R.id.grupoCategorias)
        val campoDescripcion = vista.findViewById<EditText>(R.id.campoDescripcion)

        for (playa in PlayasTarapaca.todas) {
            grupoPlayas.addView(
                RadioButton(this).apply {
                    text = playa.nombre
                    isChecked = playa == PlayasTarapaca.todas.firstOrNull()
                }
            )
        }

        for (categoria in ReportesAlmacen.CATEGORIAS) {
            grupoCategorias.addView(
                RadioButton(this).apply {
                    text = categoria
                    isChecked = categoria == ReportesAlmacen.CATEGORIAS.firstOrNull()
                }
            )
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.form_titulo)
            .setView(vista)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.guardar, null)
            .create()
            .also { dialogo ->
                dialogo.show()
                dialogo.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val playa = textoSeleccionado(grupoPlayas)
                    val categoria = textoSeleccionado(grupoCategorias)
                    val descripcion = campoDescripcion.text.toString().trim()

                    if (descripcion.isEmpty()) {
                        campoDescripcion.error = getString(R.string.form_error_descripcion)
                        return@setOnClickListener
                    }

                    val reporte = Reporte(
                        id = ReportesAlmacen.siguienteId(this),
                        playa = playa,
                        categoria = categoria,
                        descripcion = descripcion,
                        fecha = Date(),
                    )

                    ReportesAlmacen.agregar(this, reporte)
                    cargar()
                    dialogo.dismiss()
                }
            }
    }

    private fun textoSeleccionado(grupo: RadioGroup): String {
        val indice = grupo.checkedRadioButtonId
        if (indice == -1) return (grupo.getChildAt(0) as RadioButton).text.toString()
        return (grupo.findViewById<RadioButton>(indice)).text.toString()
    }

    private inner class Adaptador(
        private val items: List<Reporte>,
    ) : RecyclerView.Adapter<Adaptador.Fila>() {

        inner class Fila(inflada: View) : RecyclerView.ViewHolder(inflada) {
            val playa: TextView = inflada.findViewById(R.id.itemPlaya)
            val categoria: TextView = inflada.findViewById(R.id.itemCategoria)
            val descripcion: TextView = inflada.findViewById(R.id.itemDescripcion)
            val fecha: TextView = inflada.findViewById(R.id.itemFecha)
        }

        override fun onCreateViewHolder(padre: ViewGroup, tipo: Int): Fila {
            val inflada = LayoutInflater.from(padre.context)
                .inflate(R.layout.item_reporte, padre, false)
            return Fila(inflada)
        }

        override fun onBindViewHolder(fila: Fila, posicion: Int) {
            val reporte = items[posicion]
            fila.playa.text = reporte.playa
            fila.categoria.text = reporte.categoria
            fila.descripcion.text = reporte.descripcion
            fila.fecha.text = reporte.fechaTexto
        }

        override fun getItemCount(): Int = items.size
    }
}