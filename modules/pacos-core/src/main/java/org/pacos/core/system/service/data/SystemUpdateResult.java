package org.pacos.core.system.service.data;

public record SystemUpdateResult(String previousVersion, String currentVersion) {

    public boolean isToUpdate() {
        return previousVersion != null && currentVersion != null && !currentVersion.equals(previousVersion);
    }

    public String updateResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("System update result: \n");
        if (isToUpdate()) {
            sb.append(String.format("\tUpdated from version='%s' to version='%s'%n", previousVersion(), currentVersion()));
        } else {
            sb.append("\tNo updates");
        }
        return sb.toString();
    }
}
