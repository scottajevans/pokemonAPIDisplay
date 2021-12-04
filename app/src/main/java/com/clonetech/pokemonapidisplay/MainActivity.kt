package com.clonetech.pokemonapidisplay

import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.IOException
import org.json.JSONObject
import java.io.InputStream
import java.net.URL
import kotlin.properties.Delegates

private val client = OkHttpClient()
private var id : Int = 1

class MainActivity : AppCompatActivity() {
    private lateinit var searchField : EditText

    private lateinit var nameTextView : TextView
    private lateinit var idTextView : TextView
    private lateinit var heightTextView : TextView
    private lateinit var weightTextView : TextView

    private lateinit var spriteView : ImageView

    private lateinit var nextButton : Button
    private lateinit var prevButton : Button

    private var pokemon by Delegates.observable(JSONObject()) { _, _, newValue ->
        updatePokemonInfo(newValue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeAPICall(id.toString())
        searchField = findViewById(R.id.search)

        nameTextView = findViewById(R.id.name)
        idTextView = findViewById(R.id.id)
        heightTextView = findViewById(R.id.height)
        weightTextView = findViewById(R.id.weight)

        spriteView = findViewById(R.id.sprite_image)

        nextButton = findViewById(R.id.next)
        prevButton = findViewById(R.id.previous)

        searchField.setOnEditorActionListener { textView, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                makeAPICall(textView.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        nextButton.setOnClickListener {
            id ++
            makeAPICall(id.toString())
        }

        prevButton.setOnClickListener {
            if (id > 1) id --
            makeAPICall(id.toString())
        }
    }

    private fun updatePokemonInfo(pokemon: JSONObject) {
        val sprite = JSONObject(pokemon.get("sprites").toString()).get("front_default").toString()
        val img = BitmapFactory.decodeStream(URL(sprite).content as InputStream?)

        this@MainActivity.runOnUiThread {
            nameTextView.text = pokemon.get("name").toString().replaceFirstChar(Char::titlecase)
            idTextView.text = id.toString()
            heightTextView.text = "${pokemon.get("height").toString()} ${getString(R.string.decimetres)}"
            weightTextView.text = "${pokemon.get("weight").toString()} ${getString(R.string.hectograms)}"
            spriteView.setImageBitmap(img)
        }
    }

    fun makeAPICall(searchValue: String) {
        val request = Request.Builder()
            .url("https://pokeapi.co/api/v2/pokemon/${searchValue.lowercase()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        MainScope().launch {
                            Toast.makeText(this@MainActivity, getString(R.string.not_found), Toast.LENGTH_LONG).show()
                        }
                        throw IOException("Unexpected code $response")
                    }

                    val jObject = JSONObject(response.body!!.string())
                    pokemon = jObject
                    id = (pokemon.get("id") as Int)

                    MainScope().launch {
                        if (searchField.hasFocus()) {
                            searchField.clearFocus()
                            val `in`: InputMethodManager =
                                this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            `in`.hideSoftInputFromWindow(searchField.windowToken, 0)
                            searchField.text = null
                        }
                    }
                }
            }
        })
    }
}