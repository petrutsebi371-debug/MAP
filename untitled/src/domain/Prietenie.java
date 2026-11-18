package domain;

import java.time.LocalDateTime;

public class Prietenie extends Entity<Long> {
    private Long idUser1;
    private Long idUser2;
    private LocalDateTime date;

    public Prietenie(Long idUser1, Long idUser2) {
        this.idUser1 = Math.min(idUser1, idUser2);
        this.idUser2 = Math.max(idUser1, idUser2);
        this.date = LocalDateTime.now();
    }

    public Long getIdUser1() {
        return idUser1;
    }

    public Long getIdUser2() {
        return idUser2;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Prietenie{" +
                "id=" + getId() +
                ", idUser1=" + idUser1 +
                ", idUser2=" + idUser2 +
                ", date=" + date +
                '}';
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}