
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


public class CampoMinado {
    
    Scanner s = new Scanner(System.in);
    private final int mines;
    private boolean uncoverMinesAtEnd = true;
    private int tilesLeft;
    private final byte[][] valuesArray;
    private StatusJogo statusJogo = StatusJogo.NAO_INICIADO;
    private final StatusdoQuadrado[][] stateArray;
    

    public CampoMinado(int width, int height, int mines) {
        int tilesLeft = (width * height) - mines;
        if (width < 1 || height < 1 || mines < 0) // esse if é usado para validar se as dimensoes do campo estão corretas para o funcionamento do jogo 
            throw new IllegalArgumentException("invalid CampoMinado dimensions");
        if (tilesLeft <= 0)
            throw new IllegalArgumentException("too many mines");
        this.mines = mines;
        this.tilesLeft = tilesLeft;
        StatusdoQuadrado[][] stateArray = new StatusdoQuadrado[width][height];
        for (int x = 0; x < width; x++)
            Arrays.fill(stateArray[x], StatusdoQuadrado.COBERTO);
        this.stateArray = stateArray;
        this.valuesArray = new byte[width][height];
    }
    // pega a altura e largura do quadriculado que será o campo
    public int getHeight() {
        return valuesArray[0].length;
    }
    public int getWidth() {
        return valuesArray.length;
    }
    public int getMines() { // pega a quantidade de minas que irá existir no campo 
        return mines;
    }

    public boolean isUncoveringMinesAtEnd() { // garante que os quadrados estejam incobertos onde existem minas
        return uncoverMinesAtEnd;
    }
    public void setUncoverMinesAtEnd(boolean uncoverMinesAtEnd) {
        this.uncoverMinesAtEnd = uncoverMinesAtEnd;
    }

    public StatusJogo getStatusJogo() { // busca o status do jogo 
        return statusJogo;
    }

    public boolean isFinished() { // boolean que define se o jogo esta ou não acabado 
        return statusJogo != StatusJogo.RODANDO && statusJogo != StatusJogo.NAO_INICIADO;
    }

    public int getTileValue(int x, int y) { 
        if (statusJogo == StatusJogo.NAO_INICIADO)
            throw new IllegalStateException("Você deve pelo menos abrir uma mina para essa parte funcionar ");

        return valuesArray[x][y];
    }

