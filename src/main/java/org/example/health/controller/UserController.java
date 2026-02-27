package org.example.health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.ProfileResp;
import org.example.health.dto.UpdateProfileReq;
import org.example.health.entity.SysUser;
import org.example.health.mapper.SysUserMapper;
import org.example.health.util.JwtUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SysUserMapper sysUserMapper;

    public UserController(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 获取个人资料（根据 token）
     */
    @GetMapping("/profile")
    public ApiResult<ProfileResp> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = getUserIdFromAuthHeader(authorization);
        if (userId == null) {
            return ApiResult.fail("未登录或token无效");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return ApiResult.fail("用户不存在");
        }

        ProfileResp resp = new ProfileResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setGender(user.getGender());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setEmail(user.getEmail());

        // ✅ 身体资料回显
        resp.setHeightCm(user.getHeightCm());
        resp.setWeight(user.getWeight());
        resp.setGoalWeightKg(user.getGoalWeightKg());
        resp.setActivityLevel(user.getActivityLevel());
        resp.setTargetType(user.getTargetType());
        resp.setBirthDate(user.getBirthDate());

        return ApiResult.success(resp);
    }

    /**
     * 修改个人资料（根据 token）
     */
    @PutMapping("/profile")
    public ApiResult<String> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateProfileReq req) {

        Long userId = getUserIdFromAuthHeader(authorization);
        if (userId == null) {
            return ApiResult.fail("未登录或token无效");
        }

        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            return ApiResult.fail("用户不存在");
        }

        // 手机号唯一性校验（排除自己）
        Long phoneCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, req.getPhone())
                        .ne(SysUser::getId, userId)
                        .last("limit 1")
        );
        if (phoneCount != null && phoneCount > 0) {
            return ApiResult.fail("手机号已被其他用户使用");
        }

        // 基础资料
        currentUser.setPhone(req.getPhone());
        currentUser.setNickname(req.getNickname());
        currentUser.setGender(req.getGender() == null ? 0 : req.getGender());
        currentUser.setEmail(req.getEmail());
        currentUser.setAvatarUrl(req.getAvatarUrl()); // 可为空

        // ✅ 身体资料（都允许为空：用户可以分步完善）
        // 但如果你想强校验范围，也可以在这里做
        if (req.getHeightCm() != null) {
            currentUser.setHeightCm(req.getHeightCm());
        } else {
            currentUser.setHeightCm(null);
        }

        if (req.getWeight() != null) {
            currentUser.setWeight(req.getWeight());
        } else {
            currentUser.setWeight(null);
        }

        if (req.getGoalWeightKg() != null) {
            currentUser.setGoalWeightKg(req.getGoalWeightKg());
        } else {
            currentUser.setGoalWeightKg(null);
        }

        currentUser.setBirthDate(req.getBirthDate());
        currentUser.setActivityLevel(req.getActivityLevel());
        currentUser.setTargetType(req.getTargetType());

        int rows = sysUserMapper.updateById(currentUser);
        if (rows <= 0) {
            return ApiResult.fail("保存失败，请稍后重试");
        }

        return ApiResult.success("保存成功");
    }

    /**
     * 头像上传（本地保存）
     * 返回可访问的头像URL，例如 /uploads/avatar/xxx.png
     */
    @PostMapping("/avatar")
    public ApiResult<String> uploadAvatar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("file") MultipartFile file) {

        Long userId = getUserIdFromAuthHeader(authorization);
        if (userId == null) {
            return ApiResult.fail("未登录或token无效");
        }

        if (file == null || file.isEmpty()) {
            return ApiResult.fail("请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResult.fail("仅支持图片文件");
        }

        long maxSize = 2 * 1024 * 1024; // 2MB
        if (file.getSize() > maxSize) {
            return ApiResult.fail("图片大小不能超过2MB");
        }

        String uploadDirPath = System.getProperty("user.dir")
                + File.separator + "uploads" + File.separator + "avatar";
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            return ApiResult.fail("创建上传目录失败");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getFileExt(originalFilename);
        if (!StringUtils.hasText(ext)) {
            ext = ".png";
        }

        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "u" + userId + "_" + timePart + "_"
                + UUID.randomUUID().toString().replace("-", "") + ext;

        File dest = new File(uploadDir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return ApiResult.fail("上传失败：" + e.getMessage());
        }

        String avatarUrl = "/uploads/avatar/" + fileName;
        return ApiResult.success(avatarUrl);
    }

    // ----------------- 工具方法 -----------------

    private String getFileExt(String filename) {
        if (!StringUtils.hasText(filename)) return "";
        int idx = filename.lastIndexOf(".");
        if (idx < 0) return "";
        return filename.substring(idx).toLowerCase();
    }

    private Long getUserIdFromAuthHeader(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        String token = authorization.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            return JwtUtil.parseUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}