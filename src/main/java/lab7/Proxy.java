package lab7;

import org.zeromq.*;
import org.zeromq.ZMQ.Socket;
import java.util.HashMap;
import java.util.Map;


public class Proxy
{
    private static String TCP5555 = "tcp://*:5555";
    private static String TCP5556 = "tcp://*:5556";
    private static String PROXY_STARTED = "Proxy started";
    private static String SPACE = " ";
    private static String GET = "GET";
    private static String NO_VALUE = "No value with such index";
    private static String NOTIFY = "NOTIFY";
    private static String DOUBLE_MINUS = "--";

    private static HashMap<ZFrame, StorageData> storages;

    public static void main(String[] args)
    {
        storages = new HashMap<>();
        try (ZContext ctx = new ZContext()) {
            Socket frontend = ctx.createSocket(SocketType.ROUTER);
            Socket backend = ctx.createSocket(SocketType.ROUTER);
            frontend.setHWM(0);
            backend.setHWM(0);
            frontend.bind(TCP5555);
            backend.bind(TCP5556);
            ZMQ.Poller items = ctx.createPoller(2);
            items.register(frontend, ZMQ.Poller.POLLIN);
            items.register(backend, ZMQ.Poller.POLLIN);
            System.out.println(PROXY_STARTED);
            while (!Thread.currentThread().isInterrupted()) {
                    for (Map.Entry<ZFrame, StorageData> entry : storages.entrySet()) {
                        if (System.currentTimeMillis() - entry.getValue().getTime() > 10000){
                            storages.remove(entry.getKey());
                        }
                    }
                items.poll();
                if (items.pollin(0)) {
                    ZMsg msg = ZMsg.recvMsg(frontend);
                    if (msg == null)
                        break;
                    String[] strMsgArr = msg.getLast().toString().split(SPACE);
                    boolean found = false;
                    for (Map.Entry<ZFrame, StorageData> entry : storages.entrySet()) {
                        if (entry.getValue().getLeft() <= Integer.parseInt(strMsgArr[1]) && entry.getValue().getRight() > Integer.parseInt(strMsgArr[1])) {
                            if (!found) {
                                msg.wrap(entry.getKey().duplicate());
                            }
                            found = true;
                            msg.send(backend);
                            if (strMsgArr[0].equals(GET)) {
                                break;
                            }
                        }
                    }
                    if (!found){
                        msg.pollLast();
                        msg.addLast(NO_VALUE);
                        msg.send(frontend);
                    }
                }

                if (items.pollin(1)) {
                    ZMsg msg = ZMsg.recvMsg(backend);
                    if (msg == null)
                        break;
                    ZFrame address = msg.unwrap();
                    if (msg.getFirst().toString().equals(NOTIFY)){
                        msg.pop();
                        int left = Integer.parseInt(msg.popString());
                        int right = Integer.parseInt(msg.popString());
                        storages.put(address, new StorageData(left,right,System.currentTimeMillis()));
                        System.out.println(address.toString() + DOUBLE_MINUS + left + DOUBLE_MINUS + right);
                    }

                    else {
                        System.out.println(msg.getLast().toString());
                        msg.send(frontend);
                    }
                }
            }
        }
    }
}