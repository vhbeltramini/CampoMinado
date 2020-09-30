
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;

public final class Imagens {
    private static final String RES_DIRECTORY = "/res/"; // define onde fica o repositorio com as imagens 
    public static final BufferedImage BANDEIRA = loadImageResource("bandeira.png");
    public static final BufferedImage MINA = loadImageResource("mina.png");
    public static final BufferedImage NORMAL_ROSTO = loadImageResource("default.png");
    public static final BufferedImage PERDEU_ROSTO = loadImageResource("derrota.png");
    public static final BufferedImage VITORIA_ROSTO = loadImageResource("vitoria.png");

    private static BufferedImage loadImageResource(String name){
        // Carrega a imagem que será usada
        try (InputStream imgStream = Imagens.class.getResourceAsStream(RES_DIRECTORY + name)){
            return ImageIO.read(imgStream);
        }
        catch (IOException e){
            throw new RuntimeException("Não foi possivel carregar a imagem: " + name, e);// Se tudo der errado é aqui que paramos rsrs
        }
    }
    private Imagens() {}
}

