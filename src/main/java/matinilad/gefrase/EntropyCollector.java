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

import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ImageIcon;
import javax.swing.Timer;

/**
 * Coletor de entropia usado para alimentar o gerador de senhas
 *
 * @author Cien
 */
@SuppressWarnings("serial")
public class EntropyCollector extends javax.swing.JFrame {

    //algoritmo da chave
    public static final String KEY_ALGORITHM = "HmacSHA256";
    //tamanho da chave
    public static final int KEY_SIZE_BYTES;
    static {
        try {
            KEY_SIZE_BYTES = Mac.getInstance(KEY_ALGORITHM).getMacLength();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    //assumindo pelo menos 4 bits de entropia por tecla
    //para um total de pelo menos 256 bits de entropia no final
    public static final int KEY_RELEASE_COUNT = 64;
    //quantidade de chaves
    public static final int NUMBER_OF_KEYS = 4;
    
    //ícone do coletor
    private final ImageIcon icon = new ImageIcon(MainWindow.class.getResource("icon.png"));

    //quando a última tecla foi solta
    private long lastKeyReleasedTime = System.nanoTime();
    //quanto tempo demorou para processar o último evento
    private long lastProcessingTime = 0;
    
    //o secure random usado para gerar as chaves iniciais do coletor
    private final SecureRandom strongRandom;
    {
        try {
            this.strongRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    //hash de informações do sistema/programa
    private final byte[] info;
    
    {
        long here = System.nanoTime();
        
        byte[] totalData;
        try {
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(array)) {
                Runtime r = Runtime.getRuntime();
                
                //id do coletor
                data.write(HexFormat.of().parseHex(
                        "3dfa23dbcd3023827422f83163bcce00c61e0b99a21be51c2d3eb779a2391e16"));
                
                //tempo do sistema
                data.writeLong(System.currentTimeMillis());
                //tempo da jvm
                data.writeLong(System.nanoTime());

                //memória máxima
                data.writeLong(r.maxMemory());
                //memória total
                data.writeLong(r.totalMemory());
                //memória livre
                data.writeLong(r.freeMemory());

                //núcleos da cpu
                data.writeInt(r.availableProcessors());

                //todas as propriedades da jvm
                for (Entry<Object, Object> e : System.getProperties().entrySet()) {
                    byte[] keyData = e.getKey().toString().getBytes(StandardCharsets.UTF_8);
                    byte[] valueData = e.getValue().toString().getBytes(StandardCharsets.UTF_8);

                    data.writeInt(keyData.length);
                    data.write(keyData);
                    data.writeInt(valueData.length);
                    data.write(valueData);
                }

                //todas as propriedades do sistema
                for (Entry<String, String> e : System.getenv().entrySet()) {
                    byte[] keyData = e.getKey().getBytes(StandardCharsets.UTF_8);
                    byte[] valueData = e.getValue().getBytes(StandardCharsets.UTF_8);

                    data.writeInt(keyData.length);
                    data.write(keyData);
                    data.writeInt(valueData.length);
                    data.write(valueData);
                }
            }
            totalData = array.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        
        //info = Hmac(salt, dados)
        try {
            Mac mac = Mac.getInstance(KEY_ALGORITHM);
            mac.init(new SecretKeySpec(this.strongRandom.generateSeed(KEY_SIZE_BYTES), KEY_ALGORITHM));
            mac.update(totalData);
            this.info = mac.doFinal();
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }

        //usa o tempo de processamento do id como tempo inicial
        //de processamento do último evento
        this.lastProcessingTime = System.nanoTime() - here;
    }
    
    //salt para os dados gerados pelo coletor
    private final SecretKey saltKey = new SecretKeySpec(
            this.strongRandom.generateSeed(KEY_SIZE_BYTES), KEY_ALGORITHM);
    
    //chaves iniciais do coletor
    private final SecretKey[] keys = new SecretKey[NUMBER_OF_KEYS];
    
    {
        for (int i = 0; i < this.keys.length; i++) {
            this.keys[i] = new SecretKeySpec(
                    this.strongRandom.generateSeed(KEY_SIZE_BYTES), KEY_ALGORITHM);
        }
    }

    //se o callback já foi executado
    private boolean callbackExecuted = false;
    
    /**
     * Cria um novo coletor de entropia
     * 
     */
    public EntropyCollector() {
        initComponents();
        setLocationRelativeTo(null);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textInputBox = new javax.swing.JTextArea();
        entropyCounter = new javax.swing.JProgressBar();
        keyDataField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        generateKeysField = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Coletor de Entropia");
        setIconImage(icon.getImage());
        setMinimumSize(new java.awt.Dimension(500, 345));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        infoLabel.setText("Aperte teclas aleatórias aqui até a barra preencher e aguarde ou feche a janela para pular:");

        textInputBox.setColumns(20);
        textInputBox.setLineWrap(true);
        textInputBox.setRows(5);
        textInputBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textInputBoxKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(textInputBox);

        entropyCounter.setMaximum(KEY_RELEASE_COUNT);

        keyDataField.setEditable(false);
        keyDataField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        keyDataField.setText("00000000000000000000000000000000000000000000000000000000");

        generateKeysField.setEditable(false);
        generateKeysField.setColumns(20);
        generateKeysField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        generateKeysField.setRows(5);
        generateKeysField.setText("0000000000000000000000000000000000000000000000000000000000000000\n0000000000000000000000000000000000000000000000000000000000000000\n0000000000000000000000000000000000000000000000000000000000000000\n0000000000000000000000000000000000000000000000000000000000000000");
        updateGeneratedKeysField();
        jScrollPane2.setViewportView(generateKeysField);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(entropyCounter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(keyDataField, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(infoLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(entropyCounter, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(keyDataField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * O callback para quando a seed estiver pronta
     *
     * @param seed a seed gerada
     */
    public void onSeedReady(byte[] seed) {
        
    }

    //atualiza o campo de chaves geradas
    private void updateGeneratedKeysField() {
        HexFormat hex = HexFormat.of();
        List<String> list = new ArrayList<>();
        for (SecretKey key : this.keys) {
            list.add(hex.formatHex(key.getEncoded()));
        }
        this.generateKeysField.setText(list.stream().collect(Collectors.joining("\n")));
    }

    //fecha a janela e executa o callback
    private void executeConsumer() {
        if (!this.callbackExecuted) {
            this.callbackExecuted = true;
            setVisible(false);
            
            ByteBuffer b = ByteBuffer.allocate(this.keys.length * EntropyCollector.KEY_SIZE_BYTES);
            for (SecretKey key : this.keys) {
                b.put(key.getEncoded());
            }
            onSeedReady(b.array());
            
            dispose();
        }
    }

    //alimenta o coletor quando uma tecla é solta
    private void textInputBoxKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textInputBoxKeyReleased
        //se a barra de progresso já encheu então temos o suficiente
        if (this.entropyCounter.isIndeterminate()) {
            return;
        }
        
        //tempo de início do evento
        long nanoTime = System.nanoTime();
        //calcula a diferença de tempo entre a última tecla solta e a atual
        long keyReleaseTime = nanoTime - this.lastKeyReleasedTime;
        //define o tempo da última tecla solta para a atual
        this.lastKeyReleasedTime = nanoTime;
        
        //dados utilizados pelo coletor a cada evento:
        // tempo do sistema (8 bytes)
        // tempo de processamento do último evento (8 bytes)
        // diferença de tempo entre a última tecla solta e a atual (8 bytes)
        // código da tecla solta (4 bytes)
        byte[] ikm = ByteBuffer
                .allocate(8 + 8 + 8 + 4)
                .putLong(System.currentTimeMillis())
                .putLong(this.lastProcessingTime)
                .putLong(keyReleaseTime)
                .putInt(evt.getKeyCode())
                .array();
        
        //define o campo de dados em hexadecimal
        this.keyDataField.setText(HexFormat.of().formatHex(ikm));
        
        //processamento das chaves
        try {
            Mac mac = Mac.getInstance(KEY_ALGORITHM);
            ByteBuffer b = ByteBuffer.allocate(4);
            
            //extrai uma chave a partir dos dados coletados
            //chaveExtraida = Hmac(salt, dados)
            mac.init(this.saltKey);
            mac.update(ikm);
            SecretKey extractedKey = new SecretKeySpec(mac.doFinal(), KEY_ALGORITHM);
            
            //gera N chaves a partir da chave extraida
            //
            //iteração 0: chaveGerada(0) = Hmac(chaveExtraida, info | 0)
            //iteração i: chaveGerada(i) = Hmac(chaveExtraida, chaveGerada(i - 1) | info | i)
            mac.init(extractedKey);
            SecretKey[] generatedKeys = new SecretKey[NUMBER_OF_KEYS];
            for (int i = 0; i < generatedKeys.length; i++) {
                if (i != 0) {
                    mac.update(generatedKeys[i - 1].getEncoded());
                }
                mac.update(this.info);
                mac.update(b.putInt(0, i).array());
                generatedKeys[i] = new SecretKeySpec(mac.doFinal(), KEY_ALGORITHM);
            }
            
            //mistura as chaves geradas nas chaves do coletor
            //iteração i: chave(i) = Hmac(chaveGerada(i), chave(i) | info | contador | i)
            for (int i = 0; i < this.keys.length; i++) {
                mac.init(generatedKeys[i]);
                
                mac.update(this.keys[i].getEncoded());
                mac.update(this.info);
                mac.update(b.putInt(0, this.entropyCounter.getValue()).array());
                mac.update(b.putInt(0, i).array());

                this.keys[i] = new SecretKeySpec(mac.doFinal(), KEY_ALGORITHM);
            }
            
            //atualiza a lista de chaves geradas da gui
            updateGeneratedKeysField();
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }

        //incrementa a barra de progresso em 1 até atingir o limite
        if (this.entropyCounter.getValue() < KEY_RELEASE_COUNT) {
            this.entropyCounter.setValue(this.entropyCounter.getValue() + 1);
        } else if (!this.entropyCounter.isIndeterminate()) {
            //inicia um timer de 3 segundos para executar o callback e faz um beep
            //a função do beep é avisar ao usuário que a contagem chegou ao fim
            //a função do timer é dar tempo o suficiente para o usuário parar de digitar
            //e evitar que teclas aleatórias sejam enviadas para o próprio programa ou para outros
            Timer timer = new Timer(3000, (t) -> {
                executeConsumer();
            });
            timer.setRepeats(false);
            timer.start();
            this.entropyCounter.setIndeterminate(true);
            Toolkit.getDefaultToolkit().beep();
        }

        //define o tempo de processamento do evento atual
        this.lastProcessingTime = System.nanoTime() - nanoTime;
    }//GEN-LAST:event_textInputBoxKeyReleased

    //se a janela foi fechada, executa o consumer para iniciar o programa com a seed gerada
    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        executeConsumer();
    }//GEN-LAST:event_formWindowClosed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar entropyCounter;
    private javax.swing.JTextArea generateKeysField;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField keyDataField;
    private javax.swing.JTextArea textInputBox;
    // End of variables declaration//GEN-END:variables
}
