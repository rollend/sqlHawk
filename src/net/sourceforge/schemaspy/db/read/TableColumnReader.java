/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.sourceforge.schemaspy.db.read;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.xml.TableColumnMeta;

public class TableColumnReader {
    private static final Logger logger = Logger.getLogger(TableColumnReader.class.getName());
    private static final boolean finerEnabled = logger.isLoggable(Level.FINER);

    /**
     * Create a column associated with a table.
     *
     * @param table Table the table that this column belongs to
     * @param rs ResultSet returned from {@link DatabaseMetaData#getColumns(String, String, String, String)}
     * @throws SQLException
     */
    public static TableColumn ReadTableColumn(Table table, ResultSet rs, Pattern excludeIndirectColumns, Pattern excludeColumns) throws SQLException {
        String colName = rs.getString("COLUMN_NAME");
        String name = colName == null ? null : colName.intern();
        String comments = rs.getString("REMARKS");
        
    	TableColumn tableColumn = new TableColumn(table, name, comments);  
        String typeName = rs.getString("TYPE_NAME");
        tableColumn.setType(typeName == null ? "unknown" : typeName.intern());
        tableColumn.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
        
        Number bufLength = (Number)rs.getObject("BUFFER_LENGTH");
        int length;
        if (bufLength != null && bufLength.shortValue() > 0)
            length = bufLength.shortValue();
        else
            length = rs.getInt("COLUMN_SIZE");

        StringBuilder buf = new StringBuilder();
        buf.append(length);
        if (tableColumn.getDecimalDigits() > 0) {
            buf.append(',');
            buf.append(tableColumn.getDecimalDigits());
        }
        tableColumn.setDetailedSize(buf.toString());
        tableColumn.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
        tableColumn.setDefaultValue(rs.getString("COLUMN_DEF"));
        tableColumn.setId(new Integer(rs.getInt("ORDINAL_POSITION") - 1));
        tableColumn.setAllExcluded(tableColumn.matches(excludeColumns));
        tableColumn.setExcluded(tableColumn.isAllExcluded() || tableColumn.matches(excludeIndirectColumns));
        if (tableColumn.isExcluded() && finerEnabled) {
            logger.finer("Excluding column " + tableColumn.getTable() + '.' + tableColumn.getName() +
                        ": matches " + excludeColumns + ":" + tableColumn.isAllExcluded() + " " +
                        excludeIndirectColumns + ":" + tableColumn.matches(excludeIndirectColumns));
        }
        return tableColumn;
    }

    /**
     * Update the state of this column with the supplied {@link TableColumnMeta}.
     * Intended to be used with instances created by {@link #TableColumn(Table, TableColumnMeta)}.
     *
     * @param colMeta
     */
    public void update(TableColumn tableColumn, TableColumnMeta colMeta) {
        String newComments = colMeta.getComments();
        if (newComments != null)
        	tableColumn.setComments(newComments);

        if (!tableColumn.isPrimary() && colMeta.isPrimary()) {
        	tableColumn.getTable().setPrimaryColumn(tableColumn);
        }

        tableColumn.setAllowsImpliedParents(!colMeta.isImpliedParentsDisabled());
        tableColumn.setAllowsImpliedChildren(!colMeta.isImpliedChildrenDisabled());
        tableColumn.setExcluded(tableColumn.isExcluded() || colMeta.isExcluded());
        tableColumn.setAllExcluded(tableColumn.isAllExcluded()||colMeta.isAllExcluded());
    }
}