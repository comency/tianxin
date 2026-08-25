package com.tianxin.platform.enterprise;

public enum EnterpriseCategory {
    UPSTREAM("原材料供应商"),
    MANUFACTURER("生产制造企业"),
    CONSTRUCTION("施工安装企业"),
    OWNER("工程甲方");

    private final String displayName;

    EnterpriseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
