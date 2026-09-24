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


"""Delivery address (Adresse_livraison) endpoints for clients and deliveries."""

class Adress_livraisonList (APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request ):
        address = Adresse_livraison.objects.all()
        serializer = Adresse_livraisonSerializer(address, many=True)
        return Response(serializer.data)
    def post(self , request ):
        serializer = Adresse_livraisonSerializer(data=request.data )
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)



class Adresse_livraisonClient(APIView):
    permission_classes=[AllowAny,]
    def get_object(self, client_id):
        try:
            return Adresse_livraison.objects.get(id_client=client_id)
        except Adresse_livraison.DoesNotExist:
            return None

    def get(self, request, client_id):
        address = self.get_object(client_id)
        if address:
            serializer = Adresse_livraisonSerializer(address)
            return Response(serializer.data)
        else:
            return Response("Address not found for this client ID", status=status.HTTP_404_NOT_FOUND)

    def put(self, request, client_id):
        address = self.get_object(client_id)
        request.data['id_client'] = client_id
        if not address:
            # Create a new address if it doesn't exist

            serializer = Adresse_livraisonSerializer(data=request.data)
            if serializer.is_valid():
                serializer.save()
                return Response(serializer.data, status=status.HTTP_201_CREATED)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        else:
            # If the address exists, update it
            address.update_address(
            new_address=request.data.get('address'),
            new_city=request.data.get('city'),
            new_country=request.data.get('country'),
            new_wilaya=request.data.get('wilaya')
            )

            # Utiliser la méthode calculate_address pour recalculer les informations géographiques
            address.calculate_address()

            # Serializer l'instance de l'adresse de livraison mise à jour
            serializer = Adresse_livraisonSerializer(address)


        # If the address exists, update it
        serializer = Adresse_livraisonSerializer(address, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
          


class InitialAdresseLivraisonClientDetail(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)

    def get(self, request):
        client = request.user.client
        adresse_livraison = Adresse_livraison.objects.filter( id_client=client).first()

        if adresse_livraison:
            serializer = Adresse_livraisonSerializer(adresse_livraison)
            return Response(serializer.data, status=status.HTTP_200_OK)
        else:
            return Response({"detail": "Adresse de livraison non trouvée."}, status=status.HTTP_404_NOT_FOUND)

    def patch(self, request):
        client = request.user.client
        address = Adresse_livraison.objects.filter( id_client=client).first()

        data = request.data.copy()  # Copier les données de la requête
        data['id_client'] = client.id  # Ajouter l'ID du client aux données de la requête

        print("Data being passed to serializer:", data)  # Debugging line


        if not address:
            data = request.data.copy()  # Faites une copie mutable des données de la requête
            data['id_client'] = client.id  # Ajouter l'ID du client aux données de la requête
            serializer = Adresse_livraisonSerializer(data=data)
            if serializer.is_valid():
                serializer.save()
                return Response(serializer.data, status=status.HTTP_201_CREATED)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        else:
            # If the address exists, update it
            address.update_address(
            new_address=request.data.get('address'),
            new_city=request.data.get('city'),
            new_country=request.data.get('country'),
            new_wilaya=request.data.get('wilaya')
            )

            # Utiliser la méthode calculate_address pour recalculer les informations géographiques
            address.calculate_address()

            # Serializer l'instance de l'adresse de livraison mise à jour
            serializer = Adresse_livraisonSerializer(address)


        # If the address exists, update it
        serializer = Adresse_livraisonSerializer(address, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        



class AdresseLivraisonbyLivraison(APIView):
    permission_classes=[AllowAny,]
    def get(self, request, livraison_id):
        try:
            livraison = Livraison.objects.get(pk=livraison_id)
            adresse_livraison = livraison.adresse_livraison
            if adresse_livraison:
                serializer = Adresse_livraisonSerializer(adresse_livraison)
                return Response(serializer.data, status=status.HTTP_200_OK)
            else:
                return Response({"detail": "Adresse de livraison non trouvée pour cette livraison"}, status=status.HTTP_404_NOT_FOUND)
        except Livraison.DoesNotExist:
            return Response({"detail": "Livraison non trouvée"}, status=status.HTTP_404_NOT_FOUND)




class Adress_livraisonById (APIView):
    permission_classes=[AllowAny,]
    def get(self, request,pk ):
        try:
            address = Adresse_livraison.objects.get(pk=pk )
        except Adresse_livraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
        
        serializer = Adresse_livraisonSerializer(address)
        return Response(serializer.data)
    
    def put(self , request,pk ):
        try:
            # Récupérer l'instance de l'adresse de livraison à modifier
            adresse = Adresse_livraison.objects.get(pk=pk)
            adresse= Adresse_livraison.objects.get(pk=pk)
        except Adresse_livraison.DoesNotExist:
            # Si l'adresse de livraison n'existe pas, renvoyer une réponse 404
            return Response({"message": "Adresse de livraison non trouvée"}, status=status.HTTP_404_NOT_FOUND)
        
        # Utiliser la méthode update_address pour mettre à jour les informations de l'adresse
        adresse.update_address(
            new_address=request.data.get('address'),
            new_city=request.data.get('city'),
            new_country=request.data.get('country'),
            new_wilaya=request.data.get('wilaya')
        )

        # Utiliser la méthode calculate_address pour recalculer les informations géographiques
        adresse.calculate_address()

        # Serializer l'instance de l'adresse de livraison mise à jour
        serializer = Adresse_livraisonSerializer(adresse)

        return Response(serializer.data)


        serializer = Adresse_livraisonSerializer(adresse, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    def  delete(self,request, pk):
        try:
            address = Adresse_livraison.objects.get(pk=pk)
        except Adresse_livraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        address.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)
    
