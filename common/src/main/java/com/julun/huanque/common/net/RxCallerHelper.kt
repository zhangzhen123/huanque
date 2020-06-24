package com.julun.huanque.common.net

object RxCallerHelper {


    private val requestCache: HashMap<String, MutableList<CancelableObservableSubscriber<*>>> = hashMapOf()

    /**
     * 取消所有跟 caller相关的请求的处理
     * @param caller 请求的发起人
     */
    @Synchronized
    fun cancelRelatedRequest(callerId: String) {
        val list: MutableList<CancelableObservableSubscriber<*>>? = requestCache[callerId]
        list?.forEach {
            it.cancel()
        }
        requestCache.remove(callerId)
    }

    /**
     * 取消所有跟 caller相关的请求的处理
     * @param caller 请求的发起人
     */
    @Synchronized
    fun removeFromCacheWhenRequestIsCompleted(callerId: String, subscriber: CancelableObservableSubscriber<*>?) {
        if (subscriber == null) return

        val list: MutableList<CancelableObservableSubscriber<*>>? = requestCache[callerId]

        if (list != null) {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (subscriber.equals(next)) {
                    subscriber.cancel()
                    iterator.remove()
                    break
                }
            }
            if (!iterator.hasNext()) {
                requestCache.remove(callerId)
            }
        }

    }

    @Synchronized
    fun bindCaller(subscriber: CancelableObservableSubscriber<*>) {
        if (subscriber?.requestCaller == null) {
            return
        }
        bindCaller(subscriber.requestCaller, subscriber)
    }

    @Synchronized
    fun bindCaller(callerId: String, subscriber: CancelableObservableSubscriber<*>) {
        val list: MutableList<CancelableObservableSubscriber<*>> = requestCache[callerId] ?: mutableListOf()
        list.add(subscriber)
        requestCache[callerId] = list
    }

}
