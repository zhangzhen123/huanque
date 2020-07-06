package com.julun.huanque.common.manager.aliyunoss;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;

/**
 * @author zhangzhen
 * @data 2019/2/16
 **/
public interface OssCallback<Request extends OSSRequest, Result extends OSSResult>  {
    void onProgress(Request request, long currentSize, long totalSize);
    void onSuccess(Request request, Result result);

    void onFailure(Request request, ClientException clientException, ServiceException serviceException);
}
