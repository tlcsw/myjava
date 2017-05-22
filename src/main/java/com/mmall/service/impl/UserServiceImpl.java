package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by 汤林超 on 2017/5/21.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {

        int resultCount = userMapper.checkUserName(username);
        if(resultCount==0){
return  ServerResponse.creatByErrorMessage("用户名不存在");
        }
        // todo MD5加密

        User user = userMapper.selectLogin(username,password);
        if(user == null){
            return  ServerResponse.creatByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.creatBySuccessMessage("登录成功");
    }

    public ServerResponse<String> register(User user){
//        int resultCount = userMapper.checkUserName(user.getUsername());
//        if(resultCount>0){
//            return  ServerResponse.creatByErrorMessage("用户名已存在");
//        }
        ServerResponse<String> checkValid = checkValid(user.getUsername(),Const.USERNAME);
        if(!checkValid.isSuccess()){
        return  checkValid;
        }
        checkValid = checkValid(user.getEmail(),Const.EMAIL);
        if(!checkValid.isSuccess()){
            return  checkValid;
        }

        user.setRole(Const.role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount ==0){
            return  ServerResponse.creatByErrorMessage("注册失败");
        }

        return ServerResponse.creatBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str,String type){
        if(StringUtils.isNotBlank("type")){

            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUserName(str);
                if(resultCount>0){
                    return  ServerResponse.creatByErrorMessage("用户名已存在");
                }
            }

            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount>0){
                    return  ServerResponse.creatByErrorMessage("邮件已存在");
                }
            }
        }else{
            return   ServerResponse.creatByErrorMessage("参数错误");
        }
        return ServerResponse.creatBySuccessMessage("校验成功");
    }

    public ServerResponse<String> selectQuestion(String username){
        ServerResponse<String> validResponse = checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return   ServerResponse.creatByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUserName(username);
        if(!StringUtils.isNotBlank(username)){
            return ServerResponse.creatBySuccess(question);
        }
        return ServerResponse.creatByErrorMessage("找回密码的问题是空的");
    }

    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resuleCode = userMapper.checkAnswer(username,question,answer);
        if(resuleCode>0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey("token_"+username,forgetToken);
            return ServerResponse.creatBySuccess(forgetToken);
        }
        return ServerResponse.creatByErrorMessage("问题的答案是错误的");
    }
}
