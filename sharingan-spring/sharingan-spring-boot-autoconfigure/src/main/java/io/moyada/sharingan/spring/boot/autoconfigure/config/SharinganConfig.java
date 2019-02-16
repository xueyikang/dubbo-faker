package io.moyada.sharingan.spring.boot.autoconfigure.config;

import io.moyada.sharingan.monitor.api.config.MonitorConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xueyikang
 * @since 1.0
 **/
@Component
@ConfigurationProperties(prefix = MonitorConfig.PREFIX)
public class SharinganConfig {

    // 启用
    private boolean enable = true;

    // 应用
    private String application;

    private String groupId;

    private String artifactId;

    private String monitorType = "Database";

    private String registerType = "Database";

    private Map<String, String> attach;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(String monitorType) {
        this.monitorType = monitorType;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public Map<String, String> getAttach() {
        return attach;
    }

    public void setAttach(Map<String, String> attach) {
        this.attach = attach;
    }
}
