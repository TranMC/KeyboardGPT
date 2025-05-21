package tn.amin.keyboard_gpt.instruction;

public enum InstructionCategory {
    Prompt("?"),
    Command("!"),
    InstructionPrefix("!?"),
    None(null),
    ;

    public final String prefix;
    InstructionCategory(String prefix) {
        this.prefix = prefix;
    }
}
