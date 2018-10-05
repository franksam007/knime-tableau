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
 *   Feb 5, 2016 (wiswedel): created
 */
package org.knime.ext.tableau;

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

import com.tableausoftware.TableauException;
import com.tableausoftware.common.Collation;
import com.tableausoftware.common.Type;
import com.tableausoftware.extract.Extract;
import com.tableausoftware.extract.Row;
import com.tableausoftware.extract.Table;
import com.tableausoftware.extract.TableDefinition;

/**
 * @author Bernd Wiswedel, KNIME AG, Zurich, Switzerland
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
public final class TableauTDEExtractWriter implements TableauExtractWriter {

    private Table m_table;

    private TableauTypeSetter[] m_typeSetters;

    private Extract m_extract;

    private TableauTDEExtractWriter(final TableauTypeSetter[] typeSetters, final String path)
        throws WrappingTableauException {
        try {
            m_typeSetters = typeSetters;
            TableDefinition tableDef = new TableDefinition();
            tableDef.setDefaultCollation(Collation.EN_US);
            for (int i = 0; i < typeSetters.length; i++) {
                TableauTypeSetter tableauTypeSetter = typeSetters[i];
                tableDef.addColumn(tableauTypeSetter.getColSpec().getName(), tableauTypeSetter.getType());
            }
            m_extract = new Extract(path);
            m_table = m_extract.addTable("Extract", tableDef);
        } catch (final TableauException e) {
            throw new WrappingTableauException(e);
        }
    }

    @Override
    public void addRow(final DataRow dataRow) throws WrappingTableauException {
        try {
            Row row = new Row(m_table.getTableDefinition());
            for (int i = 0; i < m_typeSetters.length; i++) {
                TableauTypeSetter typeSetter = m_typeSetters[i];
                DataCell c = dataRow.getCell(typeSetter.getColIndex());
                typeSetter.addToRow(i, row, c);
            }
            m_table.insert(row);
        } catch (final TableauException e) {
            throw new WrappingTableauException(e);
        }
    }

    @Override
    public void close() throws Exception {
        m_extract.close();
    }

    /**
     * A {@link TableauExtractCreator} which uses the tableausdk and writes to tde files.
     */
    public static class TableauTDEExtractCreator implements TableauExtractCreator {

        @Override
        public TableauExtractWriter createExtract(final String path, final DataTableSpec spec)
            throws WrappingTableauException {
            final List<TableauTypeSetter> typeSetters = new ArrayList<>();
            for (int i = 0; i < spec.getNumColumns(); i++) {
                final DataColumnSpec colSpec = spec.getColumnSpec(i);
                final Optional<TableauTypeSetter> tableType = toTableType(colSpec, i);
                if (tableType.isPresent()) {
                    typeSetters.add(tableType.get());
                }
            }
            return new TableauTDEExtractWriter(typeSetters.toArray(new TableauTypeSetter[0]), path);
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
    }

    // TODO think about extracting this into own classes
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
