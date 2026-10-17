package com.fei.firstapp
import android.content.Context
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

data class UsageRow(val time:String,val model:String,val tokens:Long,val success:Boolean)
data class RelayUsage(val balance:Long,val totalTokens:Long,val todayTokens:Long,val requests:Long,val rows:List<UsageRow>)
class RelayRepository(context:Context){
 private val prefs=context.applicationContext.getSharedPreferences("relay_session",Context.MODE_PRIVATE)
 private fun api(url:String)=Retrofit.Builder().baseUrl(url.trimEnd('/')+"/").client(OkHttpClient()).addConverterFactory(GsonConverterFactory.create()).build().create(RelayApi::class.java)
 fun hasSession()=prefs.getString("token",null)!=null
 fun clear()=prefs.edit().clear().apply()
 suspend fun login(url:String,user:String,password:String)=withContext(Dispatchers.IO){val r=api(url).login(JsonObject().apply{addProperty("username",user);addProperty("password",password)});val d=r.getAsJsonObject("data")?:r;val t=first(d,"token","access_token")?.asString?:throw IOException("登录响应中没有 token");prefs.edit().putString("baseUrl",url.trimEnd('/')).putString("token",t).apply();load()}
 suspend fun load()=withContext(Dispatchers.IO){val url=prefs.getString("baseUrl","") ?: "";val token=prefs.getString("token","") ?: "";if(url.isNullOrBlank()||token.isNullOrBlank())throw IOException("请先连接中转站");val service=api(url);val auth="Bearer $token";val info=service.self(auth);val d=info.getAsJsonObject("data")?:info;val logs=runCatching{service.logs(auth)}.getOrNull();val rows=parse(logs);var total=num(d,"used_quota","usedQuota","total_tokens");if(total==0L)total=rows.sumOf{it.tokens};var req=num(d,"request_count","requestCount");if(req==0L)req=rows.size.toLong();RelayUsage(num(d,"quota","balance","remain_quota"),total,num(d,"today_quota","today_tokens"),req,rows)}
 private fun parse(r:JsonObject?):List<UsageRow>{val root=r?.get("data")?:return emptyList();val a=when{root.isJsonArray->root.asJsonArray;root.isJsonObject&&root.asJsonObject.has("items")->root.asJsonObject.getAsJsonArray("items");else->return emptyList()};return a.map{val o=it.asJsonObject;UsageRow(first(o,"created_at","createdAt","timestamp")?.asString?:"-",first(o,"model")?.asString?:"-",num(o,"tokens").let{v->if(v>0)v else num(o,"prompt_tokens","promptTokens")+num(o,"completion_tokens","completionTokens")},first(o,"success")?.asBoolean!=false)}}
 private fun num(o:JsonObject,vararg keys:String)=keys.firstNotNullOfOrNull{k->first(o,k)?.takeIf{it.isJsonPrimitive}?.asLong}?:0L
 private fun first(o:JsonObject,vararg keys:String):JsonElement?=keys.firstNotNullOfOrNull{o.get(it)?.takeUnless{v->v.isJsonNull}}
}
