/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package matinilad.gefrase;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A Classe responsável por gerar a impressão das janelas de texto
 *
 * @author Cien
 */
public class PrintableTextSections implements Printable {

    //seções de texto
    private final String[] textSections;
    //define se linhas de corte por seção devem ser adicionadas
    private final boolean sectionCutlines;

    //cache da página gerada
    private PageFormat format = null;
    private String[] pages = null;

    /**
     * Cria uma nova renderização de impressão de texto, o texto é impresso em seções, as seções são separadas por uma quebra de linha, se uma seção não encaixa totalmente na página atual ela é movida para uma nova página
     *
     * @param textSections as seções de texto
     * @param sectionCutlines se linhas de corte entre as seções devem ser adicionadas
     */
    public PrintableTextSections(String[] textSections, boolean sectionCutlines) {
        this.textSections = textSections.clone();
        for (int i = 0; i < textSections.length; i++) {
            Objects.requireNonNull(textSections[i], "text section at index " + i + " is null");
        }
        this.sectionCutlines = sectionCutlines;
    }
    
    //método para limitar uma linha ao máximo da página
    //se uma palavra passar do limite da linha
    //a palavra inteira é movida para uma nova linha
    private String lineWrap(String text, FontMetrics metrics, float maxWidth) {
        StringBuilder b = new StringBuilder();
        StringBuilder wordBuilder = new StringBuilder();
        
        int lineStart = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\t' || c == ' ') {
                b.append(c);
                if (metrics.stringWidth(b.substring(lineStart)) > maxWidth) {
                    b.setCharAt(b.length() - 1, '\n');
                    b.append(c);
                    lineStart = b.length() - 1;
                }
                continue;
            }

            if (c == '\n' || c == '\r') {
                char peek = '\0';
                if ((i + 1) < text.length()) {
                    peek = text.charAt(i + 1);
                }
                b.append(c);
                if (c == '\r' && peek == '\n') {
                    b.append(peek);
                    i++;
                }
                lineStart = b.length();
                continue;
            }
            
            wordBuilder.setLength(0);

            wordBuilder.append(c);
            for (int j = i + 1; j < text.length(); j++) {
                char o = text.charAt(j);
                if (o == ' ' || o == '\t') {
                    wordBuilder.append(o);
                    break;
                }
                if (o == '\n' || o == '\r') {
                    break;
                }
                wordBuilder.append(o);
            }
            String word = wordBuilder.toString();

            b.append(word);
            if (metrics.stringWidth(b.substring(lineStart)) <= maxWidth) {
                i += word.length() - 1;
                continue;
            }

            b.setLength(b.length() - word.length());
            if (lineStart != b.length()) {
                b.append('\n');
                lineStart = b.length();
            }
            for (int j = 0; j < word.length(); j++) {
                char o = word.charAt(j);
                b.append(o);
                if (metrics.stringWidth(b.substring(lineStart)) > maxWidth) {
                    b.setCharAt(b.length() - 1, '\n');
                    b.append(o);
                    lineStart = b.length() - 1;
                }
            }

            i += word.length() - 1;
        }

        return b.toString();
    }

    //método para gerar as páginas a partir das seções
    //e limitar ao máximo da página
    //se uma seção não couber numa página
    //a seção é movida para uma nova página
    private String[] createPages(String[] sections, String cutline, FontMetrics metrics, float maxHeight) {
        List<String> list = new ArrayList<>();

        StringBuilder b = new StringBuilder();
        int pageLines = 0;
        boolean lineMode = false;
        sectionLoop:
        for (int i = 0; i < sections.length; i++) {
            String section = sections[i];
            
            StringBuilder e = new StringBuilder();
            
            if (cutline != null) {
                if (b.isEmpty()) {
                    e.append(cutline).append('\n').append(section);
                } else {
                    e.append(section);
                }
                e.append('\n').append(cutline);
            } else {
                e.append(section);
            }
            e.append('\n');
            
            section = e.toString();

            int sectionStart = b.length();
            for (int j = 0; j < section.length(); j++) {
                char c = section.charAt(j);
                
                if (c == '\n' || c == '\r') {
                    b.append(c);
                    
                    if (c == '\r') {
                        char peek = '\0';
                        if ((j + 1) < section.length()) {
                            peek = section.charAt(j + 1);
                        }
                        if (c == '\r' && peek == '\n') {
                            b.append(peek);
                            j++;
                        }
                    }
                    
                    pageLines++;
                    
                    if (pageLines > 1 && (pageLines * metrics.getHeight()) > maxHeight) {
                        if (lineMode) {
                            list.add(b.toString());
                            b.setLength(0);
                            pageLines = 0;
                        } else {
                            b.setLength(sectionStart);
                            if (!b.isEmpty()) {
                                list.add(b.toString());
                                b.setLength(0);
                            }
                            pageLines = 0;
                            lineMode = true;
                            i--;
                            continue sectionLoop;
                        }
                    }
                    
                    continue;
                }

                b.append(c);
            }
            
            if (lineMode) {
                lineMode = false;
            }
        }
        list.add(b.toString());

        return list.toArray(String[]::new);
    }

    //o método para renderizar texto na página
    private void renderText(String text, Graphics2D g, FontMetrics metrics) {
        float y = 0f;
        y += metrics.getHeight();

        Iterator<String> lines = text.lines().iterator();
        while (lines.hasNext()) {
            String line = lines.next();
            g.drawString(line, 0f, y);
            y += metrics.getHeight();
        }
    }

    /**
     * Renderiza a página para o graphics, de acordo com o Printable do java
     *
     * @param graphics o graphics
     * @param pageFormat o formato da página
     * @param pageIndex o índice da página
     * @return Printable.PAGE_EXISTS se a página existe, Printable.NO_SUCH_PAGE se não
     * @throws PrinterException se alguma exception acontecer
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        Graphics2D g = (Graphics2D) graphics;

        g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        g.setColor(Color.BLACK);

        FontMetrics metrics = g.getFontMetrics();

        if (!Objects.equals(this.format, pageFormat)) {
            final float pageWidth = (float) pageFormat.getImageableWidth();
            final float pageHeight = (float) pageFormat.getImageableHeight();

            String[] sections = new String[this.textSections.length];
            for (int i = 0; i < sections.length; i++) {
                sections[i] = lineWrap(this.textSections[i], metrics, pageWidth);
            }

            String cutline = null;
            if (this.sectionCutlines) {
                cutline = "-";
                while (metrics.stringWidth(cutline + "-") < pageWidth) {
                    cutline += "-";
                }
            }

            this.pages = createPages(sections, cutline, metrics, pageHeight);
            this.format = pageFormat;
        }

        if (pageIndex < 0 || pageIndex >= this.pages.length) {
            return Printable.NO_SUCH_PAGE;
        }

        renderText(this.pages[pageIndex], g, metrics);
        return Printable.PAGE_EXISTS;
    }

}
