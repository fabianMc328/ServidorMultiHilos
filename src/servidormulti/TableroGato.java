package servidormulti;

public class TableroGato {
    private final char[][] tablero;
    private char turno; // 'X' o 'O'
    private boolean terminado;

    public TableroGato(char turnoInicial) {
        tablero = new char[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j=0; j<3; j++) {
                tablero[i][j] = ' ';
            }
        }
        this.turno = turnoInicial;
        this.terminado = false;
    }

    public char getTurno() {
        return turno;
    }

    public void cambiarTurno() {
        turno = (turno == 'X') ? 'O' : 'X';
    }

    public boolean isTerminado() {
        return terminado;
    }

    public void setTerminado(boolean terminado) {
        this.terminado = terminado;
    }

    public boolean hacerMovimiento(int fila, int columna) {
        if (fila < 0 || fila > 2 || columna < 0 || columna > 2) return false;
        if (tablero[fila][columna] != ' ') return false;

        tablero[fila][columna] = turno;
        return true;
    }

    public boolean verificarGanador() {
        // filas
        for (int i=0; i<3; i++) {
            if (tablero[i][0] != ' ' && tablero[i][0] == tablero[i][1] && tablero[i][1] == tablero[i][2])
                return true;
        }
        // columnas
        for (int j=0; j<3; j++) {
            if (tablero[0][j] != ' ' && tablero[0][j] == tablero[1][j] && tablero[1][j] == tablero[2][j])
                return true;
        }
        // diagonales
        if (tablero[0][0] != ' ' && tablero[0][0] == tablero[1][1] && tablero[1][1] == tablero[2][2])
            return true;
        if (tablero[0][2] != ' ' && tablero[0][2] == tablero[1][1] && tablero[1][1] == tablero[2][0])
            return true;

        return false;
    }

    public boolean tableroCompleto() {
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                if (tablero[i][j] == ' ') return false;
            }
        }
        return true;
    }

    public String mostrarTablero() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<3; i++) {
            sb.append(" ");
            for (int j=0; j<3; j++) {
                sb.append(tablero[i][j]);
                if (j<2) sb.append(" | ");
            }
            sb.append("\n");
            if (i<2) sb.append("---+---+---\n");
        }
        return sb.toString();
    }
}
