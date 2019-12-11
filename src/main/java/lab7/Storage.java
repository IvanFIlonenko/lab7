package lab7;

import org.zeromq.*;

public class Storage
{
    public static void main(String[] args)
    {
        try (ZContext ctx = new ZContext()) {
            ZMQ.Socket worker = ctx.createSocket(SocketType.DEALER);
            worker.setHWM(0);
            worker.setIdentity("W".getBytes(ZMQ.CHARSET));
            worker.connect("tcp://localhost:5556");
            while (!Thread.currentThread().isInterrupted()) {
                ZMsg msg = ZMsg.recvMsg(worker);
                ZFrame content = msg.getLast();
                String s = content.toString();
                System.out.println(s);
                msg.send(worker);
            }
        }
    }
}