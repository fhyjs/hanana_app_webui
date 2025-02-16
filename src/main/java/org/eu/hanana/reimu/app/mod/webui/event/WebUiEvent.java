package org.eu.hanana.reimu.app.mod.webui.event;

import lombok.Getter;
import org.eu.hanana.reimu.webui.WebUi;

public abstract class WebUiEvent {
    @Getter
    private final WebUi webui;

    public WebUiEvent(WebUi webUi){
        this.webui=webUi;
    }
}
