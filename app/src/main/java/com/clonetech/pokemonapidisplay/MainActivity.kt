package com.clonetech.pokemonapidisplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import okhttp3.*
import okio.IOException
import org.json.JSONObject

private val client = OkHttpClient()
private var pokemon : String? = null
private var id : Int = 1

private var nameTextView : TextView? = null


class MainActivity : AppCompatActivity() {

    lateinit var nextButton: Button
    lateinit var prevButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeAPICall(id)
        nameTextView = findViewById<TextView>(R.id.name)
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
}

private fun updateName (name : String) {
    nameTextView!!.text = name
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

                var jObject: JSONObject = JSONObject(response.body!!.string())
                pokemon = jObject.get("name").toString()
                updateName(pokemon!!)
            }
        }
    })
}