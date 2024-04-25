package com.example.flypack.worker.fragments_worker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.flypack.R
import com.example.flypack.databinding.ContainerFragmentsBinding



class Container_Fragments : AppCompatActivity() {

    // Definir una propiedad lateinit para vincular la vista de la actividad
    //private lateinit var binding: ContainerFragmentsBinding
    lateinit var binding: ContainerFragmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar el diseño de la actividad y asignar a la propiedad binding
        binding = ContainerFragmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reemplazar el fragmento inicial por el fragmento Home
        replaceFragment(Info_Worker())

        // Configurar un oyente para el BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                // Reemplazar el fragmento con Home cuando se seleccione el ícono de Android
                R.id.info -> replaceFragment(Info_Worker())
                // Reemplazar el fragmento con Anchor cuando se seleccione el ícono de Launcher
                R.id.scan -> replaceFragment(Scan_Worker())
                // Reemplazar el fragmento con App cuando se seleccione el ícono de App
                R.id.profile -> replaceFragment(Profile_Worker())
                // No hacer nada en caso de que se seleccione otro ícono
                else -> {
                }
            }
            true // Devolver true para indicar que se ha manejado la selección del elemento
        }
    }

    // Método para reemplazar el fragmento en el contenedor
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        // Reemplazar el fragmento actual con el nuevo fragmento
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        // Confirmar la transacción
        fragmentTransaction.commit()
    }
}