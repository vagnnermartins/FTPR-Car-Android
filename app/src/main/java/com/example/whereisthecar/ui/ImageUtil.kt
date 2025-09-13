package com.example.whereisthecar.ui

import android.widget.ImageView
import com.example.whereisthecar.R
import com.squareup.picasso.Picasso

fun ImageView.loadUrl(imageUrl: String) {
    Picasso.get()
        .load(imageUrl)
        .placeholder(R.drawable.ic_downloading)
        .error(R.drawable.ic_error)
        .transform(CircleTransform())
        .into(this)
}