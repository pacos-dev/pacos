package org.pacos.core.component.settings.view;

import org.pacos.base.component.setting.SettingTab;

/**
 * Represents menu item in settings grid menu
 */
record MenuNode(String name, SettingTab entity){

    public int getOrder(){
        if(entity==null){
            return 0;
        }
        return entity.getOrder();
    }
}