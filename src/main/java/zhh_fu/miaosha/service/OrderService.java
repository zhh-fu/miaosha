package zhh_fu.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zhh_fu.miaosha.DAO.OrderDAO;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.Util.OrderKey;
import zhh_fu.miaosha.pojo.MiaoshaOrder;
import zhh_fu.miaosha.pojo.OrderInfo;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.vo.GoodsVo;

import java.util.Date;

@Service
public class OrderService {
    @Autowired
    OrderDAO orderDAO;

    @Autowired
    JedisAdapter jedisAdapter;

    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
        return jedisAdapter.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"-"+goodsId,MiaoshaOrder.class);
        //return orderDAO.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
    }

    @Transactional
    public OrderInfo createOrder(User user, GoodsVo goodsVo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        long orderId = orderDAO.insert(orderInfo);
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setOrderId(orderId);
        miaoshaOrder.setGoodsId(goodsVo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderDAO.insertMiaoshaOrder(miaoshaOrder);

        //写入缓存
        jedisAdapter.set(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"-"+goodsVo.getId(),miaoshaOrder);

        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDAO.getOrderById(orderId);
    }
}
