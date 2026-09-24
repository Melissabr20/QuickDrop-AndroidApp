package com.example.QuickDrop.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    public Token getToken() {
        return token;
    }
    public void setToken(Token token) {
        this.token = token;
    }

    @SerializedName("token")
    private Token token;

    public User getSuperuser() {
        return superuser;
    }

    public void setSuperuser(User superuser) {
        this.superuser = superuser;
    }

    public User superuser;
    public static class Token{
        @SerializedName("refresh")
        private String refrech;

        public String getRefrech() {
            return refrech;
        }

        public void setRefrech(String refrech) {
            this.refrech = refrech;
        }

        public String getAccess() {
            return access;
        }

        public void setAccess(String access) {
            this.access = access;
        }

        @SerializedName("access")
        private String access;

    }


    @SerializedName("client")
    private Client client;

    @SerializedName("fournisseur")
    private Fournisseur fournisseur;

    public Livreur getLivreur() {
        return livreur;
    }


    public void setLivreur(Livreur livreur) {
        this.livreur = livreur;
    }

    @SerializedName("livreur")
    private Livreur livreur;


    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public static class Client {
        private int id;
        private User user;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    public static class Livreur {
        private int id;
        private boolean actif;
        private String city;
        private String wilaya;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getWilaya() {
            return wilaya;
        }

        public void setWilaya(String wilaya) {
            this.wilaya = wilaya;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public String getPlace_id() {
            return place_id;
        }

        public void setPlace_id(String place_id) {
            this.place_id = place_id;
        }

        private double lng;
        private double lat;
        private String place_id;

        private String adress;

        public String getAdress() {
            return adress;
        }

        public void setAdress(String adress) {
            this.adress = adress;
        }

        public boolean isActif() {
            return actif;
        }

        public void setActif(boolean actif) {
            this.actif = actif;
        }
        private User user;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public static class Fournisseur {
        private int id;
        private String city;
        private boolean actif;

        public boolean isActif() {
            return actif;
        }

        public void setActif(boolean actif) {
            this.actif = actif;
        }

        private String wilaya;
        private String adress;
        private Double lat;
        private Double lng;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getWilaya() {
            return wilaya;
        }

        public void setWilaya(String wilaya) {
            this.wilaya = wilaya;
        }

        public String getAdress() {
            return adress;
        }

        public void setAdress(String adress) {
            this.adress = adress;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }

        public String getPlace_id() {
            return place_id;
        }

        public void setPlace_id(String place_id) {
            this.place_id = place_id;
        }

        private String place_id;
        private User user;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }




    public static class User{
        @SerializedName("username")
        private String username;



        @SerializedName("last_name")
        private String last_name;

        @SerializedName("first_name")
        private String first_name;
        private Phone phone;

        public Phone getPhone() {
            return phone;
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        @SerializedName("email")
        private String email;

        @SerializedName("id")
        private int id;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
    public class Phone {
        private String phone_number;

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

    }
}
