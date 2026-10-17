package com.fei.firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fei.firstapp.ui.theme.FirstappTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FirstappTheme { val state by viewModel.ui.collectAsState(); if (state.usage == null) LoginScreen(state, viewModel::connect) else Dashboard(state, viewModel::refresh, viewModel::logout) } }
    }
}

@Composable private fun LoginScreen(state: MainUiState, onConnect: (String, String, String) -> Unit) {
    var base by remember { mutableStateOf("https://api.huanling.icu") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Relay Tokens", style = MaterialTheme.typography.headlineMedium)
        Text("中转站 Token 用量", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(base, { base = it }, label = { Text("中转站地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp)); OutlinedTextField(username, { username = it }, label = { Text("账号") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp)); OutlinedTextField(password, { password = it }, label = { Text("密码") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp)); Button(onClick = { onConnect(base, username, password) }, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) { Text(if (state.isLoading) "连接中..." else "连接并读取用量") }
        state.errorMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable private fun Dashboard(state: MainUiState, onRefresh: () -> Unit, onLogout: () -> Unit) {
    val usage = state.usage!!
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Relay Tokens", style = MaterialTheme.typography.headlineSmall); Text("中转站用量", color = MaterialTheme.colorScheme.onSurfaceVariant) }; Row { TextButton(onClick = onRefresh) { Text("刷新") }; TextButton(onClick = onLogout) { Text("退出") } } }
        Spacer(Modifier.height(16.dp)); if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("余额", usage.balance); Metric("累计 Token", usage.totalTokens) }
        Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("今日 Token", usage.todayTokens); Metric("请求次数", usage.requests) }
        Spacer(Modifier.height(20.dp)); Text("最近调用", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp))
        if (usage.rows.isEmpty()) Text("暂无调用记录", color = MaterialTheme.colorScheme.onSurfaceVariant) else LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) { items(usage.rows) { row -> ListItem(headlineContent = { Text(row.model) }, supportingContent = { Text(row.time) }, trailingContent = { Text(row.tokens.toString() + " token") }) } }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
@Composable private fun RowScope.Metric(label: String, value: Long) { Card(Modifier.weight(1f)) { Column(Modifier.padding(14.dp)) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value.toString(), style = MaterialTheme.typography.titleLarge) } } }
