package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.R;
import com.example.reggie.domain.User;
import com.example.reggie.service.UserService;
import com.example.reggie.utils.EmailUtils;
import com.example.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送邮箱验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    @ResponseBody
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取邮箱
        String email=user.getEmail();
        if(StringUtils.isNotEmpty(email)) {
            //生成验证码
            String code= ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码为:{}",code);
            //调用API发送短信
            EmailUtils.sendAuthCodeEmail(email,code);
            //将验证码存入Session
            session.setAttribute(email,code);

            //将验证码缓存到Redis中，并设置有效期为1分钟
            redisTemplate.opsForValue().set(email,code,1, TimeUnit.MINUTES);

        }

        return R.success("验证码发送成功");
    }

    /**
     * 登录
     * @param session
     * @param map
     * @return
     */
    @PostMapping("/login")
    @ResponseBody
    public R<User> login(HttpSession session,@RequestBody Map map){
        log.info(map.toString());

        //获取邮箱
        String email=map.get("email").toString();
        //获取验证码
        String code=map.get("code").toString();
        //获取Session中保存的验证码
        //Object codeInSession=session.getAttribute(email);
        //从Redis中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(email);
        //验证码比对
        if(codeInSession!=null&&codeInSession.equals(code)){
            //判断是否为新用户
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail,email);

            User user=userService.getOne(queryWrapper);
            if (user==null){
                //新用户自动注册
                user=new User();
                user.setName(email);
                user.setEmail(email);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //用户登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(email);
            return R.success(user);
        }
        return R.error("登录失败");
    }

    /**
     * 用户退出
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    @ResponseBody
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
