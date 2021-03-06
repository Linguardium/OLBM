package io.github.alloffabric.olbm;

import io.github.alloffabric.olbm.api.LootBagType;
import io.github.alloffabric.olbm.inventory.LootBagContainer;
import io.github.alloffabric.olbm.util.OLBRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OLBM implements ModInitializer {
	public static final String MODID = "olbm";

	public static final Logger logger = LogManager.getLogger();

	public static final OLBRegistry<LootBagType> LOOT_BAG_TYPES = new OLBRegistry<>();

	public static final ItemGroup OLBM_GROUP = FabricItemGroupBuilder.build(new Identifier(MODID, "olbm_group"), () -> {
		Random random = new Random();
		List<Item> potential = new ArrayList<>();
		for (LootBagType type : LOOT_BAG_TYPES) {
			if (type.getBag() != Items.AIR) {
				potential.add(type.getBag());
			}
		}
		if (!potential.isEmpty()) {
			return new ItemStack(potential.get(random.nextInt(potential.size())));
		} else {
			return new ItemStack(Items.ENDER_CHEST);
		}
	});

	@Override
	public void onInitialize() {
		OLBData.loadConfig();
		OLBData.loadData();

		ContainerProviderRegistry.INSTANCE.registerFactory(new Identifier(MODID, "loot_bag"), (syncId, containerId, player, buf) -> {
			Identifier id = buf.readIdentifier();
			int length = buf.readVarInt();
			DefaultedList<ItemStack> stacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
			for (int i = 0; i < Math.min(length, 27); i++) {
				ItemStack stack = buf.readItemStack();
				stacks.set(i, stack);
			}
			return new LootBagContainer(id, syncId, player, new BasicInventory(stacks.toArray(new ItemStack[]{})));
		});
	}

	public static LootBagType registerBag(LootBagType type) {
		Identifier id = type.getId();
		return Registry.register(LOOT_BAG_TYPES, id, type);
	}
}
