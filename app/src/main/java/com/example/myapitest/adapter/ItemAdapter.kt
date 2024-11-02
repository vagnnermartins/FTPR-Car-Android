package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.model.Item
import com.example.myapitest.ui.loadUrl

class ItemAdapter(
    private val itens: List<Item>)
    // private val itemClickListener: (Item) -> Unit,
        : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {


    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val modelTextView: TextView = view.findViewById(R.id.model)
        val yearTextView: TextView = view.findViewById(R.id.year)
        val licenseTextView: TextView = view.findViewById(R.id.license)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = itens.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itens[position]
       // holder.itemView.setOnClickListener {
        //    itemClickListener.invoke(item)
        //}
        holder.modelTextView.text = item.name
        holder.yearTextView.text = item.year
        holder.licenseTextView.text = item.licence

        holder.imageView.loadUrl(item.imageUrl)
    }
}