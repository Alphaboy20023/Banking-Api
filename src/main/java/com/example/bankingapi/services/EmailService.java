package com.example.bankingapi.services;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.bankingapi.models.AccountModel;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@mybankingapi.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Async
    public void sendHtmlMail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("emails/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("noreply@mybankingapi.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            // e.printStackTrace();
            // throw new RuntimeException("Failed to send email");
             System.err.println("Email failed but continuing: " + e.getMessage());
        }
    }

    public void sendTransferAlerts(AccountModel fromAccount, AccountModel toAccount, BigDecimal amount, String transactionId) {
        // 1. Prepare shared formatting
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "NG"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        String formattedAmount = nf.format(amount);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. Debit Alert for Sender
        Map<String, Object> senderData = new HashMap<>();
        senderData.put("name", fromAccount.getUser().getUsername());
        senderData.put("transactionType", "sent");
        senderData.put("direction", "from");
        senderData.put("amount", formattedAmount);
        senderData.put("transactionId", transactionId);
        senderData.put("date", timestamp);
        senderData.put("balance", nf.format(fromAccount.getBalance()));

        this.sendHtmlMail(
            fromAccount.getUser().getEmail(), 
            "Debit Alert - ₦" + formattedAmount, 
            "transaction-success", 
            senderData
        );

        // 3. Credit Alert for Receiver
        Map<String, Object> receiverData = new HashMap<>();
        receiverData.put("name", toAccount.getUser().getUsername());
        receiverData.put("transactionType", "received");
        receiverData.put("direction", "into");
        receiverData.put("amount", formattedAmount);
        receiverData.put("getSenderName", fromAccount.getUser().getUsername());
        receiverData.put("transactionId", transactionId);
        receiverData.put("date", timestamp);
        receiverData.put("balance", nf.format(toAccount.getBalance()));

        this.sendHtmlMail(
            toAccount.getUser().getEmail(), 
            "Credit Alert - ₦" + formattedAmount, 
            "transaction-success", 
            receiverData
        );
    }

    

    
}
