package com.bradesco.antifraud.service;

import com.azure.communication.email.models.*;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bradesco.antifraud.model.EmailRequest;

import jakarta.validation.constraints.Email;

import org.springframework.stereotype.Service;
import com.azure.communication.email.*;
import com.azure.core.util.polling.*;

@Service
public class EmailService {
    String connectionString = "endpoint=" + System.getenv("AZURE_COMMUNICATION_SERVICE_ENDPOINT") +
            ";accesskey=" + System.getenv("AZURE_COMMUNICATION_SERVICE_ACCESS_KEY");

    EmailAsyncClient asyncEmailClient = new EmailClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();


    public void sendEmail(EmailRequest emailRequest) {

        EmailClient emailClient = new EmailClientBuilder().connectionString(connectionString).buildClient();

        EmailAddress toAddress = new EmailAddress(emailRequest.getSenderAddress());

        EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress("DoNotReply@antifraudsystem.com.br")
                .setToRecipients(toAddress)
                .setSubject(emailRequest.getSubject())
                .setBodyPlainText(emailRequest.getSubject());

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage, null);
        PollResponse<EmailSendResult> result = poller.waitForCompletion();

    }

}
