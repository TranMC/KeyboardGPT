package tn.amin.keyboard_gpt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.language_model.LanguageModel;

public class SPManager implements ConfigInfoProvider {
    protected static final String PREF_NAME = "keyboard_gpt";

    protected static final String PREF_LANGUAGE_MODEL_COMPAT = "language_model";

    protected static final String PREF_LANGUAGE_MODEL = "language_model_v2";

    protected static final String PREF_API_KEY = "%s.api_key";

    protected static final String PREF_SUB_MODEL = "%s.sub_model";

    protected static final String PREF_BASE_URL = "%s.base_url";

    protected static final String PREF_GEN_AI_COMMANDS = "gen_ai_commands";

    private static final String KEY_INSTRUCTION_PREFIX = "instruction_prefix";

    protected final SharedPreferences mSP;

    public SPManager(Context context) {
        mSP = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasLanguageModel() {
        return mSP.contains(PREF_LANGUAGE_MODEL);
    }

    @Override
    public LanguageModel getLanguageModel() {
        String languageModelName = mSP.getString(PREF_LANGUAGE_MODEL, null);
        if (languageModelName == null) {
            languageModelName = LanguageModel.Gemini.name();
        }
        return LanguageModel.valueOf(languageModelName);
    }

    public void setLanguageModel(LanguageModel model) {
        mSP.edit().putString(PREF_LANGUAGE_MODEL, model.name()).apply();
    }

    public void setApiKey(LanguageModel model, String apiKey) {
        String key = String.format(PREF_API_KEY, model.name());
        mSP.edit().putString(key, apiKey).apply();
    }

    public String getApiKey(LanguageModel model) {
        String key = String.format(PREF_API_KEY, model.name());
        return mSP.getString(key, null);
    }

    public void setSubModel(LanguageModel model, String subModel) {
        String key = String.format(PREF_SUB_MODEL, model.name());
        mSP.edit().putString(key, subModel).apply();
    }

    public String getSubModel(LanguageModel model) {
        String key = String.format(PREF_SUB_MODEL, model.name());
        return mSP.getString(key, null);
    }

    public void setBaseUrl(LanguageModel model, String baseUrl) {
        String key = String.format(PREF_BASE_URL, model.name());
        mSP.edit().putString(key, baseUrl).apply();
    }

    public String getBaseUrl(LanguageModel model) {
        String key = String.format(PREF_BASE_URL, model.name());
        return mSP.getString(key, null);
    }

    public void setGenerativeAICommandsRaw(String commands) {
        mSP.edit().putString(PREF_GEN_AI_COMMANDS, commands).apply();
    }

    public String getGenerativeAICommandsRaw() {
        return mSP.getString(PREF_GEN_AI_COMMANDS, "[]");
    }

    public void setGenerativeAICommands(List<GenerativeAICommand> commands) {
        mSP.edit().putString(PREF_GEN_AI_COMMANDS, Commands.encodeCommands(commands)).apply();
    }

    public List<GenerativeAICommand> getGenerativeAICommands() {
        return Commands.decodeCommands(mSP.getString(PREF_GEN_AI_COMMANDS, "[]"));
    }

    public Map<LanguageModel, String> getApiKeyMap() {
        return Arrays.stream(LanguageModel.values())
                .collect(Collectors.toMap(model -> model, model -> {
                    String apiKey = getApiKey(model);
                    if (apiKey == null)
                        apiKey = "";
                    return apiKey;
                }));
    }

    @Override
    public Bundle getConfigBundle() {
        Bundle bundle = new Bundle();
        for (LanguageModel model: LanguageModel.values()) {
            Bundle configBundle = new Bundle();

            configBundle.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY, getApiKey(model));
            configBundle.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL, getSubModel(model));
            configBundle.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL, getBaseUrl(model));

            bundle.putBundle(model.name(), configBundle);
        }
        return bundle;
    }

    public void setInstructionPrefix(String prefix) {
        mSP.edit().putString(KEY_INSTRUCTION_PREFIX, prefix).apply();
    }

    public String getInstructionPrefix() {
        return mSP.getString(KEY_INSTRUCTION_PREFIX, "");
    }
}
