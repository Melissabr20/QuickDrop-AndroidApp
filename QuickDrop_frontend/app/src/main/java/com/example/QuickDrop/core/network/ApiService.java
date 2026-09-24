package com.example.QuickDrop.core.network;

import com.example.QuickDrop.core.model.CartArticleItem;
import com.example.QuickDrop.core.model.Distance;
import com.example.QuickDrop.core.model.SelectedFournisseur;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.SelectedLivreur;
import com.example.QuickDrop.core.model.DeliveryPrice;
import com.example.QuickDrop.core.model.RoutingSolution;
import com.example.QuickDrop.core.model.ProfileUpdateRequest;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetFournisseurResponse;
import com.example.QuickDrop.core.network.dto.GetListeProduitResponse;
import com.example.QuickDrop.core.network.dto.GetOrderResponse;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.ImageResponse;
import com.example.QuickDrop.core.network.dto.LoginRequest;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.PasswordRequest;
import com.example.QuickDrop.core.network.dto.PasswordResponse;
import com.example.QuickDrop.core.network.dto.PostLignePanierRequest;
import com.example.QuickDrop.core.network.dto.PostPanierRequest;
import com.example.QuickDrop.core.network.dto.PostPanierResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonRequest;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import com.example.QuickDrop.core.network.dto.PutPanier;
import com.example.QuickDrop.core.network.dto.PutLivraisonStatus;
import com.example.QuickDrop.core.network.dto.RegisterRequest;
import com.example.QuickDrop.core.network.dto.RegisterResponse;
import okhttp3.ResponseBody;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;

