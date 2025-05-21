package tn.amin.keyboard_gpt.instruction;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.command.CommandTreater;
import tn.amin.keyboard_gpt.instruction.prompt.PromptTreater;
import tn.amin.keyboard_gpt.instruction.prefix.InstructionPrefixTreater;

public class InstructionTreater implements TextTreater {
    private final CommandTreater mCommandTreater;
    private final PromptTreater mPromptTreater;
    private final InstructionPrefixTreater mInstructionPrefixTreater;

    public InstructionTreater(SPManager spManager, UiInteracter interacter, GenerativeAIController aiController) {
        mPromptTreater = new PromptTreater(spManager, interacter, aiController);
        mCommandTreater = new CommandTreater(spManager, interacter, aiController);
        mInstructionPrefixTreater = new InstructionPrefixTreater(spManager, interacter);
    }

    public InstructionCategory getInstructionCategory(String text) {
        if (text == null) {
            return InstructionCategory.None;
        }
        
        for (InstructionCategory type: InstructionCategory.values()) {
            if (type.prefix != null && text.startsWith(type.prefix)) {
                return type;
            }
        }
        
        return InstructionCategory.None;
    }

    public String removeInstructionPrefix(String text, InstructionCategory category) {
        if (text == null || category.prefix == null) {
            return null;
        }

        if (text.length() < category.prefix.length()) {
            return null;
        }

        return text.substring(category.prefix.length()).trim();
    }

    public boolean isInstruction(String text) {
        return getInstructionCategory(text) != InstructionCategory.None;
    }

    @Override
    public boolean treat(String text) {
        InstructionCategory category = getInstructionCategory(text);
        String instruction = removeInstructionPrefix(text, category);
        switch (category) {
            case Prompt:
                return mPromptTreater.treat(instruction);
            case Command:
                return mCommandTreater.treat(instruction);
            case InstructionPrefix:
                return mInstructionPrefixTreater.treat(instruction);
            case None:
                MainHook.log("Aborting performCommand because text is not a valid instruction");
                break;
        }

        return false;
    }
}
