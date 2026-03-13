package com.example.myapitest.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.MapsActivity
import com.example.myapitest.databinding.ItemCarLayoutBinding
import com.example.myapitest.model.Car
import com.squareup.picasso.Picasso

class CarAdapter(
    private val cars: List<Car>
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(val binding: ItemCarLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {

        val binding = ItemCarLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        holder.binding.model.text = car.name
        holder.binding.year.text = car.year
        holder.binding.license.text = car.licence

        Picasso.get()
            .load(car.imageUrl)
            .into(holder.binding.image)

        // Clique no item → abre mapa mostrando a Place
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MapsActivity::class.java)
            intent.putExtra("lat", car.place.lat)
            intent.putExtra("long", car.place.long)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return cars.size
    }
}