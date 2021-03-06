package com.clonetech.pokemonapidisplay

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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
    private var pokemon by Delegates.observable(JSONObject()) { _, _, newValue ->
        updatePokemonInfo(newValue)
    }

    private lateinit var container : RelativeLayout
    private lateinit var searchField : EditText

    private lateinit var nameTextView : TextView
    private lateinit var idTextView : TextView
    private lateinit var typeOneTextView: TextView
    private lateinit var typeTwoTextView: TextView

    private lateinit var spriteView : ImageView
    private lateinit var detailsContainer : LinearLayout
    private lateinit var pager2: ViewPager2
    private lateinit var tabLayout : TabLayout

    private lateinit var nextButton : Button
    private lateinit var prevButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeAPICall(id.toString())
        container = findViewById(R.id.main_container)
        searchField = findViewById(R.id.search)

        nameTextView = findViewById(R.id.name)
        idTextView = findViewById(R.id.id)
        typeOneTextView = findViewById(R.id.type_one)
        typeTwoTextView = findViewById(R.id.type_two)

        spriteView = findViewById(R.id.sprite_image)
        detailsContainer = findViewById(R.id.details_container)
        pager2 = findViewById(R.id.pager)
        tabLayout = findViewById(R.id.tab_layout)

        nextButton = findViewById(R.id.next)
        prevButton = findViewById(R.id.previous)

        pager2.adapter = PagerAdapter(this, pokemon)

        val tabTitles = arrayListOf("About", "Base Stats", "Evolution", "Moves")
        TabLayoutMediator(tabLayout, pager2) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

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
        val sprite = "https://img.pokemondb.net/artwork/large/${pokemon.get("name")}.jpg"
        val img = BitmapFactory.decodeStream(URL(sprite).content as InputStream?)

        val jsonTypes = pokemon.getJSONArray("types")
        var types = arrayListOf<String>()
        for (i in 0 until jsonTypes.length()) {
            types.add(jsonTypes.getJSONObject(i).getJSONObject("type").getString("name"))
        }

        this@MainActivity.runOnUiThread {
            nameTextView.text = pokemon.getString("name").replaceFirstChar(Char::titlecase)
            idTextView.text = getString(R.string.id_full_string, id.toString())
            spriteView.setImageBitmap(img)
            typeOneTextView.text = types[0].replaceFirstChar(Char::titlecase).trim()
            if (types.size > 1) {
                typeTwoTextView.visibility = View.VISIBLE
                typeTwoTextView.text = types[1].replaceFirstChar(Char::titlecase).trim()
            } else {
                typeTwoTextView.visibility = View.INVISIBLE
            }

            pager2.adapter = PagerAdapter(this, pokemon)
            setContainerBackground(types[0])
        }
    }

    fun setContainerBackground(type : String) {
        var colour : Int?
        when (type) {
            "grass", "poison", "bug" -> colour = ContextCompat.getColor(this@MainActivity, R.color.green)
            "fire" -> colour = ContextCompat.getColor(this@MainActivity, R.color.red)
            "water" -> colour = ContextCompat.getColor(this@MainActivity, R.color.blue)
            else -> colour = ContextCompat.getColor(this@MainActivity, R.color.white)
        }
        val colourArray = intArrayOf(getColor(R.color.white), colour)
        val gradient = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colourArray)
        pager2.background = gradient
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

    class PagerAdapter(fragmentActivity: FragmentActivity, pokemonJson: JSONObject) :
        FragmentStateAdapter(fragmentActivity) {
        var pokemon = pokemonJson.toString()
        var pokemonName = getName(pokemonJson)

        private fun getName(pokemonJson: JSONObject): String {
            return if (pokemonJson.has("name")) pokemonJson.getString("name") else ""
        }

        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AboutFragment.newInstance(pokemon)
                1 -> BaseStatsFragment.newInstance(pokemon)
                2 -> EvolutionFragment.newInstance(pokemonName)
                3 -> AboutFragment.newInstance(pokemon)
                else -> AboutFragment.newInstance(pokemon)

            }
        }

    }
}
