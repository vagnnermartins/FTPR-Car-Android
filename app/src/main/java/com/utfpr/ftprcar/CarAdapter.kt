package com.utfpr.ftprcar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utfpr.ftprcar.model.Car
import com.squareup.picasso.Picasso

class CarAdapter(
    private val context: Context,
    private var cars: List<Car>,
    private val onClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView   = view.findViewById(R.id.image)
        val model: TextView    = view.findViewById(R.id.model)
        val year: TextView     = view.findViewById(R.id.year)
        val licence: TextView  = view.findViewById(R.id.license)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.model.text   = car.name
        holder.year.text    = car.year
        holder.licence.text = car.licence

        if (car.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(car.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.image)
        }

        holder.itemView.setOnClickListener { onClick(car) }
    }

    override fun getItemCount() = cars.size

    fun updateCars(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
