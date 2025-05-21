package tn.amin.keyboard_gpt.instruction.prefix;

import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.TextTreater;

public class InstructionPrefixTreater implements TextTreater {
    private final SPManager mSPManager;
    private final UiInteracter mInteracter;

    public InstructionPrefixTreater(SPManager spManager, UiInteracter interacter) {
        mSPManager = spManager;
        mInteracter = interacter;
        MainHook.log("InstructionPrefixTreater initialized");
    }

    @Override
    public boolean treat(String text) {
        MainHook.log("InstructionPrefixTreater.treat called with: " + text);
        if (text == null || text.isEmpty()) {
            MainHook.log("Showing instruction prefix dialog");
            showInstructionPrefixDialog();
            return true;
        }
        MainHook.log("No action taken");
        return false;
    }

    private void showInstructionPrefixDialog() {
        MainHook.log("Calling showInstructionPrefixDialog");
        mInteracter.showInstructionPrefixDialog();
    }
} 