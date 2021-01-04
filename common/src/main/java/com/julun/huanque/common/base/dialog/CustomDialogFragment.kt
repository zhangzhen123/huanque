package com.julun.huanque.common.base.dialog

import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseDialogFragment

/**
 * 自定义弹窗 如果是简单的弹窗 无需网络请求或者其他复杂逻辑的 可以使用这个去直接创建 无需创建具体子类
 * 通过[builder]去建造
 */
class CustomDialogFragment(var builder: FDBuilder) : BaseDialogFragment() {

    override fun onStart() {
        builder.onStart(this)
        super.onStart()
    }

    override fun getLayoutId(): Int {
        return builder.layoutId()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        builder.initViews(this, view)
    }

    override fun configDialog() {

    }
    override fun initViews() {
    }

    override fun onDestroy() {
        super.onDestroy()
        builder.onClose()
    }

    class FDBuilder(
        val layoutId: () -> Int,
        val onStart: (dialog: BaseDialogFragment) -> Unit = {},
        val initViews: (dialog: BaseDialogFragment, rootView: View) -> Unit = { dia, view -> },
        val onClose: () -> Unit = {}
    )
}