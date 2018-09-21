package cn.moyada.sharingan.core;

import cn.moyada.sharingan.common.exception.InitializeInvokerException;
import cn.moyada.sharingan.common.utils.RuntimeUtil;
import cn.moyada.sharingan.common.utils.TimeUtil;
import cn.moyada.sharingan.common.utils.UUIDUtil;
import cn.moyada.sharingan.core.common.InvokeContext;
import cn.moyada.sharingan.core.common.QuestInfo;
import cn.moyada.sharingan.core.factory.EnvironmentFactory;
import cn.moyada.sharingan.core.factory.ListenerFactory;
import cn.moyada.sharingan.core.factory.ProviderFactory;
import cn.moyada.sharingan.core.invoke.DefaultExecutor;
import cn.moyada.sharingan.core.invoke.JobAction;
import cn.moyada.sharingan.core.listener.AbstractListener;
import cn.moyada.sharingan.core.listener.ListenerReport;
import cn.moyada.sharingan.core.proxy.RpcInvokeProxy;
import cn.moyada.sharingan.core.support.ArgsProviderContainer;
import cn.moyada.sharingan.core.task.TaskExecutor;
import cn.moyada.sharingan.module.InvokeMetaData;
import cn.moyada.sharingan.module.fetch.MetadataFetch;
import cn.moyada.sharingan.rpc.api.invoke.InvocationMetaDate;
import cn.moyada.sharingan.rpc.api.invoke.InvokeProxy;
import cn.moyada.sharingan.storage.api.InvocationRepository;
import cn.moyada.sharingan.storage.api.domain.InvocationReportDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Autowired
    private ListenerFactory listenerFactory;

    @Autowired
    private ProviderFactory providerFactory;

    @Autowired
    private EnvironmentFactory environmentFactory;

    @Autowired
    private MetadataFetch metadataFetch;

    @Autowired
    private InvocationRepository invocationRepository;

    @Autowired
    private RpcInvokeProxy rpcInvokeProxy;

    public String invoke(QuestInfo questInfo) throws InitializeInvokerException {
        questInfo.setPoolSize(RuntimeUtil.getActualSize(questInfo.getPoolSize()));

        InvokeContext environment = environmentFactory.buildEnv(questInfo);
        InvokeMetaData invokeMetaData = environment.getInvokeMetaData();

        metadataFetch.checkoutClassLoader(environment.getDependency());

        // 生成调用报告序号
        final String fakerId = UUIDUtil.getUUID();
        environment.setFakerId(fakerId);

        AbstractListener listener = listenerFactory.buildBatchListener(fakerId, questInfo);
        ArgsProviderContainer container = providerFactory.genArgsProvider(environment.getExpression(), invokeMetaData.getParamTypes(), questInfo.isRandom());

        InvokeProxy invokeProxy = rpcInvokeProxy.findInvoke(environment.getProtocol());
        InvocationMetaDate invocationMetaDate = getMetaDate(invokeMetaData);
        invokeProxy.initialization(invocationMetaDate);

        JobAction action = new DefaultExecutor(fakerId, questInfo.getPoolSize(), questInfo.getQuestNum());
        TaskExecutor taskActivity = new TaskExecutor(invokeProxy, listener, action, container);

        log.info("start task. fakerId: " + fakerId);
        Timestamp beginTime = TimeUtil.nowTimestamp();

        taskActivity.execute(questInfo);

        saveReport(fakerId, environment.getAppId(), environment.getServiceId(), environment.getFuncId(),
                beginTime, listener.buildReport());

        metadataFetch.recover();
        return "请求结果序号：" + fakerId;
    }

    private InvocationMetaDate getMetaDate(InvokeMetaData invokeMetaData) {
        InvocationMetaDate invocationMetaDate = new InvocationMetaDate();
        invocationMetaDate.setService(invokeMetaData.getClassType());
        invocationMetaDate.setMethodHandle(invokeMetaData.getMethodHandle());
        return invocationMetaDate;
    }

    private void saveReport(String fakerId, int appId, int serviceId, int funcId,
                            Timestamp dataCreate, ListenerReport report) {
        InvocationReportDO reportDO = new InvocationReportDO();
        reportDO.setFakerId(fakerId);

        reportDO.setAppId(appId);
        reportDO.setServiceId(serviceId);
        reportDO.setFuncId(funcId);

        reportDO.setTotalInvoke(report.getTotalInvoke());
        reportDO.setResponseInvoke(report.getTotalInvoke() - report.getErrorInvoke());
        reportDO.setSuccessRate(report.getSuccessRate());
        reportDO.setMinResponseTime(report.getMinResponseTime());
        reportDO.setMaxResponseTime(report.getMaxResponseTime());
        reportDO.setAvgResponseTime(report.getAvgResponseTime());
        reportDO.setDateCreate(dataCreate);

        invocationRepository.saveReport(reportDO);
    }
}