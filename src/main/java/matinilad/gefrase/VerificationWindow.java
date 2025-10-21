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
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;

/**
 * A janela do código de verificação
 *
 * @author Cien
 */
@SuppressWarnings("serial")
public class VerificationWindow extends javax.swing.JDialog {

    //um salt constante do código de verificação
    //para ser combinado com um aleatório depois
    private static final byte[] PROGRAM_SALT
            = HexFormat.of().parseHex("3cdd70694419a64afc8f5703f371c4a6");

    //o tamanho do salt aleatório
    public static final int RANDOM_SALT_SIZE = 3;
    //o tamanho do hash
    public static final int HASH_SIZE = 5;
    //o tamanho total do código de verificação
    public static final int VERIFICATION_CODE_SIZE = RANDOM_SALT_SIZE + HASH_SIZE;
    
    /**
     * Retorna um salt aleatório para ser usado na geração do código de verificação
     * @param random o SecureRandom para ser usado (não null)
     * @return um salt aleatório para ser usado na geração do código de verificação
     */
    public static byte[] getRandomSalt(SecureRandom random) {
        byte[] randomSalt = new byte[RANDOM_SALT_SIZE];
        random.nextBytes(randomSalt);
        return randomSalt;
    }

    /**
     * Gera os bytes do código de verificação a partir da senha
     * 
     * @param randomSalt o salt (não null)
     * @param password a senha
     * @return os bytes do código de verificação
     */
    public static byte[] getVerificationCode(byte[] randomSalt, char[] password) {
        if (randomSalt.length != RANDOM_SALT_SIZE) {
            throw new IllegalArgumentException("random salt length must be " + RANDOM_SALT_SIZE);
        }

        byte[] salt = new byte[PROGRAM_SALT.length + randomSalt.length];
        System.arraycopy(PROGRAM_SALT, 0, salt, 0, PROGRAM_SALT.length);
        System.arraycopy(randomSalt, 0, salt, PROGRAM_SALT.length, randomSalt.length);

        byte[] verificationBytes = new byte[randomSalt.length + HASH_SIZE];
        System.arraycopy(randomSalt, 0, verificationBytes, 0, randomSalt.length);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec pbe = new PBEKeySpec(password, salt, 10_000, 256);
            try {
                SecretKey secret = factory.generateSecret(pbe);

                System.arraycopy(
                        secret.getEncoded(), 0,
                        verificationBytes, randomSalt.length,
                        verificationBytes.length - randomSalt.length);
            } finally {
                pbe.clearPassword();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException(ex);
        }

        return verificationBytes;
    }

    /**
     * Converte os bytes de um código de verificação para sua representação
     * em hexadecimal.
     * 
     * @param code os bytes do código
     * @return o código em hexadecimal separado por um espaço a cada dois bytes.
     */
    public static String getVerificationCodeHexString(byte[] code) {
        HexFormat hex = HexFormat.of().withUpperCase();
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < code.length; i += 2) {
            byte current = code[i];

            if ((i + 1) < code.length) {
                b.append(hex.formatHex(new byte[]{current, code[i + 1]}));
            } else {
                b.append(hex.formatHex(new byte[]{current}));
            }

            if ((i + 2) < code.length) {
                b.append(" ");
            }
        }

