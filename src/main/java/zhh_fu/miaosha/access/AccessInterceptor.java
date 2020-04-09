package zhh_fu.miaosha.access;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;
import zhh_fu.miaosha.Util.AccessKey;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.config.UserContext;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.result.CodeMsg;
import zhh_fu.miaosha.result.Result;
import zhh_fu.miaosha.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;


@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    UserService userService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            User user = getUser(request,response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();

            if (needLogin){
                if (user == null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }

            //设置混合键
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = jedisAdapter.get(ak,key,Integer.class);
            if (count == null){
                jedisAdapter.set(ak,key,1);
            } else if (count < maxCount){
                jedisAdapter.incr(ak,key);
            } else {
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private User getUser(HttpServletRequest request, HttpServletResponse response){
        String paramToken = request.getParameter(UserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserService.COOKIE_NAME_TOKEN);
            if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return null;
        }
        //设置优先级
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
            return userService.getByToken(response,token);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) return null;
        for (Cookie cookie : cookies){
            if (cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
