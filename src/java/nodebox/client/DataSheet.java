package nodebox.client;

import com.google.common.collect.ImmutableList;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The Data Sheet presents data in a spreadsheet view.
 */
public class DataSheet extends JPanel implements OutputView {

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

    public void setOutputValues(List<Object> objects) {
        tableModel.setOutputValues(objects);
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

        private List<Object> outputValues = ImmutableList.of();
        private List dataTemplate = ImmutableList.of();

        public List<Object> getOutputValues() {
            return outputValues;
        }

        public void setOutputValues(List<Object> outputValues) {
            if (outputValues == null) {
                this.outputValues = ImmutableList.of();
            } else {
                this.outputValues = outputValues;
            }
            if (this.outputValues.size() == 0) {
                dataTemplate = ImmutableList.of();
            } else {
                dataTemplate = seq(this.outputValues.get(0));
            }
            fireTableChanged(new TableModelEvent(this, TableModelEvent.ALL_COLUMNS));
        }

        private List seq(Object o) {
            // Inspect the first object.
            if (o instanceof Iterable) {
                return ImmutableList.copyOf((Iterable<? extends Object>) o);
            } else {
                return ImmutableList.of(o);
            }
        }

        public int getRowCount() {
            return outputValues.size();
        }

        public int getColumnCount() {
            return dataTemplate.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            checkArgument(rowIndex < outputValues.size(), "The row index %s is larger than the number of values.", rowIndex);
            checkArgument(columnIndex < dataTemplate.size(), "The column index %s is larger than the number of columns.", columnIndex);

            List o = seq(outputValues.get(rowIndex));
            if (columnIndex > o.size()) {
                return "<not found>";
            } else {
                return o.get(columnIndex);
            }
        }

        @Override
        public String getColumnName(int columnIndex) {
            return "Data";
        }
    }

}
