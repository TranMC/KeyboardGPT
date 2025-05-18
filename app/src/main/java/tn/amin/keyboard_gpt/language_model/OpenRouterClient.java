package tn.amin.keyboard_gpt.language_model;

public class OpenRouterClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.OpenRouter;
    }
} 