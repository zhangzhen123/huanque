package com.julun.huanque.common.helper

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import org.reactivestreams.Publisher

/**
 * 此操作是把上游转换到主线程执行
 */
class DefaultRxTransformer<T> : ObservableTransformer<T, T>, SingleTransformer<T, T>
    , MaybeTransformer<T, T>, CompletableTransformer, FlowableTransformer<T, T> {
    override fun apply(upstream: Flowable<T>): Publisher<T> {
        return upstream.compose(RunOnMainSchedulerTransformer<T>())
    }

    override fun apply(upstream: Completable): CompletableSource {
        return upstream.compose(RunOnMainSchedulerTransformer<T>())
    }

    override fun apply(upstream: Maybe<T>): MaybeSource<T> {
        return upstream.compose(RunOnMainSchedulerTransformer<T>())
    }

    override fun apply(upstream: Single<T>): SingleSource<T> {
        return upstream.compose(RunOnMainSchedulerTransformer<T>())
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream.compose(RunOnMainSchedulerTransformer<T>())
    }
}

//此类的作用是切换到主线程
class RunOnMainSchedulerTransformer<T> : ObservableTransformer<T, T>, SingleTransformer<T, T>
    , MaybeTransformer<T, T>, CompletableTransformer, FlowableTransformer<T, T> {
    override fun apply(upstream: Flowable<T>): Publisher<T> {
        return upstream.observeOn(AndroidSchedulers.mainThread())
    }

    override fun apply(upstream: Completable): CompletableSource {
        return upstream.observeOn(AndroidSchedulers.mainThread())
    }

    override fun apply(upstream: Maybe<T>): MaybeSource<T> {
        return upstream.observeOn(AndroidSchedulers.mainThread())
    }

    override fun apply(upstream: Single<T>): SingleSource<T> {
        return upstream.observeOn(AndroidSchedulers.mainThread())
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        //这里subscribeOn指定上游线程没有任何意义 只会多创建一层新线程的Observable
        return upstream/*.subscribeOn(Schedulers.io())*/.observeOn(AndroidSchedulers.mainThread())
    }

}