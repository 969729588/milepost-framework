package com.milepost.api.enums;

/**
 * Created by Ruifu Hua on 2019/12/30.
 *
 * 应用类型
 */
public enum MilepostApplicationType {

    UI("UI"),
    SERVICE("SERVICE"),
    SINGLE_BOOT("SINGLE_BOOT"),
    EUREKA("EUREKA"),
    AUTH("AUTH"),
    ADMIN("ADMIN"),
    TURBINE("TURBINE");

    private final String value;

    MilepostApplicationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
