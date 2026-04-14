package com.greenloop.auth.verification;

import org.springframework.stereotype.Component;

@Component
public class VerificationEmailTemplate {

    private static final String GREENLOOP_COLOR = "#22c55e";
    private static final String SECONDARY_COLOR = "#f0fdf4";

    public String buildVerificationEmailHtml(String firstName, String verificationUrl, int expiryHours) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verify Your GreenLoop Account</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; background-color: #f9fafb; margin: 0; padding: 0; }
                        .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }
                        .header { background-color: %s; color: white; padding: 30px 20px; text-align: center; }
                        .header-logo { font-size: 28px; font-weight: bold; margin: 0; }
                        .content { padding: 40px 30px; }
                        .greeting { font-size: 18px; font-weight: 600; color: #1f2937; margin: 0 0 20px 0; }
                        .message { font-size: 14px; color: #4b5563; margin-bottom: 30px; line-height: 1.8; }
                        .verification-button { display: inline-block; background-color: %s; color: white; padding: 14px 32px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 16px; }
                        .button-container { text-align: center; margin: 30px 0; }
                        .alternative-link { background-color: %s; padding: 15px; border-radius: 4px; margin-top: 20px; text-align: center; }
                        .alternative-link a { color: %s; text-decoration: none; font-size: 13px; word-break: break-all; }
                        .expiry-notice { background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px 15px; margin: 20px 0; border-radius: 4px; font-size: 13px; color: #78350f; }
                        .footer { background-color: %s; padding: 20px 30px; text-align: center; font-size: 12px; color: #6b7280; border-top: 1px solid #e5e7eb; }
                        .footer-link { color: %s; text-decoration: none; }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header"><h1 class="header-logo">🌱 GreenLoop</h1><p style="margin:5px 0 0;opacity:.95;font-size:14px">Sustainable Campus Community</p></div>
                        <div class="content">
                            <p class="greeting">Hi %s,</p>
                            <p class="message">Welcome to <strong>GreenLoop</strong>! Please verify your university email to complete registration.</p>
                            <div class="button-container"><a href="%s" class="verification-button">Verify Email Address</a></div>
                            <p class="message">Or copy and paste this link in your browser:</p>
                            <div class="alternative-link"><a href="%s">%s</a></div>
                            <div class="expiry-notice"><strong>Important:</strong> This link expires in <strong>%d hours</strong>.</div>
                        </div>
                        <div class="footer">
                            <p style="margin:0 0 10px">GreenLoop Team | Sustainable Campus Initiative</p>
                            <p style="margin:0"><a href="https://greenloop.edu" class="footer-link">Visit GreenLoop</a> | <a href="https://greenloop.edu/privacy" class="footer-link">Privacy</a> | <a href="https://greenloop.edu/terms" class="footer-link">Terms</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                GREENLOOP_COLOR, GREENLOOP_COLOR, SECONDARY_COLOR, GREENLOOP_COLOR,
                SECONDARY_COLOR, GREENLOOP_COLOR,
                firstName, verificationUrl, verificationUrl, verificationUrl, expiryHours
        );
    }

    public String buildVerificationEmailText(String firstName, String verificationUrl, int expiryHours) {
        return String.format("""
                Welcome to GreenLoop!

                Hi %s,

                Please verify your university email by visiting:

                %s

                This link expires in %d hours.

                GreenLoop Team
                """, firstName, verificationUrl, expiryHours);
    }
}
