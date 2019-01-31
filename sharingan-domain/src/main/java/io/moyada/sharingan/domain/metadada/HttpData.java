package io.moyada.sharingan.domain.metadada;

/**
 * http请求信息
 * @author xueyikang
 * @since 0.0.1
 **/
public class HttpData {

    /**
     * 方法编号
     */
    private Integer id;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 请求类别, GET\POST\PUT\DELETE
     */
    private String methodType;

    /**
     * 参数
     */
    private String param;

    /**
     * 头信息
     */
    private String header;

    /**
     * 表达式
     */
    private String expression;

    private ServiceData serviceData;

    public void setServiceData(ServiceData serviceData) {
        this.serviceData = serviceData;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    protected void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    protected void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    protected void setParam(String param) {
        this.param = param;
    }

    protected void setHeader(String header) {
        this.header = header;
    }

    protected void setExpression(String expression) {
        this.expression = expression;
    }

    public Integer getId() {
        return id;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodType() {
        return methodType;
    }

    public String getParam() {
        return param;
    }

    public String getHeader() {
        return header;
    }

    public String getExpression() {
        return expression;
    }

    public ServiceData getServiceData() {
        return serviceData;
    }
}
