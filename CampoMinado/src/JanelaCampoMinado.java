import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class JanelaCampoMinado extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
        // interface
    private JPanel mainPanel =  new JPanel(new BorderLayout(10, 10));
    private JComboBox<String> difficultyBox = new JComboBox<String>(DIFFICULTIES);
    private PainelCampoMinado campPainel;
    private static final String INCREMENT = "incr";
    private static final String RESET = "reset";
    private static final String[] DIFFICULTIES = { "Facil", "Medio", "Dificil" };
    //temporizador
    private Timer scoreTimer = new Timer(1000, this);
    private JLabel topTimer;
    private int time = 0;
    private JButton topResetBtn;
    
    public JanelaCampoMinado() {
        // Coisas basicas usadas para criar uma intarface com java
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); //ira parar de rodar o codigo ao fechar
        this.setLayout(new BorderLayout(0,0)); //define o layout da janela 
        this.getContentPane().setBackground(Color.WHITE);
        this.setSize(new Dimension(400, 500)); // define a dimensão inicial da tela
        this.setMinimumSize(new Dimension(400, 500)); //define 
        this.setTitle("Campo Minado");

        // Strutura da interface, layout e afins 
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        topPanel.setBackground(Color.GRAY);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        centerPanel.setBackground(Color.GRAY);

        JPanel centerMidPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        campPainel = new PainelCampoMinado (new CampoMinado(16, 16, 40));
        campPainel.addStateChangeListener(new MinefieldStateChangeListener() {
            @Override
            public void stateChanged(MinefieldStateChangeEvent event) {
                CampoMinado campominado = campPainel.getCampoMinado();
                if (campominado.isFinished()) {
                    scoreTimer.stop(); // para o timer e deixa a pontuação

                    if (campominado.getStatusJogo() == StatusJogo.VENCEU)
                        topResetBtn.setIcon(new ImageIcon(Imagens.VITORIA_ROSTO));
                    else
                        topResetBtn.setIcon(new ImageIcon(Imagens.PERDEU_ROSTO));
                    } else {
                    // volta para a imagem padrao e começa o jogo novamente, junto ao timer
                    topResetBtn.setIcon(new ImageIcon(Imagens.NORMAL_ROSTO));

                    if (campominado.getStatusJogo() == StatusJogo.RODANDO)
                        scoreTimer.start();
                }

                topResetBtn.repaint();
            }
        });

        centerMidPanel.add(campPainel);

        // usado para selecionar a dificuldade
        difficultyBox.setSelectedIndex(1);
       
        topResetBtn = new JButton();
        topResetBtn.setPreferredSize(new Dimension(50, 50));
        topResetBtn.setActionCommand(RESET);
        topResetBtn.addActionListener(this);
        centerPanel.add(topResetBtn);

        topResetBtn.setIcon(new ImageIcon(Imagens.NORMAL_ROSTO));
        topTimer = new JLabel(String.valueOf(time) + " Segundos");
        scoreTimer.setActionCommand(INCREMENT);

        topPanel.add(difficultyBox); // adiciona mais partes ao layout do jogo 
        topPanel.add(centerPanel);
        topPanel.add(topTimer);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerMidPanel, BorderLayout.CENTER);

        this.getContentPane().add(mainPanel, BorderLayout.NORTH);
        this.pack();
    }
    @Override
    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals(INCREMENT)) {
            time++;
        } else if(event.getActionCommand().equals(RESET)) {
            scoreTimer.stop();
            time = 0;
            // Reseta o jogo e muda os campo de acordo com as dificuldade
            if (difficultyBox.getSelectedIndex() == 0){
                campPainel.setCampoMinado((new CampoMinado(9, 9, 10)));
            } else if (difficultyBox.getSelectedIndex() == 2) {
                campPainel.setCampoMinado((new CampoMinado(30, 16, 99)));
            } else if (difficultyBox.getSelectedIndex() == 1) {
                campPainel.setCampoMinado((new CampoMinado(16, 16, 40)));
            }
                pack();
        }
        topTimer.setText((time) + " Segundos   ");
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JanelaCampoMinado().setVisible(true);
            }
        });
    }
}

