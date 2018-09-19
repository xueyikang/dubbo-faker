package cn.moyada.sharingan.core.support;

import cn.moyada.sharingan.common.utils.AssertUtil;
import cn.moyada.sharingan.storage.api.MetadataRepository;
import cn.moyada.sharingan.storage.api.domain.AppDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouteProcessor {

    @Autowired
    private MetadataRepository metadataRepository;

    /**
     * 获取参数表达式路由信息
     * @param value
     * @return
     */
    public RouteInfo getRoute(String value) {
        String expression = ExpressionUtil.findExpression(value);
        if (null == expression) {
            return null;
        }

        String[] route = ExpressionUtil.findRoute(expression);
        AssertUtil.checkoutNotNull(route, "cannot find any route from " + expression);

        String appName = route[0];
        AppDO appDO = metadataRepository.findAppByName(appName);
        AssertUtil.checkoutNotNull(appDO, "cannot find app info by " + appName);
        Integer appId = appDO.getId();

        String domain = route[1];
        AssertUtil.checkoutNotNull(domain, "args domain is not exist in " + expression);

        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setTarget(expression);
        routeInfo.setAppId(appId);
        routeInfo.setDomain(route[1]);
        return routeInfo;
    }
}
