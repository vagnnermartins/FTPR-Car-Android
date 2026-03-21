package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.databinding.ItemCarLayoutBinding
import com.example.myapitest.model.Car
import com.squareup.picasso.Picasso

class CarAdapter(
    private var cars: List<Car>,
    private val onCarClick: (Car) -> Unit,
    private val onEditClick: (Car) -> Unit
) :
    RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(val binding: ItemCarLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.binding.model.text = car.name
        holder.binding.year.text = car.year
        holder.binding.license.text = car.licence

        if (car.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(car.imageUrl)
                .into(holder.binding.image)
        }

        holder.itemView.setOnClickListener { onCarClick(car) }
        holder.binding.btnEdit.setOnClickListener { onEditClick(car) }
    }

    override fun getItemCount() = cars.size

    fun updateCars(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
