package cn.moyada.faker.module.loader;

import cn.moyada.faker.module.Dependency;
import cn.moyada.faker.module.fetch.DependencyFetch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态状态依赖
 * @author xueyikang
 * @create 2018-04-27 16:33
 */
@Component
public class ModuleLoader implements ModuleFetch {

    private static final int CACHE_TIME = 720000;

    private final ClassLoader parent;

    private final Map<Dependency, AppClassLoader> loaderMap;

    @Autowired
    private DependencyFetch dependencyFetch;

    public ModuleLoader(ClassLoader parent) {
        this.parent = parent;
        this.loaderMap = new HashMap<>();
    }

    @Override
    public Class getClass(Dependency dependency, String className) throws ClassNotFoundException {
        ClassLoaderFetch classLoader = getClassLoader(dependency);
        if(null == classLoader) {
            throw new ClassNotFoundException(dependency + ", " + className);
        }
        return classLoader.loadLocalClass(className);
    }

    @Override
    public MethodHandles.Lookup getMethodLookup(Dependency dependency) {
        ClassLoaderFetch classLoader = getClassLoader(dependency);
        if(null == classLoader) {
            return MethodHandles.lookup();
        }
        return classLoader.getMethodLookup();
    }

    /**
     * 获取对应依赖类加载器
     * @param dependency
     * @return
     */
    private AppClassLoader getClassLoader(Dependency dependency) {
        AppClassLoader classLoader = loaderMap.get(dependency);
        if(null == classLoader || !equals(classLoader, dependency)) {
            boolean success = loadJar(dependency);
            if (!success) {
                return null;
            }
            classLoader = loaderMap.get(dependency);
        }
        return classLoader;
    }

    /**
     * 装载jar包
     * @param dependency
     * @return
     */
    public boolean loadJar(Dependency dependency) {
        String jarUrl = dependency.getUrl();
        if(null == jarUrl) {
            jarUrl = dependencyFetch.getJarUrl(dependency);
        }
        if(null == jarUrl) {
            return false;
        }

        AppClassLoader classLoader = loaderMap.get(dependency);
        if(null != classLoader) {
            // 路径不变
            if(classLoader.getUrl().equals(jarUrl)) {
                classLoader.setTimestamp(System.currentTimeMillis());
                return true;
            }
            else {
                classLoader.destroy();
            }
        }

        try {
            classLoader = newClassLoader(jarUrl, getUrls(dependency.getDependencyList()), dependency.getVersion());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        classLoader.setTimestamp(System.currentTimeMillis());
        loaderMap.put(dependency, classLoader);
        return true;
    }

    private List<String> getUrls(List<Dependency> dependencyList) {
        if(null == dependencyList) {
            return null;
        }

        List<String> dependencies = new ArrayList<>(dependencyList.size());
        for(Dependency dependency : dependencyList) {
            String jarUrl = dependency.getUrl();
            if(null == jarUrl) {
                jarUrl = dependencyFetch.getJarUrl(dependency);
            }
            dependencies.add(jarUrl);
        }
        return dependencies;
    }

    /**
     * 获取类加载器
     * @return
     */
    private AppClassLoader newClassLoader(String jarUrl, List<String> dependencies, String version) throws MalformedURLException {
        return new AppClassLoader(jarUrl, dependencies, version, parent);
    }

    /**
     * 判断请求依赖与现在类加载器信息是否一致
     * @param classLoader
     * @param dependency
     * @return
     */
    private boolean equals(AppClassLoader classLoader, Dependency dependency) {
        if(null != dependency.getUrl()) {
            // 存在指定路径并且相同
            if(classLoader.getUrl().equals(dependency.getUrl())) {
                return true;
            }
            return false;
        }

        long millis = System.currentTimeMillis();
        if(null != dependency.getVersion()) {
            // 存在指定版本并且相同
            if(classLoader.getVersion().equals(dependency.getVersion()) &&
                    millis < classLoader.getTimestamp() + CACHE_TIME) {
                return true;
            }
            return false;
        }

        // 缓存时间
        if(millis < classLoader.getTimestamp() + CACHE_TIME) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        System.setProperty("maven.host", "https://repo.souche-inc.com");
        System.setProperty("maven.version", "maven2");
        ModuleLoader moduleLoader = new ModuleLoader(ClassLoader.getSystemClassLoader());
        Dependency dependency = new Dependency();
        dependency.setGroupId("com.souche");
        dependency.setArtifactId("car-model-api");
        Class aClass = moduleLoader.getClass(dependency, "com.souche.car.model.api.model.ModelService");
        System.out.println(aClass);
        moduleLoader.getClassLoader(dependency).destroy();
    }
}