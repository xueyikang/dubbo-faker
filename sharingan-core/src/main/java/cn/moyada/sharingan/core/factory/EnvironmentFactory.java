package cn.moyada.sharingan.core.factory;

import cn.moyada.sharingan.common.exception.InitializeInvokerException;
import cn.moyada.sharingan.common.utils.AssertUtil;
import cn.moyada.sharingan.common.utils.JsonUtil;
import cn.moyada.sharingan.common.utils.RegexUtil;
import cn.moyada.sharingan.common.utils.StringUtil;
import cn.moyada.sharingan.core.common.InvokeContext;
import cn.moyada.sharingan.core.common.QuestInfo;
import cn.moyada.sharingan.core.convert.AppInfoConverter;
import cn.moyada.sharingan.core.convert.FunctionInfoConverter;
import cn.moyada.sharingan.module.Dependency;
import cn.moyada.sharingan.module.InvokeInfo;
import cn.moyada.sharingan.module.InvokeMetaData;
import cn.moyada.sharingan.module.handler.InvokeAdapter;
import cn.moyada.sharingan.rpc.api.invoke.InvocationMetaDate;
import cn.moyada.sharingan.storage.api.MetadataRepository;
import cn.moyada.sharingan.storage.api.domain.AppDO;
import cn.moyada.sharingan.storage.api.domain.FunctionDO;
import cn.moyada.sharingan.storage.api.domain.HttpDO;
import cn.moyada.sharingan.storage.api.domain.ServiceDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EnvironmentFactory {

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private InvokeAdapter invokeAdapter;

    private static final String[] httpProtocols = {"springcloud"};

    private static boolean isHttp(String protocol) {
        for (String httpProtocol : httpProtocols) {
            if (protocol.equals(httpProtocol)) {
                return true;
            }
        }
        return false;
    }

    public InvokeContext getEnv(QuestInfo questInfo) {
        Integer appId = questInfo.getAppId();
        AppDO appDO = metadataRepository.findAppById(appId);
        AssertUtil.checkoutNotNull(appDO, "cannot find application by appId: " + appId);

        Integer serviceId = questInfo.getServiceId();
        ServiceDO serviceDO = metadataRepository.findServiceById(serviceId);
        AssertUtil.checkoutNotNull(serviceDO, "cannot find service by serviceId: " + serviceId);

        String protocol = serviceDO.getProtocol();
        if (StringUtil.isEmpty(protocol)) {
            throw new InitializeInvokerException("protocol cannot be null");
        }
        InvokeContext context;
        if (isHttp(protocol)) {
            context = getHttpEnv(questInfo.getInvokeId(), questInfo.getExpression());
        } else {
            context = getFuncEnv(questInfo.getInvokeId(), questInfo.getExpression());
        }

        context.setProtocol(serviceDO.getProtocol());
        context.setAppId(appId);
        context.setServiceId(serviceId);
        context.getInvocationMetaDate().setApplicationName(appDO.getName());
        context.getInvocationMetaDate().setServiceName(serviceDO.getName());
        return context;
    }

    @SuppressWarnings("ConstantConditions")
    private InvokeContext getFuncEnv(int funcId, String inputExpression) {
        FunctionDO functionDO = metadataRepository.findFunctionById(funcId);
        AssertUtil.checkoutNotNull(functionDO, "cannot find function by funcId: " + funcId);

        String[] expression = JsonUtil.toArray(inputExpression, String[].class);

        Dependency dependency = getDependency(functionDO.getAppId());
        InvokeInfo invokeInfo = FunctionInfoConverter.getInvokeInfo(functionDO);

        InvokeMetaData invokeMetaData = invokeAdapter.wrapper(dependency, invokeInfo);

        Class<?>[] paramTypes = invokeMetaData.getParamTypes();
        if (null == expression && null != paramTypes) {
            throw new InitializeInvokerException("input expression param number cannot match with function param.");
        } else if (paramTypes.length != expression.length) {
            throw new InitializeInvokerException("input expression param number cannot match with function param.");
        }

        InvokeContext context = new InvokeContext();
        context.setDependency(dependency);
        context.setInvokeMetaData(invokeMetaData);

        context.setFuncId(funcId);
        context.setExpression(expression);

        InvocationMetaDate invocationMetaDate = new InvocationMetaDate();
        invocationMetaDate.setMethodName(functionDO.getMethodName());
        InvocationMetaDate.ClassInfo classInfo = new InvocationMetaDate.ClassInfo();
        classInfo.setServiceClass(invokeMetaData.getClassType());
        classInfo.setMethodHandle(invokeMetaData.getMethodHandle());

        invocationMetaDate.setClassInfo(classInfo);
        context.setInvocationMetaDate(invocationMetaDate);
        return context;
    }

    private InvokeContext getHttpEnv(int methodId, String inputExpression) {
        HttpDO httpDO = metadataRepository.findHttpById(methodId);
        AssertUtil.checkoutNotNull(httpDO, "cannot find http by methodId: " + methodId);

        String methodName = httpDO.getMethodName();

        String param = httpDO.getParam();
        String[] params = StringUtil.isEmpty(param) ? null : param.split(",");

        String header = httpDO.getHeader();
        String[] headers = StringUtil.isEmpty(header) ? null : header.split(",");

        String[] expression = getHttpExpression(inputExpression, methodName, params, headers);

        int length = expression.length;
        Class[] classes;
        if (length == 0) {
            classes = new Class[0];
        }
        else {
            classes = new Class[length];
            for (int i = 0; i < length; i++) {
                classes[i] = String.class;
            }
        }

        InvokeContext context = new InvokeContext();
        context.setFuncId(methodId);
        context.setExpression(expression);

        InvokeMetaData invokeMetaData = new InvokeMetaData();
        invokeMetaData.setClassType(Object.class);
        invokeMetaData.setParamTypes(classes);
        invokeMetaData.setReturnType(String.class);
        context.setInvokeMetaData(invokeMetaData);

        InvocationMetaDate invocationMetaDate = new InvocationMetaDate();
        invocationMetaDate.setMethodName(httpDO.getMethodName());
        InvocationMetaDate.HttpInfo httpInfo = new InvocationMetaDate.HttpInfo();
        httpInfo.setHttpType(httpDO.getMethodType());
        httpInfo.setParam(params);
        httpInfo.setHeader(headers);

        invocationMetaDate.setHttpInfo(httpInfo);
        context.setInvocationMetaDate(invocationMetaDate);

        return context;
    }

    private String[] getHttpExpression(String inputExpression, String method,
                                      String[] params, String[] headers) {
        List<String> variable = RegexUtil.findPathVariable(method);
        int paramLeng = variable.size();
        if (null != params) {
            paramLeng += params.length;
        }
        int headerLen = 0;
        if (null != headers) {
            headerLen += headers.length;
        }

        int length = paramLeng + headerLen;

        String[] expression = new String[length];

        Map<String, Map> expressionMap = JsonUtil.toMap(inputExpression, String.class, Map.class);
        if (null == expressionMap) {
            for (int index = 0; index < variable.size(); index++) {
                expression[index] = "{" + variable.get(index) + "}";
            }
            for (int index = variable.size(); index < length; index++) {
                expression[index] = "";
            }
            return expression;
        }

        int index = 0;
        Object value;

        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = expressionMap.get("param");
        if (null == paramMap) {
            for (int i = 0; i < paramLeng; i++) {
                expression[index++] = "";
            }
        }
        else {
            for (String item : variable) {
                value = paramMap.get(item);
                expression[index++] = null == value ? "{" + item + "}" : value.toString();
            }
            if (null != params) {
                for (String item : params) {
                    value = paramMap.get(item);
                    expression[index++] = null == value ? "" : value.toString();
                }
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> headerMap = expressionMap.get("header");
        if (null == headerMap) {
            for (int i = 0; i < headerLen; i++) {
                expression[index++] = "";
            }
        }
        else {
            if (null != headers) {
                for (String item : headers) {
                    value = headerMap.get(item);
                    expression[index++] = null == value ? "" : value.toString();
                }
            }
        }

        return expression;
    }

    public static void main(String[] args) {
        String inputExpression = "{'param': {'yest': 'wert'}}";
        Map<String, Map> expressionMap = JsonUtil.toMap(inputExpression, String.class, Map.class);
        System.out.println(expressionMap);
    }

    private Dependency getDependency(int appId) {
        AppDO appDO = metadataRepository.findAppById(appId);
        AssertUtil.checkoutNotNull(appDO, "获取应用信息失败: " + appId);

        Dependency dependency = AppInfoConverter.toDependency(appDO);

        // 获取外部依赖
        String dependencies = appDO.getDependencies();
        List<AppDO> appList = metadataRepository.findApp(dependencies);
        dependency.setDependencyList(AppInfoConverter.toDependency(appList));
        return dependency;
    }
}
