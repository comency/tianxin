package com.tianxin.platform.system.model;

public record DictionaryItem(String typeCode, String itemCode, String label, String value, int sortOrder,
                             boolean enabled) {
}
