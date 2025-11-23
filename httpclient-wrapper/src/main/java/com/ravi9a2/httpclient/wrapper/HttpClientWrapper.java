package com.ravi9a2.httpclient.wrapper;

import com.ravi9a2.nca.NonReactiveClient;
import com.ravi9a2.nca.data.RestRequestSpec;
import com.ravi9a2.nca.exceptions.NetworkClientException;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpClientWrapper implements NonReactiveClient<HttpClient> {

    protected HttpClient httpClient;
    protected String baseUrl;
    protected Map<String, String> headersMap;

    public HttpClientWrapper(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    public HttpClientWrapper headers(Map<String, String> map) {
        if (Objects.isNull(this.headersMap)) {
            this.headersMap = new HashMap<>();
        }
        if (Objects.nonNull(map)) {
            this.headersMap.putAll(map);
        }
        return this;
    }

    public HttpClientWrapper baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public <R> R post(RestRequestSpec requestSpec) {
        String url = constructPathParam(requestSpec.getUrl(), requestSpec.getPathParams());
        final StringEntity stringEntity = new StringEntity(JsonUtil.toString(requestSpec.getBody()), StandardCharsets.UTF_8);
        final HttpPost httpPost = new HttpPost(baseUrl + url);
        httpPost.setEntity(stringEntity);
        httpPost.setHeaders(constructHeaders(requestSpec.getHeaders()));
        return makeRequestAndParseResponse(httpPost, requestSpec);
    }

    @Override
    public <R> R put(RestRequestSpec requestSpec) {
        String url = constructPathParam(requestSpec.getUrl(), requestSpec.getPathParams());
        final StringEntity stringEntity = new StringEntity(JsonUtil.toString(requestSpec.getBody()), StandardCharsets.UTF_8);
        final HttpPut httpPut = new HttpPut(baseUrl + url);
        httpPut.setEntity(stringEntity);
        httpPut.setHeaders(constructHeaders(requestSpec.getHeaders()));
        return makeRequestAndParseResponse(httpPut, requestSpec);
    }

    @Override
    public <R> R get(RestRequestSpec requestSpec) {
        String url = constructPathParam(requestSpec.getUrl(), requestSpec.getPathParams());
        HttpGet httpGet = new HttpGet(baseUrl + url);
        httpGet.setHeaders(constructHeaders(requestSpec.getHeaders()));
        return makeRequestAndParseResponse(httpGet, requestSpec);
    }

    @Override
    public <R> R delete(RestRequestSpec requestSpec) {
        String url = constructPathParam(requestSpec.getUrl(), requestSpec.getPathParams());
        final HttpDelete httpDelete = new HttpDelete(baseUrl + url);
        httpDelete.setHeaders(constructHeaders(requestSpec.getHeaders()));
        return makeRequestAndParseResponse(httpDelete, requestSpec);
    }

    @Override
    public <R> R options(RestRequestSpec requestSpec) {
        return null;
    }

    @Override
    public <R> R patch(RestRequestSpec requestSpec) {
        return null;
    }

    @Override
    public <R> R call(RestRequestSpec requestSpec) {
        switch (requestSpec.getHttpMethod()) {
            case "POST":
                return post(requestSpec);
            case "GET":
                return get(requestSpec);
            case "DELETE":
                return delete(requestSpec);
            case "PUT":
                return put(requestSpec);
            default:
                throw new IllegalArgumentException("Invalid http method.");
        }
    }

    private <R> R makeRequestAndParseResponse(HttpUriRequest httpRequest,
                                              RestRequestSpec requestSpec) {
        String responseStr = makeRequest(httpRequest, requestSpec.getUrl());
        return JsonUtil.parseResponse(responseStr, requestSpec.getType());
    }

    private String makeRequest(HttpUriRequest request, String urlTemplate) {
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            emitResponseMetrics(request.getRequestLine().getMethod(), urlTemplate,
                    Objects.nonNull(httpResponse.getStatusLine()) ? httpResponse.getStatusLine().getStatusCode() : HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (HttpResponseException hre) {
            emitResponseMetrics(request.getRequestLine().getMethod(), urlTemplate, hre.getStatusCode());
            throw new NetworkClientException(hre);
        } catch (Exception e) {
            emitResponseMetrics(request.getRequestLine().getMethod(), urlTemplate, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            throw new NetworkClientException(e);
        }
    }

    private void emitResponseMetrics(String method, String urlTemplate, int statusCode) {
        Metrics.increment("HTTP_STATUS", "uri=" + urlTemplate + "," +
                "method=" + method + ",statusCode=" + statusCode + "," +
                "statusSeries=" + statusCode / 100);
    }

    private static String constructPathParam(String templateUrl, Map<String, String> pathParams) {
        if (Objects.isNull(pathParams) || pathParams.isEmpty()) {
            return templateUrl;
        }
        StringSubstitutor rootQuery = new StringSubstitutor(pathParams);
        return rootQuery.replace(templateUrl);
    }

    private Header[] constructHeaders(Map<String, String> headers) {
        int headerSize = getMapSize(this.headersMap) + getMapSize(headers);
        if (headerSize == 0) return null;
        Header[] heads = new Header[headerSize];
        int i = 0;
        if (getMapSize(this.headersMap) > 0) {
            for (final String headerKey : this.headersMap.keySet()) {
                heads[i++] = new BasicHeader(headerKey, this.headersMap.get(headerKey));
            }
        }
        if (getMapSize(headers) > 0) {
            for (final String headerKey : headers.keySet()) {
                heads[i++] = new BasicHeader(headerKey, headers.get(headerKey));
            }
        }
        return heads;
    }

    private int getMapSize(Map<String, String> headersMap) {
        return !CollectionUtils.isEmpty(headersMap) ? headersMap.size() :  0;
    }

}
