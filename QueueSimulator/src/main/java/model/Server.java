package model;

import business_logic.SimulationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Callable<Double> {
    private BlockingQueue<Task> queue;
    private AtomicInteger waitingTime;
    private AtomicInteger totalWait;
    private AtomicInteger persons;
    private AtomicInteger serverTime;
    @Override
    public Double call() throws Exception {
        while(!SimulationManager.endSim.get())
        {
            if(SimulationManager.simulationTime.get()==this.serverTime.get())
            {
                if(queue.peek()!=null){
                    int newTime = queue.peek().getServiceTime().get()-1;
                    if(newTime==0)
                    {
                        queue.take();
                    }
                    else {
                        queue.peek().setServiceTime(new AtomicInteger(newTime));
                    }
                    this.waitingTime.getAndDecrement();
                }
                serverTime.getAndIncrement();
            }
        }
       if(persons.get()>0) return (double) (totalWait.get()/persons.get());
       else return 0.0;
    }

    public Server(int capacity) {
        this.queue = new ArrayBlockingQueue<Task>(capacity);
        this.waitingTime = new AtomicInteger(0);
        this.totalWait = new AtomicInteger(0);
        this.persons = new AtomicInteger(0);
        this.serverTime=new AtomicInteger(0);
    }

    public int getWaitingTime()
    {
        return waitingTime.get();
    }
    public void setWaitingTime(int val)
    {
        this.waitingTime.set(val);
    }
    public int getSize()
    {
        return this.queue.size();
    }

    public void addTask(Task task)
    {
        this.queue.add(task);
        int newWait = this.getWaitingTime();
        this.waitingTime.getAndAdd(newWait);
        this.totalWait.getAndAdd(task.getServiceTime().get());
        this.persons.getAndIncrement();
        newWait+=task.getServiceTime().get();
        this.setWaitingTime(newWait);
    }
    public Task[] getTasks()
    {
        Task[] tasks = new Task[]{};
        return this.queue.toArray(tasks);
    }

    public AtomicInteger getServerTime() {
        return serverTime;
    }

    public void setServerTime(AtomicInteger serverTime) {
        this.serverTime = serverTime;
    }
}
