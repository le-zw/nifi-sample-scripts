import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDP报文接收
 * @author zhongwei.long
 * @date 2021年04月12日 下午12:25
 */
public class UDPReceive {
    public static void main(String[] args) {
        udpReceive();
    }



    public static void udpReceive(){
        try {
            DatagramSocket socket = new DatagramSocket(9877);
            byte[] data = new byte[255];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            while (true){
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                System.out.println("已经收到长度为:"+packet.getData().length);
            }
            //socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}