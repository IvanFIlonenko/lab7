package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Scanner;

public class Client {
    private static String TCP5555 = "tcp://localhost:5555";
    private static String CLIENT_STARTED = "Client Started";

    public static void main(String[] args) {
        try (ZContext ctx = new ZContext()) {
            ZMQ.Socket client = ctx.createSocket(SocketType.REQ);
            client.setHWM(0);
            client.connect(TCP5555);

            System.out.println(CLIENT_STARTED);

            Scanner in = new Scanner(System.in);
            while (true) {
                String message = in.nextLine();
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