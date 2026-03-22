package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.databinding.ItemCarBinding
import com.example.myapitest.model.Car
import com.bumptech.glide.Glide

class CarAdapter(
    private var cars: List<Car>,
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(cars[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.binding.tvName.text = car.name
        holder.binding.tvYear.text = car.year
        holder.binding.tvLicence.text = car.licence

        Glide.with(holder.itemView)
            .load(car.imageUrl)
            .into(holder.binding.ivCar)
    }

    override fun getItemCount() = cars.size

    fun setCars(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}