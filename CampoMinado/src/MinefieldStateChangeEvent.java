
import java.util.EventObject;

public class MinefieldStateChangeEvent extends EventObject{
    private static final long serialVersionUID = 1L;
    public MinefieldStateChangeEvent(Object source){ // diferentemente do outro state change este é chamado quando ocorre alguma alteração no campo em si e não no estado do jogo inteiro
        super(source);
    }
}
