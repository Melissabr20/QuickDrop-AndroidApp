from django.views.generic import ListView
from django.views import View
from django.shortcuts import render, redirect
from ..models import *
from ..routing_client import get_distance_duration, get_distance_matrix_km
from django.http import QueryDict

from geopy.distance import geodesic
from datetime import datetime, timedelta
from django.contrib.auth import authenticate ,login, logout, get_user_model
from django.contrib import messages
from django.contrib.auth.forms import UserCreationForm
from ..forms import SignUpForm, UpdateUserForm, ChangePasswordForm
from django import forms
from django.db.models.signals import post_save
from django.dispatch import receiver
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from ..optimisation import trouver_solution_optimale

from ..serializers import *
from django.http import JsonResponse
from rest_framework.authtoken.models import Token
from rest_framework.validators import UniqueValidator
from django.contrib.auth.password_validation import validate_password
from rest_framework.authentication import TokenAuthentication, SessionAuthentication
from rest_framework.permissions import AllowAny
from rest_framework.exceptions import ValidationError
from ..validations import *
import json
from rest_framework import permissions

from rest_framework_simplejwt.authentication import JWTAuthentication 

from rest_framework_simplejwt.tokens import RefreshToken

from rest_framework import generics

from ortools.constraint_solver import routing_enums_pb2
from ortools.constraint_solver import pywrapcp
from itertools import combinations



import requests
import json
import urllib
from ..globals import GLOBAL_EXCLUDE_IDS
from django.http import Http404
from rest_framework.parsers import MultiPartParser, FormParser


""""ListeProduits" (product listing) endpoints and article/product lookups."""

class ListeProduitsAPI (APIView):
    permission_classes = (permissions.AllowAny,)
    

    def get ( self , request ):
        liv = ListeProduits.objects.all()
        serializer =ListeProduitsSerializer(liv , many = True)
        return  Response(serializer.data)
    #ajouter une livraison a la BDD
    def post ( self , request):
        serializer = ListeProduitsSerializer(data = request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(data=serializer.data)
        return  Response(data=serializer.errors)
    


class ListeProduitDetail(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self,request, pk):
        try:
           liv = ListeProduits.objects.get(pk=pk)
        except ListeProduits.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
        serialzer = ListeProduitsSerializer (liv )  
        return Response(serialzer.data)
   
    def patch(self, request, pk):
        try:
            liv = ListeProduits.objects.get(pk=pk)
        except ListeProduits.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer = ListeProduitsSerializer(liv, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    
    def  delete(self,request, pk):
        try:
            liv = ListeProduits.objects.get(pk=pk)
        except ListeProduits.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
        liv.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


# Recherche des Produits dans ListeProduits ayant l'article propose + stock>=quantite


class ListeProduitArticle(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request, pk, quantite):
        # Recherche des produits dans ListeProduits ayant l'article proposé
        produits_trouves = ListeProduits.objects.filter(article=pk)
        
        if produits_trouves.exists():
            produits_disponibles = []
            for produit in produits_trouves:
                if produit.stock_disponible >= quantite:
                    produits_disponibles.append(produit)

            if produits_disponibles:
                # Récupérer les fournisseurs des produits disponibles
                fournisseurs = set(produit.fournisseur for produit in produits_disponibles)
                
                # Serializer les fournisseurs
                fournisseurs_serializer = FournisseurSerializer(fournisseurs, many=True)
                
                return Response(fournisseurs_serializer.data)
            else:
                return Response({"message": f"Aucun produit disponible en quantité {quantite} pour cet article."},
                                status=status.HTTP_404_NOT_FOUND)
        else:
            return Response({"message": "Aucun produit trouvé pour cet article."},
                            status=status.HTTP_404_NOT_FOUND)


#gerer la liste des paniers et la création de nouveaux paniers.



class ListeProduitsByFournisseurAPIView(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)

    def get(self, request):
        livreur = request.user.fournisseur
        queryset =ListeProduits.objects.filter(fournisseur=livreur).all()
        serializer =ListeProduitsSerializer(queryset, many=True)
        return Response(serializer.data)
    
    def post(self, request):
        serializer = ListeProduitsSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
