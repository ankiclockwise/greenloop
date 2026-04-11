package com.greenloop.auth;

import org.springframework.stereotype.Component;

/**
 * Email template builder for email verification messages.
 * Generates HTML email content with GreenLoop branding and verification instructions.
 */
@Component
public class VerificationEmailTemplate {

    private static final String GREENLOOP_COLOR = "#22c55e";
    private static final String GREENLOOP_DARK = "#16a34a";
    private static final String SECONDARY_COLOR = "#f0fdf4";

    /**
     * Builds the HTML body for a verification email.
     * Includes GreenLoop branding, verification button, and token expiry information.
     *
     * @param firstName the first name of the user
     * @param verificationUrl the URL for email verification (includes token)
     * @param expiryHours number of hours until token expires
     * @return HTML string for the email body
     */
    public String buildVerificationEmailHtml(String firstName, String verificationUrl, int expiryHours) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verify Your GreenLoop Account</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            background-color: #f9fafb;
                            margin: 0;
                            padding: 0;
                        }
                        .email-container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                            border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background-color: %s;
                            color: white;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header-logo {
                            font-size: 28px;
                            font-weight: bold;
                            margin: 0;
                        }
                        .header-subtitle {
                            font-size: 14px;
                            margin: 5px 0 0 0;
                            opacity: 0.95;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .greeting {
                            font-size: 18px;
                            font-weight: 600;
                            color: #1f2937;
                            margin: 0 0 20px 0;
                        }
                        .message {
                            font-size: 14px;
                            color: #4b5563;
                            margin-bottom: 30px;
                            line-height: 1.8;
                        }
                        .verification-button {
                            display: inline-block;
                            background-color: %s;
                            color: white;
                            padding: 14px 32px;
                            text-decoration: none;
                            border-radius: 6px;
                            font-weight: 600;
                            font-size: 16px;
                            margin: 20px 0;
                            transition: background-color 0.3s ease;
                            border: none;
                            cursor: pointer;
                        }
                        .verification-button:hover {
                            background-color: %s;
                        }
                        .button-container {
                            text-align: center;
                            margin: 30px 0;
                        }
                        .alternative-link {
                            background-color: %s;
                            padding: 15px;
                            border-radius: 4px;
                            margin-top: 20px;
                            text-align: center;
                        }
                        .alternative-link-label {
                            display: block;
                            font-size: 12px;
                            color: #6b7280;
                            margin-bottom: 8px;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                        }
                        .alternative-link a {
                            color: %s;
                            text-decoration: none;
                            font-size: 13px;
                            word-break: break-all;
                            font-weight: 500;
                        }
                        .expiry-notice {
                            background-color: #fef3c7;
                            border-left: 4px solid #f59e0b;
                            padding: 12px 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                            font-size: 13px;
                            color: #78350f;
                        }
                        .footer {
                            background-color: %s;
                            padding: 20px 30px;
                            text-align: center;
                            font-size: 12px;
                            color: #6b7280;
                            border-top: 1px solid #e5e7eb;
                        }
                        .footer-link {
                            color: %s;
                            text-decoration: none;
                            font-weight: 500;
                        }
                        .separator {
                            height: 1px;
                            background-color: #e5e7eb;
                            margin: 30px 0;
                        }
                        .highlight {
                            color: %s;
                            font-weight: 600;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1 class="header-logo">🌱 GreenLoop</h1>
                            <p class="header-subtitle">Sustainable Campus Community</p>
                        </div>

                        <div class="content">
                            <p class="greeting">Hi %s,</p>

                            <p class="message">
                                Welcome to <span class="highlight">GreenLoop</span>! We're excited to have you join our sustainable campus community.
                                To complete your registration, please verify your university email address by clicking the button below.
                            </p>

                            <div class="button-container">
                                <a href="%s" class="verification-button">Verify Email Address</a>
                            </div>

                            <p class="message">
                                Or, if the button doesn't work, you can copy and paste this link in your browser:
                            </p>

                            <div class="alternative-link">
                                <span class="alternative-link-label">Verification Link</span>
                                <a href="%s">%s</a>
                            </div>

                            <div class="expiry-notice">
                                <strong>Important:</strong> This verification link will expire in <strong>%d hours</strong>.
                                If it expires, you can request a new verification email from your account.
                            </div>

                            <div class="separator"></div>

                            <p class="message" style="font-size: 13px; color: #6b7280;">
                                <strong>Didn't sign up for GreenLoop?</strong><br>
                                If you received this email by mistake, you can safely ignore it. No account has been created yet until you verify your email.
                            </p>
                        </div>

                        <div class="footer">
                            <p style="margin: 0 0 10px 0;">
                                GreenLoop Team | Sustainable Campus Initiative
                            </p>
                            <p style="margin: 0;">
                                <a href="https://greenloop.edu" class="footer-link">Visit GreenLoop</a> |
                                <a href="https://greenloop.edu/privacy" class="footer-link">Privacy Policy</a> |
                                <a href="https://greenloop.edu/terms" class="footer-link">Terms of Service</a>
                            </p>
                            <p style="margin: 10px 0 0 0; font-size: 11px;">
                                © 2026 GreenLoop. All rights reserved.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                GREENLOOP_COLOR,
                GREENLOOP_COLOR,
                GREENLOOP_DARK,
                SECONDARY_COLOR,
                GREENLOOP_COLOR,
                SECONDARY_COLOR,
                GREENLOOP_COLOR,
                firstName,
                verificationUrl,
                verificationUrl,
                verificationUrl,
                expiryHours
        );
    }

    /**
     * Builds a plain text version of the verification email.
     * Used as fallback for email clients that don't support HTML.
     *
     * @param firstName the first name of the user
     * @param verificationUrl the URL for email verification
     * @param expiryHours number of hours until token expires
     * @return plain text string for the email body
     */
    public String buildVerificationEmailText(String firstName, String verificationUrl, int expiryHours) {
        return String.format("""
                Welcome to GreenLoop!

                Hi %s,

                Thank you for signing up for GreenLoop, a sustainable campus community initiative.
                To complete your registration, please verify your email address by visiting the link below:

                %s

                This verification link will expire in %d hours. If it expires, you can request a new
                verification email from your GreenLoop account.

                ---

                Didn't sign up for GreenLoop?
                If you received this email by mistake, you can safely ignore it. No account has been
                created yet until you verify your email.

                ---

                GreenLoop Team
                Sustainable Campus Initiative

                Privacy Policy: https://greenloop.edu/privacy
                Terms of Service: https://greenloop.edu/terms
                """,
                firstName,
                verificationUrl,
                expiryHours
        );
    }
}
