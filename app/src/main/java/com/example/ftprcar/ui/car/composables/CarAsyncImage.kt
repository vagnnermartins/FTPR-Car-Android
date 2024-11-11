package com.example.ftprcar.ui.car.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

@Composable
fun CarAsyncImage(modifier: Modifier, imageUrl: String, contentScale: ContentScale) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Car image",
        modifier = modifier,
        contentScale = contentScale,
        placeholder = rememberAsyncImagePainter("https://www.shutterstock.com/image-vector/car-logo-icon-emblem-design-600nw-473088025.jpg"),
        error = rememberAsyncImagePainter("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSvza16HOKN0P2XFV96GGa4mrOUjPL7DRy6SJNaZ7Mtre4t2GhxskiNtajQLm8rTlVk2xc&usqp=CAU")
    )
}