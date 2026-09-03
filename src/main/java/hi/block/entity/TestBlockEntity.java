package hi.block.entity;

import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult; import net.minecraft.world.InteractionResult;import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestBlockEntity extends BlockEntity {
	private static final Map<Integer, ItemStack> CHANNELS = new ConcurrentHashMap<>();
	private ItemStack cassette = ItemStack.EMPTY;
	private ItemStack sourceImage = ItemStack.EMPTY;
	private ItemStack shownImage = ItemStack.EMPTY;
	private boolean sourceMode = true;
	private int channel = 0;
	private int syncCooldown = 0;

	public TestBlockEntity(BlockPos pos, BlockState state) {
		super(CreateTheAirWarsModBlockEntities.TEST.get(), pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TestBlockEntity blockEntity) {
		if (level.isClientSide) {
			return;
		}
		if (blockEntity.syncCooldown > 0) {
			blockEntity.syncCooldown--;
		}
		blockEntity.syncChannel();
	}

	public InteractionResult use(Player player, InteractionHand hand) {
		if (this.level == null) {
			return net.minecraft.world.InteractionResult.PASS;
		}
		if (this.level.isClientSide) {
			return net.minecraft.world.InteractionResult.SUCCESS;
		}
		ItemStack heldStack = player.getItemInHand(hand);
		if (player.isShiftKeyDown() && heldStack.getItem() == CreateTheAirWarsModItems.DISPLAY.get() && !this.cassette.isEmpty()) {
			this.channel = (this.channel + 1) % 16;
			this.syncCooldown = 0;
			this.setChangedAndSync();
			player.displayClientMessage(Component.translatable("message.create_the_air_wars.test_channel", this.channel), true);
			return net.minecraft.world.InteractionResult.CONSUME;
		}
		if (player.isShiftKeyDown() && heldStack.isEmpty()) {
			this.sourceMode = !this.sourceMode;
			this.syncCooldown = 0;
			this.setChangedAndSync();
			player.displayClientMessage(Component.translatable(this.sourceMode ? "message.create_the_air_wars.test_mode_source" : "message.create_the_air_wars.test_mode_monitor"), true);
			return net.minecraft.world.InteractionResult.CONSUME;
		}
		if (heldStack.getItem() == CreateTheAirWarsModItems.DISPLAY.get() && this.cassette.isEmpty()) {
			this.cassette = heldStack.copyWithCount(1);
			if (!player.getAbilities().instabuild) {
				heldStack.shrink(1);
			}
			this.syncCooldown = 0;
			this.setChangedAndSync();
			player.displayClientMessage(Component.translatable("message.create_the_air_wars.test_inserted"), true);
			return net.minecraft.world.InteractionResult.CONSUME;
		}
		if (heldStack.isEmpty() && !this.cassette.isEmpty()) {
			ItemStack toReturn = this.cassette.copy();
			this.cassette = ItemStack.EMPTY;
			this.sourceImage = ItemStack.EMPTY;
			this.shownImage = ItemStack.EMPTY;
			if (!player.getInventory().add(toReturn)) {
				player.drop(toReturn, false);
			}
			this.syncCooldown = 0;
			this.setChangedAndSync();
			player.displayClientMessage(Component.translatable("message.create_the_air_wars.test_ejected"), true);
			return net.minecraft.world.InteractionResult.CONSUME;
		}
		if (!heldStack.isEmpty() && heldStack.getItem() != CreateTheAirWarsModItems.DISPLAY.get() && this.sourceMode && !this.cassette.isEmpty()) {
			this.sourceImage = heldStack.copyWithCount(1);
			this.shownImage = this.sourceImage.copy();
			this.syncCooldown = 0;
			this.setChangedAndSync();
			player.displayClientMessage(Component.translatable("message.create_the_air_wars.test_source_set"), true);
			return net.minecraft.world.InteractionResult.CONSUME;
		}
		return net.minecraft.world.InteractionResult.PASS;
	}

	private void syncChannel() {
		if (this.level == null || this.cassette.isEmpty() || this.syncCooldown > 0) {
			return;
		}
		this.syncCooldown = 5;
		if (this.sourceMode) {
			if (!this.sourceImage.isEmpty()) {
				CHANNELS.put(this.channel, this.sourceImage.copy());
			}
		} else {
			ItemStack image = CHANNELS.get(this.channel);
			ItemStack next = image == null ? ItemStack.EMPTY : image.copy();
			if (!ItemStack.matches(this.shownImage, next)) {
				this.shownImage = next;
				this.setChangedAndSync();
			}
		}
	}

	public ItemStack getShownImage() {
		return this.shownImage;
	}

	public ItemStack getCassette() {
		return this.cassette;
	}

	public boolean isSourceMode() {
		return this.sourceMode;
	}

	public int getChannel() {
		return this.channel;
	}

	public void dropAll() {
		if (this.level == null || this.level.isClientSide) {
			return;
		}
		if (!this.cassette.isEmpty()) {
			Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, this.cassette.copy());
		}
		if (!this.sourceImage.isEmpty()) {
			Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, this.sourceImage.copy());
		}
	}

	private void setChangedAndSync() {
		this.setChanged();
		if (this.level instanceof ServerLevel serverLevel) {
			serverLevel.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
	}

	@Override
	protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		if (!this.cassette.isEmpty()) {
			tag.put("Cassette", this.cassette.save(provider));
		}
		if (!this.sourceImage.isEmpty()) {
			tag.put("SourceImage", this.sourceImage.save(provider));
		}
		if (!this.shownImage.isEmpty()) {
			tag.put("ShownImage", this.shownImage.save(provider));
		}
		tag.putBoolean("SourceMode", this.sourceMode);
		tag.putInt("Channel", this.channel);
	}

	@Override
	protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		this.cassette = tag.contains("Cassette") ? ItemStack.parse(provider, tag.getCompound("Cassette")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
		this.sourceImage = tag.contains("SourceImage") ? ItemStack.parse(provider, tag.getCompound("SourceImage")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
		this.shownImage = tag.contains("ShownImage") ? ItemStack.parse(provider, tag.getCompound("ShownImage")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
		this.sourceMode = tag.getBoolean("SourceMode");
		this.channel = tag.getInt("Channel");
	}

	@Override
	public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
		return this.saveWithoutMetadata(provider);
	}
}
