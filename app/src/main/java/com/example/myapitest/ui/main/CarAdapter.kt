package com.example.myapitest.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.data.model.Car
import com.squareup.picasso.Picasso

// RZ - O CarAdapter é o "maestro" da lista. Ele pega a lista de carros (dados) 
// e diz ao RecyclerView como criar e preencher cada linha (item_car_layout).

class CarAdapter(
    private var carList: List<Car>,
    private val onCarClick: (Car) -> Unit // RZ - Callback de clique para abrir o mapa
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCar: ImageView = view.findViewById(R.id.image)
        val tvName: TextView = view.findViewById(R.id.model)
        val tvYear: TextView = view.findViewById(R.id.year)
        val tvLicense: TextView = view.findViewById(R.id.license)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_layout, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        
        holder.tvName.text = car.name
        holder.tvYear.text = "Ano: ${car.year}"
        holder.tvLicense.text = "Placa: ${car.licence}"

        // RZ - Usa a imagem 'fotopadrao' (WebP) como padrão no carregamento e erros
        if (!car.imageUrl.isNullOrEmpty()) {
            // RZ - Picasso carregando a imagem da API
            Picasso.get()
                .load(car.imageUrl)
                .placeholder(R.drawable.fotopadrao) // Foto padrão enquanto baixa
                .error(R.drawable.fotopadrao)       // Foto padrão se o link falhar
                .into(holder.imgCar)
        } else {
            holder.imgCar.setImageResource(R.drawable.fotopadrao)
        }

        holder.itemView.setOnClickListener {
            onCarClick(car)
        }
    }

    override fun getItemCount(): Int = carList.size

    fun updateData(newList: List<Car>) {
        carList = newList
        notifyDataSetChanged()
    }
}
