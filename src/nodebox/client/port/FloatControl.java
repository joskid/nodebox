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

public class FloatControl extends AbstractPortControl implements ChangeListener, ActionListener {

    private DraggableNumber draggable;

    public FloatControl(Port port) {
        super(port);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        draggable = new DraggableNumber();
        draggable.addChangeListener(this);
        setPreferredSize(draggable.getPreferredSize());
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
        setValueForControl(port.getValue());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        draggable.setEnabled(enabled);
    }

    public void setValueForControl(Object v) {
        Float value = (Float) v;
        draggable.setValue(value);
    }

    public void stateChanged(ChangeEvent e) {
        setValueFromControl();
    }

    public void actionPerformed(ActionEvent e) {
        setValueFromControl();
    }

    private void setValueFromControl() {
        double value = draggable.getValue();
        if (port.getBoundingMethod() == PortAttributes.BoundingMethod.HARD) {
            if (port.getMinimumValue() != null) {
                value = Math.max(port.getMinimumValue(), value);
            }
            if (port.getMaximumValue() != null) {
                value = Math.min(port.getMaximumValue(), value);
            }
        }
        if (value != (Float) port.getValue()) {
            port.setValue((float) value);
        }
    }

}
