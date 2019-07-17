package jeresources.collection;

import jeresources.entry.VillagerEntry;
import jeresources.util.FakeMerchant;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TradeList extends LinkedList<TradeList.Trade> {
    private static final Random r = new Random();
    private static final IMerchant m = new FakeMerchant();

    private VillagerEntry villagerEntry;

    public TradeList(VillagerEntry villagerEntry) {
        this.villagerEntry = villagerEntry;
    }

    public List<ItemStack> getFirstBuyStacks() {
        return this.stream().map(Trade::getMinBuyStack1).collect(Collectors.toList());
    }

    public List<ItemStack> getSecondBuyStacks() {
        return this.stream().map(Trade::getMinBuyStack2).filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
    }

    public List<ItemStack> getSellStacks() {
        return this.stream().map(Trade::getMinSellStack).collect(Collectors.toList());
    }

    public TradeList getSubListSell(ItemStack itemStack) {
        return this.stream().filter(trade -> trade.sellsItem(itemStack)).collect(Collectors.toCollection(() -> new TradeList(villagerEntry)));
    }

    public TradeList getSubListBuy(ItemStack itemStack) {
        return this.stream().filter(trade -> trade.buysItem(itemStack)).collect(Collectors.toCollection(() -> new TradeList(villagerEntry)));
    }

    public TradeList getFocusedList(IFocus<ItemStack> focus) {
        if (focus == null) {
            return this;
        } else {
            switch (focus.getMode()) {
                case INPUT:
                    return getSubListBuy(focus.getValue());
                case OUTPUT:
                    return getSubListSell(focus.getValue());
                default:
                    return this;
            }
        }
    }

    public void addITradeList(EntityVillager.ITradeList tradeList) {
        MerchantRecipeList tempList = new MerchantRecipeList();
        for (int itr = 0; itr < 100; itr++)
            tradeList.addMerchantRecipe(m, tempList, r);
        if (tempList.size() == 0) return; // Bad lists be bad
        ItemStack buy1 = tempList.get(0).getItemToBuy();
        ItemStack buy2 = tempList.get(0).getSecondItemToBuy();
        ItemStack sell = tempList.get(0).getItemToSell();
        int minBuy1, minBuy2, minSell;
        int maxBuy1, maxBuy2, maxSell;
        minBuy1 = maxBuy1 = buy1.getCount();
        if (!buy2.isEmpty()) minBuy2 = maxBuy2 = buy2.getCount();
        else minBuy2 = maxBuy2 = 1; // Needs to be one with the new ItemStack.EMPTY implementation
        minSell = maxSell = sell.getCount();
        for (MerchantRecipe merchantRecipe : tempList) {
            if (minBuy1 > merchantRecipe.getItemToBuy().getCount())
                minBuy1 = merchantRecipe.getItemToBuy().getCount();
            if (!buy2.isEmpty() && minBuy2 > merchantRecipe.getSecondItemToBuy().getCount())
                minBuy2 = merchantRecipe.getSecondItemToBuy().getCount();
            if (minSell > merchantRecipe.getItemToSell().getCount())
                minSell = merchantRecipe.getItemToSell().getCount();

            if (maxBuy1 < merchantRecipe.getItemToBuy().getCount())
                maxBuy1 = merchantRecipe.getItemToBuy().getCount();
            if (!buy2.isEmpty() && maxBuy2 < merchantRecipe.getSecondItemToBuy().getCount())
                maxBuy2 = merchantRecipe.getSecondItemToBuy().getCount();
            if (maxSell < merchantRecipe.getItemToSell().getCount())
                maxSell = merchantRecipe.getItemToSell().getCount();
        }
        this.add(
            new Trade(
                buy1, minBuy1, maxBuy1,
                buy2, minBuy2, maxBuy2,
                sell, minSell, maxSell
            )
        );
    }

    public static class Trade {
        private final ItemStack buy1, buy2, sell;
        private final int minBuy1, minBuy2, minSell;
        private final int maxBuy1, maxBuy2, maxSell;

        Trade(
            ItemStack buy1, int minBuy1, int maxBuy1,
            ItemStack buy2, int minBuy2, int maxBuy2,
            ItemStack sell, int minSell, int maxSell
        ) {
            this.buy1 = buy1;
            this.minBuy1 = minBuy1;
            this.maxBuy1 = maxBuy1;
            this.buy2 = buy2;
            this.minBuy2 = minBuy2;
            this.maxBuy2 = maxBuy2;
            this.sell = sell;
            this.minSell = minSell;
            this.maxSell = maxSell;
        }

        public boolean sellsItem(ItemStack itemStack) {
            return this.sell.isItemEqual(itemStack);
        }

        public boolean buysItem(ItemStack itemStack) {
            return this.buy1.isItemEqual(itemStack) || (!this.buy2.isEmpty() && this.buy2.isItemEqual(itemStack));
        }

        public ItemStack getMinBuyStack1() {
            ItemStack minBuyStack = this.buy1.copy();
            minBuyStack.setCount(this.minBuy1);
            return minBuyStack;
        }

        public ItemStack getMinBuyStack2() {
            if (this.buy2 == null) return ItemStack.EMPTY;
            ItemStack minBuyStack = this.buy2.copy();
            minBuyStack.setCount(this.minBuy2);
            return minBuyStack;
        }

        public ItemStack getMinSellStack() {
            ItemStack minSellStack = this.sell.copy();
            minSellStack.setCount(this.minSell);
            return minSellStack;
        }

        public ItemStack getMaxBuyStack1() {
            ItemStack maxBuyStack = this.buy1.copy();
            maxBuyStack.setCount(this.maxBuy1);
            return maxBuyStack;
        }

        public ItemStack getMaxBuyStack2() {
            if (this.buy2 == null) return ItemStack.EMPTY;
            ItemStack maxBuyStack = this.buy2.copy();
            maxBuyStack.setCount(this.maxBuy2);
            return maxBuyStack;
        }

        public ItemStack getMaxSellStack() {
            ItemStack maxSellStack = this.sell.copy();
            maxSellStack.setCount(this.maxSell);
            return maxSellStack;
        }

        @Override
        public String toString() {
            return "Buy1: " + this.buy1 + ", Buy2: " + this.buy2 + ", Sell: " + this.sell;
        }
    }
}
