package zhh_fu.miaosha.Util;

public class GoodsKey extends BasePrefix {

    private GoodsKey(int expireTime, String prefix){
        super(expireTime,prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60,"gl");
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"gd");
    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0,"gs");

}
