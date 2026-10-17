package com.fei.firstapp
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class MainViewModel(app:Application):AndroidViewModel(app){private val repo=RelayRepository(app);private val _ui=MutableStateFlow(MainUiState());val ui:StateFlow<MainUiState> = _ui.asStateFlow();init{if(repo.hasSession())refresh()};fun connect(url:String,user:String,pass:String){viewModelScope.launch{_ui.value=MainUiState(isLoading=true);runCatching{repo.login(url.trim(),user.trim(),pass)}.onSuccess{_ui.value=MainUiState(usage=it)}.onFailure{_ui.value=MainUiState(errorMessage=it.message?:"连接失败")}}};fun refresh(){viewModelScope.launch{_ui.value=_ui.value.copy(isLoading=true,errorMessage=null);runCatching{repo.load()}.onSuccess{_ui.value=MainUiState(usage=it)}.onFailure{_ui.value=_ui.value.copy(isLoading=false,errorMessage=it.message?:"刷新失败")}}};fun logout(){repo.clear();_ui.value=MainUiState()}}
