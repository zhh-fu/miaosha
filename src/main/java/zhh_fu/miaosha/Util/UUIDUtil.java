package zhh_fu.miaosha.Util;

import java.util.UUID;

public class UUIDUtil {
    public static String uuid(){
        //原生的UUID带“-”
        return UUID.randomUUID().toString().replace("-","");
    }
}
