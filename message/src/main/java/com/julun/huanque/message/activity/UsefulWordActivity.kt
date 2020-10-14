package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.interfaces.ContentScrollMeasurer
import com.effective.android.panel.view.panel.PanelView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.SingleUsefulWords
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.EmojiType
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.UsefulWordsAdapter
import com.julun.huanque.message.fragment.WordsActionFragment
import com.julun.huanque.message.viewmodel.UsefulWordViewModel
import kotlinx.android.synthetic.main.act_useful_word.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2020/10/10 17:27
 *@描述 常用语页面
 */
@Route(path = ARouterConstant.USE_FUL_WORD_ACTIVITY)
class UsefulWordActivity : BaseActivity() {

    companion object {
        fun newInstance(act: Activity) {
            val intent = Intent(act, UsefulWordActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                act.startActivity(intent)
            }
        }
    }

    private val mUsefulWordViewModel: UsefulWordViewModel by viewModels()

    //操作弹窗
    private val mActionFragment = WordsActionFragment()

    //常用语Adapter
    private val mAdapter = UsefulWordsAdapter()

    override fun getLayoutId() = R.layout.act_useful_word

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "搭讪常用语"
        initViewModel()
        initRecyclerView()
        mUsefulWordViewModel.getWordsList()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mUsefulWordViewModel.loadState.observe(this, Observer {
            if (it != null) {
                when (it.state) {
                    NetStateType.LOADING -> {
                        //加载中
//                        state_pager_view.showLoading("加载中~！")
                    }
                    NetStateType.SUCCESS -> {
                        //成功
                        state_pager_view.hide()
                    }
                    NetStateType.IDLE -> {
                        //闲置，什么都不做
                    }
                    else -> {
                        //都是异常
                        state_pager_view.showError(
                            errorTxt = "网络异常~！",
                            btnClick = View.OnClickListener {
                                mUsefulWordViewModel.getWordsList()
                            })
                    }
                }
            }
        })

        mUsefulWordViewModel.wordsList.observe(this, Observer {
            if (it != null) {
                mAdapter.setList(it)
            }
        })

