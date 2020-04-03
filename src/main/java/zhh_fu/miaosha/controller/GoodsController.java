package zhh_fu.miaosha.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.Util.UserKey;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.service.UserService;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    UserService userService;

    @RequestMapping("/to_list")
    public String toLogin(Model model,User user){
        model.addAttribute("user",user);
        return "goods_list";
    }

    @RequestMapping("/to_detail")
    public String detail(Model model,HttpServletResponse response,
                          //有的并不是在cookie中传的
                          @CookieValue(value = UserService.COOKIE_NAME_TOKEN,required = false) String cookieToken,
                          @RequestParam(value = UserService.COOKIE_NAME_TOKEN,required = false) String paramToken){
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return "login";
        }
        //设置优先级
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        User user = userService.getByToken(response,token);
        model.addAttribute("user",user);
        return "goods_list";
    }
}
