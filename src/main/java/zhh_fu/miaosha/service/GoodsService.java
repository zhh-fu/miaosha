package zhh_fu.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zhh_fu.miaosha.DAO.GoodsDAO;
import zhh_fu.miaosha.pojo.Goods;
import zhh_fu.miaosha.pojo.MiaoshaGoods;
import zhh_fu.miaosha.vo.GoodsVo;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    GoodsDAO goodsDAO;

    public List<GoodsVo> getGoodsVoList(){
        return goodsDAO.getGoodsVoList();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDAO.getGoodsVoByGoodsId(goodsId);
    }

    public void reduceStock(GoodsVo goodsVo) {
        MiaoshaGoods goods = new MiaoshaGoods();
        goods.setGoodsId(goodsVo.getId());
        //此处在sql语句中执行完成
        //goods.setStockCount(goodsVo.getStockCount() - 1);
        goodsDAO.reduceStock(goods);
    }
}
