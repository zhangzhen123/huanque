package com.julun.huanque

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionUtils.setSessionId("37d24f40f29b4330af383c336dee8eee")//test
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }
}