package zhh_fu.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.controller.MiaoshaController;
import zhh_fu.miaosha.pojo.MiaoshaOrder;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.service.GoodsService;
import zhh_fu.miaosha.service.MiaoshaService;
import zhh_fu.miaosha.service.OrderService;
import zhh_fu.miaosha.vo.GoodsVo;

@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        log.info("receive message :" + message);
        MiaoshaMessage mm = JedisAdapter.stringToBean(message,MiaoshaMessage.class);
        User user = mm.getUser();
        long goodsId = mm.getGoodsId();

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单（终于用到了数据库）
        miaoshaService.miaosha(user, goods);
    }
}
