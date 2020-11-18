package com.simpledata.frame.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/***
 * simple-data1.0 配置文件提示
 * @author Jiuchen
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "simple-data")
public class SimpleDataProperty {
    private String entity_package;
    private String vo_package;
    private String controller_package;

    public String getEntity_package() {
        return entity_package;
    }

    public void setEntity_package(String entity_package) {
        this.entity_package = entity_package;
    }

    public String getVo_package() {
        return vo_package;
    }

    public void setVo_package(String vo_package) {
        this.vo_package = vo_package;
    }

    public String getController_package() {
        return controller_package;
    }

    public void setController_package(String controller_package) {
        this.controller_package = controller_package;
    }

    @Override
    public String toString() {
        return "SimpleDataProperty{" +
                "entity_package='" + entity_package + '\'' +
                ", vo_package='" + vo_package + '\'' +
                ", controller_package='" + controller_package + '\'' +
                '}';
    }
}