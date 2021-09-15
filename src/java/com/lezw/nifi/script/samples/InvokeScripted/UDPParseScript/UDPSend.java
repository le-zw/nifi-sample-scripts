import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 发送UDP报文
 * @author zhongwei.long
 * @date 2021年04月12日 下午12:25
 */
public class UDPSend {
    public static void main(String[] args) throws IOException {
        //创建数据包对象，封装要发送的数据，接受端IP,端口
        byte[] data=Double.toString(Math.random()).getBytes();
        //创建InetAddress对象，封装自己的IP地址
        InetAddress inet=InetAddress.getByName("127.0.0.1");
        DatagramPacket dp=new DatagramPacket(data,data.length,inet,9877);
        //创建DatagramSocket对象，数据包的发送和接受对象
        DatagramSocket ds=new DatagramSocket();
        //调用ds对象的方法send，发送数据包
        ds.send(dp);
        ds.close();

    }
}