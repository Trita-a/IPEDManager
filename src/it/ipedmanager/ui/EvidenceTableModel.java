package it.ipedmanager.ui;

import it.ipedmanager.model.Evidence;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel per la tabella delle evidenze (Design V2).
 */
public class EvidenceTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    // COLUMNS: 0=TIPO (Icon), 1=NOME (Editable), 2=PERCORSO, 3=DIM, 4=PASSWORD
    // (Editable), 5=DELETE
    // COLUMNS: 0=TIPO (Icon), 1=NOME (Editable), 2=PERCORSO, 3=DIM, 4=PASSWORD,
    // 5=DELETE
    private final String[] columnKeys = { "", "mainframe.table.header.name", "mainframe.table.header.path",
            "mainframe.table.header.size", "mainframe.table.header.password", "" };
    private final List<Evidence> evidences;

    public EvidenceTableModel() {
        this.evidences = new ArrayList<>();
    }

    public EvidenceTableModel(List<Evidence> evidences) {
        this.evidences = evidences != null ? evidences : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return evidences.size();
    }

    @Override
    public int getColumnCount() {
        return columnKeys.length;
    }

    @Override
    public String getColumnName(int column) {
        String key = columnKeys[column];
        if (key.isEmpty())
            return "";
        return it.ipedmanager.utils.BundleManager.getString(key);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= evidences.size())
            return null;
        Evidence ev = evidences.get(row);

        switch (column) {
            case 0:
                return ev.isDirectory() ? "D" : "F"; // Icon type indicator
            case 1:
                return ev.getDname(); // Name (Editable)
            case 2:
                return ev.getFilePath(); // Path (Read-only)
            case 3:
                return ev.getSizeReadable(); // Size (Read-only)
            case 4:
                return ev.getPassword(); // Password (Editable)
            case 5:
                return "X"; // Delete
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (row < 0 || row >= evidences.size())
            return;
        Evidence ev = evidences.get(row);

        switch (column) {
            case 1: // Name is now column 1
                ev.setDname(value != null ? value.toString() : "");
                break;
            case 4: // Password is now column 4
                ev.setPassword(value != null ? value.toString() : "");
                break;
        }
        fireTableCellUpdated(row, column);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Only Password (column 4) is editable (User Request)
        return column == 4;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    // Metodi per gestire le evidenze
    public void addEvidence(Evidence evidence) {
        evidences.add(evidence);
        fireTableRowsInserted(evidences.size() - 1, evidences.size() - 1);
    }

    public void removeEvidence(int row) {
        if (row >= 0 && row < evidences.size()) {
            evidences.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public void clearAll() {
        int size = evidences.size();
        if (size > 0) {
            evidences.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public Evidence getEvidence(int row) {
        if (row >= 0 && row < evidences.size()) {
            return evidences.get(row);
        }
        return null;
    }

    public List<Evidence> getEvidences() {
        return evidences;
    }

    public boolean isEmpty() {
        return evidences.isEmpty();
    }
}
