package nodebox.client.port;

import nodebox.client.DraggableNumber;
import nodebox.client.port.AbstractPortControl;
import nodebox.node.Port;
import nodebox.node.PortAttributes;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

public class IntControl extends AbstractPortControl implements ChangeListener, ActionListener {

    private DraggableNumber draggable;

    public IntControl(Port port) {
        super(port);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        draggable = new DraggableNumber();
        draggable.addChangeListener(this);
        NumberFormat intFormat = NumberFormat.getNumberInstance();
        intFormat.setMinimumFractionDigits(0);
        intFormat.setMaximumFractionDigits(0);
        draggable.setNumberFormat(intFormat);
        // Set bounding
        if (port.getBoundingMethod() == PortAttributes.BoundingMethod.HARD) {
            Float minimumValue = port.getMinimumValue();
            if (minimumValue != null)
                draggable.setMinimumValue(minimumValue);
            Float maximumValue = port.getMaximumValue();
            if (maximumValue != null)
                draggable.setMaximumValue(maximumValue);
        }
        add(draggable);
        setPreferredSize(draggable.getPreferredSize());
        setValueForControl(port.getValue());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        draggable.setEnabled(enabled);
    }

    public void setValueForControl(Object v) {
        int value = (Integer) v;
        draggable.setValue(value);
    }

    public void stateChanged(ChangeEvent e) {
        setValueFromControl();
    }

    public void actionPerformed(ActionEvent e) {
        setValueFromControl();
    }

    private void setValueFromControl() {
        double doubleValue = draggable.getValue();
        if (port.getBoundingMethod() == PortAttributes.BoundingMethod.HARD) {
            if (port.getMinimumValue() != null) {
                doubleValue = Math.max(port.getMinimumValue(), doubleValue);
            }
            if (port.getMaximumValue() != null) {
                doubleValue = Math.min(port.getMaximumValue(), doubleValue);
            }
        }
        int intValue = (int) doubleValue;
        if (intValue != port.asInt()) {
            port.setValue(intValue);
        }
    }

}
