package tn.amin.keyboard_gpt.instruction.command;

import tn.amin.keyboard_gpt.instruction.InstructionPrefixManager;

import java.util.Map;

public class PrefixCommand {
    private final InstructionPrefixManager prefixManager;

    public PrefixCommand(InstructionPrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    public String handleCommand(String command) {
        String[] parts = command.split("\\s+", 3);
        if (parts.length < 2) {
            return "Invalid command format. Use: !prefix [save|get|delete|list] [name] [instruction]";
        }

        String action = parts[1];
        switch (action) {
            case "save":
                if (parts.length < 3) {
                    return "Missing instruction. Use: !prefix save [name] [instruction]";
                }
                String name = parts[2];
                String instruction = command.substring(command.indexOf(name) + name.length()).trim();
                prefixManager.savePrefix(name, instruction);
                return "Prefix saved successfully";

            case "get":
                if (parts.length < 3) {
                    return "Missing name. Use: !prefix get [name]";
                }
                String prefix = prefixManager.getPrefix(parts[2]);
                return prefix != null ? prefix : "Prefix not found";

            case "delete":
                if (parts.length < 3) {
                    return "Missing name. Use: !prefix delete [name]";
                }
                prefixManager.deletePrefix(parts[2]);
                return "Prefix deleted successfully";

            case "list":
                StringBuilder sb = new StringBuilder("Available prefixes:\n");
                for (Map.Entry<String, String> entry : prefixManager.getAllPrefixes().entrySet()) {
                    sb.append("- ").append(entry.getKey()).append("\n");
                }
                return sb.toString();

            default:
                return "Unknown action. Available actions: save, get, delete, list";
        }
    }
} 