package org.example.health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.LoginReq;
import org.example.health.dto.LoginResp;
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
    public ApiResult<LoginResp> login(@Valid @RequestBody LoginReq req) {
        String loginInput = req.getUsername(); // 这里先沿用字段名，实际含义是“账号或手机号”

        boolean isPhone = loginInput != null && loginInput.matches("^1\\d{10}$");

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .last("limit 1");

        if (isPhone) {
            wrapper.eq(SysUser::getPhone, loginInput);
        } else {
            wrapper.eq(SysUser::getUsername, loginInput);
        }

        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            return ApiResult.fail("账号/手机号不存在或密码错误");
        }

        // 练手阶段：数据库明文密码（正式项目请改 BCrypt）
        if (user.getPassword() == null || !user.getPassword().equals(req.getPassword())) {
            return ApiResult.fail("账号/手机号不存在或密码错误");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());

        return ApiResult.success(resp);
    }
    @PostMapping("/register")
    public ApiResult<String> register(@Valid @RequestBody org.example.health.dto.RegisterReq req) {
        // 1. 二次业务校验：确认密码一致
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return ApiResult.fail("两次输入的密码不一致");
        }

        // 2. 校验用户名是否已存在
        Long usernameCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, req.getUsername())
        );
        if (usernameCount != null && usernameCount > 0) {
            return ApiResult.fail("用户名已存在");
        }

        // 3. 校验手机号是否已存在
        Long phoneCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, req.getPhone())
        );
        if (phoneCount != null && phoneCount > 0) {
            return ApiResult.fail("手机号已被注册");
        }

        // 4. 组装用户对象
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPhone(req.getPhone());
        user.setPassword(req.getPassword()); // 当前阶段明文存储（后续改加密）
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank() ? req.getUsername() : req.getNickname());
        user.setGender(req.getGender() == null ? 0 : req.getGender());

        // 如果你的实体类里有这些字段就打开；没有就先不写
        // user.setStatus(1);
        // user.setDeleted(0);

        int rows = sysUserMapper.insert(user);
        if (rows <= 0) {
            return ApiResult.fail("注册失败，请稍后重试");
        }

        return ApiResult.success("注册成功");
    }
}