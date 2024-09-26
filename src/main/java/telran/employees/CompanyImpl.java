package telran.employees;

import java.util.*;

public class CompanyImpl implements Company {
    private TreeMap<Long, Employee> employees = new TreeMap<>();
    private HashMap<String, List<Employee>> employeesDepartment = new HashMap<>();
    private TreeMap<Float, List<Manager>> managerFactor = new TreeMap<>();

    private class CompanyIterator implements Iterator<Employee> {
        Iterator<Employee> iterator = employees.values().iterator();
        Employee lastIterated;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Employee next() {
            lastIterated = iterator.next();
            return lastIterated;
        }

        @Override
        public void remove() {
            iterator.remove();
            removeFromIndexMaps(lastIterated);
        }
    }

    @Override
    public Iterator<Employee> iterator() {
        return new CompanyIterator();
    }

    @Override
    public void addEmployee(Employee empl) {
        long id = empl.getId();
        if (employees.putIfAbsent(id, empl) != null) {
            throw new IllegalStateException("employee already exitis" + id);
        }
        addIndexMaps(empl);
    }

    private void addIndexMaps(Employee empl) {
        employeesDepartment.computeIfAbsent(empl.getDepartment(), k -> new ArrayList<>()).add(empl);
        if (empl instanceof Manager manager) {
            managerFactor.computeIfAbsent(manager.getFactor(), k -> new ArrayList<>()).add(manager);
        }
    }

    @Override
    public Employee getEmployee(long id) {
        return employees.get(id);
    }

    @Override
    public Employee removeEmployee(long id) {
        Employee empl = employees.remove(id);
        if (empl == null) {
            throw new NoSuchElementException("employee not found" + id);
        }
        removeFromIndexMaps(empl);
        return empl;
    }

    private void removeFromIndexMaps(Employee empl) {
        removeIndexMap(empl.getDepartment(), employeesDepartment, empl);
        if (empl instanceof Manager manager) {
            removeIndexMap(manager.getFactor(), managerFactor, manager);
        }
    }

    private <K, V extends Employee> void removeIndexMap(K key, Map<K, List<V>> map, V empl) {
        List<V> list = map.get(key);
        list.remove(empl);
        if (list.isEmpty()) {
            map.remove(key);
        }
    }

    @Override
    public int getDepartmentBudget(String department) {
        return employeesDepartment.getOrDefault(department, Collections.emptyList())
                .stream().mapToInt(Employee::computeSalary).sum();
    }

    @Override
    public String[] getDepartments() {
        return employeesDepartment.keySet().stream().sorted().toArray(String[]::new);
    }

    @Override
    public Manager[] getManagersWithMostFactor() {
        Manager[] res = new Manager[0];
        if (!managerFactor.isEmpty()) {
            res = managerFactor.lastEntry().getValue().toArray(res);
        }
        return res;
    }
}
