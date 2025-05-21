package tn.amin.keyboard_gpt.instruction;

public enum InstructionCategory {
    Prompt("?"),
    InstructionPrefix("!?"),
    Command("!"),
    None(null),
    ;

    public final String prefix;
    InstructionCategory(String prefix) {
        this.prefix = prefix;
    }
}
