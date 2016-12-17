package com.chatandfind.android.DirectionsLoader;

import android.support.annotation.NonNull;

/**
 * Created by ivan on 16.12.16.
 */

public class LoadResult<T> {

    @NonNull
    public final ResultType resultType;

    @NonNull
    public final T data;

    LoadResult(ResultType resultType, T data) {
        this.data = data;
        this.resultType = resultType;
    }

}
