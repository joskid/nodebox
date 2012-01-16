package nodebox.client;

import nodebox.ui.PaneView;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

/**
 * The Data Sheet presents data in a spreadsheet view.
 */
public class DataSheet extends JPanel implements PaneView {

    private final NodeBoxDocument document;
    private final DataTableModel tableModel;
    private final JTable table;

    public DataSheet(NodeBoxDocument document) {
        super(new BorderLayout());
        this.document = document;
        table = new DataTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.addColumn(new TableColumn(0));
        tableModel = new DataTableModel();
        table.setModel(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(tableScroll, BorderLayout.CENTER);
    }

    public void setOutputValues(List<Object> outputValues) {
        tableModel.setOutputValues(outputValues);
        table.setModel(tableModel);
    }

    // Optimization techniques based on "Christmas Tree" article:
    // http://java.sun.com/products/jfc/tsc/articles/ChristmasTree/
    private final class DataTable extends JTable {

        private final DataCellRenderer cellRenderer = new DataCellRenderer();

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            // Always return the same object for the whole table.
            return cellRenderer;
        }

    }

    private final class DataCellRenderer extends DefaultTableCellRenderer {

        private Color zebraColor = UIManager.getColor("Table.alternateRowColor");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
            setValue(value);
            if (rowIndex % 2 == 0) {
                setBackground(null);
            } else {
                setBackground(zebraColor);
            }
            return this;
        }

        @Override
        public void paint(Graphics g) {
            ui.update(g, this);
        }

        @Override
        public boolean isOpaque() {
            return getBackground() != null;
        }

        @Override
        public void invalidate() {
            // This is generally ok for non-Composite components (like Labels)
        }

        @Override
        public void repaint() {
            // Can be ignored, we don't exist in the containment hierarchy.
        }
    }

    private final class DataTableModel extends AbstractTableModel {

        private List<Object> outputValues;

        public List<Object> getOutputValues() {
            return outputValues;
        }

        public void setOutputValues(List<Object> outputValues) {
            this.outputValues = outputValues;
            fireTableChanged(new TableModelEvent(this));
        }

        public int getRowCount() {
            if (outputValues == null) return 0;
            return outputValues.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (outputValues == null) return 0;
            return outputValues.get(rowIndex);
        }

        @Override
        public String getColumnName(int columnIndex) {
            return "Data";
        }
    }

}
