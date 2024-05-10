package service;

import exceptions.LoadingFromFileException;
import model.EpicTask;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    private Path backupFile;

    private String[] readBackupFile(Path backupFile) {
        final String content;
        try {
            content = Files.readString(backupFile);
        } catch (IOException e) {
            throw new LoadingFromFileException("Ошибка чтения/загрузки из файла.", e);
        }
        return content.split(System.lineSeparator());
    }

    @BeforeEach
    public void BeforeEach() {
        try {
            backupFile = Files.createTempFile(Paths.get("test_resources"), "backupFileTest", ".csv");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании временного файла backupFileTest.", e);
        }
        backupFile.toFile().deleteOnExit();

        manager = new FileBackedTaskManager(backupFile.toFile());

        task = new Task("TaskTitle_1", "TaskDesc_1");
        manager.createTask(task);

        epic = new EpicTask("EpicTitle_2", "EpicDesc_2");
        manager.createEpicTask(epic);
        Integer epicId = epic.getId();

        sub = new Subtask("SubtaskTitle_3", "SubtaskDesc_3", epicId);
        manager.createSubtask(sub);

        subWithStatusDone = new Subtask("newSubTitle", "newSubDesc", epicId);
        subWithStatusDone.setId(sub.getId());
        subWithStatusDone.setStatus(TaskStatus.DONE);

        subWithStatusInProgress = new Subtask("newSubTitle2", "newSubDesc2", epicId);
        subWithStatusInProgress.setId(sub.getId() + 1);
        subWithStatusInProgress.setStatus(TaskStatus.IN_PROGRESS);
    }

    @Override
    @Test
    public void idShouldIncreaseBy1AfterExecutingMethodGenerateId() {
        super.idShouldIncreaseBy1AfterExecutingMethodGenerateId();
    }

    @Override
    @Test
    public void newTaskAddedToAllTasksList() {
        super.newTaskAddedToAllTasksList();
        String[] lines = readBackupFile(backupFile);
        assertEquals("1,TASK,TaskTitle_1,NEW,TaskDesc_1,", lines[1],
                "Строка в файле не совпадает с задачей.");
    }

    @Override
    @Test
    public void newSubtaskAddedToAllSubtasksList() {
        super.newSubtaskAddedToAllSubtasksList();
        String[] lines = readBackupFile(backupFile);
        assertEquals("3,SUBTASK,SubtaskTitle_3,NEW,SubtaskDesc_3,2", lines[3],
                "Строка в файле не совпадает с подзадачей.");
    }

    @Override
    @Test
    public void newEpicTaskAddedToAllSubtasksList() {
        super.newEpicTaskAddedToAllSubtasksList();
        String[] lines = readBackupFile(backupFile);
        assertEquals("2,EPIC,EpicTitle_2,NEW,EpicDesc_2,", lines[2],
                "Строка в файле не совпадает с эпиком.");
    }

    @Override
    @Test
    public void allTasksListIsEmptyWhenDeleteAllTasks() {
        super.allTasksListIsEmptyWhenDeleteAllTasks();
        String[] lines = readBackupFile(backupFile);
        assertEquals(3, lines.length, "Количество строк не уменьшилось на единицу.");
        assertEquals("2,EPIC,EpicTitle_2,NEW,EpicDesc_2,", lines[1],
                "Эпик не перезаписан в нужную строку после удаления задачи.");
        assertEquals("3,SUBTASK,SubtaskTitle_3,NEW,SubtaskDesc_3,2", lines[2],
                "Подзадача не перезаписана в нужную строку после удаления задачи.");
    }

    @Override
    @Test
    public void allSubtasksListIsEmptyWhenDeleteAllSubtasks() {
        super.allSubtasksListIsEmptyWhenDeleteAllSubtasks();
        String[] lines = readBackupFile(backupFile);
        assertEquals(3, lines.length, "Количество строк не уменьшилось на единицу.");
        assertEquals("1,TASK,TaskTitle_1,NEW,TaskDesc_1,", lines[1],
                "Строка в файле не совпадает с задачей.");
        assertEquals("2,EPIC,EpicTitle_2,NEW,EpicDesc_2,", lines[2],
                "Строка в файле не совпадает с эпиком.");
    }

    @Override
    @Test
    public void allEpictasksListIsEmptyWhenDeleteAllEpictasks() {
        super.allEpictasksListIsEmptyWhenDeleteAllEpictasks();
        String[] lines = readBackupFile(backupFile);
        assertEquals(2, lines.length, "Количество строк не уменьшилось на 2.");
        assertEquals("1,TASK,TaskTitle_1,NEW,TaskDesc_1,", lines[1],
                "Строка в файле не совпадает с задачей.");
    }

    @Override
    @Test
    public void allSubtasksListIsEmptyWhenDeleteAllEpictasks() {
        super.allSubtasksListIsEmptyWhenDeleteAllEpictasks();
        String[] lines = readBackupFile(backupFile);
        assertEquals(2, lines.length, "Количество строк не уменьшилось на 2.");
        assertEquals("1,TASK,TaskTitle_1,NEW,TaskDesc_1,", lines[1],
                "Строка в файле не совпадает с задачей.");
    }

    @Override
    @Test
    public void epicHasStatusDoneIfAllSubtasksHasStatusDone() {
        super.epicHasStatusDoneIfAllSubtasksHasStatusDone();
        String[] lines = readBackupFile(backupFile);
        assertEquals("2,EPIC,EpicTitle_2,DONE,EpicDesc_2,", lines[2],
                "Статус эпика в файле не был перезаписан в статус DONE.");
    }

    @Override
    @Test
    public void epicStatusIsInProgressIfEpicSubsListHasSubtaskWithStatusInProgress() {
        super.epicStatusIsInProgressIfEpicSubsListHasSubtaskWithStatusInProgress();
        String[] lines = readBackupFile(backupFile);
        assertEquals("2,EPIC,EpicTitle_2,IN_PROGRESS,EpicDesc_2,", lines[2],
                "Статус эпика в файле не был перезаписан в статус IN_PROGRESS.");
    }

    @Override
    @Test
    public void epicStatusIsInProgressIfEpicSubsListHasSubtaskWithStatusNew() {
        super.epicStatusIsInProgressIfEpicSubsListHasSubtaskWithStatusNew();
        String[] lines = readBackupFile(backupFile);
        assertEquals("2,EPIC,EpicTitle_2,IN_PROGRESS,EpicDesc_2,", lines[2],
                "Статус эпика в файле не был перезаписан в статус IN_PROGRESS.");
    }

    @Test
    public void shouldLoadTasksFromFile() {
        TaskManager newFBTManager = FileBackedTaskManager.loadFromFile(backupFile.toFile());
        final int totalTasks = newFBTManager.getAllTasks().size() + newFBTManager.getAllEpicTasks().size() +
                newFBTManager.getAllSubtasks().size();

        assertEquals(3, totalTasks);

        assertEquals(1, newFBTManager.getTask(1).getId());
        assertEquals("TaskTitle_1", newFBTManager.getTask(1).getTitle());
        assertEquals("TaskDesc_1", newFBTManager.getTask(1).getDescription());
        assertEquals(TaskStatus.NEW, newFBTManager.getTask(1).getStatus());

        assertEquals(2, newFBTManager.getEpicTask(2).getId());
        assertEquals("EpicTitle_2", newFBTManager.getEpicTask(2).getTitle());
        assertEquals("EpicDesc_2", newFBTManager.getEpicTask(2).getDescription());
        assertEquals(TaskStatus.NEW, newFBTManager.getEpicTask(2).getStatus());

        assertEquals(3, newFBTManager.getSubtask(3).getId());
        assertEquals("SubtaskTitle_3", newFBTManager.getSubtask(3).getTitle());
        assertEquals("SubtaskDesc_3", newFBTManager.getSubtask(3).getDescription());
        assertEquals(TaskStatus.NEW, newFBTManager.getSubtask(3).getStatus());
        assertEquals(2, newFBTManager.getSubtask(3).getEpicId());
    }

}