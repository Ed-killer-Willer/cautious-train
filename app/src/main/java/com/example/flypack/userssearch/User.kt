    package com.example.flypack.userssearch

    import android.os.Parcel
    import android.os.Parcelable

    // Definición de la clase User
    data class User(
        var nombre: String,  // Nombre del usuario
        var username: String, // Sobrenombre de usuario
        var email: String? = null // Correo electrónico del usuario (opcional)
    ) : Parcelable {
        // Constructor secundario utilizado para la deserialización desde un objeto Parcel
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "", // Lee el nombre desde el Parcel, si es nulo, establece una cadena vacía
            parcel.readString() ?: "", // Lee el nombre de usuario desde el Parcel, si es nulo, establece una cadena vacía
            parcel.readString() // Lee el correo electrónico desde el Parcel
        )

        // Método utilizado para escribir los datos del objeto en un Parcel
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(nombre) // Escribe el nombre en el Parcel
            parcel.writeString(username) // Escribe el nombre de usuario en el Parcel
            parcel.writeString(email) // Escribe el correo electrónico en el Parcel
        }

        // Describe el tipo de contenido del objeto Parcelable
        override fun describeContents(): Int {
            return 0
        }

        // Companion object para la creación del objeto Parcelable
        companion object CREATOR : Parcelable.Creator<User> {
            // Crea un objeto User desde un Parcel
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            // Crea un arreglo de objetos User del tamaño especificado
            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }
