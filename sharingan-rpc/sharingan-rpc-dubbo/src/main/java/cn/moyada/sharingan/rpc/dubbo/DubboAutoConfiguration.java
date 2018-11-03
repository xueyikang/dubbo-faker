package cn.moyada.sharingan.rpc.dubbo;

import cn.moyada.sharingan.rpc.api.invoke.InvokeProxy;
import cn.moyada.sharingan.rpc.dubbo.config.DubboConfig;
import cn.moyada.sharingan.rpc.dubbo.invocation.DubboInvoke;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xueyikang
 * @since 0.0.1
 **/
@Configuration
@ConditionalOnClass(DubboConfig.class)
@ConditionalOnProperty(value = DubboAutoConfiguration.REGISTER_URL, matchIfMissing = true)
public class DubboAutoConfiguration {

    final static String REGISTER_URL = "dubbo.registry";

    public static final String BEAN_NAME = "dubboInvoke";

    @Bean
    @ConditionalOnMissingBean(value = DubboConfig.class, search = SearchStrategy.CURRENT)
    public DubboConfig dubboConfigBean() {
        return new DubboConfig();
    }

    @Bean(DubboAutoConfiguration.BEAN_NAME)
    @ConditionalOnMissingBean(DubboInvoke.class)
    public InvokeProxy dubboInvoke() {
        return new DubboInvoke();
    }
}
