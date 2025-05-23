import controller.MainController;

public class Main {
    public static void main(String[] args) {
        // Ejecutar el servidor en un hilo separado si es necesario
        if (args.length > 0 && args[0].equals("--server")) {
            new Thread(() -> server.AgricolaServer.main(new String[]{})).start();
        }
        
        // Iniciar la aplicación cliente
        new MainController();
    }
}