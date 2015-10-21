package protocolsupport.protocol.transformer.mcpe.packet.mcpe.clientbound;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.ItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.PacketDataSerializer;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.ClientboundPEPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.PEPacketIDs;

public class SpawnPlayerPacket implements ClientboundPEPacket {

	protected UUID uuid;
	protected String name;
	protected int entityId;
	protected float locX;
	protected float locY;
	protected float locZ;
	protected float yaw;
	protected float pitch;
	protected ItemStack item;

	public SpawnPlayerPacket(UUID uuid, String name, int entityId, float locX, float locY, float locZ, float yaw, float pitch, ItemStack item) {
		this.uuid = uuid;
		this.name = name;
		this.entityId = entityId;
		this.locX = locX;
		this.locY = locY;
		this.locZ = locZ;
		this.yaw = yaw;
		this.pitch = pitch;
		this.item = item;
	}

	@Override
	public int getId() {
		return PEPacketIDs.ADD_PLAYER_PACKET;
	}

	@Override
	public ClientboundPEPacket encode(ByteBuf buf) throws Exception {
		PacketDataSerializer serializer = new PacketDataSerializer(buf, ProtocolVersion.MINECRAFT_PE);
		serializer.writeUUID(uuid);
		serializer.writeString(name);
		serializer.writeLong(entityId);
		serializer.writeFloat(locX);
		serializer.writeFloat(locY);
		serializer.writeFloat(locZ);
		serializer.writeFloat(0);
		serializer.writeFloat(0);
		serializer.writeFloat(0);
		serializer.writeFloat(yaw);
		serializer.writeFloat(0);
		serializer.writeFloat(pitch);
		serializer.writeItemStack(item);
		//TODO: actual metadata
		//flags
		serializer.writeByte((0 << 5) | (0 & 0x1F));
		serializer.writeByte(0);
		//air
		serializer.writeByte((1 << 5) | (1 & 0x1F));
		serializer.writeShort(ByteBufUtil.swapShort((short) 300));
		//nametag
		serializer.writeByte((4 << 5) | (2 & 0x1F));
		serializer.writeShort(ByteBufUtil.swapShort((short) name.length()));
		serializer.writeBytes(name.getBytes(StandardCharsets.UTF_8));
		//show nametag
		serializer.writeByte((0 << 5) | (3 & 0x1F));
		serializer.writeByte(1);
		//silent
		serializer.writeByte((0 << 5) | (4 & 0x1F));
		serializer.writeByte(0);
		//noai
		serializer.writeByte((0 << 5) | (15 & 0x1F));
		serializer.writeByte(0);
		//asleep
		serializer.writeByte((0 << 5) | (16 & 0x1F));
		serializer.writeByte(0);
		//end
		serializer.writeByte(0x7F);
		/*TIntObjectHashMap<DataWatcherObject> metadata = new TIntObjectHashMap<>();
		metadata.put(0, new DataWatcherObject(ValueType.BYTE, (byte) 0));
		metadata.put(1, new DataWatcherObject(ValueType.SHORT_PE, (short) 300));
		metadata.put(2, new DataWatcherObject(ValueType.STRING_PE, ""));
		metadata.put(3, new DataWatcherObject(ValueType.BYTE, (byte) 1));
		metadata.put(4, new DataWatcherObject(ValueType.BYTE, (byte) 0));
		metadata.put(15, new DataWatcherObject(ValueType.BYTE, (byte) 0));
		serializer.writeBytes(DataWatcherSerializer.encodeData(ProtocolVersion.MINECRAFT_PE, metadata));*/
		return this;
	}

}
