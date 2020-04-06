package zhh_fu.miaosha.Util;

public class OrderKey extends BasePrefix {

    private OrderKey(String prefix){
        super(prefix);
    }

    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("moug");

}
