package com.maretu.user.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {

    private final JavaMailSender javaMailSender;

    public MailUtil(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendVerificationCodeMail(String toEmail, String verificationCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("Maretu <20223802051@m.scnu.edu.cn>");
            helper.setTo(toEmail);
            helper.setSubject("Your Verification Code");

            String htmlContent = buildVerificationCodeHtml(verificationCode);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification code email", e);
        }
    }

    private String buildVerificationCodeHtml(String verificationCode) {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Verification Code</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                ".container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 40px 30px; }" +
                ".content p { margin: 15px 0; }" +
                ".code-container { background: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; margin: 25px 0; text-align: center; }" +
                ".verification-code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace; }" +
                ".code-label { font-size: 14px; color: #666; margin-bottom: 10px; }" +
                ".info-box { background: #e7f3ff; border-left: 4px solid #2196F3; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                ".info-box p { margin: 5px 0; font-size: 14px; }" +
                ".footer { background: #f8f9fa; padding: 20px 30px; text-align: center; font-size: 14px; color: #666; }" +
                ".footer p { margin: 5px 0; }" +
                ".highlight { color: #667eea; font-weight: bold; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1>🔐 Email Verification</h1>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Dear User,</p>" +
                "<p>Thank you for using our services! To verify your email address, please enter the verification code below within the next <span class=\"highlight\">10 minutes</span>:</p>" +
                "<div class=\"code-container\">" +
                "<div class=\"code-label\">Your Verification Code</div>" +
                "<div class=\"verification-code\">" + verificationCode + "</div>" +
                "</div>" +
                "<div class=\"info-box\">" +
                "<p>⏰ This verification code is valid for <strong>10 minutes</strong> only.</p>" +
                "<p>🔒 This code is for one-time use and must be used within the validity period.</p>" +
                "<p>❓ If you didn't request this code, please ignore this email.</p>" +
                "</div>" +
                "<p>If you have any questions or need assistance, please reply to this email.</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>Best regards,</p>" +
                "<p><strong>Maretu</strong></p>" +
                "<p style=\"font-size: 12px; color: #999;\">This is an automated message, please do not reply directly.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
