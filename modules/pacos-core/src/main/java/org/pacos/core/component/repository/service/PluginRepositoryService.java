package org.pacos.core.component.repository.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.repository.domain.PluginRepository;
import org.pacos.core.component.repository.repository.PluginRepositoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional("coreTransactionManager")
public class PluginRepositoryService {

    private static final String DEFAULT_REPOSITORY_NAME = "Default plugin repository";
    private final PluginRepositoryRepository repository;
    private final RegistryProxy registryProxy;

    public PluginRepositoryService(PluginRepositoryRepository repository, RegistryProxy registryProxy) {
        this.repository = repository;
        this.registryProxy = registryProxy;
    }

    public List<PluginRepository> findAll() {
        ensureDefaultRepository();
        return repository.findAllByOrderByNameAsc();
    }

    public PluginRepository getSelectedRepository() {
        ensureDefaultRepository();
        Optional<Long> selectedId = registryProxy.readRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID)
                .flatMap(PluginRepositoryService::parseId);
        if (selectedId.isPresent()) {
            Optional<PluginRepository> selected = repository.findById(selectedId.get());
            if (selected.isPresent()) {
                return selected.get();
            }
        }

        PluginRepository fallback = repository.findAllByOrderByNameAsc().get(0);
        storeSelectedRepository(fallback);
        return fallback;
    }

    public AppRepository getSelectedAppRepository() {
        PluginRepository selected = getSelectedRepository();
        return toAppRepository(selected);
    }

    public AppRepository toAppRepository(PluginRepository pluginRepository) {
        if (pluginRepository == null || pluginRepository.getId() == null) {
            throw new IllegalArgumentException("A persisted plugin repository is required");
        }
        return new AppRepository("pacos-plugin-repo-" + pluginRepository.getId(), pluginRepository.getUrl());
    }

    public PluginRepository addRepository(String name, String url) {
        String normalizedName = normalizeName(name);
        String normalizedUrl = normalizeUrl(url);
        ensureDefaultRepository();
        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("A repository with this name already exists");
        }
        if (repository.existsByUrlIgnoreCase(normalizedUrl)) {
            throw new IllegalArgumentException("A repository with this URL already exists");
        }
        PluginRepository saved = repository.save(new PluginRepository(normalizedName, normalizedUrl));
        storeSelectedRepository(saved);
        return saved;
    }

    public void selectRepository(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Repository ID is required");
        }
        PluginRepository selected = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found"));
        storeSelectedRepository(selected);
    }

    public void deleteRepository(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Repository ID is required");
        }
        PluginRepository toDelete = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found"));
        if (repository.count() <= 1) {
            throw new IllegalStateException("At least one plugin repository must remain configured");
        }

        PluginRepository selected = getSelectedRepository();
        if (selected.getId().equals(toDelete.getId())) {
            PluginRepository replacement = repository.findAllByOrderByNameAsc().stream()
                    .filter(candidate -> !candidate.getId().equals(toDelete.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No replacement plugin repository is available"));
            storeSelectedRepository(replacement);
        }
        repository.delete(toDelete);
    }

    private void ensureDefaultRepository() {
        if (repository.count() != 0) {
            return;
        }
        AppRepository configuredRepository = AppRepository.pluginRepo();
        String configuredUrl = normalizeUrl(configuredRepository.url());
        PluginRepository saved = repository.save(
                new PluginRepository(DEFAULT_REPOSITORY_NAME, configuredUrl));
        storeSelectedRepository(saved);
    }

    private void storeSelectedRepository(PluginRepository selected) {
        registryProxy.saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, selected.getId().toString());
    }

    private static Optional<Long> parseId(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Repository name is required");
        }
        String normalized = name.trim();
        if (normalized.length() > 120) {
            throw new IllegalArgumentException("Repository name must not exceed 120 characters");
        }
        return normalized;
    }

    static String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Repository URL is required");
        }
        String normalized = url.trim().replaceAll("/+$", "");
        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Repository URL is invalid", e);
        }
        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || uri.getHost() == null
                || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("Repository URL must be an absolute HTTP(S) URL without credentials, query or fragment");
        }
        if (normalized.length() > 2048) {
            throw new IllegalArgumentException("Repository URL must not exceed 2048 characters");
        }
        return normalized;
    }
}
