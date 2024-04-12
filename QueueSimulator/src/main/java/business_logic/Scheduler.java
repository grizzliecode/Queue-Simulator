package business_logic;

import model.SelectionPolicy;
import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    Strategy strategy;
    private List<Server> servers;
    private int maxServerNr;
    private int maxTaskNr;
    public Scheduler(int maxServerNr, int maxTaskNr)
    {
        this.maxServerNr=maxServerNr;
        this.maxTaskNr=maxTaskNr;
        servers = new ArrayList<Server>();
        for(int i=1;i<=this.maxServerNr;i++)
        {
            Server server = new Server(maxTaskNr);
            servers.add(server);
        }
    }

    public void changeStrategy(SelectionPolicy policy)
    {
        if(policy.equals(SelectionPolicy.SHORTEST_TIME))
        {
            this.strategy = new TimeStartegy();
        }else {
            this.strategy=new ShortestQueueStrategy();
        }
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public void dispatchTasks(List<Task> tasks)
    {
        ArrayList<Integer> toEliminate = new ArrayList<>();
        for(int i=0;i<tasks.size();i++)
        {
            if(tasks.get(i).getArrivalTime().get()<=SimulationManager.simulationTime.get())
            {
                strategy.addTask(this.servers,tasks.get(i));
                toEliminate.add(i);
            }
        }
        for(Integer integer : toEliminate)
        {
            tasks.remove(tasks.get(integer.intValue()-toEliminate.indexOf(integer)));
        }
    }

}
