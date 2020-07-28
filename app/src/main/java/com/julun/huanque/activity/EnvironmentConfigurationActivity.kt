package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jakewharton.processphoenix.ProcessPhoenix
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.MMKVConstant
import com.julun.huanque.common.suger.onAdapterChildClickNew
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_environment_configuration.*
import java.io.Serializable

/**
 * 环境配置
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/28
 */
@Route(path = ARouterConstant.ENVIRONMENT_CONFIGURATION_ACTIVITY)
class EnvironmentConfigurationActivity : BaseActivity() {

    private val adapter by lazy { EnvironmentConfigurationAdapter() }
    private val sources by lazy { arrayListOf<EnvironmentConfigurationBean>() }

    private val products by lazy {
        arrayListOf<String>(
            "https://api.51lm.tv/"
        )
    }
    private val developers by lazy {
        arrayListOf<String>(
            "http://192.168.96.249:7777/",
            "developer 例子，自己修改"
        )
    }
    private val tests by lazy {
        arrayListOf<String>(
            "test1 例子，自己修改",
            "test2 例子，自己修改",
            "test3 例子，自己修改"
        )
    }

    companion object {
        const val PRODUCTION = 1
        const val DEVELOPER = 2
        const val TEST = 3
    }

    override fun getLayoutId(): Int = R.layout.activity_environment_configuration

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        sources.clear()
        prepareProducts(products)
        prepareDevelopers(developers)
        prepareTests(tests)
        rvList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvList.adapter = adapter
        adapter.setList(sources)
    }

    override fun initEvents(rootView: View) {
        adapter.onAdapterChildClickNew { _, view, position ->
            when (view?.id) {
                R.id.tvUrl -> {
                    adapter.getItem(position).let {
                        if (it.url.startsWith("http")) {
                            switchEnvironment(it.url)
                        } else {
                            ToastUtils.show("不是正常的ip地址，请查看并修改~！")
                        }
                    }
                }
            }
        }

        btn.onClickNew {
            val customUrl = etUrl.text.toString()
            if (customUrl.isNotEmpty() && customUrl.startsWith("http")) {
                switchEnvironment(customUrl)
            }
        }
    }

    private fun switchEnvironment(url: String = "") {
        val isSuccess = MMKV.defaultMMKV().encode(MMKVConstant.URL, url)
        if (isSuccess) {
            val nextIntent = Intent(this, MainActivity::class.java)
            ProcessPhoenix.triggerRebirth(this, nextIntent)
        }
    }

    private fun prepareProducts(list: List<String>) {
        list.forEachIndexed { index, url ->
            val bean = EnvironmentConfigurationBean().apply {
                if (index == 0) {
                    isFirst = true
                }
                type = PRODUCTION
                this.url = url
            }
            if (!sources.contains(bean)) {
                sources.add(bean)
            }
        }
    }

    private fun prepareDevelopers(list: List<String>) {
        list.forEachIndexed { index, url ->
            val bean = EnvironmentConfigurationBean().apply {
                if (index == 0) {
                    isFirst = true
                }
                type = DEVELOPER
                this.url = url
            }
            if (!sources.contains(bean)) {
                sources.add(bean)
            }
        }
    }

    private fun prepareTests(list: List<String>) {
        list.forEachIndexed { index, url ->
            val bean = EnvironmentConfigurationBean().apply {
                if (index == 0) {
                    isFirst = true
                }
                type = TEST
                this.url = url
            }
            if (!sources.contains(bean)) {
                sources.add(bean)
            }
        }
    }
}

class EnvironmentConfigurationAdapter :
    BaseQuickAdapter<EnvironmentConfigurationBean, BaseViewHolder>(R.layout.item_environment_configuration_list) {

    init {
        addChildClickViewIds(R.id.tvUrl)
    }

    override fun convert(holder: BaseViewHolder, item: EnvironmentConfigurationBean) {
        holder.setText(R.id.tvUrl, item.url).apply {
            if (item.isFirst) {
                setVisible(R.id.tvTitle, true)
                when (item.type) {
                    EnvironmentConfigurationActivity.PRODUCTION -> {
                        setText(R.id.tvTitle, "生产环境")
                    }
                    EnvironmentConfigurationActivity.DEVELOPER -> {
                        setText(R.id.tvTitle, "开发环境")
                    }
                    else -> {
                        setText(R.id.tvTitle, "测试环境")
                    }
                }
            } else {
                setGone(R.id.tvTitle, true)
            }
        }
    }
}

class EnvironmentConfigurationBean : Serializable {
    var type: Int = 1
    var url: String = ""
    var isFirst: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other is EnvironmentConfigurationBean) {
            return this.url == other.url
        }
        return false
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }
}
