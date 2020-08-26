package com.pakhi.clicksdigital.Model;

import java.io.Serializable;

public class Certificates implements Serializable {
    String name, institute, fileUri;

    public Certificates() {
    }

    public Certificates(String name, String institute, String fileUri) {
        this.name=name;
        this.institute=institute;
        this.fileUri=fileUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute=institute;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri=fileUri;
    }
}
