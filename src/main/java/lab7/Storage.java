package lab7;

import org.zeromq.*;

public class Storage
{
    private static String str;
    private static int left;
    private static int right;

    public static void main(String[] args)
    {
        left = Integer.parseInt(args[1]);
        right = Integer.parseInt(args[2]);
        str = args[0].substring(left, right);
        try (ZContext ctx = new ZContext()) {
            ZMQ.Socket worker = ctx.createSocket(SocketType.DEALER);
            worker.setHWM(0);
            worker.setIdentity("W".getBytes(ZMQ.CHARSET));
            worker.connect("tcp://localhost:5556");
            ZMQ.Poller poller = ctx.createPoller(1);
            poller.register(worker, ZMQ.Poller.POLLIN);
            long start = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted()) {
                poller.poll(1);
                if (System.currentTimeMillis() - start > 5000) {
                    ZMsg msg1 = new ZMsg();
                    msg1.addString(left + "-" + right);
                    msg1.send(worker);
                    //worker.send(left + "-" + right);
                    //poller.pollout(0);
                    //start = System.currentTimeMillis();
                }
                if (poller.pollin(0)) {
                    ZMsg msg = ZMsg.recvMsg(worker);
                    ZFrame content = msg.getLast();
                    String s = content.toString();
                    System.out.println(s);
                    msg.send(worker);
                }
            }
        }
    }
}