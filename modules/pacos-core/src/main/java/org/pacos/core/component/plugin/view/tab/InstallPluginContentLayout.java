package org.pacos.core.component.plugin.view.tab;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.plugin.PluginPermissions;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginDownloadState;
import org.pacos.core.component.plugin.service.PluginMetaInfoReader;
import org.pacos.core.component.plugin.service.UploadedPluginInfo;
import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;
import org.pacos.core.component.session.service.ServiceListener;

public class InstallPluginContentLayout extends VerticalLayout {
    private final Binder<PluginDTO> binder = new Binder<>();
    private final Button installBtn = new Button("Install plugin");

    private final transient PluginProxy pluginProxy;
    private final Upload singleFileUpload;
    private final PluginManager pluginManager;
    private transient UploadedPluginInfo pluginInfo;

    public InstallPluginContentLayout(PluginManager pluginManager, PluginProxy pluginProxy) {
        this.pluginProxy = pluginProxy;
        this.pluginManager = pluginManager;
        this.setSizeFull();
        InfoBox infoBox = new InfoBox("Upload custom plugin. The source code of example skeleton plugin is available here ");
        Anchor anchor = new Anchor("https://github.com/pacos-dev/skeleton",
                "skeleton-app");
        anchor.setTarget("_blank");
        anchor.getStyle().set("color", "blue");
        anchor.getStyle().set("margin-left", "5px");
        infoBox.add(anchor);
        add(infoBox);
        this.singleFileUpload = createUploadField();
        singleFileUpload.setWidthFull();
        singleFileUpload.addFileRemovedListener(event -> {
            binder.readBean(new PluginDTO());
            installBtn.setEnabled(false);
        });
        Button upload = new ButtonUtils("Upload plugin").withEnabledForPermission(PluginPermissions.INSTALL_PLUGIN);
        singleFileUpload.setUploadButton(upload);
        add(singleFileUpload);




        TextField name = new TextField("Plugin name");
        name.setReadOnly(true);
        binder.forField(name)
                .bind(PluginDTO::getName, PluginDTO::setName);

        TextField author = new TextField("Author");
        author.setReadOnly(true);
        binder.forField(author)
                .bind(PluginDTO::getAuthor, PluginDTO::setAuthor);

        TextField artifactName = new TextField("Artifact name");
        artifactName.setReadOnly(true);
        binder.forField(artifactName)
                .bind(PluginDTO::getArtifactName, PluginDTO::setArtifactName);

        TextField groupId = new TextField("GroupID");
        groupId.setReadOnly(true);
        binder.forField(groupId)
                .bind(PluginDTO::getGroupId, PluginDTO::setGroupId);

        TextField version = new TextField("Version");
        version.setReadOnly(true);
        binder.forField(version)
                .bind(PluginDTO::getVersion, PluginDTO::setVersion);

        FormLayout pluginLayout = new FormLayout();
        pluginLayout.add(name, author, artifactName, groupId, version);

        installBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        installBtn.setEnabled(false);
        installBtn.addClickListener(e -> installUploadedLibrary());
        pluginLayout.add(installBtn);

        add(pluginLayout);
    }

    private void installUploadedLibrary() {
        try {
            pluginProxy.getPluginInstallService().storePluginFile(pluginInfo);
            pluginProxy.getPluginInstallService().savePlugin(pluginInfo.pluginDTO());
            pluginManager.startPlugin(pluginInfo.pluginDTO());
            ServiceListener.notifyAll(ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, new PluginDownloadState(pluginInfo.pluginDTO(),
                    DownloadPluginStatus.FINISHED));
            binder.readBean(new PluginDTO());
            singleFileUpload.clearFileList();
            installBtn.setEnabled(false);
            NotificationUtils.success("Plugin installed.");
        } catch (Exception e) {
            NotificationUtils.error(e.getMessage());
        }
    }

    private Upload createUploadField() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        Upload uploader = new Upload(memoryBuffer);
        uploader.setDropAllowed(true);
        uploader.setAutoUpload(true);
        uploader.setMaxFiles(1);
        uploader.addSucceededListener(event -> uploadFinish(memoryBuffer, event));
        return uploader;
    }

    void uploadFinish(MemoryBuffer memoryBuffer, SucceededEvent event) {
        try (InputStream inputStream = memoryBuffer.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);

            byte[] fileData = outputStream.toByteArray();

            String fileName = event.getFileName();

            PluginDTO pluginDTO = PluginMetaInfoReader.read(fileName, fileData);
            this.pluginInfo = new UploadedPluginInfo(pluginDTO, fileData, fileName);
            binder.readBean(pluginDTO);
            installBtn.setEnabled(true);
        } catch (Exception e) {
            installBtn.setEnabled(false);
            NotificationUtils.error("Can't read given file or file is not a jar plugin file");
        }
    }

}
