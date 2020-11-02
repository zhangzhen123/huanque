package com.julun.huanque.core.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexboxLayoutManager
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.AnchorBasicInfo
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.constant.AnchorSearch
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ClickListenerUtils
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.ProgramAdapter
import com.julun.huanque.core.ui.live.PlayerActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_search.*
import java.util.concurrent.TimeUnit


class SearchActivity : BaseVMActivity<SearchViewModel>() {

    //    private var isSelectAnchor = false //增加标记是不是选择主播
    private var action: String? = null
    private var emptyView: View? = null

    companion object {
        const val SEARCH_HISTORY = "SEARCH_HISTORY"
        val PK_SELECT_ANCHOR = "PK_SELECT_ANCHOR"
        fun newInstance(activity: Activity, type: String) {
            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra(AnchorSearch.TYPE, type)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_search

    private val listAdapter by lazy { SearchResultAdapter() }
    private val recentAdapter by lazy { SearchRecentAdapter() }
    private val historyAdapter by lazy {
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_search_history) {
            override fun convert(holder: BaseViewHolder, item: String) {
                holder.setText(R.id.history_anchor_name, item)
            }

        }
    }

    //布局跟首页一致
    private val recommendAdapter by lazy {
        ProgramAdapter()
    }

    private var showKeyBoardDispose: Disposable? = null

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
//        isSelectAnchor = intent.getBooleanExtra(PK_SELECT_ANCHOR, false)
        action = intent?.getStringExtra(AnchorSearch.TYPE)

        initRecyclerView()
        showKeyBoardDispose = Observable.timer(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            val systemService: InputMethodManager =
                this@SearchActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            search_text.isFocusable = true
            search_text.isFocusableInTouchMode = true
            search_text.requestFocus()
            systemService.showSoftInput(search_text, 0)
        }
        if (AnchorSearch.CONNECT_MICRO == action) {
            //连麦搜索
            mViewModel.queryRecent()
        }
        if (action.isNullOrEmpty()) {
            mViewModel.getRecommendData()
        }
    }


    /**
     * 初始化搜索结果的Recyclerview
     */
    private fun initRecyclerView() {
        search_result_list.hide()
        val listener = OnItemClickListener { adapter, view, position ->
            if (ScreenUtils.isSoftInputShow(this@SearchActivity)) {
                closeKeyBoard()//每次隐藏软键盘 不然跳转后新的activity会有问题
            }

            val data = (search_result_list.adapter as? BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder>
                ?: return@OnItemClickListener).getItem(position)
                ?: return@OnItemClickListener
            val intent = Intent(this@SearchActivity, PlayerActivity::class.java)
            when (action) {
                AnchorSearch.CONNECT_MICRO -> {
                    //连麦跳转
                    val anchorBasic = AnchorBasicInfo().apply {
//                        this.anchorLevel = data.anchorLevel
                        this.headPic = data.headPic
                        this.programId = data.programId
                        this.programName = data.programName
                    }
                    val bundle = Bundle()
                    bundle.putSerializable(AnchorSearch.CONNECT_MICRO, anchorBasic)
                    intent.putExtras(bundle)
                    this@SearchActivity.startActivity(intent)
                }
                else -> {
                    intent.putExtra(IntentParamKey.PROGRAM_ID.name, data.programId)
                    //无字段  搜索页面，不传封面，直播间使用默认背景
//                        val picUrl = data?.coverPic
//                        intent.putExtra(LiveBusiConstant.PICID, picUrl)
                    this@SearchActivity.startActivity(intent)
                }
            }
//            }
        }
        listAdapter.setOnItemClickListener(listener = listener)
        recentAdapter.setOnItemClickListener(listener = listener)
        if (action.isNullOrEmpty()) {
            history_and_recommend.show()
            val flexBoxLayoutManager = FlexboxLayoutManager(this)
            search_history.layoutManager = flexBoxLayoutManager
            search_history.adapter = historyAdapter
            //历史推荐
            val list = getSearchHistory()
            if (list.isNotEmpty()) {
                search_history_layout.show()
                historyAdapter.setList(list)
            } else {
                search_history_layout.hide()
            }
            search_recommends.layoutManager = GridLayoutManager(this, 2)
            search_recommends.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
            search_recommends.adapter = recommendAdapter
            search_recommends.setHasFixedSize(true)
            recommendAdapter.setOnItemClickListener { adapter, view, position ->
                if (ScreenUtils.isSoftInputShow(this@SearchActivity)) {
                    closeKeyBoard()//每次隐藏软键盘 不然跳转后新的activity会有问题
                }

                val data = (search_result_list.adapter as? BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder>
                    ?: return@setOnItemClickListener).getItem(position)
                    ?: return@setOnItemClickListener
                val intent = Intent(this@SearchActivity, PlayerActivity::class.java)
                intent.putExtra(IntentParamKey.PROGRAM_ID.name, data.programId)
                //无字段  搜索页面，不传封面，直播间使用默认背景
//                        val picUrl = data?.coverPic
//                        intent.putExtra(LiveBusiConstant.PICID, picUrl)
                this@SearchActivity.startActivity(intent)


            }
//            recommendAdapter.setSpanSizeLookup { _, position ->
//                when {
//                    recommendAdapter.data[position].type == ProgramAdapterNew.BANNER -> return@setSpanSizeLookup 2
//                    recommendAdapter.data[position].type == ProgramAdapterNew.RECOMMEND -> return@setSpanSizeLookup 2
//                    else -> return@setSpanSizeLookup 1
//                }
//            }
        } else {
            history_and_recommend.hide()
        }

    }

