package com.utfpr.psil.cartrack.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utfpr.psil.cartrack.R

@Composable
fun DeleteConfirmationDialog(
    carName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_car_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_car_dialog_desc, carName),
                fontSize = 16.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.positive_btn_delete_car_dialog_desc))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    stringResource(R.string.negative_btn_delete_car_dialog_desc),
                    color = Color.Gray
                )
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun DeleteConfirmationDialogPreview() {
    DeleteConfirmationDialog(
        carName = "Fiat Toro",
        onConfirm = {},
        onDismiss = {}
    )
}
