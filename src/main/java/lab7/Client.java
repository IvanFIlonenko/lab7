package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Scanner;

public class Client
{

    public static void main(String[] args)
    {
        try (ZContext ctx = new ZContext()) {
            ZMQ.Socket client = ctx.createSocket(SocketType.REQ);
            client.setHWM(0);
            client.setIdentity("C".getBytes(ZMQ.CHARSET));
            client.connect("tcp://localhost:5555");
            System.out.println("Setting up test");

            System.out.println("Client Started");

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