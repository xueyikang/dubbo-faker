package cn.moyada.dubbo.faker.filter.manager;

import cn.moyada.dubbo.faker.filter.dao.FakerDAO;
import cn.moyada.dubbo.faker.filter.domain.AppInfoDO;
import cn.moyada.dubbo.faker.filter.domain.MethodInvokeDO;
import cn.moyada.dubbo.faker.filter.domain.RealParamDO;
import cn.moyada.dubbo.faker.filter.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * @author xueyikang
 * @create 2018-03-30 01:13
 */
public class FakerManager {

    @Autowired
    private FakerDAO fakerDAO;

    /**
     * 新增方法调用信息
     * @param methodInvokeDO
     */
    public void addMethodInvoke(MethodInvokeDO methodInvokeDO) {
        Integer methodId = fakerDAO.existsInvokeInfo(methodInvokeDO);
        if(null == methodId || 0 == methodId) {
            fakerDAO.saveInvokeInfo(methodInvokeDO);
        }
    }

    /**
     * 获取项目编号
     * @param appInfoDO
     * @return
     */
    public Integer getAppId(AppInfoDO appInfoDO) {
        Integer appId = fakerDAO.findAppId(appInfoDO.getAppName());
        fakerDAO.saveApp(appInfoDO);
        if(null != appId) {
            return appId;
        }
        return fakerDAO.findAppId(appInfoDO.getAppName());
    }

    /**
     * 保存参数
     * @param paramDOs
     */
    public void batchSave(Set<RealParamDO> paramDOs) {
        fakerDAO.saveParam(paramDOs, DateUtil.now());
    }
}
