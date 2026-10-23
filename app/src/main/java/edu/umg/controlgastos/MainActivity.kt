
package edu.umg.controlgastos

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.umg.controlgastos.datos.BaseDatos
import edu.umg.controlgastos.datos.Movimiento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var baseDatos: BaseDatos

    private lateinit var tvTotalIngresos: TextView
    private lateinit var tvTotalGastos: TextView
    private lateinit var tvSaldo: TextView
    private lateinit var tvHistorial: TextView

    private lateinit var etDescripcion: EditText
    private lateinit var etMonto: EditText
    private lateinit var etCategoria: EditText

    private lateinit var rbGasto: RadioButton
    private lateinit var rbIngreso: RadioButton
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        baseDatos = BaseDatos(this)

        tvTotalIngresos = findViewById(R.id.tvTotalIngresos)
        tvTotalGastos = findViewById(R.id.tvTotalGastos)
        tvSaldo = findViewById(R.id.tvSaldo)
        tvHistorial = findViewById(R.id.tvHistorial)

        etDescripcion = findViewById(R.id.etDescripcion)
        etMonto = findViewById(R.id.etMonto)
        etCategoria = findViewById(R.id.etCategoria)

        rbGasto = findViewById(R.id.rbGasto)
        rbIngreso = findViewById(R.id.rbIngreso)
        btnGuardar = findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            guardarMovimiento()
        }

        actualizarResumen()
    }

    private fun guardarMovimiento() {
        val descripcion = etDescripcion.text.toString().trim()
        val montoTexto = etMonto.text.toString()
            .trim()
            .replace(",", ".")
        val categoria = etCategoria.text.toString().trim()

        if (descripcion.isEmpty() ||
            montoTexto.isEmpty() ||
            categoria.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Completa todos los campos.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val monto = montoTexto.toDoubleOrNull()

        if (monto == null || monto <= 0) {
            Toast.makeText(
                this,
                "Ingresa un monto válido mayor que cero.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val tipo = if (rbIngreso.isChecked) {
            "INGRESO"
        } else {
            "GASTO"
        }

        val movimiento = Movimiento(
            descripcion = descripcion,
            monto = monto,
            categoria = categoria,
            tipo = tipo
        )

        val resultado = baseDatos.guardarMovimiento(movimiento)

        if (resultado != -1L) {
            Toast.makeText(
                this,
                "Movimiento guardado correctamente.",
                Toast.LENGTH_SHORT
            ).show()

            etDescripcion.text.clear()
            etMonto.text.clear()
            etCategoria.text.clear()

            actualizarResumen()
        } else {
            Toast.makeText(
                this,
                "No se pudo guardar el movimiento.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun actualizarResumen() {
        val movimientos = baseDatos.obtenerMovimientos()

        val ingresos = movimientos
            .filter { it.tipo == "INGRESO" }
            .sumOf { it.monto }

        val gastos = movimientos
            .filter { it.tipo == "GASTO" }
            .sumOf { it.monto }

        val saldo = ingresos - gastos

        tvTotalIngresos.text = "Total de ingresos: ${formatearDinero(ingresos)}"
        tvTotalGastos.text = "Total de gastos: ${formatearDinero(gastos)}"
        tvSaldo.text = "Saldo disponible: ${formatearDinero(saldo)}"

        if (movimientos.isEmpty()) {
            tvHistorial.text = "Aún no hay movimientos registrados."
        } else {
            tvHistorial.text = movimientos.joinToString(
                separator = "\n\n"
            ) { movimiento ->

                val fecha = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(movimiento.fecha))

                val signo = if (movimiento.tipo == "INGRESO") {
                    "+"
                } else {
                    "-"
                }

                "$signo ${movimiento.descripcion}\n" +
                        "Tipo: ${movimiento.tipo}\n" +
                        "Categoría: ${movimiento.categoria}\n" +
                        "Monto: ${formatearDinero(movimiento.monto)}\n" +
                        "Fecha: $fecha"
            }
        }
    }

    private fun formatearDinero(monto: Double): String {
        return "Q " + String.format(Locale.US, "%.2f", monto)
    }

    override fun onDestroy() {
        baseDatos.close()
        super.onDestroy()
    }
}
