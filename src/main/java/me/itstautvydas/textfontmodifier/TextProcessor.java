package me.itstautvydas.textfontmodifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.kyori.adventure.key.Key;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class TextProcessor {

    private final TextFontModifierPlugin plugin;
    private final Pattern regexPattern;

    public TextProcessor(TextFontModifierPlugin plugin) {
        this.plugin = plugin;
        regexPattern = Pattern.compile(getRegex());
    }

    public Key getFont() {
        return Key.key(Objects.requireNonNull(plugin.getConfig().getString("font")));
    }

    public String getRegex() {
        return plugin.getConfig().getString("regex");
    }

    public boolean isRegexInverted() {
        return plugin.getConfig().getBoolean("invert-regex");
    }

    public String getSpecialSymbolForScoreboard() {
        return plugin.getConfig().getString("special-symbol-for-scoreboards");
    }

    public void processExtra(String font, JsonArray array, String specialSymbol) {
        int i = 0;
        for (var elem : array) {
            var obj = elem.getAsJsonObject();
            var previous = i > 0 ? array.get(i - 1).getAsJsonObject() : null;
            processText(font, previous, obj, specialSymbol);
            if (obj.has("extra"))
                processExtra(font, obj.getAsJsonArray("extra"), specialSymbol);
            i++;
        }
    }

    public void processText(String font, JsonObject previous, JsonObject obj, String specialSymbol) {
        if (obj == null)
            return;
        if (obj.has("text")) {
            String str = obj.get("text").getAsString();
            if (isRegexInverted() != regexPattern.matcher(str.toLowerCase()).matches()) {
                if (obj.has("clickEvent") && previous != null) {
                    String previousText = previous.get("text").getAsString();
                    obj.remove("clickEvent");
                    previous.addProperty("text", "");
                    str = previousText + str;
                }
                if (specialSymbol == null || str.contains(specialSymbol)) {
                    if (specialSymbol != null)
                        obj.addProperty("text", str.replace(specialSymbol, ""));
                    obj.addProperty("font", font);
                }
            }
        }
    }

    public void modifyFontJson(JsonElement json, String specialSymbol) {
        if (json == null)
            return;

        if (!json.isJsonObject())
            return;

        var obj = json.getAsJsonObject();
        if (obj == null)
            return;
        var font = getFont().toString();
        if (obj.has("extra"))
            processExtra(font, obj.getAsJsonArray("extra"), specialSymbol);
        processText(font, null, obj, specialSymbol);
    }

    public String modifyFontString(String str) {
        return str.replace("\"text\"", "\"font\":\"" + getFont() + "\",\"text\"");
    }
}
