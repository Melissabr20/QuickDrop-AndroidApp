from django.db import models
import datetime
from django.contrib.auth.models import User
from django.conf import settings
from datetime import datetime
from django.contrib.auth.base_user import BaseUserManager
import os
from .routing_client import geocode_address


class TodoUser(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    phone_number = models.CharField(max_length=15, default='')




class AppUserManager(BaseUserManager):
	def create_user(self, username ,email, password=None):
		if not email:
			raise ValueError('An email is required.')
		if not password:
			raise ValueError('A password is required.')
		email = self.normalize_email(email)
		user = self.model(email=email, username=username, **extra_fields)
		user.set_password(password)
		user.save()
		return user



# Create your models here.
class Livreur(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE,blank=True, null=True)
    is_livreur = models.BooleanField(default=True)
    actif=models.BooleanField(default=False)
    adminetat= models.CharField(max_length=20, choices=[('normal', 'normal'), ('Blocked', 'Blocked')],blank=True, null=True)
    city = models.CharField(max_length=200,blank=True, null=True)
    wilaya = models.CharField(max_length=200,blank=True, null=True)
    adress = models.CharField(max_length=200,blank=True, null=True)
    lat = models.CharField(max_length=200,blank=True, null=True)
    lng = models.CharField(max_length=200,blank=True, null=True)
    place_id = models.CharField(max_length=200,blank=True, null=True)
    capacity =models.IntegerField(default=0,null=False,blank=False)

    def add_livreur(user,  city=None,adress=None, lat=None, lng=None, place_id=None, wilaya=None):
        fournisseur = Fournisseur.objects.create(
            user=user,
            city=city,
            adress=adress,
            lat=lat,
            lng=lng,
            place_id=place_id,
            wilaya=wilaya
        )
    def delete_livreur(self):
        self.delete()
    
    def modify_livreur(self, name=None, family_name=None, city=None, wilaya=None, address=None):
        if name:
            self.name = name
        if family_name:
            self.family_name = family_name
        if city:
            self.city = city
        if wilaya:
            self.wilaya = wilaya
        if address:
            self.adress = address
        self.save()

    def get_full_address(self):
        return f"{self.adress}, {self.city}, {self.wilaya}"
    
    def calculate_address(self):
        # Free, open-source geocoding (OpenStreetMap Nominatim) -- no API key required.
        address_string = self.get_full_address()
        lat, lng = geocode_address(address_string)
        if lat is not None and lng is not None:
            self.lat = lat
            self.lng = lng


    def save(self, *args, **kwargs):    
        # Calculate the total price using get_total_price method
        self.calculate_address()
        super().save(*args, **kwargs)

    def __str__(self):
        return self.user.username
    

class Client(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE,blank=True, null=True)
    is_client = models.BooleanField(default=True)
    def __str__(self):
        return self.user.username

class Adresse_livraison (models.Model):
    id_client = models.ForeignKey(Client, on_delete=models.CASCADE)
    city = models.CharField(max_length=200,blank=True, null=True)
    country = models.CharField(max_length=200,blank=True, null=True)
    wilaya = models.CharField(max_length=200,blank=True, null=True)
    address = models.CharField(max_length=200,blank=True, null=True)
    lat = models.CharField(max_length=200,blank=True, null=True)
    lng = models.CharField(max_length=200,blank=True, null=True)
    place_id = models.CharField(max_length=200,blank=True, null=True)
    
    def get_full_address(self):
        return f"{self.address}, {self.city}, {self.country}"

    def update_address(self, new_address=None,new_city=None,new_country=None,new_wilaya=None):
        if  new_address:
            self.address = new_address
        if new_city:
            self.city=new_city
        if new_country:
            self.country=new_country
        if new_wilaya:
            self.wilaya=new_wilaya
        self.save()

    def calculate_address(self):
        # Free, open-source geocoding (OpenStreetMap Nominatim) -- no API key required.
        address_string = self.get_full_address()
        lat, lng = geocode_address(address_string)
        if lat is not None and lng is not None:
            self.lat = lat
            self.lng = lng


    def save(self, *args, **kwargs):   
        self.calculate_address() 
        super().save(*args, **kwargs)


    
class Fournisseur(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE,blank=True, null=True)
    is_fournisseur = models.BooleanField(default=True)
    actif=models.BooleanField(default=False)
    adminetat= models.CharField(max_length=20, choices=[('normal', 'normal'), ('Blocked', 'Blocked')],blank=True, null=True)
    city = models.CharField(max_length=200,blank=True, null=True)
    wilaya= models.CharField(max_length=200,blank=True, null=True)
    country = models.CharField(max_length=200,blank=True, null=True)
    adress = models.CharField(max_length=200,blank=True, null=True)
    lat = models.CharField(max_length=200,blank=True, null=True)
    lng = models.CharField(max_length=200,blank=True, null=True)
    place_id = models.CharField(max_length=200,blank=True, null=True)


    def add_fournisseur(user,  city=None, country=None, adress=None, lat=None, lng=None, place_id=None, wilaya=None):
        fournisseur = Fournisseur.objects.create(
            user=user,
            city=city,
            country=country,
            adress=adress,
            lat=lat,
            lng=lng,
            place_id=place_id,
            wilaya=wilaya
        )
        
    def delete_fournisseur(self):
        self.delete()
    
    def modify_fournisseur(self, name=None, family_name=None, city=None, country=None, address=None,wilaya=None):
        if name:
            self.name = name
        if family_name:
            self.family_name = family_name
        if city:
            self.city = city
        if country:
            self.country = country
        if address:
            self.adress = address
        if address:
            self.wilaya = wilaya
        self.save()

    def get_full_address(self):
        return f"{self.adress}, {self.city}, {self.country}"
    
    def calculate_address(self):
        # Free, open-source geocoding (OpenStreetMap Nominatim) -- no API key required.
        address_string = self.get_full_address()
        lat, lng = geocode_address(address_string)
        if lat is not None and lng is not None:
            self.lat = lat
            self.lng = lng

    def save(self, *args, **kwargs):    
        # Calculate the total price using get_total_price method
        self.calculate_address()
        super().save(*args, **kwargs)

    def __str__(self):
        return self.user.username
    
class Livraison(models.Model):
    date = models.DateField(default=datetime.today)
    montant = models.FloatField(default=0.0)  # Set default value to 0.0
    client = models.ForeignKey(Client, on_delete=models.CASCADE)
    livreur = models.ForeignKey(Livreur, on_delete=models.SET_NULL, blank=True, null=True)
    fournisseur = models.ForeignKey(Fournisseur, on_delete=models.SET_NULL, blank=True, null=True)
    prix_livraison= models.IntegerField(default=0.0)
    status = models.CharField(max_length=20, choices=[('non_fournisseur', 'non_fournisseur'),('non_livreur', 'non_livreur'),('en_attente', 'En Attente'), ('en_cours', 'En Cours'), ('livre', 'Livre')],blank=True, null=True,default='non_fournisseur')
    valide = models.BooleanField(default=False)
    poids_totale=models.IntegerField(default=0,null=False,blank=False)
 
    def create_livraison(cls, date, montant, client, livreur=None, fournisseur=None, status=None):
        return cls.objects.create(date=date, montant=montant, client=client, livreur=livreur, fournisseur=fournisseur, status=status)

    def change_status(self, new_status):
        self.status = new_status
        self.save()

    def get_total_price(self):
        order_items = LigneLivraison.objects.all()
        total=0
        for item in order_items:
            if item.livraison == self:
                total = total + item.get_total()
        return total
    def get_total_poid(self):
        order_items = LigneLivraison.objects.all()
        total=0
        for item in order_items:
            if item.livraison == self:
                total = total + item.get_total2()
        return total
    

        
  
class Article (models.Model):
    name = models.CharField(max_length=500,blank=True, null=True)
    disignation = models.CharField(max_length=200)
    price = models.DecimalField(default=0,decimal_places=2,max_digits=7)
    image= models.ImageField(upload_to='uploads/product/',null=True, blank=True)
    poids=models.IntegerField(default=0,null=False,blank=False)

    def create_article(cls, name, disn, price,image):
        return cls.objects.create(name=name, disignation=disn, price=price,image=image)
    
    def modify_price(self,newPrice):
        self.price = newPrice
        self.save()
    
    def delete_article(self):
        self.delete()

class Panier(models.Model):
    client = models.ForeignKey(Client, on_delete=models.CASCADE)
    valide = models.BooleanField(default=False)

class LignePanier(models.Model):
    panier = models.ForeignKey(Panier, on_delete=models.CASCADE)
    article = models.ForeignKey(Article, on_delete=models.CASCADE)
    quantity = models.IntegerField()

    class Meta:
        unique_together = ('article', 'panier')
    
class LigneLivraison(models.Model):
    livraison = models.ForeignKey(Livraison, on_delete=models.CASCADE,blank=True,null=True)
    article = models.ForeignKey(Article, on_delete=models.CASCADE)
    quantity = models.IntegerField()

    class Meta:
        unique_together = ('article', 'livraison')

    def get_total(self):
        total = self.article.price * self.quantity
        return total
    
    def get_total2(self):
        total = self.article.poids * self.quantity
        return total
    
    def save(self, *args, **kwargs):    
        # Calculate the total price using get_total_price method
        super().save(*args, **kwargs)
        self.livraison.montant = self.livraison.get_total_price()
        self.livraison.poids_totale=self.livraison.get_total_poid()
        self.livraison.save() 

    def __str__(self):
        return self.article.name +" "+ str(self.livraison.id) 

    class Meta:
        unique_together = ('livraison', 'article')
    
    
    
class payemenet(models.Model):
    date = models.DateField()
    montant = models.FloatField()
    id_client = models.ForeignKey(Client, on_delete=models.CASCADE) 

class ListeProduits(models.Model):
    article = models.ForeignKey(Article,on_delete=models.CASCADE)
    fournisseur = models.ForeignKey(Fournisseur, on_delete=models.CASCADE)
    stock_disponible = models.IntegerField()
    def modify_disponabilite(self):
        self.stock_disponible = not self.stock_disponible
        self.save()
    

    class Meta:
        unique_together = ('fournisseur', 'article')

