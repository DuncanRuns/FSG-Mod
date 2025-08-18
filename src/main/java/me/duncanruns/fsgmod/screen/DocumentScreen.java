package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.screen.online.LoadingOnlineFiltersScreen;
import me.duncanruns.fsgmod.screen.online.OnlineFiltersScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public class DocumentScreen extends Screen {
    private final String doc;
    private static final int LINE_GAP = 2;

    private int docHeight;
    private int docWidth;
    private int availableHeight;
    private List<StringRenderable> lines;

    protected DocumentScreen(Text title, String doc) {
        super(title);
        this.doc = doc;
    }

    @Override
    protected void init() {
        assert this.client != null;
        lines = createLines(doc);
        docHeight = (textRenderer.fontHeight + LINE_GAP) * lines.size();
        docWidth = lines.stream().mapToInt(string -> textRenderer.getWidth(string)).max().orElse(width - 40);
        availableHeight = height - 40;

        addButton(new ButtonWidget(width - 105, height - 25, 100, 20, new TranslatableText("gui.done"), b ->
                this.client.openScreen(
                        new LoadingOnlineFiltersScreen(filterInfos -> client.openScreen(
                                new OnlineFiltersScreen(filterInfos, FSGModConfig.getInstance().selectedOnlineFilters)
                        ))
                ))
        );
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        int docY;
        if (docHeight <= availableHeight) {
            docY = 10 + (availableHeight - docHeight) / 2;
        } else {
            int topPos = 10;
            int bottomPos = height - 30 - docHeight;

            float t = (mouseY - 10f) / (height - 40f);
            t = Math.max(0, Math.min(1, t));

            docY = Math.round(topPos * (1 - t) + bottomPos * t);
        }

        renderBackground(matrices);
        int x = width / 2 - docWidth / 2;
        int y = docY;
        for (StringRenderable line : lines) {
            textRenderer.draw(matrices, line, x, y, 0xFFFFFF);
            y += textRenderer.fontHeight + LINE_GAP;
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    private List<StringRenderable> createLines(String doc) {
        String[] initialLines = doc.split("\\r?\\n");
        List<StringRenderable> out = new ArrayList<>();
        for (String line : initialLines) {
            if (line.trim().isEmpty()) out.add(StringRenderable.plain(""));
            else out.addAll(textRenderer.wrapLines(StringRenderable.plain(line), width - 40));
        }
        return out;
    }
}
