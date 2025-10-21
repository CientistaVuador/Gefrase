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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.DrbgParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * A classe principal do programa
 *
 * @author Cien
 */
public class Main {
    
    private static boolean portableMode = false;
    
    public static boolean isPortableModeEnabled() {
        return Main.portableMode;
    }
    
    /**
     * O ponto de partida do programa, o método main
     *
     * @param args os argumentos do programa
     */
    public static void main(String args[]) {
        //modo portátil do programa, usa a workdir ao invés da home
        List<String> arguments = List.of(args).stream().map(String::toLowerCase).toList();
        if (arguments.contains("-portable")) {
            Main.portableMode = true;
        }
        
        //inicializa o FlatDarkLaf
        if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }
        FlatDarkLaf.setup();
        
        //cria a configuração do programa
        Configuration config;
        try {
            config = new Configuration("application");
        } catch (IOException ex) {
            TextDialog.ofThrowable(null, ex).setVisible(true);
            throw new UncheckedIOException(ex);
        }

        EventQueue.invokeLater(() -> {
            //cria o coletor de entropia e define o callback para iniciar o programa
            new EntropyCollector() {
                @Override
                public void onSeedReady(byte[] seed) {
                    try {
                        //inicializa o CSPRNG do java
                        SecureRandom random = SecureRandom.getInstance("DRBG",
                                DrbgParameters.instantiation(256,
                                        DrbgParameters.Capability.RESEED_ONLY,
                                        null));
                        random.setSeed(seed);
                        
                        //abre a janela principal
                        MainWindow m = new MainWindow(config, random);
                        m.setLocationRelativeTo(this);
                        m.setVisible(true);
                    } catch (NoSuchAlgorithmException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }.setVisible(true);
        });
    }

}
