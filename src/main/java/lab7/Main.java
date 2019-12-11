package lab7;

import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

/**
 * ROUTER-TO-REQ example
 */
public class Main
{
    private static Random    rand        = new Random();
    private static final int NBR_WORKERS = 1;

    /**
     * While this example runs in a single process, that is just to make
     * it easier to start and stop the example. Each thread has its own
     * context and conceptually acts as a separate process.
     */
    public static void main(String[] args) throws Exception {
        try (ZContext context = new ZContext()) {
            Socket broker = context.createSocket(SocketType.ROUTER);
            broker.bind("tcp://*:5671");

            //  Run for five seconds and then tell workers to end
            long endTime = System.currentTimeMillis() + 500000;
            int workersFired = 0;
            while (true) {
                //  Next message gives us least recently used worker
                String identity = broker.recvStr();
                broker.sendMore(identity);
                broker.recv(0); //  Envelope delimiter
                broker.recv(0); //  Response from worker
                broker.sendMore("");

                //  Encourage workers until it's time to fire them
                if (System.currentTimeMillis() < endTime)
                    broker.send("Work harder");
                else {
                    broker.send("Fired!");
                    if (++workersFired == NBR_WORKERS)
                        break;
                }
            }
        }
    }
}