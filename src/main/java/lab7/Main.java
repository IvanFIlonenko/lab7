package lab7;

import org.zeromq.*;
import org.zeromq.ZMQ.Socket;

import java.util.Scanner;

/**

 Round-trip demonstrator. Broker, Worker and Client are mocked as separate
 threads.

 */
public class Main
{
    static class Broker implements Runnable
    {
        @Override
        public void run()
        {
            try (ZContext ctx = new ZContext()) {
                Socket frontend = ctx.createSocket(SocketType.ROUTER);
                Socket backend = ctx.createSocket(SocketType.ROUTER);
                frontend.setHWM(0);
                backend.setHWM(0);
                frontend.bind("tcp://*:5555");
                backend.bind("tcp://*:5556");

                while (!Thread.currentThread().isInterrupted()) {
                    ZMQ.Poller items = ctx.createPoller(2);
                    items.register(frontend, ZMQ.Poller.POLLIN);
                    items.register(backend, ZMQ.Poller.POLLIN);

                    if (items.poll() == -1)
                        break; // Interrupted

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
                        address.destroy();
                        msg.addFirst(new ZFrame("C"));
                        msg.send(frontend);
                    }

                    items.close();
                }
            }
        }
    }


    public static void main(String[] args)
    {
        if (args.length == 1)
            Client.SAMPLE_SIZE = Integer.parseInt(args[0]);

        Thread brokerThread = new Thread(new Broker());
        Thread workerThread = new Thread(new Worker());
        //Thread clientThread = new Thread(new Client());

        brokerThread.setDaemon(true);
        workerThread.setDaemon(true);

        brokerThread.start();
        workerThread.start();
        //clientThread.start();

        try {
            //clientThread.join();
            workerThread.interrupt();
            brokerThread.interrupt();
            Thread.sleep(200);// give them some time
        }
        catch (InterruptedException e) {
        }
    }
}