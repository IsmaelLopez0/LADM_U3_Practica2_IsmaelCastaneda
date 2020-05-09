package mx.edu.ittepic.ladm_u3_practica2_ismaelcastaneda

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var listaID = ArrayList<String>()
    var listaData = ArrayList<String>()
    var listaEntregado = ArrayList<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mostrarTodo()

        insertar.setOnClickListener {
            insertar()
        }

        consultar.setOnClickListener {
            dialogo()
        }

    }

    private fun dialogo() {
        var dialogo = Dialog(this)

        dialogo.setContentView(R.layout.consulta)

        var nombre = dialogo.findViewById<EditText>(R.id.nombreCliente)
        var buscar = dialogo.findViewById<Button>(R.id.buscar)
        var cancelar = dialogo.findViewById<Button>(R.id.cancelar)

        dialogo.show()

        cancelar.setOnClickListener { dialogo.dismiss() }
        buscar.setOnClickListener {
            if(nombre.text.isEmpty()){
                dialogo("Debes de poner el nombre del cliente para la busqueda.\nSe mostrará información de todos los clintes.")
                mostrarTodo()
                dialogo.dismiss()
                return@setOnClickListener
            }
            consulta(nombre.text.toString())
            dialogo.dismiss()
        }

    }

    private fun consulta(nombre: String) {
        baseRemota.collection("restaurante")
            .whereEqualTo("nombre", nombre)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    mensaje("Error, no hay conexión")
                    return@addSnapshotListener
                }
                listaID.clear()
                listaData.clear()
                listaEntregado.clear()
                var data = ""
                for(document in querySnapshot!!){
                    data = "Nombre: ${document.getString("nombre")}\n" +
                            "Celular: ${document.getString("celular")}\n" +
                            "Domicilio: ${document.getString("domicilio")}\n" +
                            "Pedido:\n  Producto: ${document.getString("pedido.producto")}\n" +
                            "  Precio: ${document.getDouble("pedido.precio")}\n" +
                            "  Cantidad: ${document.get("pedido.cantidad")}\n"
                    if(document.getBoolean("pedido.entregado")!!){ data += "  Entregado: Sí"}
                    else{ data += "  Entregado: No" }
                    listaData.add(data)
                    listaID.add(document.id)
                    listaEntregado.add(document.getBoolean("pedido.entregado")!!)
                }
                if(listaData.size==0){
                    listaData.add("No hay resultados que coincidan con su busqueda.")
                }

                var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaData)
                lista.adapter = adaptador
            }

        lista.setOnItemClickListener { parent, view, position, id ->
            dialogoActElim(position)
        }
    }

    private fun dialogoActElim(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("¿Qué deceas hacer con:")
            .setMessage("${listaData[position]}?")
            .setPositiveButton("Act. entregado"){ d, i ->
                actualizarEntrega(listaID[position], position)
            }
            .setNegativeButton("Eliminar"){ d, i ->
                baseRemota.collection("restaurante")
                    .document(listaID[position])
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Se eliminó con exito", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "No se eliminó", Toast.LENGTH_LONG).show()
                    }
            }
            .setNeutralButton("Regresar"){ d, i -> }
            .show()
    }

    private fun actualizarEntrega(id: String, position: Int) {
        var valor = !listaEntregado[position]
        var data = hashMapOf<String, Any>("pedido.entregado" to valor)
        baseRemota.collection("restaurante")
            .document(id)
            .update(data)
            .addOnSuccessListener {
                mensaje("Actualización realizada")
                listaEntregado[position] = valor
            }
            .addOnFailureListener {
                mensaje( "Error, no se puede actualizar\nNo hay conexión")
            }
    }

    private fun insertar(){
        if(estanVacios()){
            dialogo("Todos los campos deben estar llenos")
            return
        }
        var ent = false
        if(entregado.isChecked){
            ent = true
        }
        var data = hashMapOf(
            "nombre" to nombre.text.toString(),
            "domicilio" to domicilio.text.toString(),
            "celular" to celular.text.toString(),
            "pedido" to hashMapOf(
                "producto" to producto.text.toString(),
                "precio" to precio.text.toString().toDouble(),
                "cantidad" to cantidad.text.toString().toInt(),
                "entregado" to ent
            )
        )

        baseRemota.collection("restaurante")
            .add(data as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Se capturó", Toast.LENGTH_LONG).show()
                limpiar()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se capturó", Toast.LENGTH_LONG).show()
            }

    }

    private fun mostrarTodo(){
        baseRemota.collection("restaurante")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    mensaje("Error, no hay conexión")
                    return@addSnapshotListener
                }
                listaID.clear()
                listaData.clear()
                listaEntregado.clear()
                var data = ""
                for(document in querySnapshot!!){
                    data = "Nombre: ${document.getString("nombre")}\n" +
                            "Celular: ${document.getString("celular")}\n" +
                            "Domicilio: ${document.getString("domicilio")}\n" +
                            "Pedido:\n  Producto: ${document.getString("pedido.producto")}\n" +
                            "  Precio: ${document.getDouble("pedido.precio")}\n" +
                            "  Cantidad: ${document.get("pedido.cantidad")}\n"
                    if(document.getBoolean("pedido.entregado")!!){ data += "  Entregado: Sí"}
                    else{ data += "  Entregado: No" }
                    listaData.add(data)
                    listaID.add(document.id)
                    listaEntregado.add(document.getBoolean("pedido.entregado")!!)
                }
                if(listaData.size==0){
                    listaData.add("No hay resultados que coincidan con su busqueda.")
                }

                var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaData)
                lista.adapter = adaptador
            }

        lista.setOnItemClickListener { parent, view, position, id ->
            dialogoActElim(position)
        }
    }

    private fun estanVacios(): Boolean{
        if(nombre.text.isEmpty() || domicilio.text.isEmpty() || celular.text.isEmpty() || producto.text.isEmpty() || precio.text.isEmpty() || cantidad.text.isEmpty()){
            return true
        }
        return false
    }

    private fun limpiar() {
        nombre.setText("")
        domicilio.setText("")
        celular.setText("")
        producto.setText("")
        precio.setText("")
        cantidad.setText("")
    }

    private fun mensaje(s: String){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun dialogo(s: String){
        AlertDialog.Builder(this)
            .setTitle("Atención")
            .setMessage(s)
            .setPositiveButton("Ok"){ d, i -> }
            .show()
    }

}
