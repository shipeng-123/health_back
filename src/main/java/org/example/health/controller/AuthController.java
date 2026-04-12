package org.example.health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.LoginReq;
import org.example.health.dto.LoginResp;
import org.example.health.dto.RegisterReq;
import org.example.health.entity.SysUser;
import org.example.health.mapper.SysUserMapper;
import org.example.health.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserMapper sysUserMapper;

    public AuthController(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @PostMapping("/login")
    public ApiResult login(@Valid @RequestBody LoginReq req) {
        String loginInput = req.getUsername();
        boolean isPhone = loginInput != null && loginInput.matches("^1\\d{10}$");

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("limit 1");

        if (isPhone) {
            wrapper.eq(SysUser::getPhone, loginInput);
        } else {
            wrapper.eq(SysUser::getUsername, loginInput);
        }

        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            return ApiResult.fail("账号/手机号不存在或密码错误");
        }

        if (user.getDeleted() != null && user.getDeleted() == 1) {
            return ApiResult.fail("账号不存在");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return ApiResult.fail("账号已被禁用");
        }

        if (user.getPassword() == null || !user.getPassword().equals(req.getPassword())) {
            return ApiResult.fail("账号/手机号不存在或密码错误");
        }

        Integer role = user.getRole() == null ? 0 : user.getRole();
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), role);

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setRole(role);

        return ApiResult.success(resp);
    }

    @PostMapping("/register")
    public ApiResult register(@Valid @RequestBody RegisterReq req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return ApiResult.fail("两次输入的密码不一致");
        }

        Long usernameCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, req.getUsername())
        );
        if (usernameCount != null && usernameCount > 0) {
            return ApiResult.fail("用户名已存在");
        }

        Long phoneCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, req.getPhone())
        );
        if (phoneCount != null && phoneCount > 0) {
            return ApiResult.fail("手机号已被注册");
        }

        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPhone(req.getPhone());
        user.setPassword(req.getPassword());
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank() ? req.getUsername() : req.getNickname());
        user.setGender(req.getGender() == null ? 0 : req.getGender());
        user.setRole(0);
        user.setStatus(1);
        user.setDeleted(0);

        int rows = sysUserMapper.insert(user);
        if (rows <= 0) {
            return ApiResult.fail("注册失败，请稍后重试");
        }

        return ApiResult.success("注册成功");
    }
}