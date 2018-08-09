package androidtv.livetv.stb.entity;

import java.util.List;

public class EpgEntity {
    private List<Epgs> epgsList;
    private String error_message;

    public List<Epgs> getEpgsList() {
        return epgsList;
    }

    public void setEpgsList(List<Epgs> epgsList) {
        this.epgsList = epgsList;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }
}
