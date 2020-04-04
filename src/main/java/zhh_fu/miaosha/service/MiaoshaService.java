package zhh_fu.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zhh_fu.miaosha.DAO.GoodsDAO;
import zhh_fu.miaosha.pojo.Goods;
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

    @Transactional
    public OrderInfo miaosha(User user,GoodsVo goodsVo){
        //减库存  写入秒杀订单
        goodsService.reduceStock(goodsVo);
        //下订单 写入秒杀订单
        return orderService.createOrder(user,goodsVo);
    }
}
