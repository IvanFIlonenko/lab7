package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;
import java.util.Scanner;

public class Client {
    private static Random rand        = new Random();
    public static void main(String[] args){
        try (ZContext context = new ZContext()) {
            Scanner in = new Scanner(System.in);
            ZMQ.Socket worker = context.createSocket(SocketType.REQ);

            worker.connect("tcp://localhost:5671");

            int total = 0;
            while (true) {
                //  Tell the broker we're ready for work
                worker.sendMore("");
                String s = in.nextLine();
                worker.send(s);

                //  Get workload from broker, until finished
                String reply = worker.recvStr();
                System.out.println(reply);
//                boolean finished = workload.equals("Fired!");
//                if (finished) {
//                    System.out.printf("Completed: %d tasks\n", total);
//                    break;
//                }
//                total++;

                //  Do some random work
//                try {
//                    Thread.sleep(rand.nextInt(500) + 1);
//                }
//                catch (InterruptedException e) {
//                }
            }
        }
    }
}
