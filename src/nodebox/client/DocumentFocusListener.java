package nodebox.client;

import nodebox.node.Macro;
import nodebox.node.Node;

import java.util.EventListener;

public interface DocumentFocusListener extends EventListener {

    public void currentMacroChanged(Macro macro);

    public void focusedNodeChanged(Node node);
}
