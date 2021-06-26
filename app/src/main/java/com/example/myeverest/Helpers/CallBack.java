
package com.example.myeverest.Helpers;

import java.util.concurrent.ExecutionException;

//Interface mit callback-Funktion um Arbeit mit Ergebnis asynchroner Funktionen zu vereinfachen
public interface CallBack<T> {
    void callback(T data) throws ExecutionException, InterruptedException;
}