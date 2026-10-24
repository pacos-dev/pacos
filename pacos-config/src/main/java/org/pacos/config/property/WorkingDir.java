package org.pacos.config.property;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkingDir {

    private WorkingDir() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(WorkingDir.class);
    private static final Pattern MODULE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]*");

    public static void initialize() {
        LOG.info("Initializing working dir");
        String workingDirPropertyValue = System.getProperty("-D" + PropertyName.WORKING_DIR.getPropertyName());
        if (workingDirPropertyValue == null) {
            LOG.debug("No property '-D{}' found", PropertyName.WORKING_DIR.getPropertyName());
            workingDirPropertyValue = WorkingDir.load().toString();
        }
        System.setProperty(PropertyName.WORKING_DIR.getPropertyName(), workingDirPropertyValue);

        LOG.info("Working dir set to: {}", workingDirPropertyValue);

        Path workingDir = Path.of(workingDirPropertyValue);
        if (workingDir.toFile().exists() && !workingDir.toFile().isDirectory()) {
            throw new WorkingDirInitializationException("Working dir can't be a regular file " + workingDir);
        }

        if (!workingDir.toFile().exists() && !workingDir.toFile().mkdirs()) {
            throw new WorkingDirInitializationException("Can't create working directory in location " + workingDir);
        }

        if (!workingDir.resolve("lib").toFile().exists() && !workingDir.resolve("lib").toFile().mkdirs()) {
            throw new WorkingDirInitializationException("Can't create mandatory directory in location " + workingDir);
        }
        System.setProperty("workingDir", workingDirPropertyValue);

    }

    public static Path load() {
        String workingDir = System.getProperty(PropertyName.WORKING_DIR.getPropertyName());
        if (workingDir == null) {
            LOG.warn("No property '-D{}' found. Set default value.", PropertyName.WORKING_DIR.getPropertyName());
        }
        Path path;
        if (workingDir == null) {
            path = getDefaultLocation();
        } else {
            path = Path.of(workingDir);
        }
        return path.normalize();
    }

    public static Path getLibPath() {
        return load().resolve("lib");
    }

    public static Path getModulePath(String moduleName) {
        if (moduleName == null || !MODULE_NAME.matcher(moduleName).matches()) {
            throw new IllegalArgumentException("Invalid module name; path separators are not allowed");
        }
        Path modulesRoot = load().resolve("module").normalize();
        Path modulePath = modulesRoot.resolve(moduleName).normalize();
        if (!modulePath.startsWith(modulesRoot)) {
            throw new IllegalArgumentException("Module path escapes the PacOS working directory");
        }
        return modulePath;
    }

    private static Path getDefaultLocation() {
        String os = System.getProperty("os.name").toLowerCase();

        String defaultDirName = ".pacos";
        if (os.contains("win")) {
            Path homeDir = Path.of(System.getProperty("user.home"));
            return homeDir.resolve(defaultDirName);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return Path.of("/opt").resolve(defaultDirName);
        } else {
            return Path.of("/usr").resolve("local").resolve(defaultDirName);
        }
    }


}
