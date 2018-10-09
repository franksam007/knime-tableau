/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Oct 9, 2018 (bw): created
 */
package org.knime.ext.tableau.hyper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.ext.tableau.TableauExtract;
import org.knime.ext.tableau.TableauExtractOpener;
import org.knime.ext.tableau.TableauTable;
import org.knime.ext.tableau.WrappingTableauException;

import com.tableausoftware.TableauException;
import com.tableausoftware.common.Collation;
import com.tableausoftware.common.Type;
import com.tableausoftware.hyperextract.Extract;
import com.tableausoftware.hyperextract.Row;
import com.tableausoftware.hyperextract.Table;
import com.tableausoftware.hyperextract.TableDefinition;

/**
 * @author Bernd Wiswedel, KNIME AG, Zurich, Switzerland
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
public class TableauHyperExtractOpener implements TableauExtractOpener {

    @Override
    public TableauExtract openExtract(final String path) throws WrappingTableauException {
        return new TableauHyperExtract(path);
    }

    private static class TableauHyperExtract implements TableauExtract {

        private final Extract m_extract;

        TableauHyperExtract(final String path) throws WrappingTableauException {
            try {
                m_extract = new Extract(path);
            } catch (final TableauException e) {
                throw new WrappingTableauException(e);
            }
        }

        @Override
        public TableauTable createTable(final String name, final DataTableSpec spec) throws WrappingTableauException {
            try {
                // Get the type setters and create the table definition
                final TableauTypeSetter[] typeSetters = createTypeSetters(spec);
                final TableDefinition tableDef = createTableDefinition(typeSetters);

                // Create the table
                return new TableauHyperTable(m_extract.addTable(name, tableDef), typeSetters);
            } catch (final TableauException e) {
                throw new WrappingTableauException(e);
            }
        }

        @Override
        public TableauTable openTable(final String name, final DataTableSpec spec) throws WrappingTableauException {
            try {
                // Get the type setters and create the table definition
                final TableauTypeSetter[] typeSetters = createTypeSetters(spec);
                final TableDefinition tableDef = createTableDefinition(typeSetters);

                final Table table = m_extract.openTable(name);

                // TODO check if equals is implemented correctly
                if (!isCompatible(tableDef, table.getTableDefinition())) {
                    throw new WrappingTableauException("The extract contains a table with the name '" + name
                        + "' but with a different table definition. "
                        + "If you want to append to an existing table make sure the table is still the same. "
                        + "If you want to overwite the existing table choose 'Overwrite' in the node configuration.");
                }

                // Create the table
                return new TableauHyperTable(table, typeSetters);
            } catch (final TableauException e) {
                throw new WrappingTableauException(e);
            }
        }

        @Override
        public boolean hasTable(final String name) throws WrappingTableauException {
            try {
                return m_extract.hasTable(name);
            } catch (final TableauException e) {
                throw new WrappingTableauException(e);
            }
        }

        @Override
        public void close() throws Exception {
            m_extract.close();
        }

        private static Optional<TableauTypeSetter> toTableType(final DataColumnSpec colSpec, final int colIndex) {
            DataType type = colSpec.getType();
            if (type.isCompatible(BooleanValue.class)) {
                return Optional.of(new TableauTypeSetter(colSpec, colIndex, Type.BOOLEAN,
                    (r, i, c) -> r.setBoolean(i, ((BooleanValue)c).getBooleanValue())));
            }
            if (type.isCompatible(IntValue.class)) {
                return Optional.of(new TableauTypeSetter(colSpec, colIndex, Type.INTEGER,
                    (r, i, c) -> r.setInteger(i, ((IntValue)c).getIntValue())));
            }
            if (type.isCompatible(DoubleValue.class)) {
                return Optional.of(new TableauTypeSetter(colSpec, colIndex, Type.DOUBLE,
                    (r, i, c) -> r.setDouble(i, ((DoubleValue)c).getDoubleValue())));
            }
            if (type.isCompatible(StringValue.class)) {
                return Optional.of(new TableauTypeSetter(colSpec, colIndex, Type.CHAR_STRING,
                    (r, i, c) -> r.setCharString(i, ((StringValue)c).getStringValue())));
            }
            return Optional.empty();
        }

        private static TableauTypeSetter[] createTypeSetters(final DataTableSpec spec) {
            final List<TableauTypeSetter> typeSetters = new ArrayList<>();
            for (int i = 0; i < spec.getNumColumns(); i++) {
                final DataColumnSpec colSpec = spec.getColumnSpec(i);
                toTableType(colSpec, i).ifPresent(t -> typeSetters.add(t));
            }
            return typeSetters.toArray(new TableauTypeSetter[0]);
        }

        private static TableDefinition createTableDefinition(final TableauTypeSetter[] typeSetters)
            throws TableauException {
            final TableDefinition tableDef = new TableDefinition();
            tableDef.setDefaultCollation(Collation.EN_US);
            for (final TableauTypeSetter t : typeSetters) {
                tableDef.addColumn(t.getColSpec().getName(), t.getType());
            }
            return tableDef;
        }

        private static boolean isCompatible(final TableDefinition a, final TableDefinition b) throws TableauException {
            // Compare the column count
            if (a.getColumnCount() != b.getColumnCount()) {
                return false;
            }
            // Compare the name and type of each column
            for (int i = 0; i < a.getColumnCount(); i++) {
                if (!a.getColumnName(i).equals(b.getColumnName(i))) {
                    return false;
                } else {
                    Type aType = a.getColumnType(i);
                    Type bType = b.getColumnType(i);
                    if (!aType.equals(bType)) {
                        if (!((aType.equals(Type.CHAR_STRING) && bType.equals(Type.UNICODE_STRING))
                            || (aType.equals(Type.UNICODE_STRING) && bType.equals(Type.CHAR_STRING)))) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    private static class TableauHyperTable implements TableauTable {

        private final Table m_table;

        private final TableauTypeSetter[] m_typeSetters;

        public TableauHyperTable(final Table table, final TableauTypeSetter[] typeSetters) {
            m_table = table;
            m_typeSetters = typeSetters;
        }

        @Override
        public void addRow(final DataRow dataRow) throws WrappingTableauException {
            try {
                final Row row = new Row(m_table.getTableDefinition());
                for (int i = 0; i < m_typeSetters.length; i++) {
                    final TableauTypeSetter typeSetter = m_typeSetters[i];
                    final DataCell c = dataRow.getCell(typeSetter.getColIndex());
                    typeSetter.addToRow(i, row, c);
                }
                m_table.insert(row);
            } catch (final TableauException e) {
                throw new WrappingTableauException(e);
            }
        }

    }

    private static final class TableauTypeSetter {

        private final DataColumnSpec m_colSpec;

        private final int m_colIndex;

        private final Type m_type;

        private final CellWriter m_cellWriter;

        TableauTypeSetter(final DataColumnSpec colSpec, final int colIndex, final Type type,
            final CellWriter cellWriter) {
            m_colSpec = colSpec;
            m_type = type;
            m_cellWriter = cellWriter;
            m_colIndex = colIndex;
        }

        Type getType() {
            return m_type;
        }

        DataColumnSpec getColSpec() {
            return m_colSpec;
        }

        int getColIndex() {
            return m_colIndex;
        }

        void addToRow(final int index, final Row row, final DataCell cell) throws TableauException {
            if (cell.isMissing()) {
                row.setNull(index);
            } else {
                m_cellWriter.addCell(row, index, cell);
            }
        }

    }

    @FunctionalInterface
    private interface CellWriter {
        public void addCell(final Row row, final int index, final DataCell cell) throws TableauException;
    }
}
