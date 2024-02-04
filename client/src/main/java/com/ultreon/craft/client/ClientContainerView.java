//package com.ultreon.craft.client;
//
//import com.ultreon.craft.client.gui.screens.container.ContainerScreen;
//import com.ultreon.craft.entity.Player;
//import com.ultreon.craft.item.ItemStack;
//import com.ultreon.craft.network.packets.c2s.C2SCloseContainerMenuPacket;
//import com.ultreon.craft.network.packets.c2s.C2SContainerClickPacket;
//import com.ultreon.craft.world.container.ContainerInteraction;
//import com.ultreon.craft.world.container.ContainerView;
//
//public class ClientContainerView implements ContainerView {
//    private final UltracraftClient client = UltracraftClient.get();
//    private final ContainerScreen screen;
//
//    public ClientContainerView(ContainerScreen screen) {
//        this.screen = screen;
//    }
//
//    @Override
//    public void onItemChanged(int slot, ItemStack newStack) {
//        screen.onItemChanged(slot, newStack);
//    }
//
//    @Override
//    public void onSlotClick(int slot, Player player, ContainerInteraction interaction) {
//        client.connection.send(new C2SContainerClickPacket(slot, interaction));
//    }
//
//    @Override
//    public void onContainerClosed(Player player) {
//        client.connection.send(new C2SCloseContainerMenuPacket());
//    }
//
//    @Override
//    public boolean hasPlaceFor(ItemStack item) {
//        return true;
//    }
//
//    @Override
//    public ItemStack moveInto(ItemStack item) {
//        return item;
//    }
//}
