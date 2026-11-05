package org.pacos.core.component.plugin.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.pacos.core.component.plugin.dto.PluginDTO;

final class PluginLifecycleLock {

    private final ConcurrentMap<PluginKey, LockReference> locks = new ConcurrentHashMap<>();

    <T> T execute(PluginDTO plugin, Supplier<T> operation) {
        PluginKey key = PluginKey.from(plugin);
        LockReference lock = locks.compute(key, (ignored, current) -> {
            LockReference selected = current == null ? new LockReference() : current;
            selected.references++;
            return selected;
        });
        lock.lock.lock();
        try {
            return operation.get();
        } finally {
            lock.lock.unlock();
            locks.computeIfPresent(key, (ignored, current) -> {
                current.references--;
                return current.references == 0 ? null : current;
            });
        }
    }

    int activeLockCount() {
        return locks.size();
    }

    private static final class LockReference {
        private final ReentrantLock lock = new ReentrantLock();
        private int references;
    }

    private record PluginKey(String groupId, String artifactName, String version) {
        private static PluginKey from(PluginDTO plugin) {
            return new PluginKey(plugin.getGroupId(), plugin.getArtifactName(), plugin.getVersion());
        }
    }
}
