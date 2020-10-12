package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.SingleUsefulWords
import com.julun.huanque.common.bean.forms.AddWordsForm
import com.julun.huanque.common.bean.forms.UpdateWordsForm
import com.julun.huanque.common.bean.forms.WordsIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/10/12 9:36
 *@描述 常用语ViewModel
 */
class UsefulWordViewModel : BaseViewModel() {

    companion object {
        //编辑事件
        val ACTION_EDIT = "ACTION_EDIT"

        //删除事件
        val ACTION_DELETE = "ACTION_DELETE"
    }

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //常用语数据
    val wordsList: MutableLiveData<MutableList<SingleUsefulWords>> by lazy { MutableLiveData<MutableList<SingleUsefulWords>>() }

    //新增常用语状态
    val wordsAddResult: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //需要更新的常用语
    val needUpdateWords: MutableLiveData<SingleUsefulWords> by lazy { MutableLiveData<SingleUsefulWords>() }

    //操作事件
    val actionData: MutableLiveData<String> by lazy { MutableLiveData<String>() }


    //正在进行操作的常用语对象
    var currentWordsBean: SingleUsefulWords? = null


    /**
     * 获取常用语列表
     */
    fun getWordsList() {
        viewModelScope.launch {
            request({
                val result = socialService.chatWordsList().dataConvert()
                wordsList.value = result.list
            }, needLoadState = true)
        }
    }

    /**
     * 新增常用语
     */
    fun saveSingleWords(words: String) {
        viewModelScope.launch {
            request({
                val result = socialService.chatwordsAdd(AddWordsForm(words)).dataConvert()
                wordsAddResult.value = true
                getWordsList()
            })
        }
    }

    /**
     * 删除常用语
     */
    fun deleteSingleWords(wordsId: Long) {
        viewModelScope.launch {
            request({
                socialService.chatwordsDelete(WordsIdForm(wordsId)).dataConvert()
                getWordsList()
            })
        }
    }

    /**
     * 编辑单个常用语
     */
    fun editSingleWords(wordsId: Long, wordsContent: String) {
        viewModelScope.launch {
            request({
                socialService.chatwordsUpdate(UpdateWordsForm(wordsContent, wordsId)).dataConvert()
                val wordsBean = SingleUsefulWords(wordsContent, wordsId)
                needUpdateWords.value = wordsBean
            })
        }
    }


}