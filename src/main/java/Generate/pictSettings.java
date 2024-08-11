package Generate;

public class pictSettings {
    private String prompt;
    private Styles style;
    private String negativePrompt;

    public pictSettings(String prompt, Styles style, String negativePrompt) {
        this.prompt = prompt;
        this.style = style;
        this.negativePrompt = negativePrompt;
    }

    public pictSettings() {
        this.prompt = "пусто";
        this.style = Styles.DEFAULT;
        this.negativePrompt = "пусто";
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Styles getStyle() {
        return style;
    }

    public void setStyle(Styles style) {
        this.style = style;
    }

    public String getNegativePrompt() {
        return negativePrompt;
    }

    public void setNegativePrompt(String negativePrompt) {
        this.negativePrompt = negativePrompt;
    }
}
