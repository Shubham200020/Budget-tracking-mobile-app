package com.example.skmaccount.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skmaccount.data.model.Category
import com.example.skmaccount.data.model.Expense
import com.example.skmaccount.ui.theme.CoralRed
import com.example.skmaccount.ui.theme.EmeraldGreen
import com.example.skmaccount.ui.theme.SurfaceDark
import com.example.skmaccount.ui.theme.CategoryHelper
import com.example.skmaccount.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToAddExpense: () -> Unit
) {
    val totalSpent by viewModel.currentMonthTotalSpent.collectAsState()
    val totalIncome by viewModel.currentMonthTotalIncome.collectAsState()
    val netBalance by viewModel.currentMonthNetBalance.collectAsState()
    val budget by viewModel.overallBudget.collectAsState()
    val categoryBudgets by viewModel.allCategoryBudgets.collectAsState()
    val expenses by viewModel.currentMonthExpenses.collectAsState()
    val categoryBreakdowns by viewModel.categoryBreakdowns.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val currentMonth by viewModel.currentMonthSelected.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("Set Monthly Budget") }
    var dialogInitialAmount by remember { mutableStateOf(10000.0) }
    var onDialogSave by remember { mutableStateOf<(Double) -> Unit>({}) }

    val budgetAmount = budget?.limitAmount ?: 10000.0
    val progress = if (budgetAmount > 0) (totalSpent / budgetAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOverBudget = totalSpent > budgetAmount
    val progressColor = if (isOverBudget) CoralRed else EmeraldGreen

    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val currentMonthText = monthFormatter.format(currentMonth.time)

    if (showBudgetDialog) {
        BudgetEditDialog(
            title = dialogTitle,
            currentBudget = dialogInitialAmount,
            onDismiss = { showBudgetDialog = false },
            onSave = { newAmount -> onDialogSave(newAmount); showBudgetDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("skmBudgex", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddExpense, containerColor = EmeraldGreen) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.Black)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Month")
                    }
                    Text(currentMonthText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month")
                    }
                }
            }

            // Income / Expense / Net cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(label = "Income", amount = totalIncome, color = EmeraldGreen, modifier = Modifier.weight(1f))
                    SummaryCard(label = "Spent", amount = totalSpent, color = CoralRed, modifier = Modifier.weight(1f))
                    SummaryCard(
                        label = "Balance",
                        amount = netBalance,
                        color = if (netBalance >= 0) EmeraldGreen else CoralRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Circular Progress (Budget)
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        drawArc(color = SurfaceDark, startAngle = 135f, sweepAngle = 270f, useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round))
                        if (progress > 0f) {
                            drawArc(color = progressColor, startAngle = 135f, sweepAngle = 270f * progress, useCenter = false,
                                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(CategoryHelper.formatCurrency(totalSpent), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("of ${CategoryHelper.formatCurrency(budgetAmount)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Spacer(Modifier.width(4.dp))
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Budget",
                                modifier = Modifier.size(16.dp).clickable {
                                    dialogTitle = "Set Monthly Budget"
                                    dialogInitialAmount = budgetAmount
                                    onDialogSave = { amt -> viewModel.setOverallBudget(amt) }
                                    showBudgetDialog = true
                                }, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Category Breakdown
            if (categoryBreakdowns.isNotEmpty()) {
                item {
                    Text("Category Breakdown", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fillMaxWidth())
                }
                items(categoryBreakdowns.entries.toList()) { entry ->
                    val category = categories.find { it.id == entry.key }
                    val catSpent = entry.value
                    val catBudget = categoryBudgets.find { it.categoryId == entry.key }?.limitAmount
                    val catProgress = if (catBudget != null && catBudget > 0)
                        (catSpent / catBudget).coerceIn(0.0, 1.0).toFloat()
                    else (catSpent / totalSpent.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()
                    val barColor = if (catBudget != null && catSpent > catBudget) CoralRed else EmeraldGreen

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                dialogTitle = "Set Budget: ${category?.name ?: "Category"}"
                                dialogInitialAmount = catBudget ?: 0.0
                                onDialogSave = { amt -> category?.id?.let { viewModel.setCategoryBudget(it, amt) } }
                                showBudgetDialog = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(barColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryHelper.getIcon(category?.name ?: ""),
                                contentDescription = category?.name,
                                tint = barColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(category?.name ?: "Unknown", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                                Text(
                                    if (catBudget != null) "${CategoryHelper.formatCurrency(catSpent)} / ${CategoryHelper.formatCurrency(catBudget)}"
                                    else CategoryHelper.formatCurrency(catSpent),
                                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(progress = { catProgress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = barColor, trackColor = SurfaceDark)
                        }
                    }
                }
            }

            // Transactions header
            item {
                Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            // Empty State
            if (expenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💸", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("No transactions this month", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap + to add your first expense", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(expenses.take(15), key = { it.id }) { expense ->
                    val category = categories.find { it.id == expense.categoryId }
                    SwipeableTransactionItem(expense = expense, category = category, onDelete = { viewModel.deleteExpense(expense) })
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(expense: Expense, category: Category?, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White,
                    modifier = Modifier.padding(end = 24.dp))
            }
        }
    ) {
        TransactionItem(expense, category)
    }
}

@Composable
fun SummaryCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(CategoryHelper.formatCurrency(kotlin.math.abs(amount)), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun BudgetEditDialog(title: String, currentBudget: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var textValue by remember { mutableStateOf(if (currentBudget > 0) String.format("%.0f", currentBudget) else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = textValue, onValueChange = { textValue = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Amount (₹)") }, singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { textValue.toDoubleOrNull()?.let { if (it >= 0) onSave(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)) {
                Text("Save", color = Color.Black)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = SurfaceDark
    )
}

@Composable
fun TransactionItem(expense: Expense, category: Category?) {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val catColorHex = category?.colorHex ?: "#607D8B"
    val iconColor = try {
        Color(android.graphics.Color.parseColor(catColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryHelper.getIcon(category?.name ?: ""),
                        contentDescription = category?.name,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(category?.name ?: "Unknown", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    if (expense.note.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(expense.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(formatter.format(Date(expense.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            Text(
                text = "${if (expense.isIncome) "+" else "-"}${CategoryHelper.formatCurrency(expense.amount)}",
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = if (expense.isIncome) EmeraldGreen else CoralRed
            )
        }
    }
}
