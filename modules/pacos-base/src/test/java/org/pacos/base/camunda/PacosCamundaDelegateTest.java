package org.pacos.base.camunda;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.junit.jupiter.api.Test;
import org.pacos.base.utils.LoggerAppender;
import org.pacos.base.utils.ObjectMapperUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacosCamundaDelegateTest {

    @Test
    void whenGetNameThenReturnCamundaBlockName() {
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        assertEquals("test", testCouplerCamundaDelegate.basicData().name());
    }

    @Test
    void whenGetCamundaDelegateNameThenReturnUniqueDelegateName() {
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        assertEquals("testName", testCouplerCamundaDelegate.basicData().camundaDelegateName());
    }

    @Test
    void whenGetDisplayGroupThenReturnDefinedGroup() {
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        assertEquals("testGroup", testCouplerCamundaDelegate.basicData().group()[0]);
    }

    @Test
    void whenCreateFormForEmptyBeanJsonThenBuildFormWithoutException() {
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        VerticalLayout layoutComponent = new VerticalLayout();
        //when
        Binder<TestBean> binder = testCouplerCamundaDelegate.blockForm().createForm(layoutComponent, "", new ArrayList<>());
        //then
        assertNotNull(binder);
        assertEquals(2, binder.getFields().count());
    }

    @Test
    void whenCreateFormForEmptyBeanJsonThenFormFieldsIsNotFilled() {
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        VerticalLayout layoutComponent = new VerticalLayout();
        //when
        Binder<TestBean> binder = testCouplerCamundaDelegate.blockForm().createForm(layoutComponent, "", new ArrayList<>());
        //then
        assertNotNull(binder);
        assertEquals("", ((TextField) layoutComponent.getComponentAt(0)).getValue());
        assertEquals(null, ((IntegerField) layoutComponent.getComponentAt(1)).getValue());
    }

    @Test
    void whenCreateFormForEmptyJsonThenFormFieldsIsNotFilled() {
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        VerticalLayout layoutComponent = new VerticalLayout();
        //when
        Binder<TestBean> binder = testCouplerCamundaDelegate.blockForm().createForm(layoutComponent, "{}", new ArrayList<>());
        //then
        assertNotNull(binder);
        assertEquals("", ((TextField) layoutComponent.getComponentAt(0)).getValue());
        assertEquals(null, ((IntegerField) layoutComponent.getComponentAt(1)).getValue());
    }

    @Test
    void whenCreateFormFromJsonThenFormFieldsIsFilled() {
        //given
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        VerticalLayout layoutComponent = new VerticalLayout();
        TestBean testBean = new TestBean();
        testBean.setName("testName");
        testBean.setAge(10);
        String testBeanJson = ObjectMapperUtils.getMapper().writeValueAsString(testBean);
        //when

        Binder<TestBean> binder = testCouplerCamundaDelegate.blockForm().createForm(layoutComponent,
                testBeanJson,
                new ArrayList<>());
        //then
        assertNotNull(binder);
        assertEquals("testName", ((TextField) layoutComponent.getComponentAt(0)).getValue());
        assertEquals(10, ((IntegerField) layoutComponent.getComponentAt(1)).getValue());
    }

    @Test
    void whenErrorWhileReadingBeanThenLogExceptionWithoutThrowing() {
        //given
        LoggerAppender appender = new LoggerAppender(BlockFormHandler.class);
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        VerticalLayout layoutComponent = new VerticalLayout();
        //when

        assertDoesNotThrow(() -> testCouplerCamundaDelegate.blockForm().createForm(layoutComponent,
                "{\"name\":asd}",
                new ArrayList<>()));
        //then
        assertTrue(appender.getLogs().get(0).contains("Unrecognized token 'asd': "));
    }

    @Test
    void whenExecuteThenParseJson() throws Exception {
        TestBean testBean = new TestBean();
        testBean.setName("testName");
        testBean.setAge(10);
        String testBeanJson = ObjectMapperUtils.getMapper().writeValueAsString(testBean);
        LoggerAppender appender = new LoggerAppender(TestExecutableBlock.class);
        TestExecutableBlock testCouplerCamundaDelegate = new TestExecutableBlock();
        //when
        testCouplerCamundaDelegate.execute(null, List.of(),testBeanJson);
        //then
        assertEquals("Executing TestBean{name='testName', age=10}",appender.getLogs().get(0));
    }
}
