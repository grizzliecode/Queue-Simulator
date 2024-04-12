package business_logic;

import model.SelectionPolicy;
import model.Server;
import model.Task;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable{
    public ExecutorService executorService;
    public int ServersNr=3;
    public int TaskNr=5;
    public int maxTime=20;
    public int maxProcTime = 4;
    public int minProcTime=1;
    private Scheduler scheduler;
    private List<Task> tasks;
    public SelectionPolicy policy = SelectionPolicy.SHORTEST_TIME;
    public static AtomicInteger simulationTime = new AtomicInteger();
    public static AtomicBoolean endSim = new AtomicBoolean();
    public SimulationManager()
    {
        this.executorService = Executors.newFixedThreadPool(this.ServersNr+3);
        this.scheduler=new Scheduler(ServersNr,TaskNr);
        endSim.set(false);
        simulationTime.set(0);
        generateTasks();
        scheduler.changeStrategy(policy);
    }
    @Override
    public void run() {
        List<Future<Double>> futureResults = new ArrayList<>();
        for(Server server: this.scheduler.getServers())
        {
            Future<Double> waitingTime = executorService.submit(server);
            futureResults.add(waitingTime);
        }
        while(simulationTime.get()<maxTime)
        {
            if(testForContinuation())
            {
                updateLog();
                simulationTime.getAndIncrement();
                scheduler.dispatchTasks(tasks);
            }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

        }
        endSim.set(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Double avg = 0.0;
        for(Future<Double> w : futureResults)
        {
            try {
                if(w.isDone())
                {
                    avg+=w.get();
                }
                else {
                    System.out.println(w.toString());
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        avg = (double) avg/ServersNr;
        System.out.println("Avg waiting time: "+ Double.toString(avg));
        executorService.shutdown();
    }

    public void generateTasks()
    {
        this.tasks = new ArrayList<Task>();
        for(int i=0;i<TaskNr;i++)
        {
            Random random = new Random();
            int arrival,service;
            arrival= random.nextInt(maxProcTime,maxTime);
            service= random.nextInt(minProcTime,maxProcTime);
            Task newTask = new Task(i+1,arrival-service,service);
            this.tasks.add(newTask);
        }
        Collections.sort(this.tasks);
    }
    private boolean testForContinuation()
    {
        for (Server server : this.scheduler.getServers())
        {
            if(server.getServerTime().get()<simulationTime.get())
                return false;
        }
        return true;
    }

    private void updateLog()
    {
        System.out.println("Time "+Integer.toString(simulationTime.get()));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Waiting: ");
        for(Task waitingTask : this.tasks)
        {
            stringBuilder.append(waitingTask.toString());
        }
        System.out.println(stringBuilder.toString());
        for(Server server:this.scheduler.getServers())
        {
            if(server.getWaitingTime()==0)
            {
                System.out.println("Queue "+Integer.toString(this.scheduler.getServers().indexOf(server))+": Closed");
            }
            else {
                StringBuilder stringBuffer = new StringBuilder();
                stringBuffer.append("Queue ");
                stringBuffer.append(Integer.toString(this.scheduler.getServers().indexOf(server)));
                stringBuffer.append(": ");
                for(Task task : server.getTasks())
                {
                    stringBuffer.append(task.toString()).append(" ");
                }
                System.out.println(stringBuffer.toString());
            }
        }
    }

    public static void main(String[] args) {
        SimulationManager simulation = new SimulationManager();
        Thread t = new Thread(simulation);
        t.start();
    }
}
