package cn.moyada.dubbo.faker.core.manager;

import cn.moyada.dubbo.faker.core.dao.FakerDAO;
import cn.moyada.dubbo.faker.core.exception.InitializeInvokerException;
import cn.moyada.dubbo.faker.core.loader.Dependency;
import cn.moyada.dubbo.faker.core.model.domain.AppInfoDO;
import cn.moyada.dubbo.faker.core.model.domain.LogDO;
import cn.moyada.dubbo.faker.core.model.domain.MethodInvokeDO;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author xueyikang
 * @create 2017-12-30 05:57
 */
@Repository
public class FakerManager {

    private static final Logger log = LoggerFactory.getLogger(FakerManager.class);

    @Autowired
    private FakerDAO fakerDAO;

    public List<MethodInvokeDO> getAll() {
        return fakerDAO.findAll();
    }

    public List<AppInfoDO> getAllApp() {
        return fakerDAO.findAllApp();
    }

    public void updateUrl(String groupId, String artifactId, String url) {
        fakerDAO.updateUrl(groupId, artifactId, url);
    }

    public Dependency getDependencyByAppId(int id) {
        AppInfoDO appInfo = fakerDAO.findAppById(id);
        if(null == appInfo) {
            return null;
        }
        Dependency dependency = new Dependency(appInfo.getGroupId(), appInfo.getArtifactId(), appInfo.getVersion(), appInfo.getUrl());
        String dependencies = appInfo.getDependencies();
        if(null != dependencies && !dependencies.equals("")) {
            int[] ints = convertInt(dependencies.split(","));
            if(null != ints) {
                List<AppInfoDO> dependenciesApp = fakerDAO.findDependencyById(ints);
                for (AppInfoDO appInfoDO : dependenciesApp) {
                    dependency.addDependency(new Dependency(appInfoDO.getGroupId(), appInfoDO.getArtifactId(), appInfoDO.getVersion(), appInfoDO.getUrl()));
                }
            }
        }
        return dependency;
    }

    private static int[] convertInt(String[] strs) {
        if(null == strs) {
            return null;
        }
        int length = strs.length;
        if(0 == length) {
            return null;
        }
        int[] ints = new int[length];
        int index = 0;
        for (String str : strs) {
            ints[index++] = Integer.valueOf(str);
        }
        return ints;
    }

    public List<String> getClassByApp(int appId) {
        return fakerDAO.findClassByApp(appId);
    }

    public List<MethodInvokeDO> getMethodByClass(String className) {
        return fakerDAO.findMethodByClass(className);
    }

    public int countMethodByFakerId(String fakerId) {
        return fakerDAO.countMethodByFakerId(fakerId);
    }

    public List<LogDO> getMethodByFakerId(String fakerId, int pageIndex, int pageSize) {
        int offset = pageSize * (pageIndex - 1);
        return fakerDAO.findMethodByFakerId(fakerId, offset, pageSize);
    }

    public boolean saveInfokeInfo(MethodInvokeDO methodInvokeDO) {
        try {
            fakerDAO.saveInvokeInfo(methodInvokeDO);
        }
        catch (Exception e) {
            log.error("save method invoke error" + e);
            return false;
        }
        return true;
    }

    public MethodInvokeDO getInvokeInfo(int id) {
        MethodInvokeDO methodInvoke = fakerDAO.findInvokeInfoById(id);
        if(null == methodInvoke) {
            log.error("unknow invokeId: " + id);
            throw InitializeInvokerException.methodError;
        }
        return methodInvoke;
    }

    public List<String> getFakerParamByRebuildParam(String param) {
        String[] split = param.split("\\.");
        Integer appId = Integer.valueOf(split[0]);
        String type = split[1];
        int count = fakerDAO.countParamByType(appId, type);
        int limit, page;
        if(count < 1000) {
            limit = 0;
            page = count;
        }
        else {
            limit = new Random().nextInt(count - 1000);
            page = 1000;
        }

        return fakerDAO.findParamByType(appId, type, limit, page);
    }

    /**
     * 根据参数表达式获取数据库预存模拟参数
     * @param paramSet 表达式集合
     * @return
     */
    public Map<String, List<String>> getFakerParamMapByRebuildParam(Set<String> paramSet) {
        if(null == paramSet || paramSet.isEmpty()) {
            return null;
        }
        Map<String, List<String>> paramMap = Maps.newHashMapWithExpectedSize(paramSet.size());
        List<String> paramValueList;
        for (String param : paramSet) {
            //TODO 过滤从结果中再获取参数的
//            if('_' == param.indexOf(0)) {
//                continue;
//            }
            paramValueList = this.getFakerParamByRebuildParam(param);
            if(paramValueList.isEmpty()) {
                throw new InitializeInvokerException("获取 " + param + " 实际参数集合失败，请检查数据库.");
            }

            paramMap.put(param, paramValueList);
        }
        return paramMap;
    }

    /**
     * 保存调用信息
     * @param logDO
     */
    public void saveLog(LogDO logDO) {
        fakerDAO.saveLog(logDO);
    }

    public void saveLog(List<LogDO> logDO) {
        fakerDAO.saveLogList(logDO);
    }
}
