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


         // Async client
        EmailAsyncClient asyncEmailClient = new EmailClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
            
        // Sync client

                

//        String bodyHTML = """
//            <html>
//                <body>
//                    <h1>Hello world via email.</h1>
//                    <h1>HTML body inline images:</h1>
//
//                </body>
//            </html>""";

            public void sendEmail(EmailRequest emailRequest) {

                EmailClient emailClient = new EmailClientBuilder().connectionString(connectionString).buildClient();

                System.out.println("\n \n \n \n"+ emailRequest.getSenderAddress());
                EmailAddress toAddress = new EmailAddress(emailRequest.getSenderAddress());



                EmailMessage emailMessage = new EmailMessage()
                        .setSenderAddress("DoNotReply@antifraudsystem.com.br")
                        .setToRecipients(toAddress)
                        .setSubject("Test Email")
                        .setBodyPlainText("Hello world via email.")
                        .setBodyHtml("""
                            <html>
                                <body>
                                    <h1>Hello world via email.</h1>
                                </body>
                            </html>""");



                SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage, null);
                PollResponse<EmailSendResult> result = poller.waitForCompletion();


            }

    


}
