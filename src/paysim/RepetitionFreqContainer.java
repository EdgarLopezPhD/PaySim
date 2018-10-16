package paysim;

public class RepetitionFreqContainer implements Comparable<RepetitionFreqContainer> {
    private RepetitionContainer cont;
    private double typeNr;
    private int freq;

    public RepetitionFreqContainer() {
        cont = new RepetitionContainer();
        typeNr = 0;
        freq = 0;
    }

    public String toString() {
        return cont.getType() + "," + ((int) cont.getLow()) + "," + ((int) cont.getHigh()) + "," + freq;
    }


    public void setCont(RepetitionContainer cont) {
        this.cont = cont;
        switch (cont.getType()) {
            case "CASH_IN":
                this.typeNr = 1000000000;
                break;

            case "CASH_OUT":
                this.typeNr = 9000000;
                break;

            case "DEBIT":
                this.typeNr = 300000;
                break;

            case "PAYMENT":
                this.typeNr = 100;
                break;

            case "TRANSFER":
                this.typeNr = -1;
                break;
        }
    }

    public void incrementFrequency() {
        freq++;
    }

    @Override
    public int compareTo(RepetitionFreqContainer r) {
        if (this.typeNr > r.typeNr) {
            return -1;

        } else if (this.typeNr < r.typeNr) {
            return 1;
        }
        return 0;
    }

    public RepetitionContainer getCont() {
        return cont;
    }

    public int getFreq() {
        return freq;
    }

    public boolean match(String type, int high, int low){
        return cont.getType().equals(type) && cont.getHigh() == high && cont.getLow() == low;
    }
}