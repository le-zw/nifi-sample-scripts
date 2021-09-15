import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileTime 转 UnixTime
 * @author zhongwei.long
 * @date 2021年04月13日 下午2:06
 */
public class FileTime {

    /**
     * Unix计时开始时间(1970-01-01 00:00:00)与FileTime计时开始时间(1601-01-01 00:00:00)
     * 毫秒数差值
     */
    private final static long UNIX_FILETIME_MILLISECOND_DIFF = 11644473600000L;

    /**
     * FileTime采用100ns为单位的，定义100ns与1ms的倍率
     */
    private final static int MILLISECOND_100NANOSECOND_MULTIPLE = 10000;

    private final long fileTime;

    public FileTime(byte[] bytes) {
        this.fileTime = bytesToLong(bytes);
    }

    /**
     * 将FileTime转为Date类型
     * @author zhongwei.long
     * @date 2021/4/14 上午10:35
     * @return String Date: yyyy-MM-dd HH:mm:ss
     */
    public String toDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(fileTime / MILLISECOND_100NANOSECOND_MULTIPLE - UNIX_FILETIME_MILLISECOND_DIFF);
        return sdf.format(date);
    }

    /**
     * 计算测点值
     * @author zhongwei.long
     * @date 2021/4/14 上午9:41
     * @param bytes 8字节测点值,eg: 6F F8 C3 CF 7F 5F 5C 40
     * @return double
     */
    public static double bytes2Double(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value |= ((long) (bytes[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    /**
     * 计算测点ID
     * @author zhongwei.long
     * @date 2021/4/13 下午1:25
     * @param bytes 4字节测点ID,eg: 03 00 00 00
     * @return int
     */
    public static int bytes2Int(byte[] bytes) {
        int ints = 0;
        for (byte item : bytes) {
            ints += Byte.toUnsignedLong(item);
        }
        return ints;
    }

    /**
     * 计算时间戳
     * @author zhongwei.long
     * @date 2021/4/14 上午10:29
     * @param bytes 8字节时间戳,eg: 00 77 A3 D6 46 30 D7 01
     * @return long FileTime以64位数字表示的值
     */
    private long bytesToLong(byte[] bytes) {
        long[] dims = new long[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            dims[i] = (bytes[i] & 0xFF);
            if (i>0){
                dims[i] <<= (8 * i);
            }
        }
        long result = dims[0];
        for (int i = 1; i < dims.length; i++) {
            result |= dims[i];
        }
        return result;
    }


    @Override
    public String toString() {
        return "fileTime: " + fileTime;
    }
}