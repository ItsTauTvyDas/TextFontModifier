package me.itstautvydas.textfontmodifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
public class TextProcessor {

    private final TextFontModifierPlugin plugin;
    private final Pattern regexPattern;
    private final Map<String, Font> fonts = new HashMap<>();

    public TextProcessor(TextFontModifierPlugin plugin) {
        this.plugin = plugin;
        regexPattern = Pattern.compile(getRegex());

        // Cache it
        var fonts = plugin.getConfig().getConfigurationSection("fonts");
        assert fonts != null;
        for (var fontName : fonts.getKeys(false)) {
            var section = fonts.getConfigurationSection(fontName);
            assert section != null;
            this.fonts.put(fontName, new Font(section.getString("name"), section.getString("special-symbol")));
        }
        plugin.getLogger().info("Registered fonts: " + String.join(", ", this.fonts.keySet()));
    }

    public String getRegex() {
        return plugin.getConfig().getString("regex.value");
    }

    public boolean isRegexInverted() {
        return plugin.getConfig().getBoolean("regex.invert");
    }

    public void processExtra(@Nullable Font font, JsonArray array) {
        int i = 0;
        for (var elem : array) {
            var obj = elem.getAsJsonObject();
            var previous = i > 0 ? array.get(i - 1).getAsJsonObject() : null;
            processText(font, previous, obj);
            if (obj.has("extra"))
                processExtra(font, obj.getAsJsonArray("extra"));
            i++;
        }
    }

    private Font findFontSymbol(String text) {
        for (var font : fonts.values()) {
            if (text.contains(font.specialSymbol()))
                return font;
        }
        return null;
    }

    public void processText(@Nullable Font font, JsonObject previous, JsonObject obj) {
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
                if (font == null) {
                    font = findFontSymbol(str);
                    if (font != null) {
                        obj.addProperty("text", str.replace(font.specialSymbol(), ""));
                        obj.addProperty("font", font.font());
                    }
                    return;
                }
                obj.addProperty("font", font.font());
            }
        }
    }

    public void modifyFontJson(String packetName, JsonElement json) {
        if (json == null)
            return;

        if (!json.isJsonObject())
            return;

        var obj = json.getAsJsonObject();
        if (obj == null)
            return;

        var section = plugin.getConfig().getConfigurationSection("packets." + packetName);
        assert section != null;

        var fontName = section.getString("forced-font");
        assert fontName != null;
        var font = fonts.get(fontName);

        if (obj.has("extra"))
            processExtra(font, obj.getAsJsonArray("extra"));
        processText(font, null, obj);
    }
}
