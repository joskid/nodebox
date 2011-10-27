package nodebox.client.port;

import nodebox.client.DraggableNumber;
import nodebox.node.Port;

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
        // TODO Fix bounding method
//        if (port.getBoundingMethod() == Parameter.BoundingMethod.HARD) {
//            Float minimumValue = port.getMinimumValue();
//            if (minimumValue != null)
//                draggable.setMinimumValue(minimumValue);
//            Float maximumValue = port.getMaximumValue();
//            if (maximumValue != null)
//                draggable.setMaximumValue(maximumValue);
//        }
        add(draggable);
        setValueForControl(port.getValue());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        draggable.setEnabled(enabled);
    }

    public void setValueForControl(Object v) {
        if (v instanceof Float) {
            draggable.setValue((Float) v);
        } else if (v instanceof Double) {
            draggable.setValue(((Double) v).floatValue());
        } else if (v instanceof Integer) {
            draggable.setValue(((Integer) v).floatValue());
        } else {
            throw new IllegalArgumentException("Value " + v + " is not a number.");
        }
    }

    public void stateChanged(ChangeEvent e) {
        setValueFromControl();
    }

    public void actionPerformed(ActionEvent e) {
        setValueFromControl();
    }

    private void setValueFromControl() {
        double value = draggable.getValue();
//        if (port.getBoundingMethod() == Parameter.BoundingMethod.HARD) {
//            if (port.getMinimumValue() != null) {
//                value = Math.max(port.getMinimumValue(), value);
//            }
//            if (port.getMaximumValue() != null) {
//                value = Math.min(port.getMaximumValue(), value);
//            }
//        }
        if (value != port.floatValue()) {
            setPortValue((float) value);
        }
    }

}