import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface ApiService {
String BASE_URL="http://192.168.100.7:8000/";

    @POST("login/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("register/")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("update_password/")
    Call<PasswordResponse> PasswordUser(@Header("Authorization") String token, @Body PasswordRequest passwordRequest);
    @PUT("update/")
    Call<LoginResponse.User> updateUser(@Header("Authorization") String token, @Body ProfileUpdateRequest user);


    @GET("articles/")
    Call<List<GetArticleResponse>> GetArticle();

    @GET("articles/{id}/")
    Call<GetArticleResponse> getArticleDetails(@Path("id") int articleId);
    @GET("paniers/{id}/")
    Call<GetPanierResponse> getPanierDetails(@Path("id") int panier);

    @PUT("paniers/{id_order}/")
    Call<GetPanierResponse> PutPanierDetails(@Path("id_order")int id_order,@Body PutPanier postPanierRequest);


    @POST("paniers/")
    Call<PostPanierResponse> CreePanier (@Header("Authorization") String token,@Body PostPanierRequest postPanierRequest);

    @GET("paniers/")
    Call<GetPanierResponse> GetPanier (@Header("Authorization") String token);


    @POST("lignes_panier/")
    Call<DeliveryOrderLine> CreeLignePanier (@Body PostLignePanierRequest postLignePanierRequest);

    @PUT("lignes_panier/{id}/")
    Call<DeliveryOrderLine> PutLignePanier (@Path("id") int id,@Body PostLignePanierRequest postLignePanierRequest);

    @DELETE("lignes_panier/{id}/")
    Call<DeliveryOrderLine> deleteLignePanier (@Path("id") int id);

    @PUT("adresseLivraison/{id}/")
    Call<PutAdresseLivraisonResponse> CreeModifierAdresseLivraison (@Path("id") int id_client,@Body PutAdresseLivraisonRequest putAdresseLivraisonRequest);

    @GET("adresseLivraison/{id}/")
    Call<PutAdresseLivraisonResponse> GetAdresseLivraison (@Path("id") int id_client);

    @GET("get_image/{id}/")
    Call<ImageResponse> getImage(@Path("id") int imageId);



    @GET("Fournisseur/{id}/")
    Call<LoginResponse.Fournisseur> GetFournisseur (@Path("id") int id_fournisseur);

    @GET("Fournisseur/")
    Call<List<LoginResponse.Fournisseur>> getFournisseurList ();


    @GET("Livreur/")
    Call<List<LoginResponse.Livreur>> getLivreurList ();



    @DELETE("Livreur/{id}/")
    Call<LoginResponse.Livreur> deleteLivreurDetails(@Path("id") int id);

    @DELETE("Fournisseur/{id}/")
    Call<LoginResponse.Fournisseur> deleteSupplierDetails (@Path("id") int id);

    @GET("Livreur/{id}/")
    Call<LoginResponse.Livreur> GetLivreursDetails(@Path("id") int id);

    @PUT("Livreur/{id}/")
    Call<LoginResponse.Livreur> UpdateLivreurDetails(@Path("id") int id,@Body LoginResponse.Livreur putlivreurActif);

    @PUT("Fournisseur/{id}/")
    Call<LoginResponse.Fournisseur> UpdateFournisseurDetails(@Path("id") int id,@Body LoginResponse.Fournisseur putlivreurActif);


    @GET("client/")
    Call<List<LoginResponse.Client>> getClientvList ();

    @GET("client/{id}/")
    Call<LoginResponse.Client> GetClient (@Path("id") int id_client);

    @GET("Produit-Article/{idarticle}/{quantite}/")
    Call<List<GetFournisseurResponse>>  ListproduitArticleDisponible (@Path("idarticle") int id_article,@Path("quantite") int quantitearticle);


    @GET("distanceClientFournisseurMin/{fournisseurs_ids}/")
    Call<SelectedFournisseur> InfofournisseurTarification(@Header("Authorization") String token, @Path("fournisseurs_ids") String fournisseur_ids);

    @GET("distance/{idfournisseur}/{excludeIds}/{id_panier}")
    Call<SelectedLivreur> SelectedLivreur(
            @Path("idfournisseur") int idfournisseur,
            @Path("excludeIds") String excludeIds,
            @Path("id_panier") int idpanier
    );
    @POST("vider/")
    Call<CartArticleItem> vider();

    @GET("LivraisonBylivreur/")
    Call<List<GetPanierResponse>> GetOrderDetails (@Header("Authorization")String token);

    @GET("LivraisonByfournisseur/")
    Call<List<GetPanierResponse>> GetOrderDetailsbyfour (@Header("Authorization")String token);

    @GET("listeProduit/")
    Call<List<GetListeProduitResponse>> GetListeProduitbyfour (@Header("Authorization")String token);



    @PUT("paniers/{id_order}/")
    Call<GetOrderResponse> UpdateOrderDetails(@Path("id_order") int id_order,@Body PutLivraisonStatus putlivraisonStatus);

    @GET("ligne/{id_order}/")
    Call<List<GetPanierResponse>> ligne (@Header("Authorization") String token,@Path("id_order") int id_order);

    @POST("DeliveryPrice/{id}/")
    Call<DeliveryPrice> DeliveryPrice (@Path("id") Integer fournisseur_ids);

    @GET("api_admin/")
    Call<List<LoginResponse.User>> getusers ();

    @GET("livraisonvalide/")
    Call<List<GetPanierResponse>> getfinal(@Header("Authorization")String token);

    @PUT("LivreurAddress/{id}/")
    Call<PutAdresseLivraisonRequest> PutAddressLivreur (@Path("id") int id_livreur,@Body PutAdresseLivraisonRequest putAdresseLivraisonRequest);

    @GET("LivreurAddress/{id}/")
    Call<LoginResponse.Livreur> GetLivreur (@Path("id") int id_livreur);



    @GET("distanceClientSupplier/{client_id}/{supplier_id}/")
    Call<Distance> GetDistanceDetails(@Path("client_id") int client_id,@Path("supplier_id")int supplier_id);

    @GET("distanceClientDelivrer/{supplier_id}/{delivrer_id}/")
    Call<Distance> GetDistanceDetails2(@Path("supplier_id") int client_id,@Path("delivrer_id")int supplier_id);

    @GET("Get_Road/{delivrer_id}/")
    Call<RoutingSolution> GetDelivrerRoad (@Path("delivrer_id") int delivrer_id);



    @GET("Livraison/")
    Call<List<GetPanierResponse>> getOrder();





    @Multipart
    @POST("articles/")
    Call<ResponseBody> uploadArticle(
            @Part("name") RequestBody name,
            @Part("disignation") RequestBody disignation,
            @Part("price") RequestBody price,
            @Part MultipartBody.Part image
    );





    @Multipart
    @PUT("articles/{article_id}/")
    Call<GetArticleResponse> putarticle (@Part MultipartBody.Part image, @Body GetArticleResponse article , @Path("article_id") int article_id);


    @DELETE("articles/{article_id}/")
    Call<GetArticleResponse> deletearticle (@Path("article_id") int article_id);

    @POST("ListeProduits/")
    Call<GetListeProduitResponse> addlistproduits(@Body GetListeProduitResponse produit);

    @DELETE("ListeProduits/{liste_id}/")
    Call<GetListeProduitResponse> deleteLigneProduit(@Path("liste_id") int liste_id);

    @PATCH("ListeProduits/{id}/")
    Call<GetListeProduitResponse> patchlistproduits(@Path("id") int id, @Body GetListeProduitResponse produit);


    @GET("available-articles/")
    Call<List<GetArticleResponse>> getarticledisponible();

    @GET("not-shared-articles/{idfournisseur}/")
    Call<List<GetArticleResponse>> nonarticlefournisseur(@Path ("idfournisseur") int idfournisseur );

    @GET("InitialAdresseLivraison/")
    Call<PutAdresseLivraisonResponse> adresseinitial(@Header("Authorization")String token);

    @PATCH("InitialAdresseLivraison/")
    Call<PutAdresseLivraisonResponse> putadresseinitial(@Header("Authorization")String token,@Body PutAdresseLivraisonRequest adresse);

    @GET("adressebyLivraison/{livraison_id}/")
    Call<PutAdresseLivraisonResponse> getadressebylivraison(@Path ("livraison_id") int livraison_id );




}






