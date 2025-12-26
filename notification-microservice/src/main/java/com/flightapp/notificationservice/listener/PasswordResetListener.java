package com.flightapp.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.flightapp.notificationservice.dto.PasswordResetMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues="password.reset.queue")
    public void handlePasswordReset(PasswordResetMessage msg){

        SimpleMailMessage mail=new SimpleMailMessage();
        mail.setTo(msg.getEmail());
        mail.setSubject("Reset Your FlightApp Password");
        mail.setText(
            "Click the link below to reset your password:\n\n"
            + msg.getResetLink()
            + "\n\nThis link expires in 15 minutes."
        );

        mailSender.send(mail);
        System.out.println("📧 Password reset email sent to "+msg.getEmail());
    }
}
