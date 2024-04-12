package business_logic;

import model.Server;
import model.Task;

import java.util.List;

public class TimeStartegy implements Strategy{
    @Override
    public void addTask(List<Server> servers, Task task) {
        int pos=-1;
        int min = Integer.MAX_VALUE;
        for(Server server: servers)
        {
            int value = server.getWaitingTime();
            if(value < min)
            {
                min = value;
                pos = servers.indexOf(server);
            }
        }
        servers.get(pos).addTask(task);
    }
}
