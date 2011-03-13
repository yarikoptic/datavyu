package org.openshapa.views.discrete;

import com.usermetrix.jclient.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import java.util.AbstractList;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;


import org.openshapa.OpenSHAPA;


import org.openshapa.models.db.legacy.DataCell;
import org.openshapa.models.db.legacy.DataColumn;
import org.openshapa.models.db.legacy.Database;
import org.openshapa.models.db.legacy.SystemErrorException;



import com.usermetrix.jclient.UserMetrix;
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openshapa.models.db.Cell;
import org.openshapa.models.db.DeprecatedCell;
import org.openshapa.models.db.DeprecatedVariable;
import org.openshapa.models.db.Variable;
import org.openshapa.util.Constants;
import org.openshapa.views.discrete.layouts.SheetLayoutOrdinal;


/**
 * ColumnDataPanel panel that contains the SpreadsheetCell panels.
 */
public final class ColumnDataPanel extends JPanel implements KeyEventDispatcher {
    /** Width of the column. */
    private int columnWidth;

    /** Layout type for this column. */
    private LayoutManager2 layoutMngr;

    /** The model that this variable represents. */
    private Variable model;

    /** The cell selection listener used for cells in this column. */
    private CellSelectionListener cellSelectionL;

    /** Collection of the SpreadsheetCells held in by this data panel. */
    private List<SpreadsheetCell> cells;

    /** The mapping between the database and the spreadsheet cells. */
    private Map<Long, Long> viewMap;

    /** The logger for this class. */
    private static final Logger LOGGER = UserMetrix.getLogger(ColumnDataPanel.class);

    /** The size of the margin to use down the sides of the columns. */
    private static final int MARGIN_SIZE = 1;

    /** button for creating a new empty cell. */
    private SpreadsheetEmptyCell newCellButton;

