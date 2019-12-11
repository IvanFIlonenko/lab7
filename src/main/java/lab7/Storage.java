package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

static class Worker implements Runnable
{
    @Override
    public void run()
    {
        try (ZContext ctx = new ZContext()) {
            ZMQ.Socket worker = ctx.createSocket(SocketType.DEALER);
            worker.setHWM(0);
            worker.setIdentity("W".getBytes(ZMQ.CHARSET));
            worker.connect("tcp://localhost:5556");
            while (!Thread.currentThread().isInterrupted()) {
                ZMsg msg = ZMsg.recvMsg(worker);
                //String s = msg.popString();
                System.out.println(msg);
                msg.send(worker);
            }
        }
    }
}