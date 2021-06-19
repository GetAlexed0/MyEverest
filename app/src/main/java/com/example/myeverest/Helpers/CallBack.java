
package com.example.myeverest.Helpers;

import java.util.concurrent.ExecutionException;

public interface CallBack<T> {
    void callback(T data) throws ExecutionException, InterruptedException;
}