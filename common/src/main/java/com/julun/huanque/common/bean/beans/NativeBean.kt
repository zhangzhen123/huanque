package com.julun.huanque.common.bean.beans

import java.io.Serializable

enum class CollapsingToolbarLayoutState {
    EXPANDED,
    COLLAPSED,
    INTERNEDIATE
}

data class CommonDialogInfo(
    var title: String? = null,
    var content: String? = null,
    var image: String? = null,
    var imageRes: Int? = null,
    var okText: String? = null,
    var cancelText: String? = null,
    var cancelable: Boolean = true
) : Serializable