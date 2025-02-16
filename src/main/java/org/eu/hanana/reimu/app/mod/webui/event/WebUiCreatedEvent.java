package org.eu.hanana.reimu.app.mod.webui.event;

import org.eu.hanana.reimu.webui.WebUi;

public class WebUiCreatedEvent extends WebUiEvent {
    public final int port;
    public WebUiCreatedEvent(WebUi webUi, int port) {
        super(webUi);
        this.port = port;
    }
}
