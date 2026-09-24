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


"""Authentication: register, login, logout, current user, profile update, password update."""

def get_tokens_for_user(user):
    refresh = RefreshToken.for_user(user)

    return {
        'refresh': str(refresh),
        'access': str(refresh.access_token),
    }


class UserRegister(APIView):
    permission_classes = (permissions.AllowAny,)
    
    def post(self, request):
        clean_data = custom_validation(request.data)
        if isinstance(clean_data, QueryDict):
            clean_data = clean_data.dict()
        
        user_type = clean_data.get('type')
        
        if user_type == 'livreur':
            serializer = UserRegisterSerializer(data=clean_data)
            if serializer.is_valid(raise_exception=True):
                user, access = serializer.create(clean_data)
                adresse = clean_data.get('adress')
                livreur=Livreur.objects.create(user=user,adress=adresse)
                return Response(serializer.data, status=status.HTTP_201_CREATED)
        elif user_type == 'client':
            serializer = UserRegisterSerializer(data=clean_data)
            if serializer.is_valid(raise_exception=True):
                user, access = serializer.create(clean_data)
                client = Client.objects.create(user=user)
                return Response(serializer.data, status=status.HTTP_201_CREATED)
        elif user_type == 'fournisseur':
            serializer = UserRegisterSerializer(data=clean_data)
            if serializer.is_valid(raise_exception=True):
                user, access = serializer.create(clean_data)
                adresse = clean_data.get('adress')
                fournisseur = Fournisseur.add_fournisseur(user=user, adress=adresse)
                return Response(serializer.data, status=status.HTTP_201_CREATED)
        
        return Response(status=status.HTTP_400_BAD_REQUEST)




class UserLogin(APIView):
    permission_classes = (permissions.AllowAny,)
    authentication_classes = (SessionAuthentication,)
    
    def post(self, request):
        data = request.data
        username = data.get('username')
        password = data.get('password')


        if not username or not password:
            return Response({'error': 'Veuillez fournir un nom d\'utilisateur et un mot de passe'}, status=status.HTTP_400_BAD_REQUEST)

        user = authenticate(request, username=username, password=password)

        if user is None:
            raise ValidationError('user not found')
        login(request, user)
        token=get_tokens_for_user(user=user)

    
        if user.is_superuser:
            serializer = UserSerializer(user)
            return Response({'superuser':serializer.data,'token': token}, status=status.HTTP_200_OK)
        else:
            if Client.objects.filter(user=user).exists():
                client_serializer = ClientSerializer(user.client)
                type="client"
            elif Livreur.objects.filter(user=user).exists():
                client_serializer = LivreurSerializer(user.livreur)
                type="livreur"
            elif Fournisseur.objects.filter(user=user).exists():
                client_serializer = FournisseurSerializer(user.fournisseur)
                type="fournisseur"
            
            return Response({type:client_serializer.data,'token': token}, status=status.HTTP_200_OK)

     


class UserLogout(APIView):
    permission_classes = (permissions.AllowAny,)
    authentication_classes = ()

    def post(self, request):
            logout(request)
            return Response({'message': 'Déconnexion réussie.'}, status=status.HTTP_200_OK)



class UserView(APIView):
    
    def get(self, request):
        user_serializer = UserSerializer(request.user)  # Sérialiser l'utilisateur actuel

        # Vérifier si l'utilisateur actuel a un client associé
        if Client.objects.filter(user=request.user).exists():
            client = Client.objects.get(user=request.user)
            client_serializer = ClientSerializer(client)
            serializer = client_serializer.data
        # Construire la réponse avec les données sérialisées de l'utilisateur et du client
        
        return Response(user_serializer.data, status=status.HTTP_200_OK)




class UpdateUser(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)
    def put(self, request):
            user_instance = request.user
            if Client.objects.filter(user=user_instance).exists():
                client_instance = Client.objects.get(user=user_instance)
            elif Livreur.objects.filter(user=user_instance).exists():
                client_instance = Livreur.objects.get(user=user_instance)
            elif Fournisseur.objects.filter(user=user_instance).exists():
                client_instance = Fournisseur.objects.get(user=user_instance)

            serializer = UpdateUserSerializer(user_instance, data=request.data)
            if serializer.is_valid():
                serializer.save()
                client_instance.save()

                return Response(serializer.data, status=status.HTTP_200_OK)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        


class UpdatePassword(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    # Retirez SessionAuthentication et utilisez uniquement JWTAuthentication
    authentication_classes = (JWTAuthentication,)

    def post(self, request):
        serializer = UpdatePasswordSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            serializer.save()
            return Response({'message': 'Mot de passe mis à jour avec succès.'}, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

