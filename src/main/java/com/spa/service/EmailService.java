package com.spa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:cuartodeunvago@gmail.com}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarcorreo(String para, String asunto, String mensaje) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(para);
        email.setSubject(asunto);
        email.setText(mensaje);
        email.setFrom(from);

        mailSender.send(email);
        System.out.println("Correo enviado a: " + para);
    }

    public void enviarCodigoReset(String para, String codigo, Duration ttl) {
        String asunto = "Código de verificación";
        String mensaje = """
                Tu código para restablecer la contraseña es: %s

                Este código caduca en %d minutos.
                Si tú no lo solicitaste, ignora este correo.
                """.formatted(codigo, ttl.toMinutes());

        enviarcorreo(para, asunto, mensaje);
    }
}
