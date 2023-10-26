package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.libs.crash.v0.CrashLog;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DedicatedServer extends UltracraftServer {
    public DedicatedServer(WorldStorage storage, String host, int port) throws UnknownHostException {
        super(storage);

        this.getConnection().startTcpServer(InetAddress.getByName(host), port);
    }

    @Override
    public void crash(Throwable t) {
        CrashLog crashLog = new CrashLog("Server crashed! :(", t);
        this.world.fillCrashInfo(crashLog);
        crashLog.createCrash();
    }

    @Override
    protected void onTerminationFailed() {
        this.crash(new Error("Termination failed!"));
    }
}
