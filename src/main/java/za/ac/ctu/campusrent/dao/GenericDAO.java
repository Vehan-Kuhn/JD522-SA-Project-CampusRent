
package za.ac.ctu.campusrent.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic DAO interface for reusable database operations.
 *
 * @param <T> entity type
 * @param <ID> primary key type
 */
public interface GenericDAO<T, ID> {

    ID insert(T entity) throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(ID id) throws SQLException;
}
