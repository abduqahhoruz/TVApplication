package com.example.mydownloadmanager.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mydownloadmanager.R
import com.example.mydownloadmanager.data.Pojo
import com.example.mydownloadmanager.data.PojoModel
import com.example.mydownloadmanager.util.NetworkStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.File

interface IHomeViewModel {
    val liveLoadPojoStatus: LiveData<NetworkStatus>
    val liveDeleteFileStatus: LiveData<NetworkStatus>
    val livePojoList: LiveData<MutableList<Pojo>>
    fun loadPojoList()
    fun deleteFile(filePath: String)
}

class HomeViewModel(val app: Application) : AndroidViewModel(app), IHomeViewModel {

    override val liveLoadPojoStatus = MutableLiveData<NetworkStatus>()
    override val liveDeleteFileStatus = MutableLiveData<NetworkStatus>()
    override val livePojoList = MutableLiveData<MutableList<Pojo>>()

    override fun loadPojoList() {
        viewModelScope.launch {
            try {
                liveLoadPojoStatus.postValue(NetworkStatus.LOADING)
                val gson = Gson()
                val text = app.applicationContext.assets.open("test.json").readBytes()
                val type = object : TypeToken<List<PojoModel>>() {}.type
                val listPojoModel =
                    suspend { gson.fromJson<MutableList<PojoModel>>(String(text), type) }
                val list = mutableListOf<Pojo>()
                listPojoModel.invoke().forEachIndexed { _, it ->
                    if (it.sources[0].endsWith(".mp4")) {
                        list.add(Pojo(it.description, it.sources, it.subtitle, it.thumb, it.title))
                    }
                }
                livePojoList.postValue(list)
                liveLoadPojoStatus.postValue(NetworkStatus.SUCCESS)
            } catch (e: Exception) {
                liveLoadPojoStatus.postValue(NetworkStatus.ERROR(R.string.error_load_pojo_list))
            }
        }

    }


    override fun deleteFile(filePath: String) {
        viewModelScope.launch {
            try {
                liveDeleteFileStatus.postValue(NetworkStatus.LOADING)
                val file = app.cacheDir
                if (file.isFile) {
                    file.delete()
                }
                if (file.exists()) {
                    val files: Array<File> = file.listFiles() ?: return@launch
                    for (f in files) {
                        if (f.name.split(".")[0] == "$filePath")
                            f.delete()
                    }
                    file.delete()
                }
                liveDeleteFileStatus.postValue(NetworkStatus.SUCCESS)
            } catch (e: Exception) {
                liveDeleteFileStatus.postValue(NetworkStatus.ERROR(R.string.error_delete_file))
            }
        }
    }
}