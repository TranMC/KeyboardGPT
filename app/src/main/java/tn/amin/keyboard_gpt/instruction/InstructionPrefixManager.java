package tn.amin.keyboard_gpt.instruction;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

public class InstructionPrefixManager {
    private static final String PREF_NAME = "instruction_prefix";
    private static final String PREFIX_KEY = "prefix_";
    private final SharedPreferences preferences;
    private final Map<String, String> prefixCache;

    public InstructionPrefixManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefixCache = new HashMap<>();
        loadPrefixes();
    }

    private void loadPrefixes() {
        Map<String, ?> all = preferences.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith(PREFIX_KEY)) {
                String name = entry.getKey().substring(PREFIX_KEY.length());
                prefixCache.put(name, (String) entry.getValue());
            }
        }
    }

    public void savePrefix(String name, String instruction) {
        preferences.edit().putString(PREFIX_KEY + name, instruction).apply();
        prefixCache.put(name, instruction);
    }

    public String getPrefix(String name) {
        return prefixCache.get(name);
    }

    public void deletePrefix(String name) {
        preferences.edit().remove(PREFIX_KEY + name).apply();
        prefixCache.remove(name);
    }

    public Map<String, String> getAllPrefixes() {
        return new HashMap<>(prefixCache);
    }

    public String applyPrefix(String name, String prompt) {
        String prefix = getPrefix(name);
        if (prefix != null) {
            return prefix + "\n\n" + prompt;
        }
        return prompt;
    }
} 