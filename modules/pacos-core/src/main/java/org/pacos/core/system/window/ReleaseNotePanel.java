package org.pacos.core.system.window;

import java.util.List;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.base.file.ReadResourceFile;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.system.window.config.ReleaseNoteConfig;
import org.springframework.context.annotation.Scope;

@Scope("prototype")
public class ReleaseNotePanel extends DesktopWindow {

    public ReleaseNotePanel(ReleaseNoteConfig moduleConfig) {
        super(moduleConfig);
        allowCloseOnEsc();
        allowCloseOnOutsideClick();
        setSize(950, 380);
        getWindowHeader().getRight().removeAll();
        setResizable(true);
        setModal(true);
        final List<String> content = ReadResourceFile.readFile("release_note.txt");
        String headerDetails = null;
        VerticalLayout detailsContent = new VerticalLayout();
        boolean firstAdded = false;

        for (String line : content) {
            if (!line.startsWith(" ")) {
                if (headerDetails != null) {
                    final Details details = new Details(headerDetails, detailsContent);
                    if (!firstAdded) {
                        details.setOpened(true);
                        firstAdded = true;
                    }
                    add(details);
                }
                headerDetails = line;
                detailsContent = new VerticalLayout();
                continue;
            }
            final Pre pre = new Pre(line);
            pre.getElement().getClassList().add("rinfo");
            detailsContent.add(pre);
        }

        add(new Details(headerDetails, detailsContent));
    }

}
