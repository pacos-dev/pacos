package org.pacos.config.repository.data;

import org.junit.jupiter.api.Test;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.PropertyName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AppRepositoryTest {

    @Test
    void whenUrlIdIsNotEqualThenObjectIsNotEqual(){
        AppRepository repo1 = new AppRepository("123","URL");
        AppRepository repo2 = new AppRepository("321","URL");

        assertNotEquals(repo1,repo2);
        assertNotEquals(repo1.hashCode(),repo2.hashCode());
    }

    @Test
    void whenUrlIsNotEqualThenObjectIsNotEqual(){
        AppRepository repo1 = new AppRepository("123","URL");
        AppRepository repo2 = new AppRepository("123","LUR");

        assertNotEquals(repo1,repo2);
        assertNotEquals(repo1.hashCode(),repo2.hashCode());
    }

    @Test
    void whenUrlAndIdIsEqualThenObjectIsEqual(){
        AppRepository repo1 = new AppRepository("123","URL");
        AppRepository repo2 = new AppRepository("123","URL");

        assertEquals(repo1,repo2);
        assertEquals(repo1.hashCode(),repo2.hashCode());
    }


    @Test
    void whenToStringThenReturnFormattedValue(){
        AppRepository repo1 = new AppRepository("123","URL");
        assertEquals("AppRepository{name='123', url='URL'}",repo1.toString());
    }

    @Test
    void whenGetModuleRepoThenReturnConfiguredRepository(){
        System.setProperty(PropertyName.MODULE_LIST_REPO_URL.getPropertyName(), "test_url");
        ApplicationProperties.reloadProperties();
        //when
        AppRepository repository = AppRepository.moduleRepo();
        //then
        assertEquals("test_url",repository.url());
    }

    @Test
    void whenGetPluginRepoThenReturnConfiguredRepository(){
        System.setProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), "test_url2");
        ApplicationProperties.reloadProperties();
        //when
        AppRepository repository = AppRepository.pluginRepo();
        //then
        assertEquals("test_url2",repository.url());
    }
}