package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Main {
    public static void main(String[] args){

    }

    public void run(){
        ZContext ctx = new ZContext();
        ZMQ.Socket snapshot = ctx.createSocket(SocketType.ROUTER);
        snapshot.bind("tcp://*.5556");
        
    }
}
