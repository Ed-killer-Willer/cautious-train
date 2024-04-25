

package com.example.flypack.worker.fragments_worker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.flypack.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator

private const val REQUEST_CODE_PERMISSIONS = 101

class Scan_Worker : Fragment() {

    // Variable para almacenar el tipo de trabajador
    private var workerType: String? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var Qr: ImageView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Database
        databaseReference = Firebase.database.reference

        // Solicitar permisos de cámara al iniciar el fragmento
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan__worker, container, false)

        // Acceder al ImageView para el escaneo del código QR
        val imageView: ImageView = view.findViewById(R.id.imageQR)


        // Configurar el clic del ImageView para iniciar el escaneo del código QR
        imageView.setOnClickListener {
            if (allPermissionsGranted()) {
                startScan()
            } else {
                Toast.makeText(requireContext(), "Permisos de cámara requeridos", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startScan()
            } else {
                Toast.makeText(requireContext(), "Permisos de cámara requeridos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    fun startScan() {
        IntentIntegrator.forSupportFragment(this@Scan_Worker)
            .setBeepEnabled(false)
            .initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Mostrar Snackbar con mensaje de escaneo cancelado en la parte inferior
                Snackbar.make(requireView(), "Escaneo cancelado", Snackbar.LENGTH_SHORT).show()
            } else {
                if (result.contents.isNotEmpty()) {
                    // El contenido del código QR es válido, proceder con la obtención del usuario
                    getUserIdFromDatabase(result.contents)
                    // Mostrar el resultado en un Toast
                    Toast.makeText(requireContext(), result.contents, Toast.LENGTH_SHORT).show()
                } else {
                    // El contenido del código QR no es válido
                    Snackbar.make(requireView(), "Contenido de código QR no válido", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            // Mostrar Snackbar indicando que ocurrió un error al escanear el código QR
            Snackbar.make(requireView(), "Error al escanear el código QR", Snackbar.LENGTH_SHORT).show()
        }
    }



    private fun getUserIdFromDatabase(qrContent: String) {
        // Verificar si el contenido del código QR es un UID válido
        if (isValidUid(qrContent)) {
            val userRef = databaseReference.child("User").child(qrContent)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // El usuario está registrado en la base de datos
                        val uidFromDatabase = dataSnapshot.key
                        if (uidFromDatabase == qrContent) {
                            // El UID del código QR coincide con el UID de la base de datos
                            Snackbar.make(requireView(), "Usuario encontrado", Snackbar.LENGTH_SHORT).show()
                        } else {
                            // El UID del código QR no coincide con el UID de la base de datos
                            Snackbar.make(requireView(), "El UID del código QR no coincide", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        // El usuario no está registrado en la base de datos
                        Snackbar.make(requireView(), "El usuario no está registrado", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos si es necesario
                    Snackbar.make(requireView(), "Error al obtener datos del usuario", Snackbar.LENGTH_SHORT).show()
                }
            })
        } else {
            // El contenido del código QR no es válido
            Snackbar.make(requireView(), "Contenido de código QR no válido", Snackbar.LENGTH_SHORT).show()
        }
    }




    private fun isValidUid(uid: String): Boolean {
        // Puedes implementar aquí la lógica para validar si el UID es válido
        // Por ejemplo, puedes verificar si tiene la longitud adecuada o si sigue un formato específico
        // Aquí tienes un ejemplo simple que verifica si la longitud del UID es mayor que 0
        return uid.isNotEmpty()
    }




    companion object {
        @JvmStatic
        fun newInstance() = Scan_Worker()
    }
}