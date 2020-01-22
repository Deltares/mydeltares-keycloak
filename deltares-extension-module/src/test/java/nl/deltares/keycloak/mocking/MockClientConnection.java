package nl.deltares.keycloak.mocking;

import org.keycloak.common.ClientConnection;

public class MockClientConnection implements ClientConnection {
    private String remoteAddr;

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }
}
