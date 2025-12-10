package org.pacos.core.component.variable.view.global;

import java.util.Objects;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import org.pacos.base.component.editor.ContentMode;
import org.pacos.base.component.editor.NativeCodeMirror;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.generator.DynamicJavaScriptException;
import org.pacos.core.component.variable.generator.DynamicJavaScriptRunner;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

public class VariableForm extends VerticalLayout {

    private final Binder<SystemVariableDTO> binder;
    private final SystemVariableDTO dto;

    public VariableForm(GlobalVariableSystem system, SystemVariableDTO dto, TabMonitoring tab) {
        this.binder = new Binder<>(SystemVariableDTO.class);
        this.dto = dto;
        TextFieldUtils nameField = new TextFieldUtils("Variable name");
        binder.forField(nameField)
                .withValidator(new AbstractValidator<>("Variable with this name already exists") {
                    @Override
                    public ValidationResult apply(String s, ValueContext valueContext) {
                        if (system.getSystemVariableProxy().isUnique(dto.getId(), nameField.getValue())) {
                            return ValidationResult.ok();
                        }
                        return ValidationResult.error("This name is already in use");
                    }
                })
                .asRequired()
                .withValidator(new RegexpValidator("Name must start with $ and can't contains white " +
                        "characters", "^(\\$)\\S*"))
                .bind(SystemVariableDTO::getName, SystemVariableDTO::setName);

        TextFieldUtils descriptionField = new TextFieldUtils("Description");
        binder.forField(descriptionField).bind(SystemVariableDTO::getDescription, SystemVariableDTO::setDescription);


        NativeCodeMirror transformClass = new NativeCodeMirror().withLanguage(ContentMode.JAVASCRIPT);
        binder.forField(transformClass)
                .bind(SystemVariableDTO::getJs, SystemVariableDTO::setJs);

        add(new FormLayout(nameField, descriptionField));
        add(transformClass);
        add(new Hr());

        TextFieldUtils generationResult = new TextFieldUtils();
        generationResult.setEnabled(false);

        ButtonUtils testBtn = new ButtonUtils("Test").primaryLayout().withClickListener(e -> {
            try {
                generationResult.setValue(DynamicJavaScriptRunner.runCode(transformClass.getValue()));
            } catch (DynamicJavaScriptException error) {
                NotificationUtils.error(error.getMessage());
            }
        });
        final FormLayout testLayout = new FormLayout(generationResult, testBtn);
        add(testLayout);

        binder.readBean(dto);
        binder.addValueChangeListener(changed -> {
            SystemVariableDTO draft = new SystemVariableDTO();
            binder.writeBeanAsDraft(draft);
            boolean noChanges = Objects.equals(draft.getName(), dto.getName())
                    && Objects.equals(draft.getDescription(), dto.getDescription())
                    && Objects.equals(draft.getJs(), dto.getJs());

            tab.markChanges(!noChanges);
        });

    }

    public boolean isValid() {
        return binder.isValid();
    }

    public SystemVariableDTO getBean() {
        binder.writeBeanIfValid(dto);
        return dto;
    }

}
