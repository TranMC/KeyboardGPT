package tn.amin.keyboard_gpt.instruction;

import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;

public class InstructionPrefixTreater implements TextTreater {
    private final SPManager mSPManager;
    private final UiInteracter mInteracter;

    public InstructionPrefixTreater(SPManager spManager, UiInteracter interacter) {
        mSPManager = spManager;
        mInteracter = interacter;
    }

    @Override
    public boolean treat(String text) {
        if (text == null || text.isEmpty()) {
            showInstructionPrefixDialog();
            return true;
        }
        return false;
    }

    private void showInstructionPrefixDialog() {
        mInteracter.showInstructionPrefixDialog();
    }
} 