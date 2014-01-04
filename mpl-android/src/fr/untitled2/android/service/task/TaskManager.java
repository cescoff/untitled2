package fr.untitled2.android.service.task;

import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.common.utils.DateTimeUtils;
import org.javatuples.Triplet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
public class TaskManager {

    private static TaskManager instance;

    private Map<Triplet<String, Integer, TimeUnit>, ITask> tasks = Maps.newHashMap();

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private TaskManager() {
    }

    private synchronized static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    public static void addTask(ITask iTask) {
        getInstance().addTaskInternal(iTask);
    }

    public static List<ITask> getTasks() {
        List<ITask> tasksList = Lists.newArrayList();
        for (Triplet<String, Integer, TimeUnit> key : getInstance().tasks.keySet()) {
            tasksList.add(getInstance().tasks.get(key));
        }
        return tasksList;
    }

    public static Multimap<SchedulingService.Scheduling, Future<Boolean>> executeTasks(Multimap<SchedulingService.Scheduling, ITask> tasksToExecute) {
        return getInstance().executeBatchTasks(tasksToExecute);
    }

    private void addTaskInternal(ITask iTask) {
        Triplet<String, Integer, TimeUnit> key = Triplet.with(iTask.getClass().getName(), iTask.getScheduling().getFrequency(), iTask.getScheduling().getTimeUnit());
        if (tasks.containsKey(key)) return;
        tasks.put(key, iTask);
    }

    private Multimap<SchedulingService.Scheduling, Future<Boolean>> executeBatchTasks(Multimap<SchedulingService.Scheduling, ITask> tasksToExecute) {
        Multimap<SchedulingService.Scheduling, Future<Boolean>> result = HashMultimap.create();
        for (SchedulingService.Scheduling scheduling : tasksToExecute.keySet()) {
            for (ITask iTask : tasksToExecute.get(scheduling)) {
                result.put(scheduling, executorService.submit(iTask));
            }
        }
        return result;
    }

}
