package zhh_fu.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zhh_fu.miaosha.DAO.GoodsDAO;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.Util.MiaoshaKey;
import zhh_fu.miaosha.pojo.Goods;
import zhh_fu.miaosha.pojo.MiaoshaOrder;
import zhh_fu.miaosha.pojo.OrderInfo;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.vo.GoodsVo;

import java.util.List;

@Service
public class MiaoshaService {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Transactional
    public OrderInfo miaosha(User user,GoodsVo goodsVo){
        //减库存  写入秒杀订单
        boolean success = goodsService.reduceStock(goodsVo);
        //下订单 写入秒杀订单
        //成功了创建订单，失败返回空
        if (success){
            return orderService.createOrder(user,goodsVo);
        } else {
            setGoodsOver(goodsVo.getId());
            return null;
        }

    }



    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId,goodsId);
        if (order != null){
            return order.getOrderId();
        } else{//订单为空的时候无法区分是秒杀失败还是在排队中
            boolean isOver = getGoodsOver(goodsId);
            if (isOver){
                return -1;
            } else{
                return 0;
            }
        }
    }

    //判断当前商品时库存是否没有了
    private void setGoodsOver(Long goodsId) {
        jedisAdapter.set(MiaoshaKey.isGoodsOver,"" + goodsId,true);

    }

    private boolean getGoodsOver(long goodsId) {
        return jedisAdapter.exists(MiaoshaKey.isGoodsOver,"" + goodsId);
    }
}