    public StatusdoQuadrado getStatusdoQuadrado(int x, int y) { // busca o status de determinado quadrado
        return stateArray[x][y];
    }
    public void setStatusdoQuadrado(int x, int y, StatusdoQuadrado newState) { // muda o status do quadrado de acordo com o comando que o jogador da 
        if (isFinished())
            throw new IllegalStateException("O jogo acabou");
        switch (newState) {
            case COBERTO:
            case MARCADO:  // usado para marcar o quadrado
            case DUVIDA:   // para deixar um ponto de interogação de duvida
                if (stateArray[x][y] == StatusdoQuadrado.DESCOBERTO) // e esse if é para caso um quadrado seja descoberto ele pode ter outros ao seu redor que também devem ser descobertos 
                    throw new UnsupportedOperationException("you cannot cover a tile once uncovered");

                stateArray[x][y] = newState;
                break;
            case DESCOBERTO:
                uncover(x, y); // da o comando para descobrir o quadrado marcado 
                break;

            default:
                throw new IllegalArgumentException("newState is not a valid tile state");
        }
    }
    public void uncover(int x, int y) { // descobre a mina selecionada 
        if (isFinished())
            throw new IllegalStateException("O jogo acabou ");
        if (statusJogo == StatusJogo.NAO_INICIADO) {
            initValues(x, y);
            statusJogo = StatusJogo.RODANDO;
        }
        uncoverNoChecks(x, y); //ira chegar os quadrados que foram incobertos
    }
    private final SurroundingTilesProcessor PROCESSOR_UNCOVER = new SurroundingTilesProcessor() {
        @Override
        public void process(int x, int y) {
            uncoverNoChecks(x, y);
        }
    };
    private void uncoverNoChecks(int x, int y) { // descobre a mina seleciona e as proximas possiveis sem que ele tenha que checar o status do jogo novamente  
        int width = getWidth();
        int height = getHeight();

        if (x < 0 || y < 0 || x >= width || y >= height) // ignora caso a casa ja estaja aberta ou se n existe uma onde ouve o click
            return;

        if (stateArray[x][y] == StatusdoQuadrado.DESCOBERTO)
            return;
        stateArray[x][y] = StatusdoQuadrado.DESCOBERTO; // descobre o quadrado determinado
        tilesLeft--;

        
        if (valuesArray[x][y] == 0) { // chega os quadrados diferentes, no caso onde tem os 0 ou onde tem uma mina
            processSurrounding(x, y, PROCESSOR_UNCOVER);
        } else if (valuesArray[x][y] < 0) {
            Random random = new Random();  
            int num1 = random.nextInt(50);// conta para se ganhar mais uma vida no jogo
            int num2 = random.nextInt(50);
            System.out.println("Resolva a operacao: "+ num1 + " + " + num2 + ": ");  // o pedido de soma ira aparecer no console 
            int resul = s.nextInt();
            if (resul != (num1 + num2)) {
            	statusJogo = StatusJogo.PERDEU;
            	uncoverAllMines();
            }else {
            	stateArray[x][y] = StatusdoQuadrado.MARCADO;
            }
        } else if (tilesLeft <= 0 && statusJogo == StatusJogo.RODANDO) {
            // Descobre todas as nao minas e muda o estado do jogo para venceu, isso caso voce não tenha clicado em nenhuma
            statusJogo = StatusJogo.VENCEU;
            uncoverAllMines();
        }
    }

