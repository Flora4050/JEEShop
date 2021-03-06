package com.itheima.web.filter;

import com.itheima.domain.User;
import com.itheima.service.UserService;
import com.itheima.service.impl.UserServiceImpl;
import com.itheima.utils.CookieUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * creater:litiecheng
 * createDate:2017-9-5
 * discription:拦截器实现用户自动登录
 * indetail:
 *
 */
@WebFilter(filterName = "LoginFilter" ,urlPatterns = "/")
public class LoginFilter implements Filter{

    /**实现拦截方法doFilter执行拦截*/
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        chain.doFilter(req, resp);
        /**1.对request，response进行强制转换*/
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        /**2.获取session，自动登陆成功后将登陆成功的用户信息存入session中*/
        HttpSession session = request.getSession();

        /**3.如果已经登陆，则放行*/
        User loginUser = (User)request.getSession().getAttribute("loginUser");
        if (loginUser != null){
            chain.doFilter(request,response);
            return;
        }

        /**4.获得自动登陆的cookie信息*/
        Cookie userCookie = CookieUtils.findCookie(request.getCookies(),"autoLoginCookie");

        /**5.判断自动登陆的cookie是否存在，如果不错在，则不需要自动登陆*/
        if(userCookie == null){
            chain.doFilter(request,response);
            return;
        }

        /**6.自动登陆的cookie是否存在,执行自动登陆*/
        if (userCookie != null){
            /**6.1获得用户信息*/
            String[] u = userCookie.getValue().split("@");
            String username = u[0];
            String password = u[1];
            /**6.2执行登陆*/
            UserService userService = new UserServiceImpl();
            try {
                loginUser = userService.login(username,password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            /**6.3若没有返回值说明自动登陆没有成功*/
            if (loginUser == null){
                chain.doFilter(request,response);
                return;
            }
            /**6.4有返回值说明自动登陆成功,将返回的user对象存入session中*/
            session.setAttribute("loginUser",loginUser);
        }
        chain.doFilter(request,response);

    }

    /**实现filter的生命周期方法*/
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