        mUsefulWordViewModel.wordsAddResult.observe(this, Observer {
            if (it == true) {
                ToastUtils.show("保存成功")
                mHelper?.hookSystemBackByPanelSwitcher()
            }
        })
        mUsefulWordViewModel.actionData.observe(this, Observer {
            if (it != null) {
                val bean = mUsefulWordViewModel.currentWordsBean ?: return@Observer
                if (it == UsefulWordViewModel.ACTION_EDIT) {
                    //编辑
                    mHelper?.toKeyboardState(true)
                    edit_text.requestFocus()
                    edit_text.setText(bean.words)
                    edit_text.setSelection(edit_text.text.length)
                } else if (it == UsefulWordViewModel.ACTION_DELETE) {
                    //删除
                    MyAlertDialog(this).showAlertWithOKAndCancel(
                        "确定删除该常用语吗？",
                        MyAlertDialog.MyDialogCallback(onRight = {
                            mUsefulWordViewModel.deleteSingleWords(bean.wordsId)
                        }), "提示", "确定"
                    )
                }
            }
        })
        mUsefulWordViewModel.needUpdateWords.observe(this, Observer { suw ->
            if (suw != null) {
                val list = mAdapter.data
                list.forEachIndexed { index, it ->
                    if (it.wordsId == suw.wordsId) {
                        it.words = suw.words
                        mAdapter.notifyItemChanged(index)
                        return@forEachIndexed
                    }
                }
                mUsefulWordViewModel.wordsAddResult.value = true
            }
        })
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.iv_action) {
                //点击操作按钮
                val words = adapter.getItem(position) as? SingleUsefulWords ?: return@setOnItemChildClickListener
                mUsefulWordViewModel.currentWordsBean = words
                mActionFragment.show(supportFragmentManager, "WordsActionFragment")
            }
        }
        mAdapter.setEmptyView(
            MixedHelper.getErrorView(
                this,
                msg = "还未添加搭讪常用语",
                showBtn = false
            )
        )
    }


    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }

        view_add.onClickNew {
            //添加常用语
            edit_text.performClick()
        }

        recyclerView.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_DOWN) {
                    mHelper?.hookSystemBackByPanelSwitcher()
                }
            }

        }

        sendBtn.onClickNew {
            //点击发送
            val message = edit_text.text.toString()
            edit_text.setText("")
            if (message.isEmpty() || message.trim().isEmpty()) {
                ToastUtils.show("常用语不能为空哦")
                return@onClickNew
            }
            if (mUsefulWordViewModel.actionData.value == UsefulWordViewModel.ACTION_EDIT) {
                //更新操作
                val bean = mUsefulWordViewModel.currentWordsBean ?: return@onClickNew
                mUsefulWordViewModel.editSingleWords(bean.wordsId, message)
            } else {
                //保存操作
                mUsefulWordViewModel.saveSingleWords(message)
            }
        }


        edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s == null) {
                    sendBtn.isEnabled = false
                    return
                }
                if (s.toString().length > 30) {
                    edit_text.setText(s.toString().substring(0, 30))
                    edit_text.setSelection(30)
                    Toast.makeText(this@UsefulWordActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                    return
                }
                judgeSendEnable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        edit_text.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendBtn.performClick()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })

        ll_input.onClickNew {
            //屏蔽事件
        }
        panel_emotion.onClickNew {
            //屏蔽事件
        }
        panel_emotion.mListener = object : EmojiInputListener {
            override fun onClick(type: String, emotion: Emotion) {
                val currentLength = edit_text.text.length
                val emojiLength = emotion.text.length
                if (currentLength + emojiLength > 30) {
                    Toast.makeText(this@UsefulWordActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                    return
                }
                val start: Int = edit_text.selectionStart
                val editable: Editable = edit_text.editableText
                val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(
                    this@UsefulWordActivity,
                    emotion.text
                )
                editable.insert(start, emotionSpannable)
            }

            override fun onLongClick(type: String, view: View, emotion: Emotion) {
                //长按,显示弹窗
                showEmojiSuspend(type, view, emotion)
            }

            override fun onActionUp() {
                mEmojiPopupWindow?.dismiss()
            }

            override fun onClickDelete() {
                //点击了删除事件
                deleteInputEmoji()
            }

            override fun showPrivilegeFragment(code: String) {
            }
        }
    }

    //悬浮表情
    private var mEmojiPopupWindow: PopupWindow? = null

    /**
     * 显示表情悬浮效果
     */
    private fun showEmojiSuspend(type: String, view: View, emotion: Emotion) {
        if (mEmojiPopupWindow?.isShowing == true) {
            return
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val content = emotion.text
        var dx = 0
        var dy = 0
        var rootView: View? = null
        when (type) {
            EmojiType.NORMAL -> {
                rootView =
                    LayoutInflater.from(this).inflate(R.layout.fragment_normal_emoji_suspend, null)
                mEmojiPopupWindow = PopupWindow(rootView, dip(50), dip(66))
                val drawable = GlobalUtils.getDrawable(R.drawable.bg_emoji_suspend)
                mEmojiPopupWindow?.setBackgroundDrawable(drawable)
                dx = location[0] + (view.width - dip(50)) / 2
                dy = location[1] - dip(66) + dip(13)
                rootView.findViewById<ImageView>(R.id.iv_emoji)?.imageResource = emotion.drawableRes
            }
            else -> {

            }
        }

        if (rootView == null) {
            return
        }


        val name = content.substring(content.indexOf("[") + 1, content.indexOf("]"))
        rootView.findViewById<TextView>(R.id.tv_emoji)?.text = name

        mEmojiPopupWindow?.isOutsideTouchable = false
        mEmojiPopupWindow?.showAtLocation(view, Gravity.TOP or Gravity.LEFT, dx, dy)
    }

    // 删除光标所在前一位(不考虑切换到emoji时的光标位置，直接删除最后一位)
    private fun deleteInputEmoji() {
        val keyCode = KeyEvent.KEYCODE_DEL
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        edit_text.onKeyDown(keyCode, keyEventDown)
        edit_text.onKeyUp(keyCode, keyEventUp)
    }

    /**
     * 判断赠送按钮是否可用
     */
    private fun judgeSendEnable() {
//        val sending = viewModel.mMessageSending.value ?: false
        //内容不为空，未处于发送状态(显示可用状态，其余显示不可用状态)
        sendBtn.isEnabled = edit_text.text.toString().isNotEmpty()
    }

    private var mHelper: PanelSwitchHelper? = null

    override fun onStart() {
        super.onStart()
        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this) //可选
                .addKeyboardStateListener {
                    onKeyboardChange { visible, height ->
                        //可选实现，监听输入法变化
                        if (visible) {
                            emojiImage.imageResource = R.mipmap.chat_emoji_input
                        } else {
                            emojiImage.imageResource = R.mipmap.chat_key_input
                        }
                    }
                }
                .addEditTextFocusChangeListener {
                    onFocusChange { _, hasFocus ->
                        //可选实现，监听输入框焦点变化
                        if (hasFocus) {
//                            scrollToBottom()
                        }
                    }
                }
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) = 0

                    override fun getScrollViewId() = R.id.recyclerView
                })
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) = 0

                    override fun getScrollViewId() = R.id.view_add
                })
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) = 0

                    override fun getScrollViewId() = R.id.tv_add
                })
                .addPanelChangeListener {
                    onKeyboard {
                        //可选实现，输入法显示回调
                        logger.info("唤起系统输入法")
                        ll_input.show()
                        mHelper?.toKeyboardState(true)
//                        liveViewManager.hideHeaderForAnimation()
//                        iv_emoji.isSelected = false
//                        scrollToBottom()
                    }
                    onNone {
                        logger.info("隐藏所有面板")
                        ll_input.hide()
                        //收起键盘的时候 重置操作事件
                        mUsefulWordViewModel.actionData.value = null
//                        actionView.show()
//                        //可选实现，默认状态回调
//                        liveViewManager.showHeaderForAnimation()
                        //显示头部
//                        iv_emoji.isSelected = false
                    }
                    onPanel { view ->
//                        liveViewManager.hideHeaderForAnimation()
                        //可选实现，面板显示回调
//                        if (view is PanelView) {
//                            iv_emoji.isSelected = view.id == R.id.panel_emotion
//                            scrollToBottom()
//                        }
                    }
                    onPanelSizeChange { panelView, _, _, _, width, height ->
                        //可选实现，输入法动态调整时引起的面板高度变化动态回调
                        if (panelView is PanelView) {
                            when (panelView.id) {
//                            R.id.panel_emotion -> {
//                                val pagerView = findViewById<EmotionPagerView>(R.id.view_pager)
//                                val viewPagerSize: Int = height - DensityHelper.dpToPx(30)
//                                pagerView.buildEmotionViews(
//                                    findViewById<PageIndicatorView>(R.id.pageIndicatorView),
//                                    edit_text,
//                                    Emotions.getEmotions(),
//                                    width,
//                                    viewPagerSize
//                                )
//                            }
                            }
                        }
                    }
                }
                .logTrack(false)
                .build()                     //可选，默认false，是否默认打开输入法
//
//            recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val layoutManager = recyclerView.layoutManager
//                    if (layoutManager is LinearLayoutManager) {
//                        val childCount = recyclerView.childCount
//                        if (childCount > 0) {
//                            val lastChildView = recyclerView.getChildAt(childCount - 1)
//                            val bottom = lastChildView.bottom
//                            val listHeight: Int = recyclerview.height - recyclerview.paddingBottom
//                            unfilledHeight = listHeight - bottom
//                        }
//                    }
//                }
//            })
            mHelper?.setContentScrollOutsideEnable(true)
        }
    }

    //增加返回键回到主页
    override fun onBackPressed() {
        //用户按下返回键的时候，如果显示面板，则需要隐藏
        if (mHelper != null && mHelper?.hookSystemBackByPanelSwitcher() == true) {
            return
        }
        super.onBackPressed()
    }
}