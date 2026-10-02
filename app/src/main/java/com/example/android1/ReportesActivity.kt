package com.example.android1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android1.data.Reporte
import com.example.android1.data.ReportesTarapaca

class ReportesActivity : AppCompatActivity() {

    private lateinit var lista: RecyclerView
    private lateinit var contador: TextView

    private val datos = mutableListOf<Reporte>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        findViewById<View>(R.id.botonVolver).setOnClickListener { finish() }

        contador = findViewById(R.id.contadorReportes)
        lista = findViewById(R.id.listaReportes)
        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = Adaptador(datos)

        cargar()
    }

    private fun cargar() {
        datos.clear()
        datos.addAll(ReportesTarapaca.todos.sortedByDescending { it.fecha })
        lista.adapter?.notifyDataSetChanged()
        contador.text = getString(R.string.reportes_contador, datos.size)
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
