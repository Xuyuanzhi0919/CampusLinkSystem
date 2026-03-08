package com.campuslink.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * 邮件发送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${campuslink.email.from}")
    private String from;

    /**
     * 异步发送验证码邮件
     */
    @Async("notificationExecutor")
    public void sendVerificationCode(String to, String code, int expireMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【CampusLink】邮箱验证码");
            helper.setText(buildHtml(code, expireMinutes), true);

            mailSender.send(message);
            log.info("验证码邮件发送成功: to={}", to);
        } catch (Exception e) {
            log.error("验证码邮件发送失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败，请检查邮箱地址是否正确");
        }
    }

    private String buildHtml(String code, int expireMinutes) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#F3F4F6;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center" style="padding:40px 16px;">
                    <table width="480" cellpadding="0" cellspacing="0"
                           style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#2563EB,#38BDF8);padding:32px;text-align:center;">
                          <h1 style="margin:0;color:#fff;font-size:24px;font-weight:700;letter-spacing:1px;">CampusLink</h1>
                          <p style="margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:14px;">高校资源互助 · 问答社区</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px 32px;">
                          <p style="margin:0 0 8px;color:#374151;font-size:16px;">您好，</p>
                          <p style="margin:0 0 24px;color:#6B7280;font-size:14px;line-height:1.6;">
                            您正在进行邮箱验证，请使用以下验证码完成操作：
                          </p>
                          <!-- Code Box -->
                          <div style="background:#EFF6FF;border:2px dashed #93C5FD;border-radius:12px;padding:20px;text-align:center;margin:0 0 24px;">
                            <span style="font-size:40px;font-weight:700;letter-spacing:12px;color:#1D4ED8;">%s</span>
                          </div>
                          <p style="margin:0 0 8px;color:#6B7280;font-size:13px;">
                            验证码有效期为 <strong>%d 分钟</strong>，请尽快使用。
                          </p>
                          <p style="margin:0;color:#9CA3AF;font-size:12px;">
                            如非本人操作，请忽略此邮件。
                          </p>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td style="background:#F9FAFB;padding:16px 32px;text-align:center;border-top:1px solid #E5E7EB;">
                          <p style="margin:0;color:#9CA3AF;font-size:12px;">© 2025 CampusLink · 高校资源共享平台</p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(code, expireMinutes);
    }
}
