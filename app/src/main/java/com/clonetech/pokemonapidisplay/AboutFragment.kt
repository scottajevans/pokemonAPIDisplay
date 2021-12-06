package com.clonetech.pokemonapidisplay

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONObject

private const val POKEMON_ARG = "pokemon"

class AboutFragment : Fragment() {
    private var pokemon: JSONObject? = null

    private lateinit var species : TextView
    private lateinit var height : TextView
    private lateinit var weight : TextView

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
        val root = inflater.inflate(R.layout.fragment_about, container, false)

        species = root.findViewById(R.id.species)
        height = root.findViewById(R.id.height)
        weight = root.findViewById(R.id.weight)

        if (pokemon.toString() != "{}")
        {
            species.text = JSONObject(pokemon!!.getString("species")).getString("name")
            height.text = pokemon!!.getString("height")
            weight.text = pokemon!!.getString("weight")
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(pokemonString: String) =
            AboutFragment().apply {
                arguments = Bundle().apply {
                    putString(POKEMON_ARG, pokemonString)
                }
            }
    }
}