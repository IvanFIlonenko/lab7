package lab7;

import org.zeromq.*;

import java.util.Scanner;

public class Storage
{
    private static String LOCALHOST5556 = "tcp://localhost:5556";
    private static String STORAGE_STARTED = "Storage started";
    private static String NULL_STRING = "";
    private static String INFO = "NOTIFY";
    private static String GET = "GET";
    private static String VALUE_EQUALS = "VALUE=";
    private static String PUT = "PUT";
    private static String VALUE_AT_POS = "Value at position ";
    private static String WAS_UPDATED = " was updated";
    private static String SPACE = " ";
    private static String MINUS = "-";

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
            ZMQ.Socket storage = ctx.createSocket(SocketType.DEALER);
            storage.setHWM(0);
            storage.connect(LOCALHOST5556);
            ZMQ.Poller poller = ctx.createPoller(1);
            poller.register(storage, ZMQ.Poller.POLLIN);
            long start = System.currentTimeMillis();
            System.out.println(STORAGE_STARTED);
            while (!Thread.currentThread().isInterrupted()) {
                poller.poll(1);
                if (System.currentTimeMillis() - start > 5000) {
                    ZMsg msg1 = new ZMsg();
                    msg1.addLast(NULL_STRING);
                    msg1.addLast(INFO);
                    msg1.addLast(Integer.toString(left));
                    msg1.addLast(Integer.toString(right));
                    msg1.addString(left + MINUS + right);
                    msg1.send(storage);
                    start = System.currentTimeMillis();
                }
                if (poller.pollin(0)) {
                    ZMsg msg = ZMsg.recvMsg(storage);
                    msg.unwrap();
                    String[] strMsgArr = msg.pollLast().toString().split(SPACE);
                    if (strMsgArr[0].equals(GET)){
                        msg.addLast(VALUE_EQUALS + str.charAt(Integer.parseInt(strMsgArr[1]) - left));
                    } else if(strMsgArr[0].equals(PUT)){
                        str = replaceChar(str,strMsgArr[2],Integer.parseInt(strMsgArr[1]) - left);
                        msg.addLast(VALUE_AT_POS + strMsgArr[1] + WAS_UPDATED);
                    }
                    msg.send(storage);
                }
            }
        }
    }

    public static String replaceChar(String str, String ch, int index) {
        return str.substring(0, index) + ch + str.substring(index+1);
    }
}