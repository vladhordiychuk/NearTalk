package com.neartalk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.neartalk.ui.theme.Primary

@Composable
fun LoginScreen(onBack: () -> Unit) {
    Scaffold(

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Username:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Enter username") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text("Password:", style = MaterialTheme.typography.bodyLarge)
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Enter password")},
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Button(
                        onClick = { /* login action */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}