package model;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class Task  implements Comparable<Task> {
    private AtomicInteger Id;
    private AtomicInteger arrivalTime;
    private AtomicInteger serviceTime;

    public Task(int id, int arrivalTime, int serviceTime) {
        this.Id = new AtomicInteger(id);
        this.arrivalTime = new AtomicInteger(arrivalTime);
        this.serviceTime = new AtomicInteger(serviceTime);
    }

    public AtomicInteger getId() {
        return Id;
    }

    public void setId(AtomicInteger id) {
        Id = id;
    }

    public AtomicInteger getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(AtomicInteger arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public AtomicInteger getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(AtomicInteger serviceTime) {
        this.serviceTime = serviceTime;
    }

    @Override
    public int compareTo(@NotNull Task o) {
      if(this.arrivalTime.get()>o.arrivalTime.get())
          return 1;
      return 0;
    }

    @Override
    public String toString() {
        return "("+Integer.toString(getId().get())+", "+Integer.toString(getArrivalTime().get())+", "+Integer.toString(getServiceTime().get())+")";
    }
}
