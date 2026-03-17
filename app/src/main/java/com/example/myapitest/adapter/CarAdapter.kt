package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myapitest.R
import com.example.myapitest.model.Car

class CarAdapter : ListAdapter<Car, CarAdapter.CarViewHolder>(CarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_layout, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.image)
        private val tvModel: TextView = itemView.findViewById(R.id.model)
        private val tvYear: TextView = itemView.findViewById(R.id.year)
        private val tvLicense: TextView = itemView.findViewById(R.id.license)

        fun bind(car: Car) {
            tvModel.text = car.name
            tvYear.text = car.year
            tvLicense.text = car.licence
            
            // Load image using Coil
            image.load(car.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery) // Placeholder res
                error(android.R.drawable.ic_menu_report_image) // Fallback on missing image
                transformations(coil.transform.CircleCropTransformation())
            }
        }
    }

    class CarDiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem == newItem
        }
    }
}
