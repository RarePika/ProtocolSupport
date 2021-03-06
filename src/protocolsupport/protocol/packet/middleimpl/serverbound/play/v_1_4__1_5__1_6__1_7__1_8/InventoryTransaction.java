package protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8;

import java.io.IOException;

import protocolsupport.protocol.packet.middle.serverbound.play.MiddleInventoryTransaction;
import protocolsupport.protocol.serializer.ProtocolSupportPacketDataSerializer;

public class InventoryTransaction extends MiddleInventoryTransaction {

	@Override
	public void readFromClientData(ProtocolSupportPacketDataSerializer serializer) throws IOException {
		windowId = serializer.readUnsignedByte();
		actionNumber = serializer.readShort();
		accepted = serializer.readBoolean();
	}

}
