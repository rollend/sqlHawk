package net.sourceforge.schemaspy.view;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.Dot;

/**
 * Represents Graphvis dot's concept of an edge.  That is, a connector between two nodes.
 *
 * @author John Currier
 */
public class DotConnector implements Comparable<DotConnector> {
    private final TableColumn parentColumn;
    private final Table parentTable;
    private final TableColumn childColumn;
    private final Table childTable;
    private final boolean implied;
    private final boolean bottomJustify;
    private String parentPort;
    private String childPort;

    /**
     * Create an edge that logically connects a child column to a parent column.
     *
     * @param parentColumn TableColumn
     * @param childColumn TableColumn
     * @param implied boolean
     */
    public DotConnector(TableColumn parentColumn, TableColumn childColumn, boolean implied) {
        this.parentColumn = parentColumn;
        this.childColumn = childColumn;
        this.implied = implied;
        parentPort = parentColumn.getName();
        parentTable = parentColumn.getTable();
        childPort = childColumn.getName();
        childTable = childColumn.getTable();
        bottomJustify = !Dot.getInstance().supportsCenteredEastWestEdges();
    }

    /**
     * Returns true if this edge logically "points to" the specified table
     *
     * @param possibleParentTable Table
     * @return boolean
     */
    public boolean pointsTo(Table possibleParentTable) {
        return possibleParentTable.equals(parentTable);
    }

    public boolean isImplied() {
        return implied;
    }

    /**
     * By default a parent edge connects to the column name...this lets you
     * connect it the parent's type column instead (e.g. for detailed parents)
     *
     * Yes, I need to find a more appropriate name/metaphor for this method....
     */
    public void connectToParentDetails() {
        parentPort = parentColumn.getName() + ".type";
    }

    public void connectToParentTitle() {
        //parentPort = parentColumn.getTable().getName() + ".heading";
        parentPort = "elipses";
    }

    public void connectToChildTitle() {
        //childPort = childColumn.getTable().getName() + ".heading";
        childPort = "elipses";
    }

    @Override
    public String toString() {
        StringBuilder edge = new StringBuilder();
        edge.append("  \"");
        if (childTable.isRemote()) {
            edge.append(childTable.getSchema());
            edge.append('.');
        }
        edge.append(childTable.getName());
        edge.append("\":\"");
        edge.append(childPort);
        edge.append("\":");
        if (bottomJustify)
            edge.append("s");
        edge.append("w -> \"");
        if (parentTable.isRemote()) {
            edge.append(parentTable.getSchema());
            edge.append('.');
        }
        edge.append(parentTable.getName());
        edge.append("\":\"");
        edge.append(parentPort);
        edge.append("\":");
        if (bottomJustify)
            edge.append("s");
        edge.append("e ");

        // if enabled makes the diagram unreadable
        // have to figure out how to render these details in a readable manner
        final boolean fullErNotation = false;

        // Thanks to Dan Zingaro for figuring out how to correctly annotate
        // these relationships
        if (fullErNotation) {
            // PK end of connector
            edge.append("[arrowhead=");
            if (childColumn.isNullable())
                edge.append("odottee"); // zero or one parents
            else
                edge.append("teetee");  // one parent
        } else {
            // PK end of connector
            edge.append("[arrowhead=none");
        }

        // FK end of connector
        edge.append(" arrowtail=");
        if (childColumn.isUnique())
            edge.append("teeodot"); // zero or one children
        else
            edge.append("crowodot");// zero or more children

        if (implied)
            edge.append(" style=dashed");
        edge.append("];");

        return edge.toString();
    }

    public int compareTo(DotConnector other) {
        int rc = childTable.compareTo(other.childTable);
        if (rc == 0)
            rc = childColumn.getName().compareToIgnoreCase(other.childColumn.getName());
        if (rc == 0)
            rc = parentTable.compareTo(other.parentTable);
        if (rc == 0)
            rc = parentColumn.getName().compareToIgnoreCase(other.parentColumn.getName());
        if (rc == 0 && implied != other.implied)
            rc = implied ? 1 : -1;
        return rc;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DotConnector))
            return false;
        return compareTo((DotConnector)other) == 0;
    }

    @Override
    public int hashCode() {
        int p = parentTable == null ? 0 : parentTable.getName().hashCode();
        int c = childTable == null ? 0 : childTable.getName().hashCode();
        return (p << 16) & c;
    }

    public TableColumn getParentColumn() {
        return parentColumn;
    }

    public Table getParentTable() {
        return parentTable;
    }

    public TableColumn getChildColumn() {
        return childColumn;
    }

    public Table getChildTable() {
        return childTable;
    }
}
