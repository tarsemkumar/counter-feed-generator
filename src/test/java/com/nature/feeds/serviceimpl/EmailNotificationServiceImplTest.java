package com.nature.feeds.serviceimpl;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nature.components.service.resources.IResourceLookUp;

public class EmailNotificationServiceImplTest {

    @Mock
    private IResourceLookUp mockIResourceLookUp;
    private EmailNotificationServiceImpl emailNotificationServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void sendEmailNotification() throws Exception {
        emailNotificationServiceImpl = new EmailNotificationServiceImpl(mockIResourceLookUp) {
            @Override
            public void sendEmailNotification(String to, String subject, String body) throws Exception {

            }
        };
        when(mockIResourceLookUp.getResource("mail.host.server")).thenReturn("10.10.0.23");
        when(mockIResourceLookUp.getResource("mail.from")).thenReturn("abc@test.com");
        when(mockIResourceLookUp.getResource("mail.subject.date.format")).thenReturn("dd/MM/yyyy");
        emailNotificationServiceImpl.sendEmailNotification("ce@test.com", "feed generation", "feed has generated");
        assertEquals(mockIResourceLookUp.getResource("mail.host.server"), "10.10.0.23");

    }

}
