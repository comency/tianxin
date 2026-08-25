package com.tianxin.platform.system.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogStore {

    private static final int MAX_ENTRIES = 1_000;
    private final ConcurrentLinkedDeque<AuditLog> entries = new ConcurrentLinkedDeque<>();

    public void append(AuditLog log) {
        entries.addFirst(log);
        while (entries.size() > MAX_ENTRIES) {
            entries.pollLast();
        }
    }

    public List<AuditLog> list() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }
}
