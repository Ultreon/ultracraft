package com.ultreon.craft.debug;

public class ValueTracker {
    private static long meshDisposes = 0L;
    private static long vertexCount;
    private static int packetsSent;
    private static int packetsReceived;
    private static int packetsReceivedTotal;
    private static int chunkLoads;
    public static long chunkMeshFrees;

    public static long getMeshDisposes() {
        return ValueTracker.meshDisposes;
    }

    public static void setMeshDisposes(long meshDisposes) {
        ValueTracker.meshDisposes = meshDisposes;
    }

    public static long getVertexCount() {
        return ValueTracker.vertexCount;
    }

    public static void setVertexCount(long vertexCount) {
        ValueTracker.vertexCount = vertexCount;
    }

    public static int getPacketsSent() {
        return ValueTracker.packetsSent;
    }

    public static void setPacketsSent(int packetsSent) {
        ValueTracker.packetsSent = packetsSent;
    }

    public static int getPacketsReceived() {
        return ValueTracker.packetsReceived;
    }

    public static void setPacketsReceived(int packetsReceived) {
        ValueTracker.packetsReceived = packetsReceived;
    }

    public static int getPacketsReceivedTotal() {
        return ValueTracker.packetsReceivedTotal;
    }

    public static void setPacketsReceivedTotal(int packetsReceivedTotal) {
        ValueTracker.packetsReceivedTotal = packetsReceivedTotal;
    }

    public static int getChunkLoads() {
        return ValueTracker.chunkLoads;
    }

    public static void setChunkLoads(int chunkLoads) {
        ValueTracker.chunkLoads = chunkLoads;
    }
}
