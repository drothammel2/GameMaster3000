public class Spiel {
    private String name;

    public Spiel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void start() {
        System.out.println("Das Spiel " + name + " wurde gestartet!");
    }
}