    /**
     * Creates a new ColumnDataPanel.
     *
     * @param width The width of the new column data panel in pixels.
     * @param variable The Data Column that this panel represents.
     * @param cellSelL Spreadsheet cell selection listener.
     */
    public ColumnDataPanel(final int width,
                           final Variable variable,
                           final CellSelectionListener cellSelL) {
        super();

        // Store member variables.
        columnWidth = width;
        cells = new ArrayList<SpreadsheetCell>();
        viewMap = new HashMap();
        cellSelectionL = cellSelL;
        model = variable;

        layoutMngr = new SheetLayoutOrdinal(Constants.BORDER_SIZE);
        setLayout(layoutMngr);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, Constants.BORDER_SIZE, new Color(175, 175, 175)));

        newCellButton = new SpreadsheetEmptyCell(getLegacyVariable());
        this.add(newCellButton);

        // Populate the data column with spreadsheet cells.
        buildDataPanelCells(getLegacyVariable(), cellSelL);
    }

    @Deprecated DataColumn getLegacyVariable() {
        return ((DeprecatedVariable) model).getLegacyVariable();
    }

    /**
     * Registers this column data panel with everything that needs to notify
     * this class of events.
     */
    public void registerListeners() {
        KeyboardFocusManager m = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        m.addKeyEventDispatcher(this);
    }

    /**
     * Deregisters this column data panel with everything that is currently
     * notiying it of events.
     */
    public void deregisterListeners() {
        KeyboardFocusManager m = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        m.removeKeyEventDispatcher(this);
    }

    /**
     * Build the SpreadsheetCells and add to the DataPanel.
     *
     * @param dbColumn
     *            DataColumn to display.
     * @param cellSelL
     *            Spreadsheet listener to notify about cell selection changes.
     */
    private void buildDataPanelCells(final DataColumn dbColumn,
                                     final CellSelectionListener cellSelL) {
        try {
            // traverse and build the cells
            for (int j = 1; j <= dbColumn.getNumCells(); j++) {
                DataCell dc = (DataCell) dbColumn.getDB().getCell(dbColumn.getID(), j);
                SpreadsheetCell sc = new SpreadsheetCell(dbColumn.getDB(), dc, cellSelL);
                dbColumn.getDB().registerDataCellListener(dc.getID(), sc);

                // add cell to the JPanel
                this.add(sc);

                // and add it to our reference list
                cells.add(sc);

                // Add the ID's to the mapping.
                viewMap.put(dc.getID(), (long) (cells.size() - 1));
            }

            this.add(newCellButton);
        } catch (SystemErrorException e) {
            LOGGER.error("Failed to populate Spreadsheet.", e);
        }
    }

    /**
     * Clears the cells stored in the column data panel.
     */
    public void clear() {
        try {
            for (SpreadsheetCell cell : cells) {
                // Need to deregister data cell listener here.
                OpenSHAPA.getProjectController().getLegacyDB().getDatabase()
                         .deregisterDataCellListener(cell.getCellID(), cell);
                this.remove(cell);
            }

            cells.clear();
            viewMap.clear();
        } catch (SystemErrorException se) {
            LOGGER.error("Unable to delete all cells", se);
        }
    }

    /**
     * Find and delete SpreadsheetCell by its ID.
     *
     * @param cellID
     *            ID of cell to find and delete.
     */
    public void deleteCellByID(final long cellID) {
        Long cellIndex = viewMap.get(cellID);
        SpreadsheetCell cell = cells.get(cellIndex.intValue());
        this.remove(cell);
        cells.remove(cell);
        viewMap.remove(cellID);
    }

    /**
     * Insert a new SpreadsheetCell given the cells ID.
     *
     * @param db The database holding the cell that is being inserted into this
     * column data panel.
     * @param cellID ID of cell to create and insert.
     * @param cellSelL SpreadsheetCellSelectionListener to notify of changes in
     * selection.
     */
    public void insertCellByID(final Database db, final long cellID,
        final CellSelectionListener cellSelL) {

        try {
            DataCell dc = (DataCell) db.getCell(cellID);
            SpreadsheetCell nCell = new SpreadsheetCell(db, dc, cellSelL);
            nCell.setWidth(this.getWidth());
            db.registerDataCellListener(dc.getID(), nCell);

            Long newOrd = new Long(dc.getOrd());
            nCell.setAlignmentX(Component.RIGHT_ALIGNMENT);

            if (cells.size() > newOrd.intValue()) {
                cells.add(newOrd.intValue() - 1, nCell);
                this.add(nCell, newOrd.intValue() - 1);
            } else {
                cells.add(nCell);
                this.add(nCell);
            }
            viewMap.put(cellID, (long) (cells.size() - 1));

            nCell.requestFocus();
        } catch (SystemErrorException e) {
            LOGGER.error("Problem inserting a new SpreadsheetCell", e);
        }
    }

    /**
     * setLayout changes the layout manager for this columnDataPanel.
     *
     * @param manager New layout manager.
     */
    public void setLayoutManager(final LayoutManager2 manager) {
       layoutMngr = manager;
    }

    /**
     * Set the width of the SpreadsheetCell.
     *
     * @param width
     *            New width of the SpreadsheetCell.
     */
    public void setWidth(final int width) {
        columnWidth = width;
    }

    /**
     * Override Maximum size to fix the width.
     *
     * @return the maximum size of the data column.
     */
    @Override public Dimension getMaximumSize() {
        return new Dimension(columnWidth, Short.MAX_VALUE);
    }

    /**
     * Override Minimum size to fix the width.
     *
     * @return the minimum size of the data column.
     */
    @Override public Dimension getMinimumSize() {
        return new Dimension(columnWidth, (int) layoutMngr.preferredLayoutSize(this).getHeight());
    }

    /**
     * Override Preferred size to fix the width.
     *
     * @return the preferred size of the data column.
     */
    @Override public Dimension getPreferredSize() {
        return new Dimension(columnWidth, (int) layoutMngr.preferredLayoutSize(this).getHeight());
    }

    /**
     * @return The SpreadsheetCells in this column temporally.
     */
    public List<SpreadsheetCell> getCellsTemporally() {
        ArrayList<SpreadsheetCell> result = new ArrayList<SpreadsheetCell>();

        for (Cell c : model.getCellsTemporally()) {
            DeprecatedCell dc = (DeprecatedCell) c;
            long l = viewMap.get(dc.getLegacyCell().getID());
            result.add(cells.get((int) l));
        }

        return result;
    }

    /**
     * @return The SpreadsheetCells in this column.
     */
    public List<SpreadsheetCell> getCells() {
        return cells;
    }

    /**
     * @return The selected spreadsheet cells in this column.
     */
    public AbstractList<SpreadsheetCell> getSelectedCells() {
        AbstractList<SpreadsheetCell> selectedCells = new ArrayList<SpreadsheetCell>();

        for (SpreadsheetCell c : selectedCells) {
            if (c.isSelected()) {
                selectedCells.add(c);
            }
        }

        return selectedCells;
    }

    /**
     * Dispatches the key event to the desired components.
     *
     * @param e The key event to dispatch.
     *
     * @return true if the event has been consumed by this dispatch, false
     * otherwise
     */
    public boolean dispatchKeyEvent(final KeyEvent e) {

        // Quick filter - if we aren't dealing with a key press or up and down
        // arrow. Forget about it - just chuck it back to Java to deal with.
        if ((e.getID() != KeyEvent.KEY_PRESSED)
                && ((e.getKeyCode() != KeyEvent.VK_UP)
                    || (e.getKeyCode() != KeyEvent.VK_DOWN))) {
            return false;
        }

        Component[] components = getComponents();
        int numCells = getComponentCount() - 1;

        // For each of the cells in the column - see if one has focus.
        for (int i = 0; i < numCells; i++) {

            if (components[i].isFocusOwner()
                    && components[i].getClass().equals(JButton.class)) {

                if ((e.getKeyCode() == KeyEvent.VK_UP) && (i > 0)) {
                    SpreadsheetCell sc = (SpreadsheetCell) components[i - 1];
                    EditorTracker et = sc.getDataView().getEdTracker();
                    EditorComponent ec = et.getCurrentEditor();

                    // Get the caret position within the active editor
                    // component.
                    int relativePos = et.getCurrentEditor().getCaretPosition();
                    int absolutePos = sc.getDataView().getCaretPosition();

                    try {

                        // Determine if we are at the top of a multi-lined cell,
                        // if we are not on the top line - pressing up should
                        // select the line above.
                        JTextArea a = (JTextArea) ec.getParentComponent();

                        if (a.getLineOfOffset(a.getCaretPosition()) == 0) {
                            sc = (SpreadsheetCell) components[i - 1];
                            et = sc.getDataView().getEdTracker();
                            ec = et.findEditor(absolutePos);
                            et.setEditor(ec);

                            a = (JTextArea) ec.getParentComponent();

                            // Determine the line start and end points.
                            int lastLine = (a.getLineCount() - 1);
                            int lineEnd = a.getLineEndOffset(lastLine);
                            int lineStart = a.getLineStartOffset(lastLine);

                            // We take either the position or the last element
                            // in the line
                            int newPos = Math.min(relativePos + lineStart,
                                    lineEnd);

                            // Set the caret position in the newly focused
                            // editor.
                            ec.setCaretPosition(newPos);
                            sc.requestFocus();
                            sc.setHighlighted(true);
                            cellSelectionL.setHighlightedCell(sc);

                            e.consume();

                            return true;
                        }
                    } catch (BadLocationException be) {
                        LOGGER.error("BadLocation on arrow up", be);
                    }
                }

                return false;

            }

            // The current cell has focus.
            if (components[i].isFocusOwner()
                    && components[i].getClass().equals(SpreadsheetCell.class)) {

                // Get the current editor tracker and component for the cell
                // that has focus.
                SpreadsheetCell sc = (SpreadsheetCell) components[i];
                EditorTracker et = sc.getDataView().getEdTracker();
                EditorComponent ec = et.getCurrentEditor();

                // Get the caret position within the active editor component.
                int relativePos = et.getCurrentEditor().getCaretPosition();
                int absolutePos = sc.getDataView().getCaretPosition();

                // The key stroke is up - select the editor component in the
                // cell above, setting the caret position to what we just found
                // in the current cell.
                if ((e.getKeyCode() == KeyEvent.VK_UP) && (i > 0)) {

                    try {

                        // Determine if we are at the top of a multi-lined cell,
                        // if we are not on the top line - pressing up should
                        // select the line above.
                        JTextArea a = (JTextArea) ec.getParentComponent();

                        if (a.getLineOfOffset(a.getCaretPosition()) == 0) {
                            sc = (SpreadsheetCell) components[i - 1];
                            et = sc.getDataView().getEdTracker();
                            ec = et.findEditor(absolutePos);
                            et.setEditor(ec);

                            a = (JTextArea) ec.getParentComponent();

                            // Determine the line start and end points.
                            int lastLine = (a.getLineCount() - 1);
                            int lineEnd = a.getLineEndOffset(lastLine);
                            int lineStart = a.getLineStartOffset(lastLine);

                            // We take either the position or the last element
                            // in the line
                            int newPos = Math.min(relativePos + lineStart,
                                    lineEnd);

                            // Set the caret position in the newly focused
                            // editor.
                            ec.setCaretPosition(newPos);
                            sc.requestFocus();
                            sc.setHighlighted(true);
                            cellSelectionL.setHighlightedCell(sc);

                            e.consume();

                            return true;
                        }
                    } catch (BadLocationException be) {
                        LOGGER.error("BadLocation on arrow up", be);
                    }
                }

                // The key stroke is down - select the editor component in the
                // cell below, setting the caret position to what we found from
                // the current cell.
                if ((e.getKeyCode() == KeyEvent.VK_DOWN)
                        && ((i + 1) < numCells)) {

                    try {

                        // Determine if we are at the bottom of a multi-lined
                        // cell, if we are not on the bottom line - pressing
                        // down should select the line below.
                        JTextArea a = (JTextArea) ec.getParentComponent();

                        if ((a.getLineOfOffset(a.getCaretPosition()) + 1)
                                >= a.getLineCount()) {
                            components[i + 1].requestFocus();

                            if (components[i + 1] instanceof SpreadsheetCell) {
                                sc = (SpreadsheetCell) components[i + 1];

                                et = sc.getDataView().getEdTracker();
                                ec = et.findEditor(absolutePos);
                                et.setEditor(ec);
                                ec.setCaretPosition(relativePos);
                                sc.setHighlighted(true);
                                cellSelectionL.setHighlightedCell(sc);
                            } else {
                                sc = (SpreadsheetCell) components[i];
                                sc.setHighlighted(false);
                                cellSelectionL.clearCellSelection();
                            }

                            e.consume();

                            return true;
                        }
                    } catch (BadLocationException be) {
                        LOGGER.error("BadLocation on arrow down", be);
                    }
                }

                return false;
            }
        }

        return false;
    }
}
