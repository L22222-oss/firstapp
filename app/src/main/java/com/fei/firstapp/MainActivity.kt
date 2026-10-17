package com.fei.firstapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fei.firstapp.ui.theme.FirstappTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { FirstappTheme { val state by viewModel.uiState.collectAsState(); when { state.isLoading && state.user == null && !state.isGuest -> LoadingScreen(); state.user == null && !state.isGuest -> LoginScreen(state,{phone,password -> if(state.isRegistering) viewModel.register(phone,password) else viewModel.login(phone,password)},{viewModel.enterGuestMode()},{viewModel.setRegistering(!state.isRegistering)}); else -> HomeScreen(state,{viewModel.logout()},{startActivity(Intent(this@MainActivity,OnlineSiteActivity::class.java))},{startActivity(Intent(this@MainActivity,AudioActivity::class.java))},{startActivity(Intent(this@MainActivity,VideoActivity::class.java))},{startActivity(Intent(this@MainActivity,CalculatorActivity::class.java))}) } } } }
}
@Composable private fun LoadingScreen(){Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){CircularProgressIndicator();Spacer(Modifier.height(12.dp));Text("正在连接服务器...")}}
@Composable private fun LoginScreen(state:MainUiState,onSubmit:(String,String)->Unit,onGuest:()->Unit,onToggleMode:()->Unit){var phone by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};Scaffold{padding->Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(if(state.isRegistering)"注册 Firstapp" else "登录 Firstapp",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(20.dp));OutlinedTextField(phone,{phone=it},label={Text("手机号")},singleLine=true);Spacer(Modifier.height(12.dp));OutlinedTextField(password,{password=it},label={Text("密码（至少 6 位）")},singleLine=true);Spacer(Modifier.height(16.dp));Button(enabled=!state.isLoading,onClick={onSubmit(phone,password)}){Text(if(state.isLoading)"请求中..." else if(state.isRegistering)"注册" else "登录")};Button(enabled=!state.isLoading,onClick=onToggleMode){Text(if(state.isRegistering)"已有账号，去登录" else "没有账号，去注册")};Button(enabled=!state.isLoading,onClick=onGuest){Text("游客进入（仅计算器）")};state.errorMessage?.let{Spacer(Modifier.height(12.dp));Text(it,color=MaterialTheme.colorScheme.error)}}}}
@Composable private fun HomeScreen(state:MainUiState,onLogout:()->Unit,onOpenOnlineSite:()->Unit,onOpenAudio:()->Unit,onOpenVideo:()->Unit,onOpenCalculator:()->Unit){Scaffold{padding->Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("欢迎使用 Firstapp",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(12.dp));if(state.isGuest)Text("当前为游客模式")else{Text("账号：${state.user?.phone}");Text(if(state.permissions?.vip==true)"VIP 用户" else "普通用户")};Spacer(Modifier.height(20.dp));Button(onClick=onOpenCalculator){Text("打开计算器")};if(!state.isGuest){Button(onClick=onOpenAudio){Text("音乐")};Button(onClick=onOpenVideo){Text("视频")}};if(state.permissions?.vip==true)Button(onClick=onOpenOnlineSite){Text("VIP 在线网站")};Button(onClick=onLogout){Text(if(state.isGuest)"返回登录" else "退出登录")}}}}
