package fly.mybatis;

import org.apache.ibatis.cursor.Cursor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 对象列表指针
 *
 * @author FlyInWind
 * @date 2021/02/12
 */
public class ObjectListCursor implements Cursor<List<Object>> {
    private final ObjectListResultSetHandler.StatementContainer statementContainer;

    List<Object> row;

    int index = -1;

    boolean fetched = false;

    CursorIterator iterator = new CursorIterator();


    /**
     * 从 {@link org.apache.ibatis.cursor.defaults.DefaultCursor} 拷贝过来
     */
    private CursorStatus status = CursorStatus.CREATED;

    public ObjectListCursor(ObjectListResultSetHandler.StatementContainer statementContainer) {
        this.statementContainer = statementContainer;
    }

    private enum CursorStatus {

        /**
         * A freshly created cursor, database ResultSet consuming has not started.
         */
        CREATED,
        /**
         * A cursor currently in use, database ResultSet consuming has started.
         */
        OPEN,
        /**
         * A closed cursor, not fully consumed.
         */
        CLOSED,
        /**
         * A fully consumed cursor, a consumed cursor is always closed.
         */
        CONSUMED
    }

    @Override
    public boolean isOpen() {
        return status == CursorStatus.OPEN;
    }

    @Override
    public boolean isConsumed() {
        return status == CursorStatus.CONSUMED;
    }

    @Override
    public int getCurrentIndex() {
        return index;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        if (statementContainer.resultSet != null) {
            try {
                statementContainer.resultSet.close();
            } catch (SQLException e) {
                // ignore
            } finally {
                status = CursorStatus.CLOSED;
            }
        }
    }

    private boolean isClosed() {
        return status == CursorStatus.CLOSED || status == CursorStatus.CONSUMED;
    }

    private boolean fetchNext() {
        try {
            if (!fetched) {
                fetched = (row = statementContainer.next()) != Collections.emptyList();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (fetched) {
            index++;
        }
        return fetched;
    }

    @Override
    public Iterator<List<Object>> iterator() {
        if (this.iterator == null) {
            throw new IllegalStateException("Cannot open more than one iterator on a Cursor");
        }
        if (isClosed()) {
            throw new IllegalStateException("A Cursor is already closed.");
        }
        CursorIterator iterator = this.iterator;
        this.iterator = null;
        return iterator;
    }

    class CursorIterator implements Iterator<List<Object>> {
        @Override
        public boolean hasNext() {
            if (!fetched) {
                fetchNext();
            }
            return fetched;
        }

        @Override
        public List<Object> next() {
            if (fetchNext()) {
                fetched = false;
                return row;
            }
            throw new NoSuchElementException();
        }
    }
}
