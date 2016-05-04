package phisten.androfpv;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Created by Phisten on 2016/4/1.
 */
public class SocketChannelPool {
    static public SocketChannelPool getInstance() {
        return ourInstance;
    }
    static private SocketChannelPool ourInstance = new SocketChannelPool();


    //Channel
    public ServerSocketChannel MasterServerChannel;
    public SocketChannel MasterChannel;
    public DatagramChannel DetecterChannel;
    public int DataChannelCount = 3;
    public ArrayList<DatagramChannel> DataChannels;

    //Connect Info
    public String LocalIP;
    InetSocketAddress RemoteISA;
    private String RemoteIP;
    protected Handler handler;
    protected Message msg;
    static final int MasterServerChannelPort = 27390;
    static final int DetecterChannelPort = 27380;

    private SocketChannelPool() {
        DataChannels = new ArrayList<>();
        try {
            MasterServerChannel = ServerSocketChannel.open();
            MasterChannel = SocketChannel.open();
            DetecterChannel = DatagramChannel.open();
            MasterServerChannel.socket().bind(new InetSocketAddress(SocketChannelPool.MasterServerChannelPort));
            DetecterChannel.socket().bind(new InetSocketAddress(SocketChannelPool.DetecterChannelPort));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ThreadControl --------------------------------------------------------------------
    Handler DetecterChannelHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    Thread DetecterChannelThread;
    private Runnable DetecterChannelRun = new Runnable() {
        @Override
        public void run() {
            // TODO: getdata


            handler.sendMessage(msg);
        }
    };

    HandlerThread MasterChannelHandlerThread = new HandlerThread("Master");
    private Runnable MasterChannelRunnable = new Runnable() {
        @Override
        public void run() {








        }
    };


    private Runnable ReceiveRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };
    private Runnable SendRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };



    // ReceiveData --------------------------------------------------------------------
    // SendData --------------------------------------------------------------------
    public int DetectServer(String targetIP) throws Exception {
        Log.d(LogTag.OutputTest, "DetectServer: " +  targetIP + " , local:" + LocalIP);
        Thread t = new Thread(new Runnable() {
            String targetIP;
            public Runnable init(String targetIP) {
                this.targetIP = targetIP;
                return(this);
            }
            @Override
            public void run() {
                try {
                    //Init DetectPacket Buffer
                    ByteBuffer detectPacketBuf = ByteBuffer.allocate(5);
                    detectPacketBuf.clear();
                    detectPacketBuf.put(Packet.PacketType.MasterChannelConnectRequest.toByte());
                    //填入本地IP
                    detectPacketBuf.put(SocketChannelPool.HostIpConvertToBytes(LocalIP));
                    detectPacketBuf.flip();

                    //發出封包
                    InetSocketAddress inetSocketAddr = new InetSocketAddress(targetIP,DetecterChannelPort);
                    int bytesSent = DetecterChannel.send(detectPacketBuf,inetSocketAddr);
                    Log.d(LogTag.OutputTest,"DetectServer: bytesSent = " + String.format("%d",bytesSent));
                }catch(Exception e) {
                    Log.e(LogTag.Error,e.getMessage());
                }finally {
                    Log.d(LogTag.OutputTest,"DetectServer: Thread End");
                }
            }
        }.init(targetIP));
        t.start();
        return 0;
    }




    // Info GetSet --------------------------------------------------------------------
    public int SetRemoteIP(String host) throws Exception {
        if (validIP(host))
        {
            RemoteIP = host;
            return Succeed;
        }
        throw new Exception("IP規格不符");
        //return UndefinedError;
    }


    // IP Convert --------------------------------------------------------------------
    public static boolean validIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15)) return false;

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }
    /*將字串表示的IP轉換為4個int組成的陣列*/
    static public int[] HostIpConvertToInts(String hostIp) throws Exception {
        String[] ipStrArr = hostIp.split("\\.");
        if (ipStrArr.length != 4) {
            throw new Exception("func: HostIpConvertToInts IP Input Error(String[] ipStrArr.length == " + ipStrArr.length + " != 4 ) hostIp = " + hostIp.toString());
        }
        int[] ints = new int[4];
        for (int i = 0; i < 4; i++) {
            ints[i] = Integer.parseInt(ipStrArr[i]);
        }
        return ints;
    }
    /*將4個int組成之陣列所表示的IP轉換為字串表示*/
    static public String HostIpConvertToString(int[] hostIp) throws Exception {
        if (hostIp.length != 4) {
            throw new Exception("func: HostIpConvertToString IP Input Error(byte[] hostIp.length == " + hostIp.length + " != 4)");
        }
        String str1 = String.format("%d.%d.%d.%d",hostIp[0],hostIp[1],hostIp[2],hostIp[3]);
        Log.d(LogTag.OutputTest,str1);
        return str1;
    }
    /*將4個byte組成之陣列所表示的IP轉換為字串表示*/
    static public String HostIpConvertToString(byte[] hostIp) throws Exception {
        if (hostIp.length != 4) {
            throw new Exception("func: HostIpConvertToString IP Input Error(byte[] hostIp.length == " + hostIp.length + " != 4)");
        }
        int intConverter = 0;
        String str1 = String.format("%d.%d.%d.%d",intConverter | hostIp[0],intConverter | hostIp[1],intConverter | hostIp[2],intConverter | hostIp[3]);
        Log.d(LogTag.OutputTest,str1);
        return str1;
    }
    /*將字串表示的IP轉換為4組byte組成的陣列*/
    static public byte[] HostIpConvertToBytes(String hostIp) throws Exception {
        String[] ipStrArr = hostIp.split("\\.");
        if (ipStrArr.length != 4) {
            throw new Exception("func: HostIpConvertToInts IP Input Error(String[] ipStrArr.length == " + ipStrArr.length + " != 4 ) hostIp = " + hostIp.toString());
        }
        byte[] bs = new byte[4];
        for (int i = 0; i < 4; i++) {
            bs[i] = Byte.parseByte(ipStrArr[i]);
        }
        return bs;
    }
    /*將4個int組成之陣列所表示的IP轉換為byte陣列*/
    static public byte[] HostIpConvertToBytes(int[] hostIp) throws Exception {
        if (hostIp.length != 4) {
            throw new Exception("func: HostIpConvertToString IP Input Error(byte[] hostIp.length == " + hostIp.length + " != 4)");
        }
        byte[] bs = new byte[4];
        for (int i = 0; i < 4; i++) {
            bs[i] = (byte)(hostIp[i] & 0xff);
        }
        return bs;
    }

    // return Code --------------------------------------------------------------------
    static public final int Succeed = 0;
    static public final int UndefinedError = -1;

}
