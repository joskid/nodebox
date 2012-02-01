package nodebox.client.port;

import nodebox.node.Port;
import nodebox.ui.DraggableNumber;

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
        // TODO add bounding
//        if (port.getBoundingMethod() == Parameter.BoundingMethod.HARD) {
//            Float minimumValue = port.getMinimumValue();
//            if (minimumValue != null)
//                draggable.setMinimumValue(minimumValue);
//            Float maximumValue = port.getMaximumValue();
//            if (maximumValue != null)
//                draggable.setMaximumValue(maximumValue);
//        }
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
        if (v instanceof Integer) {
            draggable.setValue((Integer) v);
        } else if (v instanceof Long) {
            draggable.setValue((Long) v);
        } else {
            throw new IllegalArgumentException("This function only accept integers or longs, not " + v);
        }
    }

    public void stateChanged(ChangeEvent e) {
        setValueFromControl();
    }

    public void actionPerformed(ActionEvent e) {
        setValueFromControl();
    }

    private void setValueFromControl() {
        double doubleValue = draggable.getValue();
//        if (port.getBoundingMethod() == Parameter.BoundingMethod.HARD) {
//            if (port.getMinimumValue() != null) {
//                doubleValue = Math.max(port.getMinimumValue(), doubleValue);
//            }
//            if (port.getMaximumValue() != null) {
//                doubleValue = Math.min(port.getMaximumValue(), doubleValue);
//            }
//        }
        int intValue = (int) doubleValue;
        if (intValue != port.intValue()) {
            setPortValue(intValue);
        }
    }

}
