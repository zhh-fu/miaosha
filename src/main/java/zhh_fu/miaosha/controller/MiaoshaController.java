package zhh_fu.miaosha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zhh_fu.miaosha.pojo.MiaoshaOrder;
import zhh_fu.miaosha.pojo.OrderInfo;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.result.CodeMsg;
import zhh_fu.miaosha.result.Result;
import zhh_fu.miaosha.service.GoodsService;
import zhh_fu.miaosha.service.MiaoshaService;
import zhh_fu.miaosha.service.OrderService;
import zhh_fu.miaosha.service.UserService;
import zhh_fu.miaosha.vo.GoodsVo;

import javax.xml.ws.Response;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {
    private static Logger log = LoggerFactory.getLogger(MiaoshaController.class);

    @Autowired
    UserService userService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @RequestMapping(value="/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> miaosha(Model model,User user,
                                     @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.MIAOSHA_REPEAT);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
    }

}
