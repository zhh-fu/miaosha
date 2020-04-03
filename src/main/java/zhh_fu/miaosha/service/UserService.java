package zhh_fu.miaosha.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zhh_fu.miaosha.DAO.UserDAO;
import zhh_fu.miaosha.Util.JedisAdapter;
import zhh_fu.miaosha.Util.MD5Util;
import zhh_fu.miaosha.Util.UUIDUtil;
import zhh_fu.miaosha.Util.UserKey;
import zhh_fu.miaosha.exception.GlobalException;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.result.CodeMsg;
import zhh_fu.miaosha.vo.LoginVo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {
    @Autowired
    UserDAO userDAO;

    @Autowired
    JedisAdapter jedisAdapter;

    public static final String COOKIE_NAME_TOKEN = "token";

    public User getById(long id){
        return userDAO.getById(id);
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //判断手机号是否存在
        User user = getById(Long.parseLong(mobile));
        if (user == null){
            throw new GlobalException(CodeMsg.USER_NOT_EXISTS);
        }
        //验证密码
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();
        String caclPass = MD5Util.formPassToDBPass(password,dbSalt);
        if (!caclPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        addCookie(response, user);
        return true;
    }

    public User getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        User user = jedisAdapter.get(UserKey.token,token,User.class);
        //延长有效期
        if (user != null){
            addCookie(response, user);
        }
        return user;
    }

    //回写cookie
    private void addCookie(HttpServletResponse response, User user){
        //生成cookie
        //标识一下这个tokin对应哪一个用户
        //生成cookie的key和value
        String token = UUIDUtil.uuid();
        jedisAdapter.set(UserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        //设置成和session一样的有效期
        cookie.setMaxAge(UserKey.token.expireSeconds());
        cookie.setPath("/");
        //写入客户端
        response.addCookie(cookie);
    }
}
