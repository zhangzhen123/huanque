package com.julun.huanque.common.bean.forms

import com.julun.huanque.common.utils.SessionUtils
import java.io.Serializable

/**
 * Created by djp on 2016/11/1.
 */
open class SessionForm : Serializable {
    var sessionId: String = SessionUtils.getSessionId()

    constructor(sessionId: String) {
        this.sessionId = sessionId
    }

    constructor()
}
