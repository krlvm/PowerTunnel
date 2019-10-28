package ru.krlvm.powertunnel.utilities;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Utility with working with
 * HTTP/HTTPS packets
 *
 * @author krlvm
 */
public class PacketUtility {

    /**
     * Retrieves list of packet's ByteBuf chunks
     *
     * @param buf - ByteBuf of packet
     * @return - ByteBuf chunks
     */
    public static LinkedList<ByteBuf> bufferChunk(ByteBuf buf) {
        LinkedList<byte[]> chunks = chunk(buf);
        LinkedList<ByteBuf> buffers = new LinkedList<>();
        for (byte[] chunk : chunks) {
            buffers.add(Unpooled.wrappedBuffer(chunk));
        }
        return buffers;
    }

    /**
     * Retrieves list (byte[]) of packet's ByteBuf chunks
     *
     * @param buf - ByteBuf of packet
     * @return - ByteBuf chunks (byte[])
     */
    public static LinkedList<byte[]> chunk(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        LinkedList<byte[]> byteChunks = new LinkedList<>();
        int len = bytes.length;
        int chunkSize = 1;
        int i = 0;
        while (i < len) {
            byteChunks.add(Arrays.copyOfRange(bytes, i, i += chunkSize));
        }
        return byteChunks;
    }
}
