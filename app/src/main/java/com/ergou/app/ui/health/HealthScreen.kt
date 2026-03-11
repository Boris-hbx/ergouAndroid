package com.ergou.app.ui.health

import androidx.annotation.OptIn as AndroidOptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    onBack: () -> Unit,
    viewModel: HealthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory = uiState.categories.find { it.id == uiState.selectedCategoryId }
    val selectedIndex = uiState.categories.indexOfFirst { it.id == uiState.selectedCategoryId }.coerceAtLeast(0)
    val filteredItems = uiState.filteredItems
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("养生功法") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setSearchActive(!uiState.isSearchActive) }) {
                        Icon(
                            if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (uiState.isSearchActive) "关闭搜索" else "搜索"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Search bar
                AnimatedVisibility(visible = uiState.isSearchActive) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("搜索功法、功效、穴位...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        }
                    )
                }

                // Practice stats bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val totalItems = uiState.categories.sumOf { it.items.size }
                    val streakColor = if (uiState.streakDays > 7)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\uD83D\uDD25 连续 ${uiState.streakDays} 天",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = streakColor
                        )
                        Text(
                            text = "今日 ${uiState.todayPracticed.size}/$totalItems",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "累计 ${uiState.totalPracticeCount} 次",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tabs
                if (uiState.categories.isNotEmpty()) {
                    ScrollableTabRow(selectedTabIndex = selectedIndex) {
                        uiState.categories.forEachIndexed { index, category ->
                            Tab(
                                selected = index == selectedIndex,
                                onClick = { viewModel.selectCategory(category.id) },
                                text = { Text(category.name) }
                            )
                        }
                    }
                }

                // Favorites filter chip (not for meridians tab)
                if (uiState.selectedCategoryId != "meridians") {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.showFavoritesOnly,
                            onClick = { viewModel.toggleShowFavoritesOnly() },
                            label = { Text("常练") },
                            leadingIcon = {
                                Icon(
                                    if (uiState.showFavoritesOnly) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                selectedCategory?.let { category ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Meridian diagram at top of every tab
                        item(key = "meridian_browser") {
                            MeridianBrowser(
                                uiState = uiState,
                                onToggleViewSide = { viewModel.toggleMeridianViewSide() },
                                onToggleMeridian = { viewModel.toggleMeridianSelected(it) },
                                onSelectAll = { viewModel.selectAllMeridians() },
                                onDeselectAll = { viewModel.deselectAllMeridians() },
                                onAcupointTap = { viewModel.selectAcupoint(it) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Category intro card
                        item(key = "intro_${category.id}") {
                            CategoryIntroCard(category)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (filteredItems.isEmpty()) {
                            item(key = "empty") {
                                Text(
                                    if (uiState.showFavoritesOnly) "还没有收藏的项目，点击星标添加收藏"
                                    else "没有匹配的结果",
                                    modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        items(filteredItems, key = { it.id }) { item ->
                            HealthItemCard(
                                item = item,
                                isExpanded = item.id in uiState.expandedItems,
                                isFavorite = item.id in uiState.favorites,
                                isPracticed = item.id in uiState.todayPracticed,
                                isVideoPlaying = uiState.playingVideoItemId == item.id,
                                isVideoBuffering = uiState.playingVideoItemId == item.id && uiState.isVideoBuffering,
                                exoPlayer = viewModel.exoPlayer,
                                searchQuery = uiState.searchQuery,
                                onToggleExpand = { viewModel.toggleExpanded(item.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                                onTogglePractice = { viewModel.togglePractice(item.id) },
                                onToggleVideo = { viewModel.toggleVideo(item.id, item.videoUrl) }
                            )
                        }

                        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun MeridianBrowser(
    uiState: HealthUiState,
    onToggleViewSide: () -> Unit,
    onToggleMeridian: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onAcupointTap: (Acupoint?) -> Unit
) {
    val activeMeridians = remember(uiState.selectedMeridianIds) {
        MeridianData.meridians.filter { it.id in uiState.selectedMeridianIds }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Front/Back toggle + select all/none
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = uiState.selectedMeridianIds.size == MeridianData.meridians.size,
                    onClick = {
                        if (uiState.selectedMeridianIds.size == MeridianData.meridians.size)
                            onDeselectAll()
                        else
                            onSelectAll()
                    },
                    label = { Text("全部") }
                )
            }
            TextButton(onClick = onToggleViewSide) {
                Text(if (uiState.meridianViewSide == "front") "切换背面" else "切换正面")
            }
        }

        // Horizontal scrollable meridian chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MeridianData.meridians.forEach { meridian ->
                val isSelected = meridian.id in uiState.selectedMeridianIds
                val chipColor = androidx.compose.ui.graphics.Color(meridian.color)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleMeridian(meridian.id) },
                    label = { Text(meridian.shortName, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                        )
                    }
                )
            }
        }

        // Meridian Canvas (3:4 aspect ratio like the web version)
        MeridianDiagram(
            activeMeridians = activeMeridians,
            viewSide = uiState.meridianViewSide,
            selectedAcupoint = uiState.selectedAcupoint,
            onAcupointTap = onAcupointTap,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .padding(horizontal = 8.dp)
        )

        // Selected acupoint detail panel
        uiState.selectedAcupoint?.let { acupoint ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "${acupoint.name} (${acupoint.id})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (acupoint.pinyin.isNotBlank()) {
                        Text(
                            acupoint.pinyin,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (acupoint.functions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "功效",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        acupoint.functions.forEach { func ->
                            Text(
                                "\u00B7 $func",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (acupoint.indication.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "主治",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            acupoint.indication,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryIntroCard(category: HealthCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                category.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                category.intro,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HealthItemCard(
    item: HealthItem,
    isExpanded: Boolean,
    isFavorite: Boolean,
    isPracticed: Boolean,
    isVideoPlaying: Boolean,
    isVideoBuffering: Boolean = false,
    exoPlayer: androidx.media3.exoplayer.ExoPlayer? = null,
    searchQuery: String,
    onToggleExpand: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePractice: () -> Unit,
    onToggleVideo: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: name + favorite + expand icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    HighlightedText(
                        text = item.name,
                        query = searchQuery,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onTogglePractice,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        if (isPracticed) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = if (isPracticed) "取消打卡" else "打卡",
                        tint = if (isPracticed) Color(0xFF43A047)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "取消收藏" else "收藏",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expandable detail section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Benefits — bullet list if available, else flat string
                    Text(
                        "功效",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.benefitsList.isNotEmpty()) {
                        item.benefitsList.forEach { benefit ->
                            Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp)) {
                                Text(
                                    "\u00B7 ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HighlightedText(
                                    text = benefit,
                                    query = searchQuery,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    } else {
                        HighlightedText(
                            text = item.benefits,
                            query = searchQuery,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Meridian details — chips with intensity + notes
                    if (item.meridianDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "经络刺激",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        item.meridianDetails.forEach { detail ->
                            Row(
                                modifier = Modifier.padding(start = 4.dp, bottom = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (detail.intensity == "primary")
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outline
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    detail.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (detail.intensity == "primary") FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                if (detail.note.isNotBlank()) {
                                    Text(
                                        " — ${detail.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (item.meridians.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "经络",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            item.meridians,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Key acupoints
                    if (item.keyPoints.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "穴位",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HighlightedText(
                            text = item.keyPoints,
                            query = searchQuery,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Insights (站桩 specific)
                    if (item.insights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "要领",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        item.insights.forEach { insight ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        insight.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        insight.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Video player
                    if (item.videoUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = onToggleVideo) {
                            Icon(
                                Icons.Default.PlayCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isVideoPlaying) "收起视频" else "观看演示视频")
                        }

                        AnimatedVisibility(
                            visible = isVideoPlaying,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                if (exoPlayer != null) {
                                    InlineVideoPlayer(
                                        exoPlayer = exoPlayer,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (isVideoBuffering) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@AndroidOptIn(UnstableApi::class)
@Composable
fun InlineVideoPlayer(
    exoPlayer: androidx.media3.exoplayer.ExoPlayer,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        update = { playerView ->
            playerView.player = exoPlayer
        },
        modifier = modifier
    )
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    if (query.isBlank()) {
        Text(text, style = style, color = color)
        return
    }

    val highlightColor = MaterialTheme.colorScheme.tertiary
    val annotatedString = remember(text, query) {
        buildAnnotatedString {
            val lowerText = text.lowercase()
            val lowerQuery = query.lowercase()
            var start = 0
            while (start < text.length) {
                val index = lowerText.indexOf(lowerQuery, start)
                if (index == -1) {
                    append(text.substring(start))
                    break
                }
                if (index > start) {
                    append(text.substring(start, index))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)) {
                    append(text.substring(index, index + query.length))
                }
                start = index + query.length
            }
        }
    }
    Text(annotatedString, style = style, color = color)
}
