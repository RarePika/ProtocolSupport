package protocolsupport.protocol.transformer.middlepacketimpl.clientbound.play.v_1_8;

import java.io.IOException;

import org.bukkit.World.Environment;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.ClientBoundPacket;
import protocolsupport.protocol.transformer.middlepacket.clientbound.play.MiddleChunk;
import protocolsupport.protocol.transformer.middlepacketimpl.PacketData;
import protocolsupport.protocol.transformer.utils.ChunkTransformer;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

public class Chunk extends MiddleChunk<RecyclableCollection<PacketData>>  {

	private final ChunkTransformer transformer = new ChunkTransformer();

	@Override
	public RecyclableCollection<PacketData> toData(ProtocolVersion version) throws IOException {
		PacketData serializer = PacketData.create(ClientBoundPacket.PLAY_CHUNK_SINGLE_ID, version);
		serializer.writeInt(chunkX);
		serializer.writeInt(chunkZ);
		serializer.writeBoolean(full);
		serializer.writeShort(bitmask);
		transformer.loadData(data, bitmask, player.getWorld().getEnvironment() == Environment.NORMAL, full);
		serializer.writeArray(transformer.to18Data());
		return RecyclableSingletonList.create(serializer);
	}

}
