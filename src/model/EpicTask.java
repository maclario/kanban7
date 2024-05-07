package model;

import java.util.ArrayList;

public class EpicTask extends Task {
    private ArrayList<Integer> subtasks = new ArrayList<>();

    public EpicTask(String title, String description) {
        super(title, description);
    }

    public void addSubtask(Integer id) {
        subtasks.add(id);
    }

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    }

    public void removeLinkedSubtask(Integer id) {
        subtasks.remove(id);
    }

    public void deleteSubtasks() {
        subtasks.clear();
    }

    @Override
    public String toString() {
        return "EpicTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}