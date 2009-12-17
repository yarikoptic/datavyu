package org.openshapa.uitests;

import org.uispec4j.interception.WindowInterceptor;
import org.openshapa.views.discrete.SpreadsheetPanel;
import java.util.Vector;
import org.openshapa.OpenSHAPA;
import org.openshapa.util.FloatUtils;
import org.openshapa.views.discrete.SpreadsheetCell;
import org.uispec4j.Cell;
import org.uispec4j.Key;
import org.uispec4j.KeyItem;
import org.uispec4j.MenuBar;
import org.uispec4j.OpenSHAPAUISpecTestCase;
import org.uispec4j.Spreadsheet;
import org.uispec4j.TextItem;
import org.uispec4j.Timestamp;
import org.uispec4j.UISpec4J;
import org.uispec4j.Window;

/**
 * Test for the DataController.
 */
public final class UIDataControllerTest extends OpenSHAPAUISpecTestCase {

    /**
     * Initialiser called before each unit test.
     *
     * @throws java.lang.Exception When unable to initialise test
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

     /**
     * Called after each test.
     * @throws Exception When unable to tear down
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Different cell variable types.
     */
     private static final String[] VAR_TYPES = {"TEXT", "PREDICATE", "INTEGER",
        "NOMINAL", "MATRIX", "FLOAT"
    };

    /**
      * Nominal test input.
      */
     private String[] nominalTestInput = {"Subject stands )up ", "$10,432",
            "Hand me (the manual!", "Tote_that_bale", "Jeune; fille celebre",
            "If x>7 then x|2"};

     /**
      * Nominal test output.
      */
     private String[] expectedNominalTestOutput = {"Subject stands up",
            "$10432", "Hand me the manual!", "Tote_that_bale",
            "Jeune fille celebre", "If x7 then x2"};

     /**
      * Text test input.
      */
     private String[] textTestInput = {"Subject stands up ", "$10,432",
            "Hand me the manual!", "Tote_that_bale", "Jeune fille celebre",
            "If x?7 then x? 2"};

     /**
      * Integer test input.
      */
     private String[] integerTestInput = {"1a9", "10-432",
            "!28.9(", "178&", "~~~)",
            "If x?7 then x? 2 ", "99999999999999999999", "000389.5", "-", "-0",
            "-123"};

     /**
      * Integer test output.
      */
     private String[] expectedIntegerTestOutput = {"19", "-43210", "289", "178",
        "<val>", "72", "999999999999999999", "3895", "-", "0", "-123"};

     /**
      * Float test input.
      */
     private String[] floatTestInput = {"1a.9", "10-43.2",
            "!289(", "178.&", "0~~~)",
            "If x?7 then. x? 2 ", "589.138085638", "000389.5",
            "-0.1", "0.2", "-0.0", "-", "-0", "-.34", "-23.34", ".34", "12.34",
            "-123"};

     /**
      * Float test output.
      */
     private String[] expectedFloatTestOutput = {"1.9", "-43.21", "289", "178",
        "0", "7.2", "589.138085", "389.5", "-0.1", "0.2", "0", "0", "0",
        "-0.34", "-23.34", "0.34", "12.34", "-123"};

    static {
      UISpec4J.setWindowInterceptionTimeLimit(120000);
      UISpec4J.init();
    }

