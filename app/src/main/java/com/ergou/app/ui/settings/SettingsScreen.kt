package com.ergou.app.ui.settings

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ergou.app.R
import com.ergou.app.util.ModelProvider
import com.ergou.app.util.ThemeMode
import org.koin.androidx.compose.koinViewModel

/** 模型选项：展示名 + 型号 */
private data class ModelOption(
    val provider: ModelProvider,
    val displayName: String,
    val modelId: String
)

private val MODEL_OPTIONS = listOf(
    ModelOption(ModelProvider.DEEPSEEK, "DeepSeek", "deepseek-chat"),
    ModelOption(ModelProvider.CLAUDE, "Claude", "opus4.6")
)

private val PRESET_AVATARS = listOf(
    "preset_boris", "preset_doge", "preset_shiba", "preset_cheems",
    "preset_samoyed", "preset_cat", "preset_cutecat", "preset_cartooncat",
    "preset_catpaw", "preset_whitecat", "preset_bunny", "preset_hamster",
    "preset_panda", "preset_pandaman", "preset_pandatea",
    "preset_shibaflower", "preset_shibapair", "preset_shibarest",
    "preset_backview", "preset_tangping", "preset_emoji_ball"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToSoul: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModelSelectorDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showNextLoginDialog by remember { mutableStateOf(false) }
    var showNextLogoutDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // 当前选中模型的展示信息
    val currentModel = MODEL_OPTIONS.first { it.provider == uiState.modelProvider }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 账号 — 头像 + 昵称 + Next 状态
            SectionHeader("账号")
            ProfileItem(
                avatarResName = uiState.avatarResName,
                nickname = uiState.nickname,
                nextLoggedIn = uiState.nextLoggedIn,
                nextUsername = uiState.nextUsername,
                onClickProfile = { showProfileDialog = true },
                onClickNext = {
                    if (uiState.nextLoggedIn) showNextLogoutDialog = true
                    else showNextLoginDialog = true
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 模型
            SectionHeader("模型")
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "模型设置",
                subtitle = "${currentModel.displayName} · ${currentModel.modelId}",
                onClick = { showModelSelectorDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 外观
            SectionHeader("外观")
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "主题",
                subtitle = when (uiState.themeMode) {
                    ThemeMode.SYSTEM -> "跟随系统"
                    ThemeMode.LIGHT -> "亮色"
                    ThemeMode.DARK -> "暗色"
                },
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 数据
            SectionHeader("数据")
            SettingsItem(
                icon = Icons.Default.Info,
                title = "记忆管理",
                subtitle = "${uiState.memoryCount} 条记忆，${uiState.peopleCount} 个人物",
                onClick = onNavigateToMemory
            )
            SettingsItem(
                icon = Icons.Default.Favorite,
                title = "人格状态",
                subtitle = uiState.soulStageLabel,
                onClick = onNavigateToSoul
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 关于
            SectionHeader("关于")
            SettingsItem(
                icon = Icons.Default.Info,
                title = "关于二狗",
                subtitle = "v1.0.0",
                onClick = { showAboutDialog = true }
            )
        }
    }

    if (showModelSelectorDialog) {
        ModelSelectorDialog(
            currentProvider = uiState.modelProvider,
            hasDeepSeekKey = uiState.hasApiKey,
            hasClaudeKey = uiState.hasClaudeApiKey,
            onSelectProvider = { viewModel.onModelProviderChanged(it) },
            onSaveDeepSeekKey = { viewModel.onSaveApiKey(it) },
            onSaveClaudeKey = { viewModel.onSaveClaudeApiKey(it) },
            onDismiss = { showModelSelectorDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            currentMode = uiState.themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                viewModel.onThemeModeChanged(mode)
                showThemeDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("关于二狗") },
            text = {
                Column {
                    Text("二狗 v1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "你的古文幕僚 AI 助手。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "沉稳可靠，言之有物。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "所有数据本地存储，隐私第一。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }

    if (showNextLoginDialog) {
        NextLoginDialog(
            isLoading = uiState.nextLoginLoading,
            error = uiState.nextLoginError,
            onDismiss = {
                showNextLoginDialog = false
                viewModel.clearNextLoginError()
            },
            onLogin = { username, password ->
                viewModel.onNextLogin(username, password)
            },
            onLoginSuccess = uiState.nextLoggedIn
        )
    }

    if (showNextLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showNextLogoutDialog = false },
            title = { Text("登出 Next") },
            text = { Text("确定要登出 ${uiState.nextUsername} 吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onNextLogout()
                    showNextLogoutDialog = false
                }) { Text("登出") }
            },
            dismissButton = {
                TextButton(onClick = { showNextLogoutDialog = false }) { Text("取消") }
            }
        )
    }

    if (showProfileDialog) {
        ProfileEditDialog(
            currentNickname = uiState.nickname,
            currentAvatarResName = uiState.avatarResName,
            onDismiss = { showProfileDialog = false },
            onSave = { nickname, avatarResName ->
                viewModel.onSaveNickname(nickname)
                viewModel.onSaveAvatar(avatarResName)
                showProfileDialog = false
            }
        )
    }
}

/** 通用头像组件：支持预置 drawable 名称或 custom:开头的内部文件路径 */
@Composable
fun AvatarImage(avatarResName: String, size: Int) {
    val context = LocalContext.current
    val modifier = Modifier.size(size.dp).clip(CircleShape)

    when {
        avatarResName.startsWith("custom:") -> {
            val filePath = avatarResName.removePrefix("custom:")
            val bitmap = remember(filePath) {
                try {
                    BitmapFactory.decodeFile(filePath)?.asImageBitmap()
                } catch (_: Exception) { null }
            }
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = "头像", modifier = modifier, contentScale = ContentScale.Crop)
            } else {
                DefaultAvatarIcon(size)
            }
        }
        avatarResName.isNotBlank() -> {
            val resId = context.resources.getIdentifier(avatarResName, "drawable", context.packageName)
                .takeIf { it != 0 }
            if (resId != null) {
                Image(painter = painterResource(id = resId), contentDescription = "头像", modifier = modifier, contentScale = ContentScale.Crop)
            } else {
                DefaultAvatarIcon(size)
            }
        }
        else -> DefaultAvatarIcon(size)
    }
}

@Composable
private fun DefaultAvatarIcon(size: Int) {
    Icon(
        Icons.Default.Person,
        contentDescription = "头像",
        modifier = Modifier
            .size(size.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .padding((size / 5).dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ProfileItem(
    avatarResName: String,
    nickname: String,
    nextLoggedIn: Boolean,
    nextUsername: String,
    onClickProfile: () -> Unit,
    onClickNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickProfile)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(avatarResName = avatarResName, size = 48)

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nickname.ifBlank { "点击设置昵称" },
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (nextLoggedIn) "Next: $nextUsername" else "Next: 未登录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onClickNext)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileEditDialog(
    currentNickname: String,
    currentAvatarResName: String,
    onDismiss: () -> Unit,
    onSave: (nickname: String, avatarResName: String) -> Unit
) {
    var nickname by remember { mutableStateOf(TextFieldValue(currentNickname)) }
    var selectedAvatar by remember { mutableStateOf(currentAvatarResName) }
    val context = LocalContext.current

    // 相册选图 → 复制到内部存储
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val avatarFile = java.io.File(context.filesDir, "custom_avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    avatarFile.outputStream().use { output -> input.copyTo(output) }
                }
                selectedAvatar = "custom:${avatarFile.absolutePath}"
            } catch (e: Exception) {
                timber.log.Timber.w(e, "[Settings] 头像保存失败")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑个人信息") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("选择头像", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // 对话框宽度约 312dp - 48dp padding = 264dp 可用
                // 每个头像 56dp + 8dp 间距 = 64dp，一行约放 4 个
                val itemsPerRow = 4
                // 第一行包含上传按钮 + 3个预置，第二行4个预置 → 前7个预置显示
                val collapsedPresetCount = itemsPerRow * 2 - 1 // 上传按钮占1格
                var showAllAvatars by remember { mutableStateOf(false) }
                val visiblePresets = if (showAllAvatars) PRESET_AVATARS
                    else PRESET_AVATARS.take(collapsedPresetCount)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 自定义上传按钮
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .then(
                                if (selectedAvatar.startsWith("custom:")) Modifier.border(
                                    2.dp, MaterialTheme.colorScheme.primary, CircleShape
                                ) else Modifier
                            )
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatar.startsWith("custom:")) {
                            AvatarImage(avatarResName = selectedAvatar, size = 56)
                        } else {
                            Text(
                                "+",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // 预置头像（折叠/展开）
                    visiblePresets.forEach { resName ->
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        if (resId != 0) {
                            val isSelected = resName == selectedAvatar
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = resName,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .clickable { selectedAvatar = resName },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                if (PRESET_AVATARS.size > collapsedPresetCount) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (showAllAvatars) "收起" else "更多头像...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showAllAvatars = !showAllAvatars }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(nickname.text.trim(), selectedAvatar) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun NextLoginDialog(
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onLoginSuccess: Boolean
) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    // 登录成功自动关闭
    if (onLoginSuccess) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("登录 Next") },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("登录中...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLogin(username.text.trim(), password.text) },
                enabled = username.text.isNotBlank() && password.text.isNotBlank() && !isLoading
            ) { Text("登录") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("取消") }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModelSelectorDialog(
    currentProvider: ModelProvider,
    hasDeepSeekKey: Boolean,
    hasClaudeKey: Boolean,
    onSelectProvider: (ModelProvider) -> Unit,
    onSaveDeepSeekKey: (String) -> Unit,
    onSaveClaudeKey: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 内部状态：正在编辑哪个模型的 Key（null 表示不在编辑）
    var editingKeyFor by remember { mutableStateOf<ModelProvider?>(null) }
    // 选中了一个 Key 未设置的模型，保存 Key 后自动切换
    var pendingProvider by remember { mutableStateOf<ModelProvider?>(null) }

    if (editingKeyFor != null) {
        val provider = editingKeyFor!!
        val option = MODEL_OPTIONS.first { it.provider == provider }
        ApiKeyInputDialog(
            title = "${option.displayName} API Key",
            subtitle = option.modelId,
            placeholder = if (provider == ModelProvider.CLAUDE) "sk-ant-..." else "sk-...",
            onDismiss = {
                editingKeyFor = null
                pendingProvider = null
            },
            onSave = { key ->
                when (provider) {
                    ModelProvider.DEEPSEEK -> onSaveDeepSeekKey(key)
                    ModelProvider.CLAUDE -> onSaveClaudeKey(key)
                }
                editingKeyFor = null
                // 如果是因为选中未配置 Key 的模型触发的，保存后自动切换
                if (pendingProvider == provider) {
                    onSelectProvider(provider)
                    pendingProvider = null
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("模型设置") },
        text = {
            Column {
                MODEL_OPTIONS.forEach { option ->
                    val hasKey = when (option.provider) {
                        ModelProvider.DEEPSEEK -> hasDeepSeekKey
                        ModelProvider.CLAUDE -> hasClaudeKey
                    }
                    val isSelected = option.provider == currentProvider

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hasKey) {
                                    onSelectProvider(option.provider)
                                    onDismiss()
                                } else {
                                    // Key 未设置，先弹输入框
                                    pendingProvider = option.provider
                                    editingKeyFor = option.provider
                                }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (hasKey) {
                                    onSelectProvider(option.provider)
                                    onDismiss()
                                } else {
                                    pendingProvider = option.provider
                                    editingKeyFor = option.provider
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(option.displayName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                option.modelId,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                if (hasKey) {
                                    Text(
                                        "Key 已设置",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                } else {
                                    Text(
                                        "Key 未设置",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = if (hasKey) "修改" else "设置",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { editingKeyFor = option.provider }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ApiKeyInputDialog(
    title: String,
    subtitle: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var keyInput by remember { mutableStateOf(TextFieldValue()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(keyInput.text.trim()) },
                enabled = keyInput.text.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun ThemeModeDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    val label = when (mode) {
                        ThemeMode.SYSTEM -> "跟随系统"
                        ThemeMode.LIGHT -> "亮色模式"
                        ThemeMode.DARK -> "暗色模式"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onSelect(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

