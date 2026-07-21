package com.freewdcmkt.bck.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun TitleText(text: String){
    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
}
@Composable
fun ContentText(text: String){
    Text(text = text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
}
@Composable
fun UsernameText(text: String){
    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
}