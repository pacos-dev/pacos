package org.pacos.base.camunda;

import java.util.List;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.variablefield.data.Scope;

class TestExecutableBlock implements ExecutableBlock<TestBean> {

    private static final Logger LOG = LoggerFactory.getLogger(TestExecutableBlock.class);

    @Override
    public BlockMetadata basicData() {
        return new BlockMetadata() {

            @Override
            public String name() {
                return "test";
            }

            @Override
            public String camundaDelegateName() {
                return "testName";
            }

            @Override
            public String[] group() {
                return new String[] { "testGroup" };
            }

            @Override
            public ResultVariable[] resultVariables() {
                return new ResultVariable[0];
            }
        };
    }

    @Override
    public BlockFormHandler<TestBean> blockForm() {
        return new BlockFormHandler<TestBean>() {

            @Override
            public Binder<TestBean> createForm(VerticalLayout layout, List<Scope> scopes) {
                Binder<TestBean> binder = new Binder<>(TestBean.class);
                TextField nameField = new TextField("Name");
                IntegerField ageField = new IntegerField("ageField");
                layout.add(nameField);
                layout.add(ageField);
                binder.forField(nameField).bind(TestBean::getName, TestBean::setName);
                binder.forField(ageField).bind(TestBean::getAge, TestBean::setAge);
                return binder;
            }

            @Override
            public Class<TestBean> beanClas() {
                return TestBean.class;
            }

            @Override
            public TestBean writeBean(Binder binder) throws ValidationException {
                TestBean testBean = new TestBean();
                binder.writeBean(testBean);
                return testBean;
            }
        };
    }

    @Override
    public void execute(ProcessVariableManager processVariableManager, @Nullable TestBean bean, List<Scope> scopes) {
        LOG.debug("Executing {}", bean);
    }
}