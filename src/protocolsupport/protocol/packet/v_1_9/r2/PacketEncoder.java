package protocolsupport.protocol.packet.v_1_9.r2;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_10_R1.EnumProtocol;
import net.minecraft.server.v1_10_R1.EnumProtocolDirection;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PacketListener;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.middle.ClientBoundMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.PacketData;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_8__1_9_r1__1_9_r2.BlockChangeMulti;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_8__1_9_r1__1_9_r2.BlockChangeSingle;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_9_r1__1_9_r2.EntityMetadata;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_9_r1__1_9_r2.Login;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_9_r1__1_9_r2.SpawnLiving;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_9_r1__1_9_r2.SpawnNamed;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_9_r1__1_9_r2.SpawnObject;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_9_r1__1_9_r2.WorldSound;
import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_9_r2.Chunk;
import protocolsupport.protocol.packet.middleimpl.clientbound.status.v_1_7__1_8__1_9_r1.ServerInfo;
import protocolsupport.protocol.pipeline.IPacketEncoder;
import protocolsupport.protocol.serializer.ChainedProtocolSupportPacketDataSerializer;
import protocolsupport.protocol.storage.LocalStorage;
import protocolsupport.protocol.storage.SharedStorage;
import protocolsupport.protocol.utils.registry.MiddleTransformerRegistry;
import protocolsupport.protocol.utils.registry.MiddleTransformerRegistry.InitCallBack;
import protocolsupport.utils.netty.Allocator;
import protocolsupport.utils.netty.ChannelUtils;
import protocolsupport.utils.recyclable.RecyclableCollection;

//TODO: Full remapping rewrite table
public class PacketEncoder implements IPacketEncoder {

	private final MiddleTransformerRegistry<ClientBoundMiddlePacket<RecyclableCollection<PacketData>>> registry = new MiddleTransformerRegistry<>();
	{
		registry.register(EnumProtocol.STATUS, ClientBoundPacket.STATUS_SERVER_INFO_ID, ServerInfo.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_CHUNK_SINGLE_ID, Chunk.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_WORLD_SOUND_ID, WorldSound.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_LOGIN_ID, Login.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_SPAWN_NAMED_ID, SpawnNamed.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_SPAWN_LIVING_ID, SpawnLiving.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_SPAWN_OBJECT_ID, SpawnObject.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_ENTITY_METADATA_ID, EntityMetadata.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_BLOCK_CHANGE_SINGLE_ID, BlockChangeSingle.class);
		registry.register(EnumProtocol.PLAY, ClientBoundPacket.PLAY_BLOCK_CHANGE_MULTI_ID, BlockChangeMulti.class);
		registry.setCallBack(new InitCallBack<ClientBoundMiddlePacket<RecyclableCollection<PacketData>>>() {
			@Override
			public void onInit(ClientBoundMiddlePacket<RecyclableCollection<PacketData>> object) {
				object.setSharedStorage(sharedstorage);
				object.setLocalStorage(storage);
			}
		});
	}

	protected final SharedStorage sharedstorage;
	protected final LocalStorage storage = new LocalStorage();
	public PacketEncoder(SharedStorage storage) {
		this.sharedstorage = storage;
	}

	private final ChainedProtocolSupportPacketDataSerializer middlebuffer = new ChainedProtocolSupportPacketDataSerializer();

	@Override
	public void encode(ChannelHandlerContext ctx, Packet<PacketListener> packet, ByteBuf output) throws Exception {
		Channel channel = ctx.channel();
		EnumProtocol currentProtocol = channel.attr(ChannelUtils.CURRENT_PROTOCOL_KEY).get();
		final Integer packetId = currentProtocol.a(EnumProtocolDirection.CLIENTBOUND, packet);
		if (packetId == null) {
			throw new IOException("Can't serialize unregistered packet");
		}
		middlebuffer.clear();
		packet.b(middlebuffer.getNativeSerializer());
		ClientBoundMiddlePacket<RecyclableCollection<PacketData>> packetTransformer = registry.getTransformer(currentProtocol, packetId);
		if (packetTransformer != null) {
			if (packetTransformer.needsPlayer()) {
				packetTransformer.setPlayer(ChannelUtils.getBukkitPlayer(channel));
			}
			packetTransformer.readFromServerData(middlebuffer);
			packetTransformer.handle();
			RecyclableCollection<PacketData> data = packetTransformer.toData(ProtocolVersion.MINECRAFT_1_9_4);
			try {
				for (PacketData packetdata : data) {
					ByteBuf senddata = Allocator.allocateBuffer();
					ChannelUtils.writeVarInt(senddata, packetdata.getPacketId());
					senddata.writeBytes(packetdata);
					ctx.write(senddata);
				}
				ctx.flush();
			} finally {
				for (PacketData packetdata : data) {
					packetdata.recycle();
				}
				data.recycle();
			}
		} else {
			ByteBuf senddata = Allocator.allocateBuffer();
			ChannelUtils.writeVarInt(senddata, packetId);
			senddata.writeBytes(middlebuffer);
			ctx.writeAndFlush(senddata);
		}
	}

}
