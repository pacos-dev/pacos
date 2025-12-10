package org.pacos.core.config.worker;

import java.util.Optional;

import org.pacos.core.system.event.RestartSystemEvent;
import org.pacos.core.system.service.AutoUpdateService;
import org.pacos.core.system.service.data.SystemUpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Starts a background task that checks the latest version on the pacos repository side.
 * In case of any changes on plugin/system side, the system will download the new library,
 * prepare configuration and restart the system
 */
@Service
public class UpdateCheckerWorker {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateCheckerWorker.class);
    private final AutoUpdateService autoUpdateService;

    public UpdateCheckerWorker(AutoUpdateService autoUpdateService) {
        this.autoUpdateService = autoUpdateService;
    }

    @Scheduled(cron = "0 0 2 * * *")// set to 2 o clock
    public void runCheckTask() {
        LOG.info("Start auto update task");
        autoUpdateService.updatePlugins(false);
        Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(false);
        if (result.isPresent() && result.get().isToUpdate()) {
            RestartSystemEvent.fireEvent();
        }
    }
}
