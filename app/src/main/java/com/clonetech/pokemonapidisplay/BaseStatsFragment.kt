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
import org.json.JSONArray
import org.json.JSONObject

private const val POKEMON_ARG = "pokemon"

class BaseStatsFragment : Fragment() {
    private var pokemon: JSONObject? = null

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val pokemonString = it.getString(POKEMON_ARG)
            pokemon = JSONObject(pokemonString!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_base_stats, container, false)

        listView = root.findViewById(R.id.stats_list)

        if (pokemon != null){
            val adapter = ListAdapter(requireContext(), pokemon!!.getJSONArray("stats"))
            listView.adapter = adapter
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(pokemonString: String) =
            BaseStatsFragment().apply {
                arguments = Bundle().apply {
                    putString(POKEMON_ARG, pokemonString)
                }
            }
    }

    class ListAdapter(context : Context, private val dataSource: JSONArray) : BaseAdapter() {
        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource.length()
        }

        override fun getItem(position: Int): JSONObject {
            return dataSource.getJSONObject(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {
            val rowView = inflater.inflate(R.layout.base_stat_row, parent, false)

            val statName = rowView.findViewById<TextView>(R.id.stat_name)
            val statValue = rowView.findViewById<TextView>(R.id.stat_value)

            val poke = getItem(position)

            val nameOriginal = poke.getJSONObject("stat").getString("name")
            val name: String
            when (nameOriginal) {
                "hp" -> name = "HP"
                "special-attack" -> name = "Sp. Atk"
                "special-defense" -> name = "Sp. Def"
                else -> name= nameOriginal.replaceFirstChar(Char::titlecase)
            }
            statName.text = name
            statValue.text = poke.getString("base_stat")

            return rowView
        }

    }
}