import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Scanner;

static class Client implements Runnable
{
    private static int SAMPLE_SIZE = 10000;

    @Override
    public void run()
    {
        try (ZContext ctx = new ZContext()) {
            ZMQ.Socket client = ctx.createSocket(SocketType.REQ);
            client.setHWM(0);
            client.setIdentity("C".getBytes(ZMQ.CHARSET));
            client.connect("tcp://localhost:5555");
            System.out.println("Setting up test");
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            int requests;
            long start;

            System.out.println("Synchronous round-trip test");
            start = System.currentTimeMillis();

            Scanner in = new Scanner(System.in);
            while (true) {
                String message = in.nextLine();
                if (message.equals("Stop")) {
                    break;
                }
                ZMsg req = new ZMsg();
                req.addString(message);
                req.send(client);
                ZMsg ans = ZMsg.recvMsg(client);
                String s = ans.popString();
                System.out.println(s);
                ans.destroy();
            }
        }
    }
}