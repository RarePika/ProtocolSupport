package protocolsupport.protocol.transformer.mcpe.middlepacketimpl.clientbound;

import java.io.IOException;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.ClientboundPEPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.clientbound.SetGameModePacket;
import protocolsupport.protocol.transformer.middlepacket.clientbound.play.MiddleGameStateChange;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableEmptyList;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

public class GameStateChange extends MiddleGameStateChange<RecyclableCollection<? extends ClientboundPEPacket>> {

	@Override
	public RecyclableCollection<? extends ClientboundPEPacket> toData(ProtocolVersion version) throws IOException {
		if (type == 3) {
			return RecyclableSingletonList.create(new SetGameModePacket((int) value));
		} else {
			return RecyclableEmptyList.get();
		}
	}

}