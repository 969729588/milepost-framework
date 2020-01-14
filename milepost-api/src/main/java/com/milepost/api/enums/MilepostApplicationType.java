package com.milepost.api.enums;

/**
 * Created by Ruifu Hua on 2019/12/30.
 *
 * 应用类型
 */
public enum MilepostApplicationType {

    UI("UI"),
    SERVICE("SERVICE"),
    EUREKA("EUREKA"),
    AUTH("AUTH");

    private final String value;

    MilepostApplicationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
