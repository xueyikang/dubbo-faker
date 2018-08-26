package cn.moyada.faker.core.handler;


import cn.moyada.faker.module.Dependency;

import java.lang.invoke.MethodHandle;

/**
 * 代理抽象类
 */
public abstract class AbstractHandler {

    /**
     * 获取代理
     */
    public abstract MethodHandle fetchHandleInfo(Dependency dependency, String className, String methodName,
                                                 String returnType, Class<?> paramClass[]);
}
