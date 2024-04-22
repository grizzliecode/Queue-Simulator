package business_logic;

import com.example.queuesimulator.SimulationFrame;
import javafx.stage.Stage;
import model.SelectionPolicy;
import model.Server;
import model.Task;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable{
    public ExecutorService executorService;
    private StringBuilder log = new StringBuilder();
    private  int totalServiceTime = 0;
    private int peakHour = 0;
    private int peakClientNumber = 0;
    public int ServersNr=20;
    public int TaskNr=1000;
    public int maxTime=100;
    private int maxProcTime = 9;
    private int minProcTime=3;
    private int arrTime = 10;
    private int stopArrTime = 100;
    private Scheduler scheduler;
    private List<Task> tasks;
    public SelectionPolicy policy = SelectionPolicy.SHORTEST_TIME;
    public static AtomicInteger simulationTime = new AtomicInteger();
    public static AtomicBoolean endSim = new AtomicBoolean();
    private ArrayList<ArrayList<String>> clients;
    public static AtomicBoolean update = new AtomicBoolean(false);

    public ArrayList<ArrayList<String>> getClients() {
        return clients;
    }

    public SimulationManager()
    {
        this.executorService = Executors.newFixedThreadPool(this.ServersNr+3);
        this.scheduler=new Scheduler(ServersNr,TaskNr);
        endSim.set(false);
        simulationTime.set(0);
        generateTasks();
        scheduler.changeStrategy(policy);
    }
    public SimulationManager(int maxTime, int taskNr, int serversNr, SelectionPolicy selectionPolicy, int minArr, int maxArr, int minSrv, int maxSrv)
    {
        this.minProcTime = minSrv;
        this.maxProcTime = maxSrv;
        this.arrTime = minArr;
        this.stopArrTime = maxArr;
        this.ServersNr = serversNr;
        this.TaskNr = taskNr;
        this.maxTime=maxTime;
        this.policy =  selectionPolicy;
        this.executorService = Executors.newFixedThreadPool(this.ServersNr+3);
        this.scheduler=new Scheduler(ServersNr,TaskNr);
        this.clients= new ArrayList<ArrayList<String>>(serversNr);
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
                while (SimulationManager.update.get()) {
                }
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
//        System.out.println("Avg waiting time: "+ Double.toString(avg));
        log.append("Average waiting time: "+ Double.toString(avg)+"\n");
        log.append("Average service time: "+Double.toString((double) totalServiceTime/TaskNr)+"\n");
        log.append("Peak hour: "+Integer.toString(peakHour)+"\n");
        try {
            writeFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        executorService.shutdown();
    }

    public void generateTasks()
    {
        this.tasks = new ArrayList<Task>();
        for(int i=0;i<TaskNr;i++)
        {
            Random random = new Random();
            int arrival,service;
            arrival= random.nextInt(arrTime,stopArrTime);
            service= random.nextInt(minProcTime,maxProcTime);
            totalServiceTime+=service;
            Task newTask = new Task(i+1,arrival,service);
            this.tasks.add(newTask);
        }
        this.tasks.sort(null);
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
        this.clients.clear();
        int clientNumber = 0;
//        System.out.println("Time "+Integer.toString(simulationTime.get()));
        log.append("Time "+Integer.toString(simulationTime.get())+"\n");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Waiting: ");
        for(Task waitingTask : this.tasks)
        {
            stringBuilder.append(waitingTask.toString());
        }
//        System.out.println(stringBuilder.toString());
        log.append(stringBuilder.toString()+"\n");
        for(Server server:this.scheduler.getServers())
        {
            ArrayList<String> serverClients = new ArrayList<>();
            if(server.getWaitingTime()==0)
            {
//                System.out.println("Queue "+Integer.toString(this.scheduler.getServers().indexOf(server))+": Closed");
                log.append("Queue "+Integer.toString(this.scheduler.getServers().indexOf(server))+": Closed\n");
            }
            else {
                StringBuilder stringBuffer = new StringBuilder();
                stringBuffer.append("Queue ");
                stringBuffer.append(Integer.toString(this.scheduler.getServers().indexOf(server)));
                stringBuffer.append(": ");
                for(Task task : server.getTasks())
                {
                    stringBuffer.append(task.toString()).append(" ");
                    clientNumber++;
                    serverClients.add(task.toString());
                }
                log.append(stringBuffer.toString()+"\n");
            }
            this.clients.add(serverClients);
        }
        SimulationManager.update.set(true);
        if(peakClientNumber<clientNumber) {
            peakClientNumber=clientNumber;
            peakHour = simulationTime.get();
        }
    }


    public void writeFile() throws IOException {
        Writer writer = new FileWriter("LOG.txt",false);
        writer.write(log.toString());
        writer.close();
    }
}