        return b.toString();
    }

    /**
     * Converte o hexadecimal de um código de verificação de volta
     * para os seus bytes originais.
     * 
     * @param code o código de verificação em hexadecimal
     * @return os bytes do código de verificação
     * @throws IllegalArgumentException se acontecer algum erro na leitura
     */
    public static byte[] getVerificationCodeBytes(String code) throws IllegalArgumentException {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            b.append(Character.toLowerCase(c));
        }
        
        return HexFormat.of().parseHex(b.toString());
    }

    //o SecureRandom para ser usado para gerar os códigos
    private final SecureRandom random;

    /**
     * Cria uma nova janela de verificação
     * @param parent a janela pai
     * @param random o SecureRandom para ser usado
     */
    public VerificationWindow(java.awt.Frame parent, SecureRandom random) {
        super(parent, true);
        this.random = (random == null ? new SecureRandom() : random);
        initComponents();
        setLocationRelativeTo(parent);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        verificationCodeField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        showPasswordCheckBox = new javax.swing.JCheckBox();
        verifyButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        generateVerificationCode = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        aboutButton = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Verificar Senha");
        setMinimumSize(new java.awt.Dimension(240, 220));

        jLabel1.setText("Código de Verificação:");

        verificationCodeField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        verificationCodeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                verificationCodeFieldKeyReleased(evt);
            }
        });

        jLabel2.setText("Senha:");

        passwordField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwordFieldKeyReleased(evt);
            }
        });

        showPasswordCheckBox.setText("Mostrar Senha");
        showPasswordCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPasswordCheckBoxActionPerformed(evt);
            }
        });

        verifyButton.setText("Verificar");
        verifyButton.setEnabled(false);
        verifyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verifyButtonActionPerformed(evt);
            }
        });

        jMenu2.setText("Editar");

        generateVerificationCode.setText("Gerar Código de Verificação");
        generateVerificationCode.setEnabled(false);
        generateVerificationCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateVerificationCodeActionPerformed(evt);
            }
        });
        jMenu2.add(generateVerificationCode);

        jMenuBar1.add(jMenu2);

        jMenu1.setText("Ajuda");

        aboutButton.setText("Sobre");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });
        jMenu1.add(aboutButton);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(verificationCodeField)
                    .addComponent(passwordField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(showPasswordCheckBox))
                        .addGap(0, 219, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(verifyButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(verificationCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel2)
                .addGap(6, 6, 6)
                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(showPasswordCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(verifyButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    //o callback da caixa de mostrar a senha
    private void showPasswordCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPasswordCheckBoxActionPerformed
        boolean selected = this.showPasswordCheckBox.isSelected();
        this.passwordField.setEchoChar(selected ? '\0' : '\u2022');
    }//GEN-LAST:event_showPasswordCheckBoxActionPerformed
    
    //o botão de verificar
    private void verifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verifyButtonActionPerformed
        //pegamos a senha do campo
        char[] password = this.passwordField.getPassword();
        try {
            byte[] code;
            try {
                //pegamos o código em bytes do campo contendo o código de verificação
                code = getVerificationCodeBytes(this.verificationCodeField.getText());
                //validação de input: tamanho do código
                if (code.length != VERIFICATION_CODE_SIZE) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(this,
                            "Necessário Apenas " + (VERIFICATION_CODE_SIZE * 2) + " Caracteres!",
                            "Código de Verificação Inválido!",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (IllegalArgumentException ex) {
                //validação de input: caracteres ilegais
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this,
                        "Apenas Bytes em Hexadecimal! Ex: 0123 4567 89AB CDEF",
                        "Código de Verificação Inválido!",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //geramos o nosso código para ver se bate com o código inserido pelo usuário
            byte[] otherCode = getVerificationCode(Arrays.copyOf(code, RANDOM_SALT_SIZE), password);
            
            //comparamos com o código inserido pelo usuário
            Toolkit.getDefaultToolkit().beep();
            if (MessageDigest.isEqual(code, otherCode)) {
                //se deu certo, uma mensagem de sucesso
                JOptionPane.showMessageDialog(this,
                        "Senha Correta!",
                        "Verificado com Sucesso!",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                //se não, uma mensagem de erro
                JOptionPane.showMessageDialog(this,
                        "Tente Novamente.",
                        "Senha Incorreta!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            //limpamos a senha
            //(é na verdade sem sentido por vários motivos
            //só está sendo feito por ser algo fácil
            //nessa área do programa)
            Arrays.fill(password, '\0');
        }
    }//GEN-LAST:event_verifyButtonActionPerformed

    //o código do botão sobre, mostra uma janela de texto
    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        try {
            TextDialog.ofResourceFile(this,
                    "Código de Verificação",
                    "verificationCode.txt"
            ).setVisible(true);
        } catch (IOException ex) {
            Toolkit.getDefaultToolkit().beep();
            ex.printStackTrace(System.err);
            TextDialog.ofThrowable(this, ex).setVisible(true);
        }
    }//GEN-LAST:event_aboutButtonActionPerformed

    //checa se ambos os campos do código e da senha foram preenchidos
    //para liberar o botão de verificação
    private void checkVerifyButton() {
        boolean hasCode = !this.verificationCodeField.getText().isBlank();
        
        boolean hasPassword;
        char[] password = this.passwordField.getPassword();
        try {
            hasPassword = (password.length != 0);
        } finally {
            Arrays.fill(password, '\0');
        }
        
        this.verifyButton.setEnabled(hasCode && hasPassword);
    }

    //a cada tecla solta no campo de senha
    //verificamos se o botão de verificar deve ser liberado
    //ou o botão de gerar o código de verificação
    private void passwordFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldKeyReleased
        checkVerifyButton();
        
        char[] password = this.passwordField.getPassword();
        try {
            this.generateVerificationCode.setEnabled(password.length != 0);
        } finally {
            Arrays.fill(password, '\0');
        }
    }//GEN-LAST:event_passwordFieldKeyReleased

    //o botão de gerar código de verificação com a senha inserida
    private void generateVerificationCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateVerificationCodeActionPerformed
        char[] password = this.passwordField.getPassword();
        try {
            if (password.length == 0) {
                return;
            }
            byte[] randomSalt = getRandomSalt(this.random);
            byte[] verification = getVerificationCode(randomSalt, password);
            this.verificationCodeField.setText(getVerificationCodeHexString(verification));
            Toolkit.getDefaultToolkit().beep();
            
            this.verifyButton.setEnabled(true);
        } finally {
            Arrays.fill(password, '\0');
        }
    }//GEN-LAST:event_generateVerificationCodeActionPerformed

    //a cada tecla solta no campo do código de verificação
    //verificamos se o botão de verificar deve ser liberado
    private void verificationCodeFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_verificationCodeFieldKeyReleased
        checkVerifyButton();
    }//GEN-LAST:event_verificationCodeFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutButton;
    private javax.swing.JMenuItem generateVerificationCode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JCheckBox showPasswordCheckBox;
    private javax.swing.JTextField verificationCodeField;
    private javax.swing.JButton verifyButton;
    // End of variables declaration//GEN-END:variables
}
