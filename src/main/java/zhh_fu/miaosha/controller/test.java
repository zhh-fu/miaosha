package zhh_fu.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.Util.UserKey;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.rabbitmq.MQSender;
import zhh_fu.miaosha.result.CodeMsg;
import zhh_fu.miaosha.result.Result;
import zhh_fu.miaosha.service.UserService;

@Controller
@RequestMapping
public class test {
    @Autowired
    UserService userService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    MQSender mqSender;

    /*
    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        mqSender.sent("hello,zhegeshijie");
        return Result.success("hello world");
    }*/

    @RequestMapping("/test")
    @ResponseBody
    public Result<User> hello(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/testError")
    @ResponseBody
    public Result<String> helloError(){
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    @RequestMapping("/testThy")
    public String helloError(Model model){
        model.addAttribute("name","fuzhihang");
        return "hello";
    }



    @RequestMapping("/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = jedisAdapter.get(UserKey.getById, "-1",User.class);
        return Result.success(user);
    }
}
