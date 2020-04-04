package zhh_fu.miaosha.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.Util.UserKey;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.service.GoodsService;
import zhh_fu.miaosha.service.UserService;
import zhh_fu.miaosha.vo.GoodsVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    UserService userService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/to_list")
    public String toLogin(Model model,User user){
        model.addAttribute("user",user);
        //查询商品列表
        List<GoodsVo> goodsVoList = goodsService.getGoodsVoList();
        model.addAttribute("goodsList",goodsVoList);
        return "goods_list";
    }

    @RequestMapping("/to_detail/{goodsId}")
    public String toLogin(Model model, User user, @PathVariable("goodsId") long goodsId){
        model.addAttribute("user",user);
        //查询商品列表
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);

        //判断秒杀时间
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        //秒杀状态
        int miaoshaStatus = 0;
        //剩余开始时间
        int remainSeconds = 0;

        if (now < startAt){//秒杀还没开始，倒计时，状态为0
            miaoshaStatus = 0;
            remainSeconds = (int) (startAt - now)/1000;
        } else if(now > endAt){//秒杀已经结束,状态为2
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else{//秒杀进行中，状态为1
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        return "goods_detail";
    }
}
