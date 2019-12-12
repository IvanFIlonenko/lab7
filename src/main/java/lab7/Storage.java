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
            //worker.setIdentity("W".getBytes(ZMQ.CHARSET));
            worker.connect("tcp://localhost:5556");
            ZMQ.Poller poller = ctx.createPoller(1);
            poller.register(worker, ZMQ.Poller.POLLIN);
            long start = System.currentTimeMillis();
            boolean check = true;
            while (!Thread.currentThread().isInterrupted()) {
                poller.poll(1);
                //if (System.currentTimeMillis() - start > 5000) {
                if (check){
                    check = false;
                    ZMsg msg1 = new ZMsg();
                    msg1.addLast("INFO");
                    msg1.addLast(Integer.toString(left));
                    msg1.addLast(Integer.toString(right));
                    msg1.addString(left + "-" + right);
                    msg1.send(worker);
                    start = System.currentTimeMillis();
                }
                //if (poller.pollin(0)) {
                    ZMsg msg = ZMsg.recvMsg(worker);
                    msg.unwrap();
                    String[] strMsgArr = msg.pollLast().toString().split(" ");
                    if (strMsgArr[0].equals("GET")){
                        msg.addLast("VALUE=" + str.charAt(Integer.parseInt(strMsgArr[1])));
                    }
                    msg.send(worker);
                //}
            }
        }
    }
}