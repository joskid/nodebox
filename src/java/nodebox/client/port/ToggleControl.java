package nodebox.client.port;

import nodebox.node.Port;
import nodebox.ui.NButton;

import java.awt.*;

public class ToggleControl extends AbstractPortControl {

    //private JCheckBox checkBox;
    private NButton checkBox;

    public ToggleControl(Port port) {
        super(port);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        checkBox = new NButton(NButton.Mode.CHECK, port.getLabel());
        checkBox.setActionMethod(this, "toggle");
//        checkBox = new JCheckBox(port.getLabel());
//        checkBox.putClientProperty("JComponent.sizeVariant", "small");
//        checkBox.setOpaque(false);
//        checkBox.setPreferredSize(new Dimension(150, 18));
//        checkBox.setFont(Platform.getSmallFont());
        //checkBox.addActionListener(this);
        add(checkBox);
        setValueForControl(port.getValue());
        setPreferredSize(new Dimension(120, 30));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkBox.setEnabled(enabled);
    }

    public void setValueForControl(Object v) {
        if (v == null) return;
        boolean value = (Boolean) v;
        checkBox.setChecked(value);
    }

    public void toggle() {
        setPortValue(checkBox.isChecked());
    }

}
