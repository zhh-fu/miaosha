package zhh_fu.miaosha.Util;

public class MiaoshaKey extends BasePrefix{
    private MiaoshaKey(String prefix){
        super(prefix);
    }

    public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
}