    private fun initViewModel() {
        mViewModel.queryData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                if (it.isSuccess()) {
                    showQueryData(it.requireT())
                } else {
                    showQueryError()
                }

            }

        })
        mViewModel.recentData.observe(this, androidx.lifecycle.Observer {
            if (it != null && it.isSuccess()) {
                showRecentAnchor(it.requireT())
                mViewModel.recentData.value = null
            }
        })
        mViewModel.recommendData.observe(this, androidx.lifecycle.Observer {
            if (it != null && it.isSuccess() && it.requireT().isNotEmpty()) {
                search_recommends.show()
                recommend_tips.show()
                recommendAdapter.setList(it.requireT())
            } else {
                search_recommends.hide()
                recommend_tips.hide()
            }
        })
    }

    //显示最近连麦主播
    private fun showRecentAnchor(list: MutableList<ProgramLiveInfo>) {
        if (AnchorSearch.CONNECT_MICRO != action || list.isEmpty()) {
            return
        }
        history_and_recommend.hide()
        tv_attention.show()
        tv_attention.text = resources.getString(R.string.recent_anchor)
//        search_result_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        search_result_list.layoutManager = GridLayoutManager(this, 5)
        search_result_list.adapter = recentAdapter
        search_result_list.show()
//        recentDecoration = RecentAnchorDecoration()
//        recentDecoration?.let {
//            search_result_list.addItemDecoration(it)
//        }
        search_result_list.setPadding(dp2px(20), 0, 0, 0)
        //当前需求  最多展示5个
        if (list.size <= 5) {
            recentAdapter.setList(list)
        } else {
            val anchors = ArrayList<ProgramLiveInfo>()
            list.forEachIndexed { index, data ->
                if (index < 5) {
                    anchors.add(data)
                }
            }
            recentAdapter.setList(anchors)
        }
    }

    private fun searchData(searchKeyWork: CharSequence) {
        val keyWord = searchKeyWork.trim().toString()
        if (keyWord.trim().isEmpty()) {
            ToastUtils.show("请输入关键字之后再查询")
            return
        }
        saveSearchHistory(searchKeyWork.toString())
        isShowLoadingView(true)

        mViewModel.searchProgram(keyWord)
    }

    //取出搜索关键字
    private fun getSearchHistory(): List<String> {
        val list = arrayListOf<String>()
        val string = SPUtils.getString(SEARCH_HISTORY, "")
        val strList = string.split("__").filter { it.isNotEmpty() }
        logger.info("strList=$strList")
        list.addAll(strList)
        list.removeDuplicate()
        return list
    }

    //保存搜索关键字 最大存储10个
    private fun saveSearchHistory(text: String) {
        var oldString = SPUtils.getString(SEARCH_HISTORY, "")
        logger.info("原始数据str=$oldString")
        if (oldString.contains(text)) {
            logger.info("已经包含的不再存储：$text")
            return
        }
        val newString = if (oldString.isEmpty()) {
            text
        } else {
            val strList = oldString.split("__")
            if (strList.size >= 5) {
                val newList = arrayListOf<String>()
//                val dif = strList.size - 9
//                repeat(dif) {
//                    strList.removeAt(strList.size - 1)
//                }
                //只要前9个
                for (i in 0..3) {
                    newList.add(strList[i])
                }
                val sb = StringBuilder()
                newList.forEachIndexed { index, item ->
                    if (index == newList.size - 1) {
                        sb.append(item)
                    } else {
                        sb.append("${item}__")
                    }
                }
                logger.info("超出上限：$sb")
                oldString = sb.toString()
            }
            "${text}__$oldString"
        }
        SPUtils.commitString(SEARCH_HISTORY, newString)
    }

    private fun deleteHistory() {
        SPUtils.commitString(SEARCH_HISTORY, "")
    }

    override fun initEvents(rootView: View) {
        cancel_btn.onClick {
            closeKeyBoard()
            finish()
        }
        iv_delete.onClickNew {
            search_text.setText("")
            tv_attention.hide()

            search_result_list.hide()
            history_and_recommend.show()
            val list = getSearchHistory()
            if (list.isNotEmpty()) {
                search_history_layout.show()
                historyAdapter.setList(list)
            } else {
                search_history_layout.hide()
            }
        }
        activity_anchor_search.onClick {
            closeKeyBoard()
        }
        scroll_container.onClick {
            closeKeyBoard()
        }
        search_result_list.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    closeKeyBoard()
                }
            }
            false
        }
        history_and_recommend.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (Math.abs(oldScrollY - scrollY) >= 5) {
                closeKeyBoard()
            }
        }
        view_top.onClick {
            //用于屏蔽顶部的点击事件
        }

        search_text.onClick {
            openKeyBoard(search_text)
        }

        search_text.setOnEditorActionListener(TextView.OnEditorActionListener { view, actionId, event ->
            if (event != null)
                if (actionId == EditorInfo.IME_ACTION_SEND || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    when (event.action) {
                        KeyEvent.ACTION_DOWN -> {//只在按下调用,回车事件会在up and down各执行一次
                            val searchKeyWork: CharSequence = "${view.text}"
                            closeKeyBoard()
                            searchData(searchKeyWork)
                            return@OnEditorActionListener true
                        }
                    }
                }
            false
        })
        search_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    ll_search_big_button.show()
                    search_big_button.text = "点击搜索：$s"
                } else {
                    ll_search_big_button.hide()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    iv_delete.show()
                } else {
                    iv_delete.hide()
                }
            }

        })

        ll_search_big_button.onClickNew {
            val searchKeyWork: CharSequence = "${search_text.text}"
            closeKeyBoard()
            searchData(searchKeyWork)
        }
        clear_history.onClickNew {
            deleteHistory()
            search_history_layout.hide()
        }
        historyAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            if (item != null) {
                search_text.setText(item.toString())
                search_text.setSelection(item.toString().length)
                searchData(item.toString())
            }
        }
        //添加点击事件
        recommendAdapter.setOnItemClickListener { _, _, position ->
            if (ClickListenerUtils.isDoubleClick) return@setOnItemClickListener
            logger.info("setOnItemClickListener:$position")
//            if (CommonInit.getInstance().inSDK && !SessionUtils.getIsRegUser()) {
//                return@setOnItemClickListener
//            }
            val data = recommendAdapter.getItem(position).content
            if (data is ProgramLiveInfo) {
                val intent = Intent(this@SearchActivity, PlayerActivity::class.java)
                intent.putExtra(IntentParamKey.PROGRAM_ID.name, data.programId)
                this@SearchActivity.startActivity(intent)
            }
        }
    }

    private fun showQueryError() {
        isShowLoadingView(false)
        history_and_recommend.hide()
        tv_attention.hide()
        if (search_result_list.adapter != listAdapter) {
            search_result_list.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        }

        search_result_list.show()
        emptyView = emptyView
            ?: MixedHelper.getEmptyView(this, getString(R.string.search_result_kong))
        listAdapter.setEmptyView(emptyView!!)
        listAdapter.notifyDataSetChanged()
    }

    private fun showQueryData(indexInfoList: MutableList<ProgramLiveInfo>) {
        history_and_recommend.hide()
        isShowLoadingView(false)
        tv_attention.show()
        tv_attention.text = resources.getString(R.string.search_result)
        if (search_result_list.adapter != listAdapter) {
            search_result_list.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//            recentDecoration?.let {
//                search_result_list.removeItemDecoration(it)
//                search_result_list.setPadding(0, 0, 0, 0)
//            }
        }

        search_result_list.show()
        search_result_list.adapter = listAdapter
        listAdapter.setList(indexInfoList)
        if (indexInfoList.size == 0) {
            showQueryError()
        }

    }

    private fun isShowLoadingView(isShow: Boolean) {
        if (isShow) {
            bounceView.show()
        } else {
            bounceView.hide()
        }
    }

    private fun closeKeyBoard() {
        if (ScreenUtils.isSoftInputShow(this)) {
            logger.info("isSoftShowing")
            ScreenUtils.hideSoftInput(this)
        }
        search_text.isCursorVisible = false
        search_text.isFocusable = false
        search_text.isFocusableInTouchMode = false
    }

    private fun openKeyBoard(view: EditText) {
        if (view != null) {
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.requestFocus()
            search_text.isCursorVisible = true
            ScreenUtils.showSoftInput(this, view)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        showKeyBoardDispose?.dispose()
        super.onDestroy()
    }

    override fun showLoadState(state: NetState) {
        TODO("Not yet implemented")
    }
}
