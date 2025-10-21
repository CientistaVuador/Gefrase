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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Classe utilizada para guardar as configurações do programa em xml com o Properties do java
 * @author Cien
 */
public class Configuration {

    //pasta do programa
    public static final String PROGRAM_DIRECTORY_NAME;
    static {
        String userHome = (Main.isPortableModeEnabled() ? null : System.getProperty("user.home"));
        PROGRAM_DIRECTORY_NAME = (userHome == null ? "" : userHome + File.separator) + "Gefrase";
    }
    //extensão do arquivo
    public static final String EXTENSION = ".xml";
    
    //lê as propriedades padrões de dentro da jar (se houver)
    private static Properties readDefault(String name) throws IOException {
        InputStream in = Configuration.class.getResourceAsStream(name + EXTENSION);
        if (in == null) {
            return null;
        }
        try (BufferedInputStream buffered = new BufferedInputStream(in)) {
            Properties prop = new Properties();
            prop.loadFromXML(buffered);
            return prop;
        }
    }

    //nome do arquivo de configuração, caminho do arquivo e propriedades
    private final String name;
    private final Path path;
    private final Properties properties;

    /**
     * Cria um novo objeto de configuração, se houver um arquivo de propriedades padrão dentro
     * da jar ao lado da classe Configuration ele será lido e utilizado como padrão se a
     * propriedade não tiver sido definida
     * 
     * @param name O nome da configuração/arquivo sem extensão
     * @throws IOException Se acontecer algum erro durante a leitura das propriedades
     */
    public Configuration(String name) throws IOException {
        this.name = name;
        this.path = Path.of(PROGRAM_DIRECTORY_NAME, name + EXTENSION);
        this.properties = new Properties(readDefault(name));
        read();
        //salva automaticamente quando a JVM encerrar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                save();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }));
    }
    
    private void read() throws IOException {
        if (!Files.exists(this.path)) {
            return;
        }
        if (!Files.isRegularFile(this.path)) {
            return;
        }
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(this.path))) {
            this.properties.loadFromXML(in);
        }
    }

    /**
     * Retorna o nome da configuração
     * @return O nome da configuração
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna o caminho do arquivo de configuração
     * @return O caminho do arquivo de configuração
     */
    public Path getPath() {
        return path;
    }
    
    /**
     * Retorna as propriedades da configuração
     * @return As propriedades da configuração
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Salva as configurações
     * @throws IOException Se acontecer algum erro
     */
    public void save() throws IOException {
        if (this.properties.isEmpty()) {
            return;
        }
        Files.createDirectories(this.path.getParent());
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(this.path))) {
            this.properties.storeToXML(out, null);
        }
    }

}
