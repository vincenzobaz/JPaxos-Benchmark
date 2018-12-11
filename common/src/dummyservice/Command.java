package dummyservice;

import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

public class Command {
    private final CommandType type;
    private static final int PAYLOAD_LEN = 60000;
    private static final int CONTROL_LEN = 8;
    private static final byte[] PAYLOAD = new byte[PAYLOAD_LEN];
    private final int value;

    public Command(CommandType type, int value) {
        this.type = type;
        this.value = value;
    }

    public Command(byte[] bytes) throws IOException {
        DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(bytes));
        type = CommandType.fromInt(dataInput.readInt());
        value = dataInput.readInt();
    }

    public int getValue() { return value; }
    public CommandType getType() { return type; }

    public byte[] toByteArray() {
        if (PAYLOAD_LEN < CONTROL_LEN) {
            throw new IllegalArgumentException("Payload cannon be smaller than control");
        }
        ByteBuffer bb = ByteBuffer.allocate(CONTROL_LEN);
        bb.putInt(type.ordinal());
        bb.putInt(value);

        System.arraycopy(bb.array(), 0, PAYLOAD, 0, CONTROL_LEN);
        return PAYLOAD;
    }

    @Override
    public String toString() {
        if (getType() == CommandType.READ) return "READ";
        else return "WRITE(" + value + ")";
    }
}
