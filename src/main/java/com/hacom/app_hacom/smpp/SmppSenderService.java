package com.hacom.app_hacom.smpp;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class SmppSenderService {

    private static final Logger log = LoggerFactory.getLogger(SmppSenderService.class);

    private DefaultSmppClient client;
    private SmppSession session;

    public SmppSenderService() {
        connect();
    }

    private void connect() {
        try {
            client = new DefaultSmppClient();
            SmppSessionConfiguration config = new SmppSessionConfiguration();
            config.setWindowSize(1);
            config.setName("Tester.Session.0");
            config.setType(SmppBindType.TRANSCEIVER);
            // Simulator config (we can use an open simulator or dummy values for now)
            config.setHost("127.0.0.1");
            config.setPort(2775);
            config.setConnectTimeout(10000);
            config.setSystemId("smppclient1");
            config.setPassword("password");
            config.getLoggingOptions().setLogBytes(true);

            // Connect to simulator
            try {
                session = client.bind(config);
                log.info("SMPP session established successfully.");
            } catch (Exception e) {
                log.warn("Could not bind to SMPP server. Is the simulator running on port 2775? SMS will be disabled or simulated natively.", e.getMessage());
            }
        } catch (Exception ex) {
            log.error("Failed to initialize SMPP Client", ex);
        }
    }

    public boolean sendSms(String phoneNumber, String messageText) {
        if (session == null || !session.isBound()) {
            log.warn("SMPP session is not bound. Attempting to reconnect...");
            connect();
        }

        if (session == null || !session.isBound()) {
            log.error("Cannot send SMS. SMPP session is not bound even after reconnect attempt.");
            return false;
        }

        try {
            SubmitSm submit = new SubmitSm();
            // Source address (Sender)
            Address sourceAddress = new Address((byte) 0x01, (byte) 0x01, "HACOM-API");
            submit.setSourceAddress(sourceAddress);
            // Destination address
            Address destAddress = new Address((byte) 0x01, (byte) 0x01, phoneNumber);
            submit.setDestAddress(destAddress);

            // SMS content
            byte[] textBytes = CharsetUtil.encode(messageText, CharsetUtil.CHARSET_GSM);
            submit.setShortMessage(textBytes);

            SubmitSmResp submitResponse = session.submit(submit, 10000);

            if (submitResponse.getCommandStatus() == 0) {
                log.info("SMS submitted successfully with message id: {}", submitResponse.getMessageId());
                return true;
            } else {
                log.error("Failed to submit SMS. Status code: {}", submitResponse.getCommandStatus());
                return false;
            }

        } catch (Exception e) {
            log.error("Error during SMS submission via SMPP", e);
            return false;
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Destroying SMPP session & client...");
        if (session != null) {
            session.unbind(5000);
            session.destroy();
        }
        if (client != null) {
            client.destroy();
        }
    }
}
