package nodebox.client.port;

import nodebox.client.NButton;
import nodebox.node.Port;

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
//        checkBox.setFont(PlatformUtils.getSmallFont());
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
        int value = (Integer) v;
        checkBox.setChecked(value == 1);
        //checkBox.setSelected(value == 1);
    }

    public void toggle() {
        port.setValue(checkBox.isChecked() ? 1 : 0);

    }

}
