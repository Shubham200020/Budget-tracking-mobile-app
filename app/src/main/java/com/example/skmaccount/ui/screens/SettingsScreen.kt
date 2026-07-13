package com.example.skmaccount.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skmaccount.ui.viewmodel.FinanceViewModel
import com.example.skmaccount.ui.theme.EmeraldGreen
import com.example.skmaccount.ui.theme.SurfaceDark
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    isAppLockEnabled: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
    onRequireAuthentication: (onSuccess: () -> Unit) -> Unit
) {
    var showCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Lock Setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = "App Lock", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("App Lock (PIN/Biometric)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Switch(
                        checked = isAppLockEnabled,
                        onCheckedChange = { isChecking ->
                            if (isChecking) {
                                onRequireAuthentication { onAppLockToggle(true) }
                            } else {
                                onRequireAuthentication { onAppLockToggle(false) }
                            }
                        }
                    )
                }
            }

            // Add Category Setting
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Category, contentDescription = "Add Category", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Add Custom Category", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }

    if (showCategoryDialog) {
        var categoryName by remember { mutableStateOf("") }
        // For simplicity in Phase 3, we default the icon and color to generic ones,
        // but let user pick a name. 
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            viewModel.addCategory(
                                name = categoryName.trim(),
                                iconName = "Category", // generic placeholder
                                colorHex = "#00E676"   // generic placeholder
                            )
                            showCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
