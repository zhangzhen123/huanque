package com.julun.rnlib.module.banner

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter


class BannerIndexChangeEvent(viewTag: Int, private val mPosition: Int) :
    Event<BannerIndexChangeEvent>(viewTag) {

    override fun dispatch(rctEventEmitter: RCTEventEmitter) {
        rctEventEmitter.receiveEvent(viewTag, eventName, serializeEventData())
    }

    private fun serializeEventData(): WritableMap {
        val eventData: WritableMap = Arguments.createMap()
        eventData.putInt("index", mPosition)
        return eventData
    }

    companion object {
        const val EVENT_NAME = "topBannerIndexChange"
    }

    override fun getEventName(): String = EVENT_NAME
}