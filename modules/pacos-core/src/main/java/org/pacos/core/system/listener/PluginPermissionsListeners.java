package org.pacos.core.system.listener;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.pacos.base.exception.PacosException;
import org.pacos.base.listener.PluginListener;
import org.pacos.base.security.Permission;
import org.pacos.core.component.security.service.PermissionService;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * This listener collect all available permissions for which access can be limited
 */
@Component
public class PluginPermissionsListeners implements PluginListener {

    private final PermissionService permissionService;

    public PluginPermissionsListeners(ApplicationContext applicationContext, PermissionService permissionService) {
        this.permissionService = permissionService;
        pluginInitialized(applicationContext);
        loadPermissions(applicationContext);
    }

    @Override
    public void pluginInitialized(ApplicationContext context) {
        Set<Permission> permissions = loadPermissions(context);
        permissionService.resolvePermissionDefinition(permissions);
    }

    @Override
    public void pluginRemoved(ApplicationContext context) {
        //no implementation needed
    }

    @NotNull
    static Set<Permission> loadPermissions(ApplicationContext context) {
        Set<Permission> permissions = new HashSet<>();

        ClassLoader cl = context.getClassLoader();
        if (cl == null) {
            throw new PacosException("Class loader is null");
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);

        try {
            Resource[] resources = resolver.getResources("classpath*:org/pacos/**/*.class");

            for (Resource resource : resources) {
                String className = toClassName(resource);
                Class<?> clazz;

                try {
                    clazz = cl.loadClass(className);
                } catch (ClassNotFoundException e) {
                    continue;
                }

                if (clazz.isEnum() && Permission.class.isAssignableFrom(clazz)) {
                    Permission[] enumValues = (Permission[]) clazz.getEnumConstants();
                    permissions.addAll(Arrays.asList(enumValues));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Cannot scan permissions", e);
        }

        return permissions;
    }

    private static String toClassName(Resource resource) throws IOException {
        String path = resource.getURL().getPath();

        // load parts for.../classes/ lub z JAR-a
        if (path.contains("!")) {
            // for JAR: file:/.../module.jar!/org/pacos/X.class
            path = path.substring(path.indexOf("!") + 2);
        } else {
            // for directory
            path = path.substring(path.indexOf("classes/") + "classes/".length());
        }

        return path.replace("/", ".").replace(".class", "");
    }

}
