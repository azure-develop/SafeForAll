package ph.dlsu.s11.caih.machineproject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.security.Security;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMailAPI extends AsyncTask<Void, Void, Void> {

    private Context context;
    private Session session;
    private String subject, message;
    private String[] email;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public JavaMailAPI(Context context, String[] email, String subject, String message){
        this.context=context;
        this.email=email;
        this.subject=subject;
        this.message=message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        session = Session.getDefaultInstance(properties, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("caihennry@gmail.com", "uudssxctzmyshhgs");
            }
        });

        MimeMessage mimeMessage = new MimeMessage(session);
        try{
            mimeMessage.setFrom(new InternetAddress("caihennry@gmail.com"));
            for (int i=0; i<email.length; i++) {
                mimeMessage.addRecipients(Message.RecipientType.TO,  InternetAddress.parse(email[i]));
            }
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            Log.d("main", Arrays.toString(mimeMessage.getRecipients(Message.RecipientType.TO)));
            Transport.send(mimeMessage);
        }catch (MessagingException e){
            e.printStackTrace();
        }

        return null;
    }
}