    private void uncoverAllMines() {
        if (uncoverMinesAtEnd) {// se o jogo tiver acabado ele descobre todas as minas
            int width = getWidth();
            int height = getHeight();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (valuesArray[x][y] < 0) {
                        if (stateArray[x][y] != StatusdoQuadrado.MARCADO) // descobre o campo se ele n estiver marcado
                            stateArray[x][y] = StatusdoQuadrado.DESCOBERTO;
                    } else {
                        if (stateArray[x][y] == StatusdoQuadrado.MARCADO) // deixa o ponto de interrogação para marcar um ponto de "duvida" do jogador 
                            stateArray[x][y] = StatusdoQuadrado.DUVIDA;
                    }
                }
            }
        }
    }
    //usada para dar um "refresh" nas minas que estão por perto
    private final SurroundingTilesProcessor PROCESSOR_CHORD = new SurroundingTilesProcessor() {
        @Override
        public void process(int x, int y) {
            if (stateArray[x][y] != StatusdoQuadrado.MARCADO)
                uncoverNoChecks(x, y);
        }
    };
    
    public void chord(int x, int y) { // novametne um uma classe que é usada para dar um "refresh" nas minas que estão por perto
        if (isFinished())
            throw new IllegalStateException("O jogo acabou");

        if (stateArray[x][y] != StatusdoQuadrado.DESCOBERTO) // garante que os quadrados estejam descobertos 
            return;
        if (valuesArray[x][y] == countSurroundingFlags(x, y)) { // checa quais sao as marcações de bandeira por perto
            processSurrounding(x, y, PROCESSOR_CHORD); // descobre todas as minas por perto que possam ser desmarcadas e que nao estejam com a bandeira 
        }
    }

    private final SurroundingTilesProcessor PROCESSOR_INIT_VALUES = new SurroundingTilesProcessor() { //determina o valor que ira aparecer nos quadrados que nao tem mina mas que existem minas por perto 
        @Override
        public void process(int x, int y) {
            if (valuesArray[x][y] >= 0)
                valuesArray[x][y]++;
        }
    };

    private void initValues(int startX, int startY) { // recomeça a criação de um no campo quadriculado para o prox jogo
        int width = getWidth();
        int height = getHeight();
        // Colocar aleatoriamente as minas no campo
        Random rnd = new Random();

        for (int i = 0; i < mines; i++) {
            int x, y;
            do {  // um do while que vai ficar passando pelos pontos possiveis de se ter uma mina e vai coloca-la somente onde os parametros permitem ter uma mina 
                x = rnd.nextInt(width);
                y = rnd.nextInt(height);
            }
            while(valuesArray[x][y] < 0 || (x == startX && y == startY));
            valuesArray[x][y] = -1; // define onde vai determinada mina
            // Aumenta o numero das minas presentes 
            processSurrounding(x, y, PROCESSOR_INIT_VALUES);
        }
    }
    // o codigo abaixo ele vai contantar quais sao as minas que existem por perto de onde o jogador clicou 
    private int countSurroundingFlags(int x, int y) {
        int count = 0;
        int width = getWidth();
        int height = getHeight();

        if (y > 0) {
            if (x > 0)          if (stateArray[x - 1][y - 1] == StatusdoQuadrado.MARCADO) count++;
                                if (stateArray[x    ][y - 1] == StatusdoQuadrado.MARCADO) count++;
            if (x < width - 1)  if (stateArray[x + 1][y - 1] == StatusdoQuadrado.MARCADO) count++;
        }
        if (x > 0)              if (stateArray[x - 1][y    ] == StatusdoQuadrado.MARCADO) count++;
        if (x < width - 1)      if (stateArray[x + 1][y    ] == StatusdoQuadrado.MARCADO) count++;

        if (y < height - 1) {
            if (x > 0)          if (stateArray[x - 1][y + 1] == StatusdoQuadrado.MARCADO) count++;
                                if (stateArray[x    ][y + 1] == StatusdoQuadrado.MARCADO) count++;
            if (x < width - 1)  if (stateArray[x + 1][y + 1] == StatusdoQuadrado.MARCADO) count++;
        }
        return count;
    }
    private void processSurrounding(int x, int y, SurroundingTilesProcessor processor) {
        int width = getWidth();
        int height = getHeight();

        if (y > 0) {
            if (x > 0)          processor.process(x - 1, y - 1);
                                processor.process(x    , y - 1);
            if (x < width - 1)  processor.process(x + 1, y - 1);
        }

        if (x > 0)              processor.process(x - 1, y    );
        if (x < width - 1)      processor.process(x + 1, y    );

        if (y < height - 1) {
            if (x > 0)          processor.process(x - 1, y + 1);
                                processor.process(x    , y + 1);
            if (x < width - 1)  processor.process(x + 1, y + 1);
        }
    }
    @Override
    public String toString() { // essa função busca uma string que vai repsentar o estado do campo minado
        int width = getWidth();
        int height = getHeight();

        StringBuilder builder = new StringBuilder();

        builder.append('+');
        for (int x = 0; x < width; x++)
            builder.append('-');
        builder.append("+\n");
        for (int y = 0; y < height; y++) { // outro for para as definições do campo minado 
            builder.append('|');
            for (int x = 0; x < width; x++) {
                char c;
                switch (getStatusdoQuadrado(x, y)) { // switch que fica encaregado de defini o status do quadrado 
                    case COBERTO:
                        c = '#';
                        break;
                    case MARCADO:
                        c = 'f';
                        break;
                    case DUVIDA:
                        c = '?';
                        break;
                    default:
                        int tileValue = getTileValue(x, y);

                        if (tileValue < 0)
                            c = '!';
                        else if(tileValue == 0)
                            c = ' ';
                        else
                            c = (char) ('0' + tileValue);
                }
                builder.append(c);
            }
            builder.append("|\n");
        }
        builder.append('+');
        for (int x = 0; x < width; x++)
            builder.append('-');
        builder.append("+\n");

        return builder.toString();
    }
    private interface SurroundingTilesProcessor { // essa interface é responsavel pelos quadrados perto dos selecionados, isso so ocorre depois de varificar se os quadrados realmente existem e se estão seguindo os parametros
        public void process(int x, int y);
    }
}