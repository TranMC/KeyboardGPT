package tn.amin.keyboard_gpt.ui;

public enum DialogType {
    ChoseModel(true),
    ConfigureModel(true),
    WebSearch(false),
    EditCommandsList(false),
    EditCommand(false),
    InstructionPrefix,
    ;

    final boolean isModelConfig;

    DialogType (boolean isModelConfig) {
        this.isModelConfig = isModelConfig;
    }
}
