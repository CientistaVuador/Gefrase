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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Classe do Dicionário
 * @author Cien
 */
public class Dictionary {

    //mapa dos dicionários padrões, carregados na inicialização da classe
    private static Map<String, Dictionary> dictionaries = new HashMap<>();

    //carrega os dicionários padrões a partir do arquivo dictionaries.xml
    //a propriedade @dictionaries define os ids dos dicionários separados por vírgula
    //definir "idAqui.name" define o nome do dicionário, não é obrigatório
    //definir "idAqui.file" define o nome do arquivo, é obrigatório
    //palavras duplicadas não são permitidas e nem dicionários vazios
    //linhas só com whitespace no dicionário são ignoradas
    private static void loadDictionaries() throws IOException {
        Properties properties = new Properties();
        InputStream in = Dictionary.class.getResourceAsStream("dictionaries.xml");
        if (in == null) {
            return;
        }
        try (BufferedInputStream buffered = new BufferedInputStream(in)) {
            properties.loadFromXML(new BufferedInputStream(buffered));
        }

        String[] ids = properties.getProperty("@dictionaries", "").split(",");
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ids[i].trim();
        }

        for (String id : ids) {
            if (id.isEmpty()) {
                continue;
            }
            
            String file = properties.getProperty(id + ".file");
            if (file == null) {
                throw new IOException("Dictionary file not set for "+id);
            }

            InputStream fileIn = Dictionary.class.getResourceAsStream(file);
            if (fileIn == null) {
                throw new IOException("Dictionary file '"+file+"' not found for "+id);
            }
            
            Set<String> duplicateWordCheck = new HashSet<>();
            List<String> wordsList = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn, StandardCharsets.UTF_8))) {
                int lineCount = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    
                    if (line.isBlank()) {
                        continue;
                    }
                    wordsList.add(line);
                    if (!duplicateWordCheck.add(line)) {
                        throw new IOException("Duplicated word found in line "+lineCount+" in file '"+file+"' of dictionary "+id);
                    }
                }
            }
            
            if (wordsList.isEmpty()) {
                throw new IOException("Dictionary file '"+file+"' is empty for "+id);
            }
            
            dictionaries.put(id, 
                    new Dictionary(
                            id,
                            properties.getProperty(id + ".name"),
                            wordsList.toArray(String[]::new)
                    ));
        }
    }
    
    //carrega os dicionários padrões quando a classe iniciar
    static {
        try {
            loadDictionaries();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    /**
     * Retorna um dicionário definido pelo id
     * @param id o id do dicionário
     * @return o dicionário ou null se não houver
     */
    public static Dictionary getDictionary(String id) {
        return dictionaries.get(id);
    }
    
    /**
     * Retorna a lista de dicionários padrões carregados
     * @return a lista de dicionários padrões carregados
     */
    public static Dictionary[] getDictionaries() {
        return dictionaries.values().toArray(Dictionary[]::new);
    }

    private final String id;
    private final String name;
    private final String[] words;
    private final float bitsPerWord;

    /**
     * Cria um novo dicionário
     * A quantidade de bits por palavra é calculada como log(words.length) / log(2.0)
     * @param id O id do dicionário, não pode ser null
     * @param name O nome do dicionário, id será utilizado no lugar se for null
     * @param words O array de palavras não pode ser null, conter elementos nulls ou ser vazio
     */
    public Dictionary(String id, String name, String[] words) {
        this.id = Objects.requireNonNull(id);
        this.name = (name == null ? this.id : name);
        this.words = words.clone();
        if (this.words.length == 0) {
            throw new IllegalArgumentException("Words is empty");
        }
        for (int i = 0; i < this.words.length; i++) {
            if (this.words[i] == null) {
                throw new NullPointerException("Word is null at index " + i);
            }
        }
        this.bitsPerWord = (float) (Math.log(this.words.length) / Math.log(2.0));
    }

    /**
     * Retorna o id do dicionário
     * @return o id do dicionário, nunca null
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna o nome do dicionário
     * @return o nome do dicionário, nunca null
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna uma cópia do array de palavras
     * @return uma cópia do array de palavras, nunca null
     */
    public String[] getWords() {
        return words.clone();
    }

    /**
     * Retorna a quantidade de bits por palavra
     * @return a quantidade de bits por palavra
     */
    public float getBitsPerWord() {
        return bitsPerWord;
    }

    /**
     * Retorna a quantidade de palavras no dicionário
     * @return a quantidade de palavras no dicionário
     */
    public int getNumberOfWords() {
        return this.words.length;
    }

    /**
     * Retorna a palavra de um índice do dicionário
     * @param index o índice, maior ou igual a zero, menor que a quantidade de palavras no dicionário
     * @return a palavra, nunca null
     */
    public String getWord(int index) {
        return this.words[index];
    }

    /**
     * Retorna uma string para exibição do nome do dicionário para o usuário final
     * @return uma string para exibição
     */
    @Override
    public String toString() {
        return getNumberOfWords() + "p/" + String.format("%.2f", getBitsPerWord()) + "b " + getName();
    }

}
