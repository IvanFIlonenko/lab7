package lab7;

import org.zeromq.*;

import java.util.Scanner;

public class Storage
{
    private static String str;
    private static int left;
    private static int right;

    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        String temp = in.nextLine();
        left = Integer.parseInt(in.nextLine());
        right = Integer.parseInt(in.nextLine());
        str = temp.substring(left, right);
        try (ZContext ctx = new ZContext()) {
            ZMQ.Socket worker = ctx.createSocket(SocketType.DEALER);
            worker.setHWM(0);
            //worker.setIdentity("W".getBytes(ZMQ.CHARSET));
            worker.connect("tcp://localhost:5556");
            ZMQ.Poller poller = ctx.createPoller(1);
            poller.register(worker, ZMQ.Poller.POLLIN);
            long start = System.currentTimeMillis();
            boolean check = true;
            System.out.println("Storage started");
            while (!Thread.currentThread().isInterrupted()) {
                poller.poll(1);
                //if (System.currentTimeMillis() - start > 5000) {
                if (check){
                    check = false;
                    ZMsg msg1 = new ZMsg();
                    msg1.addLast("");
                    msg1.addLast("INFO");
                    msg1.addLast(Integer.toString(left));
                    msg1.addLast(Integer.toString(right));
                    msg1.addString(left + "-" + right);
                    msg1.send(worker);
                    start = System.currentTimeMillis();
                }
                if (poller.pollin(0)) {
                    ZMsg msg = ZMsg.recvMsg(worker);
                    msg.unwrap();
                    String[] strMsgArr = msg.pollLast().toString().split(" ");
                    if (strMsgArr[0].equals("GET")){
                        msg.addLast("VALUE=" + str.charAt(Integer.parseInt(strMsgArr[1])));
                    } else if(strMsgArr[0].equals("PUT")){
                        str = replaceChar(str,strMsgArr[2],Integer.parseInt(strMsgArr[1]));
                        msg.addLast("Value at position " + strMsgArr[1] + " was updated");
                    }
                    msg.send(worker);
                }
            }
        }
    }

    public static String replaceChar(String str, String ch, int index) {
        return str.substring(0, index) + ch + str.substring(index+1);
    }
}