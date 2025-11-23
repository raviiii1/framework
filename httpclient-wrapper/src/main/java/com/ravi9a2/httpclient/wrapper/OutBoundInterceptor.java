package com.ravi9a2.httpclient.wrapper;

import com.ravi9a2.nca.exceptions.Status4XXException;
import com.ravi9a2.nca.exceptions.Status5XXException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

public class OutBoundInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse httpResponse, HttpContext context)  {

        HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
        String requestUri = request.getRequestLine().getUri();
        int statusCode =  httpResponse.getStatusLine().getStatusCode();

//        Metrics.increment("HTTP_STATUS", "uri=" + requestUri + "," +
//                "method=" + request.getRequestLine().getMethod() + ",statusCode=" + statusCode + "," +
//                "statusSeries=" + statusCode / 100);
        if (is4XX(statusCode)) {
            throw new Status4XXException(requestUri + " returned " + statusCode, statusCode);
        }
        else if(is5XX(statusCode)) {
            throw new Status5XXException(requestUri + " returned " + statusCode, statusCode);
        }

    }

    private boolean is5XX(int statusCode) {
        return 499 < statusCode && statusCode < 600;
    }

    private boolean is4XX(int statusCode) {
        return 399 < statusCode && statusCode < 500;
    }
}
