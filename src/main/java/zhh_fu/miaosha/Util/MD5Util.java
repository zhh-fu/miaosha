package zhh_fu.miaosha.Util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.scheduling.support.SimpleTriggerContext;

public class MD5Util {
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    //固定的salt
    private static final String salt = "1a2b3c4d";
    //明文密码经过md5之后传给表单提交,第一层md5
    public static String inputPassToFormPass(String inputPass){
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //表单提交的已经固定salt并md5之后的串再经过随机salt并md5后存到库中
    public static String formPassToDBPass(String formPass, String salt){
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //完整流程,saltDB是随机的
    public static String inputPassToDBPass(String inputPass, String saltDB){
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass,saltDB);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }
}
