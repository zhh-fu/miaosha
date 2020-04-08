package zhh_fu.miaosha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zhh_fu.miaosha.Util.*;
import zhh_fu.miaosha.pojo.MiaoshaOrder;
import zhh_fu.miaosha.pojo.OrderInfo;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.rabbitmq.MQSender;
import zhh_fu.miaosha.rabbitmq.MiaoshaMessage;
import zhh_fu.miaosha.result.CodeMsg;
import zhh_fu.miaosha.result.Result;
import zhh_fu.miaosha.service.GoodsService;
import zhh_fu.miaosha.service.MiaoshaService;
import zhh_fu.miaosha.service.OrderService;
import zhh_fu.miaosha.service.UserService;
import zhh_fu.miaosha.vo.GoodsVo;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
    private static Logger log = LoggerFactory.getLogger(MiaoshaController.class);

    @Autowired
    UserService userService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    MQSender mqSender;

    //做一个标记
    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    @RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model,User user,
                                   @RequestParam("goodsId") long goodsId,
                                   @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证path
        boolean check = miaoshaService.checkPath(user,goodsId,path);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over){
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //从redis减库存
        long stock =  jedisAdapter.decr(GoodsKey.getMiaoshaGoodsStock, ""+ goodsId);
        if (stock < 0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }

        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.MIAOSHA_REPEAT);
        }

        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        mqSender.sentMiaoshaMessage(mm);
        //排队中
        return  Result.success(0);
        /*
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
        */
    }

    /**
     * orderId :成功
     * -1：秒杀成功
     * 0：排队中
     */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,User user,
                                   @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(),goodsId);
        return Result.success(result);
    }

    /**
     * 系统初始化
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.getGoodsVoList();
        if (goodsVoList == null) return;
        for (GoodsVo goodsVo : goodsVoList){
            //存放到redis中
            jedisAdapter.set(GoodsKey.getMiaoshaGoodsStock, ""+goodsVo.getId(),goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(),false);
        }
    }

    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(Model model,User user,
                                   @RequestParam("goodsId") long goodsId,
                                   @RequestParam("verifyCode") int verifyCode) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //检查验证码
        boolean check = miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user,goodsId);
        return Result.success(path);
    }

    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, Model model, User user,
                                               @RequestParam("goodsId") long goodsId) {

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        BufferedImage image = miaoshaService.createVerifyCode(user,goodsId);
        try{
            OutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;
        } catch (Exception ex){
            ex.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
