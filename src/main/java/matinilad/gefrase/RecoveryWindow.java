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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A janela de recuperação de senha
 *
 * @author Cien
 */
@SuppressWarnings("serial")
public class RecoveryWindow extends javax.swing.JDialog {

    //a classe do painel de uma palavra na janela de recuperação
    private class WordPanel extends JPanel {

        //o botão de limpar a palavra
        private final JButton clearButton = new JButton("X");
        //o campo da palavra
        private final JTextField field = new JTextField();
        //o botão de selecionar a palavra
        private final JButton addButton = new JButton("+");

        //o índice da palavra, 0 para a palavra de recuperação
        private final int index;
        //a palavra selecionada, -1 para nada
        private int selected = -1;

        //cria o painel de uma palavra para o índice dela
        //0 para a palavra de recuperação
        public WordPanel(int index) {
            this.index = index;
            initComponents();
        }

        //retorna o índice da palavra
        public int getIndex() {
            return index;
        }

        //retorna a palavra selecionada
        public int getSelected() {
            return selected;
        }

        //callback para quando uma palavra for selecionada
        public void onWordSelected() {

        }

        //define a palavra selecionada pelo índice no dicionário
        public void setSelected(int value) {
            //qualquer número negativo remove a palavra atual
            if (value < 0) {
                this.selected = -1;
                this.field.setText("");

                onWordSelected();
                return;
            }

            //pega o dicionário da janela de recuperação e mostra a palavra
            //ou coloca um valor em hexadecimal caso esteja fora dos limites
            //do dicionário
            Dictionary dict = RecoveryWindow.this.dictionary;
            if (value >= RecoveryWindow.this.dictionary.getNumberOfWords()) {
                this.field.setText("0x" + Integer.toHexString(value).toUpperCase());
            } else {
                this.field.setText(dict.getWord(value));
            }
            this.selected = value;

            onWordSelected();
        }

        //inicializa os componentes do painel
        private void initComponents() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            String title;
            if (this.index == 0) {
                title = "Palavra de Recuperação:";
            } else {
                title = "Palavra " + this.index + ":";
            }

            setBorder(BorderFactory
                    .createTitledBorder(BorderFactory
                            .createEmptyBorder(10, 5, 0, 5), title));

            this.field.setEditable(false);
            this.field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            this.field.setFont(new Font("Monospaced", 0, 12));

            this.clearButton.addActionListener((e) -> {
                setSelected(-1);
            });

            this.addButton.addActionListener((e) -> {
                new SelectWord(RecoveryWindow.this, RecoveryWindow.this.dictionary) {
                    @Override
                    public void onWordSelected(int index) {
                        setSelected(index);
                    }
                }.setVisible(true);
            });

            add(this.clearButton);
            add(this.field);
            add(this.addButton);
        }
    }

    //o dicionário
    private final Dictionary dictionary;
    //a quantidade de palavras da senha
    private final int numberOfWords;
    //os painéis de palavra da senha
    private final WordPanel[] panels;

    /**
     * Cria uma nova janela de recuperação de senha
     *
     * @param parent a janela pai
     * @param dictionary o dicionário (não null)
     * @param numberOfWords a quantidade de palavras da senha (maior que zero)
     */
    public RecoveryWindow(java.awt.Frame parent, Dictionary dictionary, int numberOfWords) {
        super(parent, true);
        this.dictionary = Objects.requireNonNull(dictionary, "dictionary is null");
        if (numberOfWords < 1) {
            throw new IndexOutOfBoundsException(numberOfWords + " < 1");
        }
        this.numberOfWords = numberOfWords;
        initComponents();

        this.panels = new WordPanel[this.numberOfWords + 1];
        for (int i = 1; i <= this.numberOfWords; i++) {
            addWordPanel(i);
        }
        addWordPanel(0);

        setLocationRelativeTo(parent);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        recoveryPanel = new javax.swing.JPanel();
        recoverButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        clearFieldsButton = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        aboutButton = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Recuperar Senha");
        setMinimumSize(new java.awt.Dimension(550, 300));

        jLabel1.setText("Insira as palavras que você se lembra aqui e aperte \"Recuperar\", apenas um campo pode estar vazio.");

        recoveryPanel.setLayout(new javax.swing.BoxLayout(recoveryPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(recoveryPanel);

        recoverButton.setText("Recuperar");
        recoverButton.setEnabled(false);
        recoverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recoverButtonActionPerformed(evt);
            }
        });

        jMenu2.setText("Editar");

        clearFieldsButton.setText("Limpar Campos");
        clearFieldsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFieldsButtonActionPerformed(evt);
            }
        });
        jMenu2.add(clearFieldsButton);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Ajuda");

        aboutButton.setText("Sobre");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });
        jMenu3.add(aboutButton);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(recoverButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 13, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recoverButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    //a ação do botão de recuperação
    private void recoverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recoverButtonActionPerformed
        int xor = 0;
        int w = -1;
        for (int i = 0; i < this.panels.length; i++) {
            int sel = this.panels[i].getSelected();
            if (sel == -1) {
                w = i;
                continue;
            }
            xor ^= sel;
        }
        
        if (w != -1) {
            this.panels[w].setSelected(xor);
            Toolkit.getDefaultToolkit().beep();
        }
    }//GEN-LAST:event_recoverButtonActionPerformed

    //botão de sobre
    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        try {
            TextDialog
                    .ofResourceFile(this, "Palavra de Recuperação", "recoveryWord.txt")
                    .setVisible(true);
        } catch (IOException ex) {
            Toolkit.getDefaultToolkit().beep();
            ex.printStackTrace(System.err);
            TextDialog.ofThrowable(this, ex);
        }
    }//GEN-LAST:event_aboutButtonActionPerformed

    //o botão de limpar campos
    private void clearFieldsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFieldsButtonActionPerformed
        for (WordPanel panel:this.panels) {
            panel.setSelected(-1);
        }
    }//GEN-LAST:event_clearFieldsButtonActionPerformed

    //adiciona um novo painel de palavra
    private void addWordPanel(int index) {
        WordPanel panel = new WordPanel(index) {
            @Override
            public void onWordSelected() {
                RecoveryWindow.this.onWordSelected();
            }
        };
        this.panels[index] = panel;
        this.recoveryPanel.add(panel);
    }
    
    //chamado pelo painel de palavra quando uma palavra é selecionada
    private void onWordSelected() {
        //vê quantos painéis estão vazios
        int emptyFound = 0;
        for (WordPanel panel : this.panels) {
            if (panel.getSelected() == -1) {
                emptyFound++;
            }
        }

        //se apenas um painel está vazio, então o botão de recuperação é ativado
        this.recoverButton.setEnabled(emptyFound == 1);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutButton;
    private javax.swing.JMenuItem clearFieldsButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton recoverButton;
    private javax.swing.JPanel recoveryPanel;
    // End of variables declaration//GEN-END:variables
}
