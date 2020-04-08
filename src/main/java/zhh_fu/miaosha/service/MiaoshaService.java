package zhh_fu.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.Util.MD5Util;
import zhh_fu.miaosha.Util.MiaoshaKey;
import zhh_fu.miaosha.Util.UUIDUtil;
import zhh_fu.miaosha.pojo.Goods;
import zhh_fu.miaosha.pojo.MiaoshaOrder;
import zhh_fu.miaosha.pojo.OrderInfo;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.vo.GoodsVo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

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
        //System.out.println(order.getOrderId());
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

    public boolean checkPath(User user, long goodsId, String path) {
        if (user == null || path == null) return false;
        String pathOld = jedisAdapter.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);
        //传入的path和存储的path是否相等
        return path.equals(pathOld);
    }

    //生成path参数，并存储
    public String createMiaoshaPath(User user, long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        jedisAdapter.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, str);
        return str;
    }

    public BufferedImage createVerifyCode(User user, long goodsId) {
        if(user == null || goodsId <= 0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        jedisAdapter.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    public boolean checkVerifyCode(User user, long goodsId, int verifyCode) {
        if(user == null || goodsId <= 0) {
            return false;
        }
        Integer codeOld = jedisAdapter.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
        if(codeOld == null || codeOld != verifyCode ) {
            return false;
        }
        jedisAdapter.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return true;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

}
