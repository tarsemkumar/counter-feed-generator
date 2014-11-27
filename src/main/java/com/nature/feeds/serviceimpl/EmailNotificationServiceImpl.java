package com.nature.feeds.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.service.EmailNotificationService;

public class EmailNotificationServiceImpl implements EmailNotificationService {
    private final IResourceLookUp resourceLookUp;

    @Inject
    public EmailNotificationServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to send email notification after feed uploading. */

    @Override
    public void sendEmailNotification(String to, String subject, String body) throws Exception {
        //String host = ;//or IP address  
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", resourceLookUp.getResource("mail.host.server"));
        Session session = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(resourceLookUp.getResource("mail.from")));
        message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject + getEmailSubjectDateInDDMMYYYYFormat());
        message.setText(body);
        Transport.send(message);
    }

    private String getEmailSubjectDateInDDMMYYYYFormat() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(resourceLookUp.getResource("mail.subject.date.format"));
        return sdf.format(Calendar.getInstance().getTime());
    }

}
