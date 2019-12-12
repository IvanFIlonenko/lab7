package lab7;

import javafx.util.Pair;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;

import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.StreamSupport;

/**

 Round-trip demonstrator. Broker, Worker and Client are mocked as separate
 threads.

 */
public class Main
{
    private static HashMap<ZFrame, Pair<Integer,Integer>> storages;

    public static void main(String[] args)
    {
        try (ZContext ctx = new ZContext()) {
            Socket frontend = ctx.createSocket(SocketType.ROUTER);
            Socket backend = ctx.createSocket(SocketType.ROUTER);
            frontend.setHWM(0);
            backend.setHWM(0);
            frontend.bind("tcp://*:5555");
            backend.bind("tcp://*:5556");
            ZMQ.Poller items = ctx.createPoller(2);
            items.register(frontend, ZMQ.Poller.POLLIN);
            items.register(backend, ZMQ.Poller.POLLIN);

            while (!Thread.currentThread().isInterrupted()) {
                items.poll();
                if (items.pollin(0)) {
                    ZMsg msg = ZMsg.recvMsg(frontend);
                    if (msg == null)
                        break; // Interrupted
                    ZFrame address = msg.pop();
                    address.destroy();
                    msg.addFirst(new ZFrame("W"));
                    msg.send(backend);
                }

                if (items.pollin(1)) {
                    ZMsg msg = ZMsg.recvMsg(backend);
                    if (msg == null)
                        break; // Interrupted
                    ZFrame address = msg.pop();
                    if (msg.popString().equals("INFO")){
                        int left = Integer.parseInt(msg.popString());
                        int right = Integer.parseInt(msg.popString());
                        storages.put(address, new Pair<>(left,right));
                        System.out.println(address.toString() + "--" + left + "--" + right);
                    }

                    else {
                        System.out.println(msg.getLast().toString());
                        msg.addFirst(new ZFrame("C"));
                        msg.send(frontend);
                    }
                }
            }
        }
    }
}