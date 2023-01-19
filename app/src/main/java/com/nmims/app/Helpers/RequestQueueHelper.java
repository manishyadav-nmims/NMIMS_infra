package com.nmims.app.Helpers;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

public class RequestQueueHelper implements RequestQueue.RequestFilter {
    Object tag;
    int count = 0;

    public RequestQueueHelper(Object tag) {
        this.tag = tag;
    }

    @Override
    public boolean apply(Request<?> request) {
        if (request.getTag().equals(tag)) {
            count++;
        }
        return false;  // always return false.
    }

    public int getCount() {
        return count;
    }
}
