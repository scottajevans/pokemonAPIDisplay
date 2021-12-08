package com.clonetech.pokemonapidisplay

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import okhttp3.*
import okio.IOException
import org.json.JSONObject

private const val POKEMON_NAME = "pokemonName"
private val client = OkHttpClient()

class EvolutionFragment : Fragment() {
    private var pokemonName: String? = null

    private lateinit var evolutionList: ListView
    private lateinit var noEvolutionFoundText : TextView
    private lateinit var evolutionArray: ArrayList<JSONObject>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pokemonName = it.getString(POKEMON_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_evolution, container, false)

        evolutionList = root.findViewById(R.id.evolution_list)
        noEvolutionFoundText = root.findViewById(R.id.no_evolution_found)

        getSpeciesData()

        return root
    }

    private fun getSpeciesData() {
        val request = Request.Builder()
            .url("https://pokeapi.co/api/v2/pokemon-species//${pokemonName!!.lowercase()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    val speciesData = JSONObject(response.body!!.string())
                    val evolutionChain = speciesData.getJSONObject("evolution_chain")
                    val evolutionUrl = evolutionChain.getString("url")
                    getEvolutionData(evolutionUrl)
                }
            }
        })
    }

    private fun getEvolutionData(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    val evolutionData = JSONObject(response.body!!.string()).getJSONObject("chain")

                    if (evolutionData.getJSONArray("evolves_to").length() > 0) {
                        evolutionArray = convertEvolutionData(evolutionData)

                        if (evolutionArray.size > 0){
                            val adapter = ListAdapter(requireContext(), evolutionArray)
                            activity!!.runOnUiThread {
                                evolutionList.adapter = adapter
                            }
                        }
                    } else {
                        activity!!.runOnUiThread {
                            noEvolutionFoundText.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }

    private fun getEvolutionInstance(data: JSONObject, currentPokemon: String): JSONObject {
        val evolvesTo = data.getJSONArray("evolves_to")
        val nextPokemon = evolvesTo.getJSONObject(0).getJSONObject("species").getString("name")
        val nextEvolvesTo = evolvesTo.getJSONObject(0).getJSONArray("evolves_to")

        val returnObj = JSONObject()

        returnObj.put("evolvesFrom", currentPokemon)
        returnObj.put("evolvesTo", nextPokemon)
        returnObj.put("evolves_to", nextEvolvesTo)

        if (nextEvolvesTo.length() > 0){
            var nextSpecies = if (nextEvolvesTo.getJSONObject(0).has("species")) nextEvolvesTo.getJSONObject(0).getJSONObject("species") else ""
            returnObj.put("species", nextSpecies)
        }

        return returnObj
    }

    private fun convertEvolutionData(evolutionData: JSONObject): ArrayList<JSONObject> {
        var evolutionList = arrayListOf<JSONObject>()
        var data: JSONObject = evolutionData
        var completedSearch = false
        var pokemonName = evolutionData.getJSONObject("species").getString("name")
        do {
            data = getEvolutionInstance(data, pokemonName)

            pokemonName = data.getString("evolvesTo")

            evolutionList.add(JSONObject(
                """{"evolvesFrom":${data.getString("evolvesFrom").replaceFirstChar(Char::titlecase)}, "evolvesTo":${pokemonName.replaceFirstChar(Char::titlecase)}}"""
            ))

            if (data.getJSONArray("evolves_to").length() == 0) {
                completedSearch = true
            }
        }
        while (!completedSearch)

        return evolutionList
    }

    class ListAdapter(context : Context, private val dataSource: ArrayList<JSONObject>) : BaseAdapter() {
        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource.size
        }

        override fun getItem(position: Int): JSONObject {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {
            val rowView = inflater.inflate(R.layout.evolution_row, parent, false)

            val evolvesFromName = rowView.findViewById<TextView>(R.id.evolves_from_name)
            val evolvesToName = rowView.findViewById<TextView>(R.id.evolves_to_name)

            val evolution = getItem(position)

            evolvesFromName.text = evolution.getString("evolvesFrom")
            evolvesToName.text = evolution.getString("evolvesTo")

            return rowView
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(pokemonName: String) =
            EvolutionFragment().apply {
                arguments = Bundle().apply {
                    putString(POKEMON_NAME, pokemonName)
                }
            }
    }
}