    /**
     * Standard test sequence focussing on jogging.
     * @throws Exception any exception
     */
    private void StandardSequence1(final String varName, final String varType,
            final String[] testInputArray, final String[] testExpectedArray)
            throws Exception {
        // Retrieve the components and set variable
        Window window = getMainWindow();
        MenuBar menuBar = window.getMenuBar();
        Spreadsheet ss = new Spreadsheet((SpreadsheetPanel) (
                window.getUIComponents(Spreadsheet.class)[0]
                .getAwtComponent()));

        ss.deselectAll();

        //1. Create a new variable of random type
        String vName = varName;
        String vRadio = varType;

        createNewVariable(vName, vRadio);

        //2. Open Data Viewer Controller and get starting time
        Window dvc = WindowInterceptor.run(menuBar.getMenu("Controller")
                .getSubMenu("Data Viewer Controller").triggerClick());

        Timestamp expectedDVCTime = new Timestamp(dvc.getTextBox(
                "timestampLabel").getText());


        //3. Create new cell - so we have something to send key to because
        // no focus handling
        ss.getSpreadsheetColumn(vName).requestFocus();
        menuBar.getMenu("Spreadsheet").getSubMenu("New Cell").click();
        Vector<Cell> cells = ss.getSpreadsheetColumn(vName).getCells();

        //4. Test jogging back and forth
        Cell c = cells.elementAt(0);

        Vector<TextItem> ti = new Vector<TextItem>();
        //ti.add(new StringItem("hello"));
        ti.add(new KeyItem(Key.NUM3));

        // Jog forward 5 times
        for (int i = 0; i < 5; i++) {
            c.enterText(Cell.VALUE, ti);
        }

        expectedDVCTime.add(new Timestamp("00:00:05:000"));
        assertTrue(expectedDVCTime.equals(dvc.getTextBox("timestampLabel")
                .getText()));


        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM1));
        //Jog back 2 times
        for (int i = 0; i < 2; i++) {
            c.enterText(Cell.VALUE, ti);
        }

        expectedDVCTime.subtract(new Timestamp("00:00:02:000"));
        assertTrue(expectedDVCTime.equals(dvc.getTextBox("timestampLabel")
                .getText()));

        //5. Test Create New Cell with Onset
        ss.getSpreadsheetColumn(vName).requestFocus();
        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM0));
        c.enterText(Cell.VALUE, ti);

        cells = ss.getSpreadsheetColumn(vName).getCells();

        Timestamp oneLess = new Timestamp(expectedDVCTime.toString());
        oneLess.subtract(new Timestamp("00:00:00:001"));

        assertTrue(cells.size() == 2);
        assertTrue(cells.elementAt(0).getOffsetTime().toString()
                .equals(oneLess.toString()));
        assertTrue(cells.elementAt(1).getOnsetTime().toString()
                .equals(expectedDVCTime.toString()));
        assertTrue(cells.elementAt(1).getOffsetTime().toString()
                .equals("00:00:00:000"));

        //6. Insert value into both cells
        cells.elementAt(0).enterText(Cell.VALUE, testInputArray[0]);
        cells.elementAt(1).enterText(Cell.VALUE, testInputArray[1]);

        assertTrueEqualValues(cells.elementAt(0).getValueText(),
                testExpectedArray[0]);
        assertTrueEqualValues(cells.elementAt(1).getValueText(),
                testExpectedArray[1]);

        //7. Jog forward 60 times and change cell onset
        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM3));

        for (int i = 0; i < 60; i++) {
            c.enterText(Cell.ONSET, ti);
        }

        expectedDVCTime.add(new Timestamp("00:01:00:000"));

        //Set cell onset
        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM_DIVIDE));

        // Select cell 2 - cheating, should be resolved when
        // focus handling is implemented
        ((SpreadsheetCell) cells.elementAt(1).getAwtComponent())
                .setSelected(true);

        c.enterText(Cell.OFFSET, ti);
        assertTrue(expectedDVCTime.equals(cells.elementAt(1).getOnsetTime()
                .toString()));

        //8. Change cell offset
        assertTrue(oneLess.equals(cells.elementAt(0).getOffsetTime()
                .toString()));

        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM_ASTERISK));
        // Select cell 2 - cheating, should be resolved when
        // focus handling is implemented
        ((SpreadsheetCell) cells.elementAt(1).getAwtComponent())
                .setSelected(false);
        ((SpreadsheetCell) cells.elementAt(0).getAwtComponent())
                .setSelected(true);
        //Mouse.click(cells.elementAt(1));

        c.enterText(Cell.OFFSET, ti);
        ((SpreadsheetCell) cells.elementAt(0).getAwtComponent())
                .setSelected(false);
        assertTrue(expectedDVCTime.equals(cells.elementAt(0).getOffsetTime()
                .toString()));

        //9. Jog back and forward, then create a new cell with onset
        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM1));
        //Jog back 21 times
        for (int i = 0; i < 21; i++) {
            c.enterText(Cell.VALUE, ti);
        }
        expectedDVCTime.subtract(new Timestamp("00:00:21:000"));
        assertTrue(dvc.getTextBox("timestampLabel").getText()
                .equalsIgnoreCase(expectedDVCTime.toString()));

        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM3));
        //Jog forward 99 times
        for (int i = 0; i < 99; i++) {
            c.enterText(Cell.VALUE, ti);
        }
        expectedDVCTime.add(new Timestamp("00:01:39:000"));
        assertTrue(expectedDVCTime.equals(dvc.getTextBox("timestampLabel")
                .getText()));

        //Create new cell with offset
        ti.removeAllElements();
        ti.add(new KeyItem(Key.NUM0));
        c.enterText(Cell.VALUE, ti);

        cells = ss.getSpreadsheetColumn(vName).getCells();

        assertTrue(cells.size() == 3);
        /*BugzID:892 - assertTrue(cells.elementAt(1).getOffsetTime().toString()
                .equals("00:02:20:999"));*/
        assertTrue(expectedDVCTime.equals(cells.elementAt(2).getOnsetTime()
                .toString()));
        assertTrue(cells.elementAt(2).getOffsetTime().toString()
                .equals("00:00:00:000"));

        //Test data controller view onset, offset and find
        ss.deselectAll();
        Vector<TextItem> find = new Vector<TextItem>();
        find.add(new KeyItem(Key.NUM_ADD));
        Vector<TextItem> shiftFind = new Vector<TextItem>();
        shiftFind.add(new KeyItem(Key.shift(Key.NUM_ADD)));
        for (Cell cell : cells) {
            cell.setSelected(true);
            assertTrue(dvc.getTextBox("findOnsetLabel").getText()
                    .equals(cell.getOnset().getText()));
            assertTrue(dvc.getTextBox("findOffsetLabel").getText()
                    .equals(cell.getOffset().getText()));
            cell.enterText(Cell.VALUE, find);
            assertTrue(dvc.getTextBox("timestampLabel").getText()
                    .equals(cell.getOnset().getText()));
            cell.enterText(Cell.VALUE, shiftFind);
            assertTrue(dvc.getTextBox("timestampLabel").getText()
                    .equals(cell.getOffset().getText()));
        }
        OpenSHAPA.getDataController().setVisible(false);
    }

    /**
     * Runs standardsequence1 for different variable types (except matrix and
     * predicate), side by side.
     * @throws Exception any exception
     */
    public void testStandardSequence1() throws Exception {
        //Text
        StandardSequence1("textVar", "text", textTestInput, textTestInput);
        //Integer
        StandardSequence1("intVar", "integer", integerTestInput,
                expectedIntegerTestOutput);
        //Float
        StandardSequence1("floatVar", "float", floatTestInput,
                expectedFloatTestOutput);
        //Nominal
        StandardSequence1("nomVar", "nominal", nominalTestInput,
                expectedNominalTestOutput);
    }



    /**
     * Asserts true is two cell values are equal.
     * @param value1 first cell value
     * @param value2 second cell value
     */
    private void assertTrueEqualValues(final String value1,
            final String value2) {
        if ((value1.startsWith("<") && value1.endsWith(">"))
                || (value2.startsWith("<") && value2.endsWith(">"))) {
            assertTrue(value1.equalsIgnoreCase(value2));
        } else {
            try {
                //Handle doubles
                assertTrue(FloatUtils.closeEnough(Double.parseDouble(value1),
                        Double.parseDouble(value2)));
            } catch (NumberFormatException nfe) {
                //Handle other variable types
                assertTrue(value1.equalsIgnoreCase(value2));
            }
        }
    }

    /**
     * Create a new variable.
     * @param varName String for the name of the variable
     * @param varRadio String for the corresponding radio button to click
     * @throws java.lang.Exception on any error
     */
    private void createNewVariable(final String varName,
            final String varRadio) throws Exception {
        // 1. Retrieve the components
        Window window = getMainWindow();
        MenuBar menuBar = window.getMenuBar();
        // 2a. Create new variable,
        //open spreadsheet and check that it's there
        Window newVarWindow = WindowInterceptor.run(menuBar.getMenu(
                "Spreadsheet").getSubMenu("New Variable").triggerClick());
        newVarWindow.getTextBox("nameField").insertText(varName, 0);
        newVarWindow.getRadioButton(varRadio).click();
        newVarWindow.getButton("Ok").click();
    }

      /**
      * Parses a matrix value and returns an arg.
      * @param matrixCellValue matrix cell value
      * @param arg argument number
      * @return argument as a string
      */
     private String getArgFromMatrix(final String matrixCellValue,
             final int arg) {
         String argList = matrixCellValue.substring(1,
                 matrixCellValue.length() - 2);

         String [] tokens = argList.split(", ");

         return tokens[arg];
     }

          /**
      * Parses a matrix value and returns an arg.
      * @param matrixCellValue matrix cell value
      * @return int number of arguments
      */
     private int getNumberofArgFromMatrix(final String matrixCellValue) {
         String argList = matrixCellValue.substring(1,
                 matrixCellValue.length() - 1);

         String [] tokens = argList.split(", ");

         return tokens.length;
     }

     /**
      * Parses a matrix value and returns array of arguments.
      * @param matrixCellValue matrix cell value
      * @return arguments in an array
      */
     private String [] getArgsFromMatrix(final String matrixCellValue) {
         String argList = matrixCellValue.substring(1,
                 matrixCellValue.length() - 1);

         String [] tokens = argList.split(", ");

         return tokens;
     }
}

