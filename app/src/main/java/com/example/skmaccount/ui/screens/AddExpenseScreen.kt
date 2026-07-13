package com.example.skmaccount.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skmaccount.ui.theme.CoralRed
import com.example.skmaccount.ui.theme.EmeraldGreen
import com.example.skmaccount.ui.theme.SurfaceDark
import com.example.skmaccount.ui.theme.CategoryHelper
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.skmaccount.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.allCategories.collectAsState()
    var amount by remember { mutableStateOf("0") }
    var noteText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var isIncome by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val accentColor = if (isIncome) EmeraldGreen else CoralRed

    // Detect keyboard visibility to hide custom numpad
    val isKeyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isIncome) "Add Income" else "Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scrollable upper content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Income / Expense Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(false to "Expense", true to "Income").forEach { (income, label) ->
                        val selected = isIncome == income
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) (if (income) EmeraldGreen else CoralRed) else Color.Transparent)
                                .clickable { isIncome = income }
                                .padding(horizontal = 32.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Amount display
                Text(
                    text = "₹$amount",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Category Selection
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category.id == selectedCategoryId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) accentColor else SurfaceDark)
                                .clickable { selectedCategoryId = category.id }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = CategoryHelper.getIcon(category.name),
                                    contentDescription = category.name,
                                    tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = category.name,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notes field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g. Zomato - office lunch") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        focusedLabelColor = accentColor
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Custom Numpad (Hidden when software keyboard is visible)
            if (!isKeyboardVisible) {
                Numpad(
                    onNumberClick = { num ->
                        if (num == "." && amount.contains(".")) return@Numpad
                        if (amount == "0" && num != ".") amount = num
                        else if (amount.length < 10) amount += num
                    },
                    onDeleteClick = {
                        if (amount.length > 1) amount = amount.dropLast(1)
                        else amount = "0"
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Save button
            Button(
                onClick = {
                    val finalAmount = amount.toDoubleOrNull() ?: 0.0
                    if (finalAmount > 0 && selectedCategoryId != null) {
                        viewModel.addExpense(
                            amount = finalAmount,
                            categoryId = selectedCategoryId!!,
                            title = noteText.ifEmpty { if (isIncome) "Income" else "Expense" },
                            note = noteText,
                            isIncome = isIncome
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isIncome) "✅ Income added!" else "✅ Expense saved!"
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isIncome) "Save Income" else "Save Expense",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun Numpad(onNumberClick: (String) -> Unit, onDeleteClick: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (key in row) {
                    NumpadKey(key = key, onClick = {
                        if (key == "DEL") onDeleteClick() else onNumberClick(key)
                    })
                }
            }
        }
    }
}

@Composable
fun NumpadKey(key: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(SurfaceDark)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = if (key == "DEL") CoralRed else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
