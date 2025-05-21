package tn.amin.keyboard_gpt;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.widget.TextView;
import android.widget.EditText;
import tn.amin.keyboard_gpt.instruction.InstructionTreater;
import tn.amin.keyboard_gpt.instruction.InstructionPrefixManager;
import tn.amin.keyboard_gpt.instruction.command.PrefixCommand;

public class KeyboardGPTBrain {
    private final SPManager mSPManager;
    private final UiInteracter mInteracter;
    private final GenerativeAIController mAIController;
    private final InstructionTreater mInstructionTreater;
    private final InstructionPrefixManager prefixManager;
    private final PrefixCommand prefixCommand;
    private EditText editText;
    private String currentPrefix;

    public KeyboardGPTBrain(Context context) {
        mSPManager = new SPManagerCompat(context);
        mInteracter = new UiInteracter(context, mSPManager);

        mAIController = new GenerativeAIController(mSPManager, mInteracter);
        mInstructionTreater = new InstructionTreater(mSPManager, mInteracter, mAIController);
        this.prefixManager = new InstructionPrefixManager(context);
        this.prefixCommand = new PrefixCommand(prefixManager);
    }

    public boolean consumeText(String text) {
        if (text.startsWith("!prefix")) {
            String result = prefixCommand.handleCommand(text);
            if (editText != null) {
                editText.setText(result);
            }
            return true;
        }
        return mInstructionTreater.isInstruction(text) || isEditTextOwned();
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
        mInteracter.setEditText(editText);
    }

    public boolean performCommand() {
        if (editText == null) return false;
        
        String text = editText.getText().toString();
        if (text.startsWith("!prefix")) {
            return false;
        }

        if (currentPrefix != null) {
            text = prefixManager.applyPrefix(currentPrefix, text);
        }

        if (mInstructionTreater.isInstruction(text)) {
            return mInstructionTreater.treat(text);
        }

        return true;
    }

    public boolean isEditTextOwned() {
        return mInteracter.isEditTextOwned();
    }

    public UiInteracter getInteracter() {
        return mInteracter;
    }

    public void onInputMethodDestroy(InputMethodService inputMethodService) {
        getInteracter().unregisterService(inputMethodService);
    }

    public void onInputMethodCreate(InputMethodService inputMethodService) {
        getInteracter().registerService(inputMethodService);
    }

    public void setCurrentPrefix(String prefix) {
        this.currentPrefix = prefix;
    }
}
