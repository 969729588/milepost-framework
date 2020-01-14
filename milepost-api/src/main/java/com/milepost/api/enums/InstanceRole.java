package com.milepost.api.enums;

/**
 * Created by Ruifu Hua on 2019/12/31.
 */
public enum InstanceRole {

    MASTER("master"),
    SLAVE("slave");

    private final String value;

    InstanceRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
