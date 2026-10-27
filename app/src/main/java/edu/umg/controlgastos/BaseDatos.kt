package edu.umg.controlgastos.datos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Movimiento(
    val id: Long = 0,
    val descripcion: String,
    val monto: Double,
    val categoria: String,
    val tipo: String,
    val fecha: Long = System.currentTimeMillis()
)

class BaseDatos(context: Context) :
    SQLiteOpenHelper(context, "control_gastos.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE movimientos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT NOT NULL,
                monto REAL NOT NULL,
                categoria TEXT NOT NULL,
                tipo TEXT NOT NULL,
                fecha INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Aquí agregaremos futuras actualizaciones sin perder los registros.
    }

    fun guardarMovimiento(movimiento: Movimiento): Long {
        val valores = ContentValues().apply {
            put("descripcion", movimiento.descripcion)
            put("monto", movimiento.monto)
            put("categoria", movimiento.categoria)
            put("tipo", movimiento.tipo)
            put("fecha", movimiento.fecha)
        }

        return writableDatabase.insert("movimientos", null, valores)
    }

    fun obtenerMovimientos(): List<Movimiento> {
        val lista = mutableListOf<Movimiento>()

        readableDatabase.rawQuery(
            """
            SELECT id, descripcion, monto, categoria, tipo, fecha
            FROM movimientos
            ORDER BY fecha DESC, id DESC
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Movimiento(
                        id = cursor.getLong(0),
                        descripcion = cursor.getString(1),
                        monto = cursor.getDouble(2),
                        categoria = cursor.getString(3),
                        tipo = cursor.getString(4),
                        fecha = cursor.getLong(5)
                    )
                )
            }
        }

        return lista
    }

    fun eliminarMovimiento(id: Long): Int {
        return writableDatabase.delete(
            "movimientos",
            "id = ?",
            arrayOf(id.toString())
        )
    }
}
