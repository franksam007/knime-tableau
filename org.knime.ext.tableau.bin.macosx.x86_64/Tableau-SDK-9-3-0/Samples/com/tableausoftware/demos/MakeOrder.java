/**
 * -----------------------------------------------------------------------------
 * 
 * This file is the copyrighted property of Tableau Software and is protected 
 * by registered patents and other applicable U.S. and international laws and 
 * regulations.
 * 
 * Unlicensed use of the contents of this file is prohibited. Please refer to 
 * the NOTICES.txt file for further details.
 * 
 * -----------------------------------------------------------------------------
 */
package com.tableausoftware.demos;

import com.tableausoftware.TableauException;
import com.tableausoftware.common.*;
import com.tableausoftware.extract.*;

public final class MakeOrder {

    // Define the table's schema
    private static TableDefinition makeTableDefinition() throws TableauException {
        TableDefinition tableDef = new TableDefinition();
        tableDef.setDefaultCollation(Collation.EN_GB);

        tableDef.addColumn("Purchased",       Type.DATETIME);
        tableDef.addColumn("Product",         Type.CHAR_STRING);
        tableDef.addColumn("uProduct",        Type.UNICODE_STRING);
        tableDef.addColumn("Price",           Type.DOUBLE);
        tableDef.addColumn("Quantity",        Type.INTEGER);
        tableDef.addColumn("Taxed",           Type.BOOLEAN);
        tableDef.addColumn("Expiration Date", Type.DATE);

        // Column with non-default collation
        tableDef.addColumnWithCollation("Produkt", Type.CHAR_STRING, Collation.DE);

        return tableDef;
    }

    // Print a Table's schema to stderr.
    private static void printTableDefinition(TableDefinition tableDef) throws TableauException {
        int numColumns = tableDef.getColumnCount();
        for ( int i = 0; i < numColumns; ++i ) {
            Type type = tableDef.getColumnType(i);
            String name = tableDef.getColumnName(i);

            System.err.format("Column %d: %s (%#06x)\n", i, name, type.getValue());
        }
    }

    // Insert a few rows of data
    private static void insertData(Table table) throws TableauException {
        TableDefinition tableDef = table.getTableDefinition();
        Row row = new Row(tableDef);

        row.setDateTime(  0, 2012, 7, 3, 11, 40, 12, 4550); // Purchased
        row.setCharString(1, "Beans");                      // Product
        row.setString(    2, "uniBeans");                   // uProduct
        row.setDouble(    3, 1.08);                         // Price
        row.setDate(      6, 2029, 1, 1);                   // Expiration date
        row.setCharString(7, "Bohnen");                     // Produkt

        for ( int i = 0; i < 10; ++i ) {
            row.setInteger(4, i * 10);                      // Quantity
            row.setBoolean(5, i % 2 == 1);                  // Taxed
            table.insert(row);
        }
    }

    public static void main( String[] args ) {

        try {
            // Initialize Tableau Extract API
            ExtractAPI.initialize();

            try (Extract extract = new Extract("order-java.tde")) {

                Table table;
                if (!extract.hasTable("Extract")) {
                    // Table does not exist; create it
                    TableDefinition tableDef = makeTableDefinition();        
                    table = extract.addTable("Extract", tableDef);
                }
                else {
                    // Open an existing table to add more rows
                    table = extract.openTable("Extract");
                }

                TableDefinition tableDef = table.getTableDefinition();
                printTableDefinition(tableDef);

                insertData(table);
            }

            // Clean up Tableau Extract API
            ExtractAPI.cleanup();
        }
        catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }
}
