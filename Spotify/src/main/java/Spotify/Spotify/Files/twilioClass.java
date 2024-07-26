package Spotify.Spotify.Files;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jdk.jfr.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class twilioClass {
    @Value("${twilio.api.token}")
    String API_TOKEN;
    @Value("${twilio.api.account_sid}")
    String API_ACCOUNT_SID;
    public void TwilioClass() {
        Twilio.init(API_ACCOUNT_SID, API_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("whatsapp:+254759274504"),
                new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
                "Your appointment is coming up on July 21 at 3PM"
        ).create();

        System.out.println(message.getSid());
    }
}