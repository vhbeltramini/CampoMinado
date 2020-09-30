import javax.swing.JComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class PainelCampoMinado extends JComponent{
    private static final long serialVersionUID = 1L;
    private static final int BEVEL_WIDTH = 2;
    private static final int TILE_SIZE = 32; //começamos definindo o tamanho dos quadrados
    private static final int FONT_VOFFSET = 24;
    private static final Font FONT = new Font(Font.MONOSPACED, Font.BOLD, 24); //define a fonte e suas principais propriedades
    //cores usadas no campominado
    private static final Color COLOUR_QUESTION = Color.WHITE; 
    private static final Color COLOUR_BACKGROUND = new Color(0xC0, 0xC0, 0xC0);
    private static final Color COLOUR_LIGHT = new Color(0xE0, 0xE0, 0xE0);
    private static final Color COLOUR_DARK = new Color(0x80, 0x80, 0x80);
    private static final Color[] COLOUR_NUMBERS = new Color[] {
        null,                           
        new Color(0x00, 0x00, 0xFF),    // 1- Azul
        new Color(0x00, 0x7F, 0x00),    // 2- Verde
        new Color(0xFF, 0x00, 0x00),    // 3- Vermelho
        new Color(0x2F, 0x2F, 0x9F),    // 4- Azul escuro
        new Color(0x7F, 0x00, 0x00),    // 5- Marron
        new Color(0x9F, 0x9F, 0x2F),    // 6- azulClaro
        new Color(0x00, 0x00, 0x00),    // 7- Preto
        new Color(0x7F, 0x7F, 0x7F),    // 8- Cinza
    };

    
    private CampoMinado campoMinado; //"Chama" o a clase do campominado que esta com as definições do jogo
    private Point selectedTile;

    
    private final ArrayList<MinefieldStateChangeListener> listeners = new ArrayList<MinefieldStateChangeListener>(); // usado para os estados pussiveis do jogo

    // aqui se inicia realmente o painel do campominado, para isso ele chama a classe principal do campo minado que é logo o primeiro codigo abaixo
    public PainelCampoMinado(final CampoMinado campoMinado) {
        this.addMouseListener(new MouseEventListener());
        this.setBackground(COLOUR_BACKGROUND);
        this.setOpaque(true);
        this.setFont(FONT);
        this.setCampoMinado(campoMinado);
    }

    public void addStateChangeListener(final MinefieldStateChangeListener listener) { // adiciona a classe que irá verificar o estados do jogo
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    public void removeStateChangeListener(final MinefieldStateChangeListener listener) {
        listeners.remove(listener);
    }

   
    private void fireStateChangeEvent() { // usado para inicializar a classe que manda nas mudanças de status do jogo 
        final MinefieldStateChangeEvent event = new MinefieldStateChangeEvent(this);
        for (final MinefieldStateChangeListener listener : listeners)
            listener.stateChanged(event);
    }
    
    public CampoMinado getCampoMinado() { // pega as definições do campo minado ja existentes e as repassa 
        return campoMinado;
    }

    public void setCampoMinado(final CampoMinado newCampoMinado) { // e agora as muda de acordo com o valores pegos na dificuldade selecionada
        if (newCampoMinado == null)
            throw new IllegalArgumentException("Valores invalidos");

        this.campoMinado = newCampoMinado;
        
        this.setSize(getPreferredSize()); // Atualiza tudo de acordo com a mundaça de tamanho
        this.repaint();
        this.selectedTile = null;

        this.fireStateChangeEvent(); // Muda o estado do jogo
    }

    private static void drawCharacter(final Graphics g, final int x, final int y, final char c) {
        final int drawX = x + (TILE_SIZE - g.getFontMetrics().charWidth(c)) / 2;
        final int drawY = y + FONT_VOFFSET;
        g.drawChars(new char[] { c }, 0, 1, drawX, drawY);
    }

    private static void drawImage(final Graphics g, final int tileX, final int tileY, final BufferedImage img) {
        final int xOff = tileX + (TILE_SIZE - img.getWidth()) / 2;
        final int yOff = tileY + (TILE_SIZE - img.getHeight()) / 2;

        g.drawImage(img, xOff, yOff, null);
    }

    @Override
    public void paintComponent(final Graphics gOld) {
        final Graphics2D g = (Graphics2D) gOld;

        // Pega a posição dos quadrados
        final int selectedX = (selectedTile == null ? -1 : selectedTile.x);
        final int selectedY = (selectedTile == null ? -1 : selectedTile.y);


        if (isOpaque()) { //Desenha o fundo
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // for que desenha/define todo o campo minado, 
        for (int x = 0; x < campoMinado.getWidth(); x++) {
            for (int y = 0; y < campoMinado.getHeight(); y++) {
                final int graphicsX1 = x * TILE_SIZE;
                final int graphicsY1 = y * TILE_SIZE;
                // cria o fundo da parte onde fica o campo minado
                g.setColor(COLOUR_DARK);
                g.drawLine(graphicsX1, graphicsY1, graphicsX1 + TILE_SIZE, graphicsY1);
                g.drawLine(graphicsX1, graphicsY1, graphicsX1, graphicsY1 + TILE_SIZE);

                if (campoMinado.getStatusdoQuadrado(x, y) == StatusdoQuadrado.DESCOBERTO) {
                    final int tileValue = campoMinado.getTileValue(x, y);

                    if (tileValue < 0) {
                        drawImage(g, graphicsX1, graphicsY1, Imagens.MINA);
                    } else if (tileValue > 0) {
                        g.setColor(COLOUR_NUMBERS[tileValue]);
                        drawCharacter(g, graphicsX1, graphicsY1, (char) ('0' + tileValue));
                    }
                } else {
                    // Cria uma sombra atras das casas não abertas
                    if (x != selectedX || y != selectedY) {
                        final int bevelX2 = graphicsX1 + TILE_SIZE - BEVEL_WIDTH;
                        final int bevelY2 = graphicsY1 + TILE_SIZE - BEVEL_WIDTH;

                        g.setColor(COLOUR_LIGHT);
                        g.fillRect(graphicsX1, graphicsY1, TILE_SIZE, BEVEL_WIDTH);
                        g.fillRect(graphicsX1, graphicsY1, BEVEL_WIDTH, TILE_SIZE);
                        g.setColor(COLOUR_DARK);
                        g.fillRect(graphicsX1, bevelY2, TILE_SIZE, BEVEL_WIDTH);
                        g.fillRect(bevelX2, graphicsY1, BEVEL_WIDTH, TILE_SIZE);
                    }

                    // coloca a bandeira ou o ponto de interogação dependendo da ação
                    if (campoMinado.getStatusdoQuadrado(x, y) == StatusdoQuadrado.MARCADO) {
                        drawImage(g, graphicsX1, graphicsY1, Imagens.BANDEIRA);
                    } else if (campoMinado.getStatusdoQuadrado(x, y) == StatusdoQuadrado.DUVIDA) {
                        g.setColor(COLOUR_QUESTION);
                        drawCharacter(g, graphicsX1, graphicsY1, '?');
                    }
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(TILE_SIZE * campoMinado.getWidth(), TILE_SIZE * campoMinado.getHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    // essa parte é um import de uma biblioteca do java que é usada para indentificar o mouse e seus clicks/movimentação
    private class MouseEventListener extends MouseAdapter {
        private Point getTileFromEvent(final MouseEvent e) { // variavel que calcula onde ocorreu o click dentro da janela do jogo
            return new Point(e.getX() / TILE_SIZE, e.getY() / TILE_SIZE);
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            if (selectedTile != null) {
                selectedTile = null;
                repaint();
            }
        }

            //abaixo usamos uma extensão do java para reconhecer o mouse e suas ações
        @Override
        public void mousePressed(final MouseEvent e) {
            if (campoMinado.isFinished())
                return;
            final Point tile = getTileFromEvent(e);
            // Basicamente uma booleana que irá falar se qual botao do mouse foi usado, o direito ou esquerdo (isso se torna necessario para poder indentificar as marcações do jogador)
            if (SwingUtilities.isLeftMouseButton(e)) {
                // se o quadrado estiver com a bandeira isso ira impedir de que o jogar consiga "abri-la"
                if (campoMinado.getStatusdoQuadrado(tile.x, tile.y) == StatusdoQuadrado.MARCADO)
                    return;
                selectedTile = tile;
            } else if (SwingUtilities.isRightMouseButton(e)) {
                StatusdoQuadrado newState;

                // Switch criado a fim de definir o como esta o quadrado, a partir disso a imagem muda de o jogo também 
                switch (campoMinado.getStatusdoQuadrado(tile.x, tile.y)) {
                    case COBERTO:
                        newState = StatusdoQuadrado.MARCADO;
                        break;
                    case MARCADO:
                        newState = StatusdoQuadrado.DUVIDA;
                        break;
                    default:
                        newState = StatusdoQuadrado.COBERTO;
                        break;
                    case DESCOBERTO:
                        newState = StatusdoQuadrado.DESCOBERTO;
                        break;
                }
                campoMinado.setStatusdoQuadrado(tile.x, tile.y, newState); // aqui ele muda o status do quadrado de acordo com o que o switch selecionar 
            }
            repaint();
        }
        @Override
        public void mouseReleased(final MouseEvent e) {
            // Ignora o click do mouse se o jogo tiver acabado
            if (campoMinado.isFinished())
                return;

            if (selectedTile != null) { 
                if (selectedTile.equals(getTileFromEvent(e))) {
                    final StatusJogo state = campoMinado.getStatusJogo(); // feito para garantir que onde o jogador clicou foi onde o quadrado que ele quis selecionar 

                    if (e.getClickCount() == 2)
                        campoMinado.chord(selectedTile.x, selectedTile.y);
                    else if (e.getClickCount() == 1)
                        campoMinado.uncover(selectedTile.x, selectedTile.y);

                    // muda o status do jogo se necessario
                    if (campoMinado.getStatusJogo() != state)
                        fireStateChangeEvent();
                }
                // deixa o quadrado em brando
                selectedTile = null;
                repaint();
            }
        }
    }
    
}
