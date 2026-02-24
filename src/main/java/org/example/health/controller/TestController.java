package org.example.health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.health.entity.SysUser;
import org.example.health.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final SysUserMapper sysUserMapper;

    public TestController(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/test/hello")
    public String hello() {
        return "后端启动成功";
    }

    @GetMapping("/test/db")
    public Object testDb() {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().last("limit 1")
        );
        return user;
    }
}