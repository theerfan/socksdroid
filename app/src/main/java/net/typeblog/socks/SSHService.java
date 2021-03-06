package net.typeblog.socks;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import org.apache.sshd.client.*;
import org.apache.sshd.client.auth.pubkey.KeyPairIdentity;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;

import java.io.IOException;
import java.security.KeyPair;

public class SSHService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startSSHConnection(String username, String host, int port, String password) {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        try(ClientSession session = client.connect(username, host, port).verify().getSession()) {
            session.addPasswordIdentity(password);
            session.auth().verify(10000);
            session.addPortForwardingEventListener(new PortForwardingEventListener() {
                @Override
                public void establishedDynamicTunnel(Session session, SshdSocketAddress local, SshdSocketAddress boundAddress, Throwable reason) throws IOException {
                    Log.i("DyanmicTunnelTag", "Dynamic tunnel created successfully!");
                }
            });
            SshdSocketAddress sshdSocketAddress = session.startDynamicPortForwarding(new SshdSocketAddress("0.0.0.0", ))
        }
        catch (Exception ignore) {
        }
    }
}
