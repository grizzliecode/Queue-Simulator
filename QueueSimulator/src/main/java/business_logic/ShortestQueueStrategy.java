package business_logic;

import model.Server;
import model.Task;

import java.util.List;

public class ShortestQueueStrategy implements Strategy{
    @Override
    public void addTask(List<Server> servers, Task task) {
        int min = Integer.MAX_VALUE;
        int pos =0;
        for(Server server : servers)
        {
            int size = server.getSize();
            if(size<min)
            {
                min = size;
                pos=servers.indexOf(server);
            }
        }
        servers.get(pos).addTask(task);
    }
}
