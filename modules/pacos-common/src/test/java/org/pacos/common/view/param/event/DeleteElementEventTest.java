package org.pacos.common.view.param.event;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.pacos.common.view.param.GridParam;
import org.pacos.common.view.param.Param;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteElementEventTest {

    @Test
    void whenNoSelectedItemsThenDoNotModifyGridContent() {
        GridParam gridParam = new GridParam(List.of(new Param("key", "value", false)));
        //when
        DeleteElementEvent.fireEvent(gridParam);
        //then
        assertEquals(1, gridParam.getItems().size());
    }

    @Test
    void whenSelectedItemsThenNotModifyGridContent() {
        Param param = new Param("key", "value", false);
        GridParam gridParam = new GridParam(List.of(param));
        gridParam.select(param);
        //when
        DeleteElementEvent.fireEvent(gridParam);
        //then
        assertEquals(0, gridParam.getItems().size());
    }
}