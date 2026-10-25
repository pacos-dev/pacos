package org.pacos.core.component.plugin.view.tab;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.streams.UploadHandler;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.session.UserSession;
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
    public static final String INFO = "Upload custom plugin. The source code of example skeleton " +
            "plugin is available here ";
    public static final String SKELETON_APP = "skeleton-app";
    public static final String UPLOAD_PLUGIN = "Upload plugin";
    public static final String PLUGIN_NAME = "Plugin name";
    public static final String AUTHOR = "Author";
    public static final String ARTIFACT_NAME = "Artifact name";
    public static final String GROUP_ID = "GroupID";
    public static final String VERSION = "Version";

    private final Binder<PluginDTO> binder = new Binder<>();
    private final Button installBtn = new Button("Install plugin");

    private final transient PluginProxy pluginProxy;
    private final Upload singleFileUpload;
    @SuppressWarnings("java:S1948")
    private final PluginManager pluginManager;
    private transient UploadedPluginInfo pluginInfo;

    public InstallPluginContentLayout(PluginManager pluginManager, PluginProxy pluginProxy) {
        this.pluginProxy = pluginProxy;
        this.pluginManager = pluginManager;
        this.setSizeFull();
        InfoBox infoBox = new InfoBox(INFO);
        Anchor anchor = new Anchor("https://github.com/pacos-dev/skeleton",
                SKELETON_APP);
        anchor.setTarget("_blank");
        anchor.getStyle().set("color", "blue");
        anchor.getStyle().set("margin-left", "5px");
        infoBox.add(anchor);
        add(infoBox);
        this.singleFileUpload = createUploadField();
        singleFileUpload.setWidthFull();
        singleFileUpload.setEnabled(hasInstallPermission());
        singleFileUpload.addFileRemovedListener(event -> {
            pluginInfo = null;
            binder.readBean(new PluginDTO());
            installBtn.setEnabled(false);
        });
        Button upload = new ButtonUtils(UPLOAD_PLUGIN).withEnabledForPermission(PluginPermissions.INSTALL_PLUGIN);
        singleFileUpload.setUploadButton(upload);
        add(singleFileUpload);

        TextField name = new TextField(PLUGIN_NAME);
        name.setReadOnly(true);
        binder.forField(name)
                .bind(PluginDTO::getName, PluginDTO::setName);

        TextField author = new TextField(AUTHOR);
        author.setReadOnly(true);
        binder.forField(author)
                .bind(PluginDTO::getAuthor, PluginDTO::setAuthor);

        TextField artifactName = new TextField(ARTIFACT_NAME);
        artifactName.setReadOnly(true);
        binder.forField(artifactName)
                .bind(PluginDTO::getArtifactName, PluginDTO::setArtifactName);

        TextField groupId = new TextField(GROUP_ID);
        groupId.setReadOnly(true);
        binder.forField(groupId)
                .bind(PluginDTO::getGroupId, PluginDTO::setGroupId);

        TextField version = new TextField(VERSION);
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

    public static String getSearchIndex() {
        return INFO + SKELETON_APP + UPLOAD_PLUGIN + PLUGIN_NAME + AUTHOR + ARTIFACT_NAME + GROUP_ID + VERSION;
    }

    void installUploadedLibrary() {
        if (!requireInstallPermission()) {
            installBtn.setEnabled(false);
            return;
        }
        if (pluginInfo == null) {
            installBtn.setEnabled(false);
            return;
        }

        try {
            pluginProxy.getPluginInstallService().storePluginFile(pluginInfo);
            pluginProxy.getPluginInstallService().savePlugin(pluginInfo.pluginDTO());
            pluginManager.startPlugin(pluginInfo.pluginDTO());
            ServiceListener.notifyAll(ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, new PluginDownloadState(pluginInfo.pluginDTO(),
                    DownloadPluginStatus.FINISHED));
            binder.readBean(new PluginDTO());
            pluginInfo = null;
            singleFileUpload.getElement().executeJs("this.files = []; this._files = [];");
            installBtn.setEnabled(false);
            NotificationUtils.success("Plugin installed.");
        } catch (Exception e) {
            NotificationUtils.error(e.getMessage());
        }
    }

    private Upload createUploadField() {
        UploadHandler uploadHandler = UploadHandler.inMemory((metadata, fileData) ->
                uploadFinish(metadata.fileName(), fileData));
        Upload uploader = new Upload(uploadHandler);
        uploader.setDropAllowed(true);
        uploader.setAutoUpload(true);
        uploader.setMaxFiles(1);
        return uploader;
    }

    void uploadFinish(String fileName, byte[] fileData) {
        if (!requireInstallPermission()) {
            pluginInfo = null;
            installBtn.setEnabled(false);
            return;
        }

        try {
            PluginDTO pluginDTO = PluginMetaInfoReader.read(fileName, fileData);
            this.pluginInfo = new UploadedPluginInfo(pluginDTO, fileData, fileName);
            binder.readBean(pluginDTO);
            installBtn.setEnabled(hasInstallPermission());
        } catch (Exception e) {
            pluginInfo = null;
            installBtn.setEnabled(false);
            NotificationUtils.error("Can't read given file or file is not a jar plugin file");
        }
    }

    private boolean hasInstallPermission() {
        UserSession userSession = UserSession.getCurrent();
        return userSession != null && userSession.hasPermission(PluginPermissions.INSTALL_PLUGIN);
    }

    private boolean requireInstallPermission() {
        if (hasInstallPermission()) {
            return true;
        }
        NotificationUtils.info("You don't have permission to install plugins");
        return false;
    }
}
