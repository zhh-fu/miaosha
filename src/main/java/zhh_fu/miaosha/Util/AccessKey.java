package zhh_fu.miaosha.Util;

public class AccessKey extends BasePrefix{


    private AccessKey(String prefix){
        super(prefix);
    }

    private AccessKey(int expireSeconds, String prefix){
        super(expireSeconds, prefix);
    }

    public static AccessKey withExpire(int expireSeconds){
        return new AccessKey(expireSeconds,"access");
    }
}
