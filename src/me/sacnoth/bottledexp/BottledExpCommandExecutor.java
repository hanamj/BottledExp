package me.sacnoth.bottledexp;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BottledExpCommandExecutor implements CommandExecutor {

	public BottledExpCommandExecutor(BottledExp plugin) {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("bottle")
					&& BottledExp.checkPermission("bottle.use", player)) {
				int currentxp = BottledExp.getPlayerExperience(player);

				if (args.length == 0) {
					sender.sendMessage(BottledExp.langCurrentXP + ": "
							+ currentxp + " XP!");
				} else if (args.length == 1) {
					int amount = 0;
					if (args[0].equals("max")) {
						if (BottledExp.checkPermission("bottle.max", player)) {
							amount = (int) Math.floor(currentxp
									/ BottledExp.xpCost);
							if (BottledExp.settingUseItems) {
								amount = Math.min(
										BottledExp.countItems(player,
												BottledExp.settingConsumedItem)
												/ BottledExp.amountConsumed,
										amount);
							}
							if (BottledExp.useVaultEcon) {
								amount = Math.min((int) Math.floor(BottledExp
										.getBalance(player)
										/ BottledExp.moneyCost), amount);
							}
						} else {
							return false;
						}
					} else if (args[0].equals("reload")) {
						if (BottledExp.checkPermission("bottle.reload", player)) {
							BottledExp.config.reload(sender);
							sender.sendMessage(ChatColor.GREEN
									+ "Config reloaded!");
							return true;
						} else {
							return false;
						}
					} else {
						try {
							amount = Integer.valueOf(args[0]).intValue();
						} catch (NumberFormatException nfe) {
							sender.sendMessage(ChatColor.RED
									+ BottledExp.errAmount);
							return false;
						}
					}
					if (currentxp < amount * BottledExp.xpCost) {
						sender.sendMessage(ChatColor.RED + BottledExp.errXP);
						return true;
					} else if (amount <= 0) {
						amount = 0;
						sender.sendMessage(BottledExp.langOrder1 + " " + amount
								+ " " + BottledExp.langOrder2);
						return true;
					}

					boolean money = false;
					if (BottledExp.useVaultEcon) // Check if the player has enough
												// money
					{
						if (BottledExp.getBalance(player) > BottledExp.moneyCost
								* amount) {
							money = true;
						} else {
							player.sendMessage(BottledExp.errMoney);
							return true;
						}
					}

					boolean consumeItems = false;
					if (BottledExp.settingUseItems) // Check if the player has
													// enough items
					{
						consumeItems = BottledExp.checkInventory(player,
								BottledExp.settingConsumedItem, amount
										* BottledExp.amountConsumed);
						if (!consumeItems) {
							sender.sendMessage(ChatColor.RED
									+ BottledExp.langItemConsumer);
							return true;
						}
					}

					PlayerInventory inventory = player.getInventory();
					ItemStack items = new ItemStack(384, amount);
					HashMap<Integer, ItemStack> leftoverItems = inventory
							.addItem(items);
					player.setTotalExperience(0);
					player.setLevel(0);
					player.setExp(0);
					player.giveExp(currentxp - (amount * BottledExp.xpCost));
					if (leftoverItems.containsKey(0)) {
						int refundAmount = leftoverItems.get(0).getAmount();
						player.giveExp(refundAmount * BottledExp.xpCost);
						player.sendMessage(BottledExp.langRefund + ": "
								+ refundAmount);
						amount -= refundAmount;
					}

					if (money) // Remove money from player
					{
						BottledExp.withdrawMoney(player, BottledExp.moneyCost
								* amount);
						player.sendMessage(BottledExp.langMoney + ": "
								+ BottledExp.moneyCost * amount + " "
								+ BottledExp.economy.currencyNamePlural());
					}

					if (consumeItems) // Remove items from Player
					{
						if (!BottledExp.consumeItem(player,
								BottledExp.settingConsumedItem, amount
										* BottledExp.amountConsumed)) {
							sender.sendMessage(ChatColor.RED
									+ BottledExp.langItemConsumer);
							return true;
						}
					}

					sender.sendMessage(BottledExp.langOrder1 + " " + amount
							+ " " + BottledExp.langOrder2);
				}
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "You have to be a player!");
			return false;
		}
		return false;
	}
}
