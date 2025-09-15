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
    private val items: List<Item>,
    private val itemClickListener: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemAdapter.ItemViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.year.text = item.year
        holder.license.text = item.licence

        holder.imageView.loadUrl(item.imageUrl)
        holder.itemView.setOnClickListener {
            itemClickListener(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val name: TextView = view.findViewById(R.id.model)
        val year: TextView = view.findViewById(R.id.year)
        val license: TextView = view.findViewById(R.id.license)

    }
}