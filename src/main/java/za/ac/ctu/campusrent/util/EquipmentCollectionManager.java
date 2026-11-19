package za.ac.ctu.campusrent.util;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.model.EquipmentCategory;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Demonstrates the Java Collections Framework
 * using CampusRent equipment and booking data.
 */
public class EquipmentCollectionManager {

    // List: stores equipment records in an ordered collection.
    private final List<Equipment> equipmentList =
            new ArrayList<>();

    // Set: prevents duplicate equipment serial numbers.
    private final Set<String> serialNumbers =
            new HashSet<>();

    // EnumMap: groups equipment by its category.
    private final EnumMap<EquipmentCategory, List<Equipment>>
            equipmentByCategory =
            new EnumMap<>(EquipmentCategory.class);

    // Deque: stores actions in reverse order for undo functionality.
    private final Deque<Runnable> undoHistory =
            new ArrayDeque<>();

    // PriorityQueue: keeps the highest-priority request first.
    private final PriorityQueue<WaitingRequest> waitingList =
            new PriorityQueue<>(
                    Comparator.comparingInt(WaitingRequest::priority)
                            .reversed()
                            .thenComparing(WaitingRequest::requestTime)
            );

    public EquipmentCollectionManager() {

        for (EquipmentCategory category :
                EquipmentCategory.values()) {

            equipmentByCategory.put(
                    category,
                    new ArrayList<>()
            );
        }
    }

    /**
     * Adds equipment while preventing duplicate serial numbers.
     */
    public boolean addEquipment(Equipment equipment) {

        if (equipment == null
                || equipment.getSerialNumber() == null) {
            return false;
        }

        String serial = equipment.getSerialNumber()
                .trim()
                .toUpperCase();

        if (serial.isEmpty() || !serialNumbers.add(serial)) {
            return false;
        }

        try {

            EquipmentCategory category =
                    EquipmentCategory.valueOf(
                            equipment.getCategory().toUpperCase());

            equipmentList.add(equipment);

            equipmentByCategory
                    .get(category)
                    .add(equipment);

            return true;

        } catch (IllegalArgumentException
                 | NullPointerException ex) {

            serialNumbers.remove(serial);

            return false;
        }
    }

    /**
     * Returns all equipment.
     */
    public List<Equipment> getAllEquipment() {
        return new ArrayList<>(equipmentList);
    }

    /**
     * Returns equipment belonging to a category.
     */
    public List<Equipment> getEquipmentByCategory(
            EquipmentCategory category) {

        return new ArrayList<>(
                equipmentByCategory.getOrDefault(
                        category,
                        Collections.emptyList())
        );
    }

    /**
     * Sorts equipment by name and then serial number.
     */
    public List<Equipment> getSortedEquipment() {

        List<Equipment> sorted =
                new ArrayList<>(equipmentList);

        sorted.sort(
                Comparator.comparing(
                                Equipment::getEquipmentName,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(
                                Equipment::getSerialNumber,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER))
        );

        return sorted;
    }

    /**
     * Demonstrates Collections.sort().
     */
    public List<Equipment> sortUsingCollections() {

        List<Equipment> sorted =
                new ArrayList<>(equipmentList);

        Collections.sort(
                sorted,
                Comparator.comparing(
                        Equipment::getEquipmentName,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER))
        );

        return sorted;
    }

    /**
     * Demonstrates Collections.reverse().
     */
    public List<Equipment> getReverseEquipment() {

        List<Equipment> reversed =
                new ArrayList<>(equipmentList);

        Collections.reverse(reversed);

        return reversed;
    }

    /**
     * Adds an action to the undo history.
     */
    public void recordUndoAction(Runnable undoAction) {

        if (undoAction != null) {
            undoHistory.push(undoAction);
        }
    }

    /**
     * Executes the most recently recorded undo action.
     */
    public boolean undoLastAction() {

        if (undoHistory.isEmpty()) {
            return false;
        }

        Runnable lastAction = undoHistory.pop();

        lastAction.run();

        return true;
    }

    /**
     * Adds a booking request to the waiting list.
     */
    public void addWaitingRequest(
            int userId,
            int equipmentId,
            int priority) {

        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException(
                    "Priority must be between 1 and 5.");
        }

        waitingList.offer(
                new WaitingRequest(
                        userId,
                        equipmentId,
                        priority,
                        LocalDateTime.now()
                )
        );
    }

    /**
     * Retrieves the next highest-priority waiting request.
     */
    public WaitingRequest getNextWaitingRequest() {
        return waitingList.poll();
    }

    public int getWaitingListSize() {
        return waitingList.size();
    }

    public int getEquipmentCount() {
        return equipmentList.size();
    }

    public int getUniqueSerialCount() {
        return serialNumbers.size();
    }

    /**
     * Immutable waiting-list record.
     */
    public record WaitingRequest(
            int userId,
            int equipmentId,
            int priority,
            LocalDateTime requestTime) {
    }
}
