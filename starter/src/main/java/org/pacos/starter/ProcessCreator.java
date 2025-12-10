package org.pacos.starter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pacos.config.property.WorkingDir;

public class ProcessCreator {
    private ProcessCreator() {
    }

    public static ProcessBuilder build(String javaHome, List<Path> modules, Path jarPath, List<String> args) {
        StringBuilder classpath = new StringBuilder();

        for (Path path : modules) {
            classpath.append(path.toString()).append(",");
        }

        List<String> commands = new ArrayList<>();

        commands.add(javaHome + "/bin/java");
        commands.add("-DworkingDir=" + WorkingDir.load());

        String javaOpts = System.getenv("JAVA_OPTS");
        if (javaOpts != null) {
            String[] optsArray = javaOpts.split("\\s+");
            commands.addAll(Arrays.asList(optsArray));
        }

        String profiles = getProfile(args);
        commands.add("-Dspring.profiles.active=" + (profiles != null ? profiles : "prod"));

        if (!classpath.isEmpty()) {
            commands.add("-Dloader.path=" + classpath);
        }

        for (String arg : args) {
            if (arg.startsWith("-D")) {
                commands.add(arg);
            } else {
                commands.add("-D" + arg);
            }
        }

        commands.add("-jar");
        commands.add(jarPath.toString());
        commands.add("org.springframework.boot.loader.launch.PropertiesLauncher");

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(WorkingDir.load().toFile());

        // pass all env variable
        Map<String, String> env = processBuilder.environment();
        env.putAll(System.getenv());
        return processBuilder;
    }

    static String getProfile(List<String> args) {
        Optional<String> profile = args.stream().filter(c -> c.startsWith("-Dspring.profiles.active=")).findFirst();
        if (profile.isPresent()) {
            String profileValue = profile.get().substring(25);
            if (!profileValue.isBlank()) {
                return profileValue;
            }
        }
        return null;
    }
}
