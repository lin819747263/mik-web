package com.mik.user.captcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 简单验证码服务（无外部依赖）
 */
@Service
public class SimpleCaptchaService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:code:";
    private static final String CAPTCHA_VERIFIED_PREFIX = "captcha:verified:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int IMAGE_WIDTH = 150;
    private static final int IMAGE_HEIGHT = 50;

    /**
     * 生成验证码
     * @return [captchaId, base64Image]
     */
    public CaptchaResult generateCaptcha() {
        // 生成随机验证码（4位数字）
        String code = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        String captchaId = UUID.randomUUID().toString();

        // 存储到 Redis
        String key = CAPTCHA_PREFIX + captchaId;
        stringRedisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 生成图片
        String base64Image = generateImage(code);

        return new CaptchaResult(captchaId, base64Image);
    }

    /**
     * 验证验证码
     */
    public boolean verifyCaptcha(String captchaId, String code) {
        if (captchaId == null || code == null) {
            return false;
        }

        String key = CAPTCHA_PREFIX + captchaId;
        String storedCode = stringRedisTemplate.opsForValue().get(key);

        if (storedCode != null && storedCode.equalsIgnoreCase(code)) {
            // 验证成功，删除验证码，存储已验证状态
            stringRedisTemplate.delete(key);
            String verifiedKey = CAPTCHA_VERIFIED_PREFIX + captchaId;
            stringRedisTemplate.opsForValue().set(verifiedKey, "1", CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    /**
     * 检查是否已验证
     */
    public boolean isVerified(String captchaId) {
        if (captchaId == null) {
            return false;
        }
        String key = CAPTCHA_VERIFIED_PREFIX + captchaId;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            stringRedisTemplate.delete(key);
            return true;
        }
        return false;
    }

    /**
     * 生成验证码图片
     */
    private String generateImage(String code) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景
        g.setColor(getRandomColor(200, 250, rnd));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 30));

        // 画干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(getRandomColor(160, 200, rnd));
            int x1 = rnd.nextInt(IMAGE_WIDTH);
            int y1 = rnd.nextInt(IMAGE_HEIGHT);
            int x2 = rnd.nextInt(IMAGE_WIDTH);
            int y2 = rnd.nextInt(IMAGE_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 画验证码
        for (int i = 0; i < code.length(); i++) {
            g.setColor(getRandomColor(20, 130, rnd));
            int x = 20 + i * 30;
            int y = 35 + rnd.nextInt(10);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        g.dispose();

        // 转换为 Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取随机颜色
     */
    private Color getRandomColor(int min, int max, ThreadLocalRandom rnd) {
        int r = min + rnd.nextInt(max - min);
        int g = min + rnd.nextInt(max - min);
        int b = min + rnd.nextInt(max - min);
        return new Color(r, g, b);
    }

    /**
     * 验证码结果
     */
    public static class CaptchaResult {
        private String captchaId;
        private String image;

        public CaptchaResult(String captchaId, String image) {
            this.captchaId = captchaId;
            this.image = image;
        }

        public String getCaptchaId() {
            return captchaId;
        }

        public String getImage() {
            return image;
        }
    }
}
