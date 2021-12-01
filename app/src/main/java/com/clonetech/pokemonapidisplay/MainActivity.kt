package com.clonetech.pokemonapidisplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import okhttp3.*
import okio.IOException
import org.json.JSONObject
import kotlin.properties.Delegates

private val client = OkHttpClient()
private var id : Int = 1

class MainActivity : AppCompatActivity() {
    private lateinit var nameTextView : TextView
    private lateinit var idTextView : TextView
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button

    private var pokemon by Delegates.observable(JSONObject()) { property, oldValue, newValue ->
        updatePokemonInfo(newValue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeAPICall(id)
        nameTextView = findViewById(R.id.name)
        idTextView = findViewById(R.id.id)
        nextButton = findViewById(R.id.next)
        prevButton = findViewById(R.id.previous)

        nextButton.setOnClickListener {
            id ++
            makeAPICall(id)
        }

        prevButton.setOnClickListener {
            if (id > 1) id --
            makeAPICall(id)
        }
    }

    private fun updatePokemonInfo(pokemon: JSONObject) {
        this@MainActivity.runOnUiThread {
            nameTextView.text = pokemon.get("name").toString()
            idTextView.text = id.toString()
        }
    }

    private fun makeAPICall(id : Int) {
        val request = Request.Builder()
            .url("https://pokeapi.co/api/v2/pokemon/$id")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val jObject = JSONObject(response.body!!.string())
                    pokemon = jObject
                }
            }
        })
    }
}