package org.pacos.common.view.param.event;

import java.util.List;

import org.pacos.common.view.param.GridParam;
import org.pacos.common.view.param.Param;

public final class DeleteElementEvent {
    private DeleteElementEvent() {
    }

    public static void fireEvent(GridParam gridParam) {
        List<Param> paramList = gridParam.getItems();
        paramList.removeAll(gridParam.getSelectedItems());
        gridParam.modifyItems(paramList);
    }
}
