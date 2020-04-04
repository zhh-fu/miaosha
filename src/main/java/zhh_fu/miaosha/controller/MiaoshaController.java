package zhh_fu.miaosha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zhh_fu.miaosha.pojo.MiaoshaOrder;
import zhh_fu.miaosha.pojo.OrderInfo;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.result.CodeMsg;
import zhh_fu.miaosha.service.GoodsService;
import zhh_fu.miaosha.service.MiaoshaService;
import zhh_fu.miaosha.service.OrderService;
import zhh_fu.miaosha.service.UserService;
import zhh_fu.miaosha.vo.GoodsVo;

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

    @RequestMapping("/do_miaosha")
    public String toLogin(Model model, User user, @RequestParam("goodsId") long goodsId){
        //未登录直接返回使其登陆
        if (user == null) return "login";
        model.addAttribute("user",user);

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        //判断库存
        int stockCount = goods.getStockCount();
        if (stockCount <= 0){
            model.addAttribute("errmsg",CodeMsg.MIAOSHA_OVER.getMsg());
            return "miaosha_fail";
        }

        //判断用户是否重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null){
            model.addAttribute("errmsg",CodeMsg.MIAOSHA_REPEAT.getMsg());
            return "miaosha_fail";
        }

        //可以正常秒杀，减库存，下订单，写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goods);
        return "order_detail";
    }

}
