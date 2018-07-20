package com.askel.oiyu;

public class Friends {
    private String name;
    private String image;
    private String status;

    public Friends(){

    }
    public Friends(String name, String image, String status){
        this.name=name;
        this.image=image;
        this.status=status;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
