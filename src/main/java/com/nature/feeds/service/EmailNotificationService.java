package com.nature.feeds.service;

public interface EmailNotificationService {

    public void sendEmailNotification(String to, String subject, String body) throws Exception;

}
