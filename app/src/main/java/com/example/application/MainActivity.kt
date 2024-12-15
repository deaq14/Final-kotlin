package com.example.arquitectura

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.arquitectura.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout con View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el toolbar
        setSupportActionBar(binding.toolbar)

        // Botón para enviar correo
        binding.Correo.setOnClickListener {
            enviarCorreo()
        }

        // Botón para iniciar pago PSE
        binding.PSE.setOnClickListener {
            iniciarPagoPSE()
        }

        // Botón para enviar SMS
        binding.SMS.setOnClickListener {
            enviarSMS("1234567890", "Hola, este es un mensaje desde la app.")
        }

        // Configuración de los botones para los fragmentos
        binding.POT.setOnClickListener {
            abrirSubpagina(
                "Plan de Ordenamiento Territorial (POT)",
                "En Colombia, un Plan de Ordenamiento Territorial (POT) es un instrumento de planificación y gestión territorial que establece directrices para el desarrollo urbano y rural. Su objetivo es ordenar el uso del suelo, definir las áreas de expansión urbana, conservar recursos naturales, y mejorar la calidad de vida de los habitantes. El POT es elaborado por las autoridades municipales y debe ser aprobado por los concejos municipales, y se revisa cada doce años para adaptarse a las nuevas necesidades y circunstancias."
            )
        }

        binding.PlanesParciales.setOnClickListener {
            abrirSubpagina(
                "Plan Parcial",
                "Un Plan Parcial es una herramienta de planificación de menor escala que se utiliza dentro del marco del POT. Se enfoca en zonas específicas del municipio para detallar y viabilizar proyectos de desarrollo urbano, como nuevos barrios, centros comerciales, o parques industriales. El Plan Parcial define aspectos como el uso del suelo, la infraestructura vial, las áreas verdes, y los equipamientos urbanos, asegurando que el desarrollo cumpla con los lineamientos establecidos en el POT y responda a las necesidades específicas de la comunidad local."
            )
        }

        binding.Obras.setOnClickListener {
            abrirSubpagina(
                "Licencias de Construcción",
                "Las licencias de construcción son permisos que las autoridades municipales otorgan a los propietarios o desarrolladores para construir, modificar, o demoler edificaciones. Estas licencias aseguran que las obras cumplan con las normativas urbanísticas y de seguridad establecidas en el POT y otras regulaciones locales. El proceso para obtener una licencia de construcción incluye la presentación de planos y documentos técnicos, así como la aprobación de diversas entidades para garantizar que el proyecto es viable desde el punto de vista ambiental, técnico, y urbano."
            )
        }
    }

    private fun abrirSubpagina(titulo: String, descripcion: String) {
        val intent = Intent(this, SubpaginaActivity::class.java).apply {
            putExtra("TITULO", titulo)
            putExtra("DESCRIPCION", descripcion)
        }
        startActivity(intent)
    }

    private fun enviarCorreo() {
        val destinatario = arrayOf("example@dominio.com")
        val asunto = "Consulta desde la app"
        val mensaje = "Hola, escribo porque tengo la siguiente consulta:"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, destinatario)
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, "No hay aplicaciones de correo instaladas", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun enviarSMS(numero: String, mensaje: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$numero")
                putExtra("sms_body", mensaje)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Snackbar.make(binding.root, "No hay aplicaciones de mensajería instaladas.", Snackbar.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error al intentar enviar SMS: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun iniciarPagoPSE() {
        val url = "https://sandbox.wompi.co/v1/transactions"
        val client = OkHttpClient()

        val jsonBody = JSONObject()
        jsonBody.put("amount_in_cents", 100000)
        jsonBody.put("currency", "COP")
        jsonBody.put("customer_email", "example@correo.com")
        jsonBody.put("payment_method", JSONObject().apply {
            put("type", "PSE")
            put("user_type", 0)
            put("user_legal_id", "123456789")
            put("user_legal_id_type", "CC")
            put("financial_institution_code", "1040")
        })
        jsonBody.put("redirect_url", "https://tu-app.com/retorno")

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), jsonBody.toString()))
            .addHeader("Authorization", "Bearer YOUR_PUBLIC_API_KEY")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Snackbar.make(binding.root, "Error al iniciar pago con PSE", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                val jsonResponse = JSONObject(responseData ?: "")
                val transactionData = jsonResponse.optJSONObject("data")
                val paymentLink = transactionData?.optString("payment_link")

                if (paymentLink != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink))
                    startActivity(intent)
                } else {
                    runOnUiThread {
                        Snackbar.make(binding.root, "No se pudo generar el enlace de pago", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
