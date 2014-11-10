package com.quickblox.q_municate_core.core.ui;

public class LoaderResult<T> {

    private Exception e;
    private T result;

    public LoaderResult(T result) {
        this.result = result;
    }

    public LoaderResult(Exception e) {
        this.e = e;
    }

    public Exception getException() {
        return e;
    }

    public T getResult() {
        return result;
    }
}
