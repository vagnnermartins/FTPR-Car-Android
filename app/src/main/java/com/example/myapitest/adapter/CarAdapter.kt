package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.model.Car
import com.example.myapitest.R

class CarAdapter(
    private val cars: List<Car>
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarAdapter.CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent)

        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarAdapter.CarViewHolder, position: Int) {
        val car = cars[position]
        holder
    }

    override fun getItemCount(): Int = cars.size

    class CarViewHolder(view : View) : RecyclerView.ViewHolder(view) {

    }

}