package net.gongmingqm10.smackdemo.xmpp;

import android.content.Intent;
import android.util.Log;

import net.gongmingqm10.smackdemo.activity.MainActivity;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

public class XMPPManager {


    private final String TAG = "XMPPManager";

    private final String serverAddress = "172.16.15.164"; // Your server address or IP
    public static final String serverName = "xubc"; //xmpp name or your server name
    public static final String mPassword = "000000";
    private XMPPState state = XMPPState.NOT_CONNECTED;
    private XMPPTCPConnectionConfiguration config;
    private static XMPPManager instance;
    public XMPPTCPConnection connection;

    private String username;
    private String password;

    private XMPPManager() {
        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
        builder.setServiceName(serverAddress);
        builder.setResource("SmackAndroidTestClient");
        builder.setPort(5222);
        builder.setDebuggerEnabled(false);
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setConnectTimeout(10000);
        config = builder.build();
        SASLMechanism mechanism = new SASLDigestMD5Mechanism();
        SASLAuthentication.registerSASLMechanism(mechanism);
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        connection = new XMPPTCPConnection(config);
        connection.addConnectionListener(new XMPPConnectionListener());
    }

    public static XMPPManager getInstance() {
        if (instance == null) {
            instance = new XMPPManager();
        }
        return instance;
    }

    public XMPPState getState() {
        return state;
    }

    public void connect(final String username, final String password) {
        this.username = username;
        this.password = password;
        reConnect();
    }

    public void reConnect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.connect();
                    connection.login(username, mPassword);
                } catch (XMPPException e) {
                    e.printStackTrace();
                    Log.i(TAG, "XMPPException " + e.toString());
                } catch (SmackException e) {
                    e.printStackTrace();
                    Log.i(TAG, "SmackException " + e.toString());
                } catch (IOException e) {
                    Log.i(TAG, "IOException " + e.toString());
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private class XMPPConnectionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection xmppConnection) {
            Log.i(TAG, "--connected--");
            state = XMPPState.CONNECTED;
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            Log.i(TAG, "--authenticated--");
            state = XMPPState.AUTHENTICATED;
            onAuthenticatedListener.onAuthenticated();
        }

        @Override
        public void connectionClosed() {
            Log.i(TAG, "--connectionClosed--");
            state = XMPPState.NOT_CONNECTED;
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.e(TAG, "--connectionClosedOnError--");
        }

        @Override
        public void reconnectingIn(int i) {
            Log.i(TAG, "--reconnectingIn--");
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "--reconnectionSuccessful--");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.e(TAG, "--reconnectionFailed--");
        }
    }


    private AuthenticatedListener onAuthenticatedListener;

    public void setOnAuthenticatedListener(AuthenticatedListener onAuthenticatedListener) {
        this.onAuthenticatedListener = onAuthenticatedListener;
    }

    public interface AuthenticatedListener {
        void onAuthenticated();
    }
}
