package net.es.oscars.dto.resv;


public enum Status {
    SUBMITTED("SUBMITTED"),
    HELD("HELD"),
    COMMITTED("COMMITTED"),
    ABORTED("ABORTED"),
    FAILED_TO_RETRIEVE("FAILED_TO_RETRIEVE");

    private String code;

    Status(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
