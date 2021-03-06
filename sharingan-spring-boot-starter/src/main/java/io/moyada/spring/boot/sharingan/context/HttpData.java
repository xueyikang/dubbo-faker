package io.moyada.spring.boot.sharingan.context;

import io.moyada.sharingan.monitor.api.entity.ContentType;
import io.moyada.sharingan.monitor.api.entity.HttpType;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class HttpData {

    private String methodName;

    private HttpType type;

    private ContentType contentType;

    private String[] param;

    private String[] header;

    public HttpData(String methodName, HttpType type, ContentType contentType, String[] param, String[] header) {
        this.methodName = methodName;
        this.type = type;
        this.contentType = contentType;
        this.param = param;
        this.header = header;
    }

    public String getMethodName() {
        return methodName;
    }

    public HttpType getType() {
        return type;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String[] getParam() {
        return param;
    }

    public String[] getHeader() {
        return header;
    }
}
