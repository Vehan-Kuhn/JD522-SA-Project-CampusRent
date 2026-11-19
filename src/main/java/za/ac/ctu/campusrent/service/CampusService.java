package za.ac.ctu.campusrent.service;
import za.ac.ctu.campusrent.dao.CampusDAO;
import za.ac.ctu.campusrent.model.Campus;
import java.sql.SQLException;
import java.util.List;

/** Handles campus business logic and validation. **/
public class CampusService {

    private final CampusDAO campusDAO;

    // Default constructor
    public CampusService() {
        this.campusDAO = new CampusDAO();
    }

    // Register a new campus
    public int addCampus(Campus campus) throws SQLException {

        validateCampus(campus);

        return campusDAO.addCampus(campus);
    }

    // Retrieve a campus by ID
    public Campus getCampusById(int campusId) throws SQLException {

        if (campusId <= 0) {
            throw new IllegalArgumentException("Invalid campus ID.");
        }

        return campusDAO.getCampusById(campusId);
    }

    // Retrieve all campuses
    public List<Campus> getAllCampuses() throws SQLException {

        return campusDAO.getAllCampuses();
    }

    // Update an existing campus
    public boolean updateCampus(Campus campus) throws SQLException {

        if (campus == null || campus.getCampusId() <= 0) {
            throw new IllegalArgumentException("Invalid campus information.");
        }

        validateCampus(campus);

        return campusDAO.updateCampus(campus);
    }

    // Delete a campus
    public boolean deleteCampus(int campusId) throws SQLException {

        if (campusId <= 0) {
            throw new IllegalArgumentException("Invalid campus ID.");
        }

        return campusDAO.deleteCampus(campusId);
    }

    // Validate campus information
    private void validateCampus(Campus campus) {

        if (campus == null) {
            throw new IllegalArgumentException("Campus cannot be empty.");
        }

        if (campus.getCampusName() == null
                || campus.getCampusName().isBlank()) {
            throw new IllegalArgumentException("Campus name is required.");
        }

        if (campus.getCity() == null || campus.getCity().isBlank()) {
            throw new IllegalArgumentException("Campus city is required.");
        }

        if (campus.getEmail() != null && !campus.getEmail().isBlank()
                && !campus.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Please enter a valid campus email.");
        }
    }
}
