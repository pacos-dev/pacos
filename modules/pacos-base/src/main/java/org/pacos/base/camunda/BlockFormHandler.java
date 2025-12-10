package org.pacos.base.camunda;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.pacos.base.utils.ObjectMapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.vaadin.addons.variablefield.data.Scope;

public interface BlockFormHandler<T> {

    Logger LOGGER = LoggerFactory.getLogger(BlockFormHandler.class);

    /**
     * Return class represents data model
     *
     * @return model class
     */
    Class<T> beanClas();

    /**
     * Create form which will be displayed on configuration page
     *
     * @param layout on which form component must be added
     * @return binder connected with all form fields
     */
    Binder<T> createForm(VerticalLayout layout, List<Scope> scopes);

    /**
     * Writer record/DTO on save action triggered from UI
     *
     * @throws ValidationException while form validation
     */
    T writeBean(Binder<?> binder) throws ValidationException;


    default Binder<T> createForm(VerticalLayout layoutComponent, String modelJson, List<Scope> scopes) {
        Optional<T> bean = readModel(modelJson);
        Binder<T> binder = createForm(layoutComponent, scopes);
        bean.ifPresent(binder::readBean);
        return binder;
    }


    default Optional<T> readModel(String modelJson) {
        if (StringUtils.hasLength(modelJson)) {
            try {
                if (!modelJson.equals("{}")) {
                    return Optional.of(ObjectMapperUtils.getMapper().readValue(modelJson, beanClas()));
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
        return Optional.empty();
    }

}
