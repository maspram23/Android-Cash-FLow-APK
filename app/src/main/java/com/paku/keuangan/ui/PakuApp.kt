package com.paku.keuangan.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paku.keuangan.data.Category
import com.paku.keuangan.data.Transaction
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.ui.theme.Coral
import com.paku.keuangan.ui.theme.CoralDeep
import com.paku.keuangan.ui.theme.PakuTheme
import kotlinx.coroutines.launch

private const val TAB_HOME = 0
private const val TAB_STATS = 1
private const val TAB_TRANSACTIONS = 2
private const val TAB_SETTINGS = 3

@Composable
fun PakuApp(viewModel: PakuViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSplash by rememberSaveable { mutableStateOf(true) }

    PakuTheme(
        themeMode = state.settings.themeMode,
        languageMode = state.settings.languageMode
    ) {
        Crossfade(
            targetState = showSplash,
            animationSpec = tween(420),
            label = "splashCrossfade"
        ) { splash ->
            if (splash) {
                SplashScreen(onFinished = { showSplash = false })
            } else if (!state.settings.onboarded) {
                OnboardingScreen(
                    onFinish = { name, withDemo ->
                        viewModel.completeOnboarding(name, withDemo)
                    }
                )
            } else {
                MainContent(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MainContent(
    state: PakuUiState,
    viewModel: PakuViewModel
) {
    var currentTab by rememberSaveable { mutableStateOf(TAB_HOME) }
    var formOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val s = LocalStrings.current

    val editingTransaction: Transaction? = remember(editingId, state.transactions) {
        editingId?.let { id -> state.transactions.firstOrNull { it.id == id } }
    }

    fun openForm(transaction: Transaction? = null) {
        editingId = transaction?.id
        formOpen = true
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    fun deleteWithUndo(transaction: Transaction) {
        viewModel.delete(transaction)
        formOpen = false
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = s.transactionDeleted,
                actionLabel = s.undo,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restore(transaction)
            }
        }
    }

    BackHandler(enabled = currentTab != TAB_HOME) {
        currentTab = TAB_HOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (
                    slideInHorizontally(animationSpec = tween(280)) { width ->
                        direction * width / 8
                    } + fadeIn(tween(220))
                ) togetherWith (
                    slideOutHorizontally(animationSpec = tween(280)) { width ->
                        -direction * width / 8
                    } + fadeOut(tween(160))
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            label = "tabContent"
        ) { tab ->
            when (tab) {
                TAB_HOME -> HomeScreen(
                    state = state,
                    onPreviousMonth = { viewModel.shiftMonth(-1) },
                    onNextMonth = { viewModel.shiftMonth(1) },
                    onThisMonth = { viewModel.goToCurrentMonth() },
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onOpenStatistics = {
                        viewModel.clearFocus()
                        currentTab = TAB_STATS
                    },
                    onOpenAllTransactions = {
                        viewModel.clearTransactionFilters()
                        currentTab = TAB_TRANSACTIONS
                    },
                    onEditTransaction = { openForm(it) },
                    onAddTransaction = { openForm() },
                    onOpenSettings = { currentTab = TAB_SETTINGS },
                    modifier = Modifier.fillMaxSize()
                )

                TAB_STATS -> StatsScreen(
                    state = state,
                    onPreviousMonth = { viewModel.shiftMonth(-1) },
                    onNextMonth = { viewModel.shiftMonth(1) },
                    onThisMonth = { viewModel.goToCurrentMonth() },
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onPieTypeChange = { viewModel.setPieType(it) },
                    onBarMetricChange = { viewModel.setBarMetric(it) },
                    onCategoryClick = { category: Category ->
                        viewModel.focusCategory(category, state.selectedMonth)
                        currentTab = TAB_TRANSACTIONS
                    },
                    onOpenTransactions = {
                        viewModel.clearTransactionFilters()
                        currentTab = TAB_TRANSACTIONS
                    },
                    onAddTransaction = { openForm() },
                    modifier = Modifier.fillMaxSize()
                )

                TAB_TRANSACTIONS -> TransactionsScreen(
                    state = state,
                    onSearchChange = { viewModel.setSearch(it) },
                    onTypeFilterChange = { viewModel.setTypeFilter(it) },
                    onClearFocus = { viewModel.clearFocus() },
                    onResetFilters = { viewModel.clearTransactionFilters() },
                    onAddTransaction = { openForm() },
                    onEditTransaction = { openForm(it) },
                    onDeleteTransaction = { deleteWithUndo(it) },
                    modifier = Modifier.fillMaxSize()
                )

                else -> SettingsScreen(
                    state = state,
                    onUpdateName = { viewModel.updateName(it) },
                    onThemeChange = { viewModel.setThemeMode(it) },
                    onLanguageChange = { viewModel.setLanguageMode(it) },
                    onLoadDemo = { viewModel.loadDemoData() },
                    onClearAll = { viewModel.clearAll() },
                    onMessage = { showMessage(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = if (formOpen) 104.dp else 106.dp
                )
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                actionColor = MaterialTheme.colorScheme.inversePrimary
            )
        }

        AnimatedVisibility(
            visible = !formOpen,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomDock(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it },
                onAddClick = { openForm() }
            )
        }

        AnimatedVisibility(
            visible = formOpen,
            enter = slideInVertically(animationSpec = tween(340)) { it } + fadeIn(tween(200)),
            exit = slideOutVertically(animationSpec = tween(300)) { it } + fadeOut(tween(200))
        ) {
            TransactionFormScreen(
                initial = editingTransaction,
                onClose = {
                    formOpen = false
                },
                onSave = { transaction ->
                    val wasEditing = editingTransaction != null
                    viewModel.save(transaction)
                    formOpen = false
                    showMessage(if (wasEditing) s.changesSaved else s.transactionSaved)
                },
                onDelete = { transaction -> deleteWithUndo(transaction) }
            )
        }
    }
}

@Composable
private fun BottomDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val s = LocalStrings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
            ),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(
                    icon = PakuIcons.Home,
                    label = s.tabHome,
                    selected = selectedTab == TAB_HOME,
                    onClick = { onTabSelected(TAB_HOME) },
                    modifier = Modifier.weight(1f)
                )
                DockItem(
                    icon = PakuIcons.Chart,
                    label = s.tabStats,
                    selected = selectedTab == TAB_STATS,
                    onClick = { onTabSelected(TAB_STATS) },
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = CircleShape,
                                spotColor = Coral.copy(alpha = 0.55f)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Coral, CoralDeep))
                            )
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PakuIcons.Add,
                            contentDescription = s.addTransaction,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                DockItem(
                    icon = PakuIcons.Receipt,
                    label = s.tabTransactions,
                    selected = selectedTab == TAB_TRANSACTIONS,
                    onClick = { onTabSelected(TAB_TRANSACTIONS) },
                    modifier = Modifier.weight(1f)
                )
                DockItem(
                    icon = PakuIcons.Settings,
                    label = s.tabSettings,
                    selected = selectedTab == TAB_SETTINGS,
                    onClick = { onTabSelected(TAB_SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1
        )
    }
}
