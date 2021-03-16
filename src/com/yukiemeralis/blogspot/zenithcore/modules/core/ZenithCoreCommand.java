package com.yukiemeralis.blogspot.zenithcore.modules.core;

import java.util.Arrays;

import com.yukiemeralis.blogspot.zenithcore.ZenithCore;
import com.yukiemeralis.blogspot.zenithcore.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenithcore.gui.ZenithGUI;
import com.yukiemeralis.blogspot.zenithcore.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenithcore.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenithcore.utils.TextComponentBuilder;
import com.yukiemeralis.blogspot.zenithcore.utils.VersionCtrl;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.ClickEvent;

@SuppressWarnings("unused")
public class ZenithCoreCommand extends ZenithCommand 
{
    public ZenithCoreCommand() 
    {
        super("zen", Arrays.asList("zenith"));

        linkCommandDescription("", "Display information about this plugin.");
        linkCommandDescription("help", "Displays a list of registered Zenith commands.");
        linkCommandDescription("enchant <enchantment | list> <level>", "Enchant held item, or display a list of enchantments.");
        linkCommandDescription("recolor <name>", "Apply a name to held item. Supports standard colors/formatting using &.");
        linkCommandDescription("mods", "Display currently loaded modules.");
        linkCommandDescription("setlore", "Set held item's lore. Supports \\n as a newline character.");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) 
    {
        if (args.length == 0)
        {
            PrintUtils.sendMessage(sender, "ZenithCore version: " + VersionCtrl.getVersion());
            return true;
        }

        String subcmd = args[0];
        ItemStack held_item;
        String target_name;

        Player player;

        ZenithGUI gui;

        switch (subcmd)
        {
            case "help":
                ZenithCore.getCommands().forEach(command -> {
                    PrintUtils.sendMessage(sender, "§f- §oFrom parent command: /" + command.getName() + " §r-");

                    command.getCommandDescriptions().forEach((name, desc) -> {
                        if (name.equals(""))
                        {
                            if (!(sender instanceof ConsoleCommandSender))
                            {
                                PrintUtils.sendTextComponent((Player) sender, 
                                    TextComponentBuilder.regularText("§8[§dz§8] "),
                                    TextComponentBuilder.setHoverText(
                                        TextComponentBuilder.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "§7/" + command.getName(), "/" + command.getName()), 
                                        "§aClick here to autofill command!"  
                                    ),
                                    TextComponentBuilder.regularText("§7 - " + desc)
                                );
                            } else {
                                PrintUtils.sendMessage(sender, "/" + command.getName() + " - " + desc);
                            }
                        } else {
                            if (!(sender instanceof ConsoleCommandSender))
                            {
                                PrintUtils.sendTextComponent((Player) sender, 
                                    TextComponentBuilder.regularText("§8[§dz§8] "),
                                    TextComponentBuilder.setHoverText(
                                        TextComponentBuilder.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "§7/" + command.getName() + " " + name, "/" + command.getName() + " " + name), 
                                        "§aClick here to autofill command!"  
                                    ),
                                    TextComponentBuilder.regularText("§7 - " + desc)
                                );
                            } else {
                                PrintUtils.sendMessage(sender, "/" + command.getName() + " " + name + " - " + desc);
                            }
                        }
                    });
                });

                if (sender instanceof Player)
                    PrintUtils.sendMessage(sender, "Tip: You can click a command to autofill it.");

                return true;
            case "recolor":
                if (!(sender instanceof Player))
                {
                    PrintUtils.sendMessage(sender, "ERROR: Only players can recolor items.");
                    return true;
                }

                if (args.length == 1)
                {
                    sender.sendMessage("ERROR: A name must be specified.");
                    return true;
                }

                held_item = ((Player) sender).getInventory().getItem(EquipmentSlot.HAND);
                target_name = PrintUtils.concatStringArray(args, 1);

                target_name = target_name.replace("&", "§");

                ItemUtils.applyName(held_item, target_name);

                PrintUtils.sendMessage(sender, "Success! Renamed item.");
                return true;
            case "enchant":
                if (!(sender instanceof Player))
                {
                    PrintUtils.sendMessage(sender, "ERROR: Only players can enchant items.");
                    return true;
                }

                held_item = ((Player) sender).getInventory().getItem(EquipmentSlot.HAND);

                if (args.length == 1)
                {
                    sender.sendMessage("ERROR: Usage: /zen enchant <enchantment> <level> OR /zen enchant list");
                    return true;
                }

                if (args.length == 2)
                {
                    if (args[1].equals("list"))
                    {
                        for (Enchantment e : Enchantment.values())
                            PrintUtils.sendMessage(sender, e.getKey().getKey());
                        return true;
                    } else {
                        sender.sendMessage("ERROR: Usage: /zen enchant <enchantment> <level> OR /zen enchant list");
                        return true;
                    }
                }

                Enchantment enchant = ItemUtils.enchantFromName(args[1]);

                if (enchant == null)
                {
                    PrintUtils.sendMessage(sender, "ERROR: Invalid enchantment. See /zen enchant list for a list of valid enchantments.");
                    return true;
                }

                int level;
                try {
                    level = Integer.parseInt(args[2]);

                    if (level > 32767)
                        PrintUtils.sendMessage(sender, "WARN: Enchantment level is higher than 32767. Item may not work correctly.");
                } catch (NumberFormatException error) {
                    PrintUtils.sendMessage(sender, "ERROR: Level must be a number.");
                    return true;
                }

                ItemUtils.applyEnchantment(held_item, enchant, level);
                PrintUtils.sendMessage(sender, "Success! Enchanted item with " + args[1] + " " + level + ".");
                return true;
            case "mods":
                if (sender instanceof ConsoleCommandSender)
                {
                    PrintUtils.sendMessage(sender, "- Loaded Zenith modules: -");
                    ZenithCore.getModules().forEach(module -> {
                        PrintUtils.sendMessage(sender, module.getName() + " (" + module.getCommands().size() + " commands, " + module.getListeners().size() + " events | " + module.getPriority() + " priority)");
                    });
                } else {
                    gui = new ZenithModsGUI();
                    gui.init();
                    gui.display((Player) sender);
                }
                
                return true;
            case "setlore":
                if (!(sender instanceof Player))
                {
                    PrintUtils.sendMessage(sender, "ERROR: Only players can set lore for items.");
                    return true;
                }

                if (args.length == 1)
                {
                    sender.sendMessage("ERROR: Lore must be specified.");
                    return true;
                }

                held_item = ((Player) sender).getInventory().getItem(EquipmentSlot.HAND);
                String lore_buffer = PrintUtils.concatStringArray(args, 1).replace("&", "§");
                ItemUtils.applyLore(held_item, lore_buffer.split("\n"));
                return true;
            default:
                PrintUtils.sendMessage(sender, "ERROR: \"" + subcmd + "\" is not a recognized command!");
                return true;
        }
    }
}